package edu.nd.pmcburne.hello

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.nd.pmcburne.hello.data.AppDatabase
import edu.nd.pmcburne.hello.data.MapPlacemark
import edu.nd.pmcburne.hello.data.PlacemarkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoading: Boolean = true,
    val selectedTag: String = "core",
    val availableTags: List<String> = emptyList(),
    val visiblePlacemarks: List<MapPlacemark> = emptyList(),
    val errorMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PlacemarkRepository(
        AppDatabase.getDatabase(application).placemarkDao()
    )

    private val selectedTag = MutableStateFlow("core")
    private val loading = MutableStateFlow(true)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MainUiState> = combine(
        repository.tags,
        repository.placemarks,
        selectedTag,
        loading,
        errorMessage
    ) { tags, placemarks, currentTag, isLoading, currentError ->
        val sortedTags = tags.distinct().sorted()
        val chosenTag = when {
            sortedTags.isEmpty() -> currentTag
            sortedTags.contains(currentTag) -> currentTag
            sortedTags.contains("core") -> "core"
            else -> sortedTags.first()
        }

        MainUiState(
            isLoading = isLoading,
            selectedTag = chosenTag,
            availableTags = sortedTags,
            visiblePlacemarks = placemarks.filter { chosenTag in it.tags },
            errorMessage = currentError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState()
    )

    init {
        refreshData()
    }

    fun onTagSelected(tag: String) {
        selectedTag.value = tag
    }

    fun refreshData() {
        viewModelScope.launch {
            loading.value = true
            errorMessage.value = null
            try {
                repository.syncPlacemarks()
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Unable to load map data."
            } finally {
                loading.value = false
            }
        }
    }
}