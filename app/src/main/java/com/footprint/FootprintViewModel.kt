package com.footprint

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.footprint.data.model.Mood
import com.footprint.data.model.TravelGoal
import com.footprint.data.model.FootprintEntry
import com.footprint.data.repository.FootprintAnalytics
import com.footprint.data.repository.FootprintRepository
import com.footprint.ui.state.FilterState
import com.footprint.ui.state.FootprintUiState
import com.footprint.ui.theme.ThemeMode
import com.footprint.utils.PreferenceManager
import com.google.gson.Gson
import android.net.Uri
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FootprintViewModel(
    application: Application,
    private val repository: FootprintRepository = (application as FootprintApplication).repository
) : AndroidViewModel(application) {

    private val preferenceManager = PreferenceManager(application)
    private val gson = Gson()
    
    private val moodFilter = MutableStateFlow<Mood?>(null)
    private val searchQuery = MutableStateFlow("")
    private val yearFilter = MutableStateFlow(LocalDate.now().year)
    private val themeMode = MutableStateFlow(preferenceManager.themeMode)
    private val nickname = MutableStateFlow(preferenceManager.nickname)
    private val avatarId = MutableStateFlow(preferenceManager.avatarId)

    val uiState = combine(
        repository.observeEntries(),
        repository.observeGoals(),
        moodFilter,
        searchQuery,
        yearFilter,
        themeMode,
        nickname,
        avatarId
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val entries = args[0] as List<FootprintEntry>
        @Suppress("UNCHECKED_CAST")
        val goals = args[1] as List<TravelGoal>
        val mood = args[2] as Mood?
        val search = args[3] as String
        val year = args[4] as Int
        val theme = args[5] as ThemeMode
        val nk = args[6] as String
        val av = args[7] as String

        val visibleEntries = entries
            .filter { if (search.isBlank()) it.happenedOn.year == year else true }
            .filter { mood == null || it.mood == mood }
            .filter {
                if (search.isBlank()) true
                else {
                    val queryText = search.trim().lowercase()
                    it.title.lowercase().contains(queryText) ||
                        it.location.lowercase().contains(queryText) ||
                        it.tags.any { tag -> tag.lowercase().contains(queryText) }
                }
            }
        FootprintUiState(
            entries = entries,
            visibleEntries = visibleEntries,
            goals = goals,
            summary = FootprintAnalytics.buildSummary(entries),
            filterState = FilterState(mood, search, year),
            themeMode = theme,
            userNickname = nk,
            userAvatarId = av,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FootprintUiState(themeMode = preferenceManager.themeMode)
    )

    init {
        repository.ensureSeedData()
    }

    fun updateProfile(newNickname: String, newAvatarId: String) {
        nickname.value = newNickname
        avatarId.value = newAvatarId
        preferenceManager.nickname = newNickname
        preferenceManager.avatarId = newAvatarId
    }

    fun updateAvatar(uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val path = com.footprint.utils.ImageUtils.saveImageToInternalStorage(getApplication(), uri)
                if (path != null) {
                    withContext(Dispatchers.Main) {
                        updateProfile(nickname.value, path)
                    }
                }
            }
        }
    }

    fun exportData(uri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val backup = repository.prepareBackup()
                val json = gson.toJson(backup)
                withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { outputStream ->
                        OutputStreamWriter(outputStream).use { writer ->
                            writer.write(json)
                        }
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "导出失败")
            }
        }
    }

    fun importData(uri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(uri)?.use { inputStream ->
                        InputStreamReader(inputStream).use { reader ->
                            reader.readText()
                        }
                    }
                } ?: throw Exception("无法读取文件")
                
                val backup = gson.fromJson(json, com.footprint.data.model.BackupData::class.java)
                repository.restoreFromBackup(backup)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "导入失败")
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        themeMode.value = mode
        preferenceManager.themeMode = mode
    }

    fun toggleMoodFilter(mood: Mood?) {
        moodFilter.value = if (moodFilter.value == mood) null else mood
    }

    fun updateSearch(query: String) {
        searchQuery.value = query
    }

    fun shiftYear(delta: Int) {
        val maxYear = LocalDate.now().year + 5
        yearFilter.value = (yearFilter.value + delta).coerceIn(1970, maxYear)
    }

    fun updateFootprint(entry: com.footprint.data.model.FootprintEntry) {
        viewModelScope.launch {
            repository.saveEntry(entry)
        }
    }

    fun deleteFootprint(entry: com.footprint.data.model.FootprintEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry.id)
        }
    }

    fun addFootprint(
        title: String,
        location: String,
        detail: String,
        mood: Mood,
        tags: List<String>,
        distanceKm: Double,
        photos: List<String>,
        energyLevel: Int,
        date: LocalDate,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            val entry = FootprintEntry(
                title = title,
                location = location,
                detail = detail,
                mood = mood,
                tags = tags,
                distanceKm = distanceKm,
                photos = photos,
                energyLevel = energyLevel,
                happenedOn = date,
                latitude = latitude,
                longitude = longitude
            )
            repository.saveEntry(entry)
        }
    }

    fun addGoal(
        title: String,
        targetLocation: String,
        targetDate: LocalDate,
        notes: String
    ) {
        viewModelScope.launch {
            val goal = TravelGoal(
                title = title,
                targetLocation = targetLocation,
                targetDate = targetDate,
                notes = notes,
                isCompleted = false,
                progress = 5
            )
            repository.saveGoal(goal)
        }
    }

    fun updateGoal(goal: TravelGoal) {
        viewModelScope.launch {
            repository.saveGoal(goal)
        }
    }

    fun toggleGoal(goal: TravelGoal) {
        viewModelScope.launch {
            repository.updateGoalCompletion(goal, !goal.isCompleted)
        }
    }

    fun getTrackPoints(start: Long, end: Long) = repository.getTrackPoints(start, end)

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(
                    extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                ) { "Application was not provided in ViewModel extras" }
                @Suppress("UNCHECKED_CAST")
                return FootprintViewModel(application as FootprintApplication) as T
            }
        }
    }
}
