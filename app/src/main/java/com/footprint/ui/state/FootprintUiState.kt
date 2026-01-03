package com.footprint.ui.state

import com.footprint.data.model.FootprintEntry
import com.footprint.data.model.FootprintSummary
import com.footprint.data.model.TravelGoal
import com.footprint.data.model.Mood
import com.footprint.ui.theme.ThemeMode
import java.time.LocalDate

data class FilterState(
    val selectedMood: Mood? = null,
    val searchQuery: String = "",
    val year: Int = LocalDate.now().year
)

data class FootprintUiState(
    val entries: List<FootprintEntry> = emptyList(),
    val visibleEntries: List<FootprintEntry> = emptyList(),
    val goals: List<TravelGoal> = emptyList(),
    val summary: FootprintSummary = FootprintSummary(),
    val filterState: FilterState = FilterState(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val userNickname: String = "旅行者",
    val userAvatarId: String = "avatar_1",
    val isLoading: Boolean = true
)
