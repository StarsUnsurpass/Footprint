package com.footprint

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.footprint.data.model.FootprintEntry
import com.footprint.data.model.Mood
import com.footprint.data.model.TravelGoal
import com.footprint.data.repository.FootprintAnalytics
import com.footprint.data.repository.FootprintRepository
import com.footprint.ui.state.FilterState
import com.footprint.ui.state.FootprintUiState
import com.footprint.ui.theme.ThemeMode
import com.footprint.utils.PreferenceManager
import com.google.gson.Gson
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
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
    private val themeStyle = MutableStateFlow(preferenceManager.themeStyle)
    private val nickname = MutableStateFlow(preferenceManager.nickname)
    private val avatarId = MutableStateFlow(preferenceManager.avatarId)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val yearlyTrackPointCount: Flow<Int> = yearFilter.flatMapLatest { year ->
        flow<Int> {
            emit(repository.getTrackPointCount(year))
        }
    }

    private val monthlyTrackPointCount: Flow<Int> = flow<Int> {
        val now = LocalDate.now()
        emit(repository.getTrackPointCount(now.year, now.monthValue))
    }

    // 定义显式的数据组结构
    private data class DataGroup(val entries: List<FootprintEntry>, val goals: List<TravelGoal>, val yPoints: Int, val mPoints: Int)
    private data class FilterGroup(val mood: Mood?, val search: String, val year: Int)
    private data class PrefsGroup(val theme: ThemeMode, val style: com.footprint.ui.theme.AppThemeStyle, val nk: String, val av: String)

    // 强类型合并流
    private val dataFlow: Flow<DataGroup> = combine(
        repository.observeEntries(),
        repository.observeGoals(),
        yearlyTrackPointCount,
        monthlyTrackPointCount
    ) { entries, goals, yPoints, mPoints ->
        DataGroup(entries, goals, yPoints, mPoints)
    }

    private val filterFlow: Flow<FilterGroup> = combine(
        moodFilter,
        searchQuery,
        yearFilter
    ) { mood, search, year ->
        FilterGroup(mood, search, year)
    }

    private val prefsFlow: Flow<PrefsGroup> = combine(
        themeMode,
        themeStyle,
        nickname,
        avatarId
    ) { theme, style, nk, av ->
        PrefsGroup(theme, style, nk, av)
    }

    // 最终合并，参数减少到 3 个，编译器推断不再压力
    val uiState: StateFlow<FootprintUiState> = combine(dataFlow, filterFlow, prefsFlow) { data, filter, prefs ->
        val visibleEntries = data.entries
            .filter { if (filter.search.isBlank()) it.happenedOn.year == filter.year else true }
            .filter { filter.mood == null || it.mood == filter.mood }
            .filter {
                if (filter.search.isBlank()) true
                else {
                    val queryText = filter.search.trim().lowercase()
                    it.title.lowercase().contains(queryText) ||
                        it.location.lowercase().contains(queryText) ||
                        it.tags.any { tag -> tag.lowercase().contains(queryText) }
                }
            }
        
        val visibleGoals = data.goals
            .filter { if (filter.search.isBlank()) it.targetDate.year == filter.year else true }

        val today = LocalDate.now()
        val historicalMemories = data.entries.filter { 
            it.happenedOn.monthValue == today.monthValue && 
            it.happenedOn.dayOfMonth == today.dayOfMonth &&
            it.happenedOn.year < today.year
        }
        
        val randomMemory = if (historicalMemories.isNotEmpty()) historicalMemories.random() else null

        val memoryQuote = if (randomMemory == null) {
            val quotes = listOf(
                "所有的昨日，都是为了迎接更好的明天。",
                "记忆是阵阵花香，我们说好永远不能忘。",
                "时光荏苒，唯有足迹证明我们曾热烈地活过。",
                "那些被翻阅过的日子，都成了生命里的光。",
                "生活不在于走了多少路，而在于留下了多少回忆。",
                "去发现，去记录，去成为更好的自己。",
                "每一个今天，都是未来最想回到的昨天。"
            )
            quotes[(today.toEpochDay() % quotes.size).toInt()]
        } else null

        FootprintUiState(
            entries = data.entries,
            visibleEntries = visibleEntries,
            goals = visibleGoals,
            yearlyEntries = data.entries.filter { it.happenedOn.year == filter.year },
            yearlyGoals = data.goals.filter { it.targetDate.year == filter.year },
            summary = FootprintAnalytics.buildSummary(data.entries, filter.year, data.yPoints, data.mPoints),
            filterState = FilterState(filter.mood, filter.search, filter.year),
            themeMode = prefs.theme,
            themeStyle = prefs.style,
            userNickname = prefs.nk,
            userAvatarId = prefs.av,
            randomMemory = randomMemory,
            memoryQuote = memoryQuote,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FootprintUiState(
            themeMode = preferenceManager.themeMode,
            themeStyle = preferenceManager.themeStyle
        )
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

    fun setThemeStyle(style: com.footprint.ui.theme.AppThemeStyle) {
        themeStyle.value = style
        preferenceManager.themeStyle = style
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
        longitude: Double? = null,
        icon: String = "LocationOn"
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
                longitude = longitude,
                icon = icon
            )
            repository.saveEntry(entry)
        }
    }

    fun addGoal(
        title: String,
        targetLocation: String,
        targetDate: LocalDate,
        notes: String,
        icon: String = "Flag"
    ) {
        viewModelScope.launch {
            val goal = TravelGoal(
                title = title,
                targetLocation = targetLocation,
                targetDate = targetDate,
                notes = notes,
                isCompleted = false,
                progress = 5,
                icon = icon
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

    fun deleteGoal(goal: TravelGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal.id)
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
