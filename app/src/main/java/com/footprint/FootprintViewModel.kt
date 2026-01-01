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
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FootprintViewModel(
    application: Application,
    private val repository: FootprintRepository = (application as FootprintApplication).repository
) : AndroidViewModel(application) {

    private val moodFilter = MutableStateFlow<Mood?>(null)
    private val searchQuery = MutableStateFlow("")
    private val yearFilter = MutableStateFlow(LocalDate.now().year)

    val uiState = combine(
        repository.observeEntries(),
        repository.observeGoals(),
        moodFilter,
        searchQuery,
        yearFilter
    ) { entries, goals, mood, search, year ->
        val visibleEntries = entries
            .filter { it.happenedOn.year == year }
            .filter { mood == null || it.mood == mood }
            .filter {
                if (search.isBlank()) true
                else {
                    val query = search.trim().lowercase()
                    it.title.lowercase().contains(query) ||
                        it.location.lowercase().contains(query) ||
                        it.tags.any { tag -> tag.lowercase().contains(query) }
                }
            }
        FootprintUiState(
            entries = entries,
            visibleEntries = visibleEntries,
            goals = goals,
            summary = FootprintAnalytics.buildSummary(entries),
            filterState = FilterState(mood, search, year),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FootprintUiState()
    )

    init {
        repository.ensureSeedData()
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
            repository.saveEntry(entry) // Repository 的 saveEntry 内部使用的是 upsert，所以直接调用即可
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
        date: LocalDate
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
                happenedOn = date
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
