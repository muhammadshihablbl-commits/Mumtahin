package com.mumtahin.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mumtahin.data.ExamInfo
import com.mumtahin.data.SavedQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Holds and persists everything SubjectScreen shows for one subject.
 *
 * This is the MVVM boundary: SubjectScreen (the View) only reads
 * [uiState] and calls these functions — it never mutates a question
 * list or ExamInfo itself. "Is this an add or an update by id", "when do
 * we write to disk" all live here in one place, instead of being
 * duplicated inside every bottom sheet's onSave callback like before.
 */
internal class SubjectViewModel(
    private val subjectName: String,
    private val repository: SubjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubjectData(examInfo = ExamInfo(subject = subjectName)))
    val uiState: StateFlow<SubjectData> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observe(subjectName).collect { saved ->
                if (saved != null) {
                    _uiState.value = saved
                }
                // If saved == null (first time opening this subject),
                // keep the default empty state already set above — no
                // need to overwrite it with null.
            }
        }
    }

    fun updateExamInfo(newInfo: ExamInfo) {
        _uiState.update { it.copy(examInfo = newInfo) }
        persist()
    }

    fun setExamInfoExpanded(expanded: Boolean) {
        _uiState.update { it.copy(examInfoExpanded = expanded) }
        persist()
    }

    /** Adds [question] as new, or replaces the existing question with the same id if editing. */
    fun upsertQuestion(question: SavedQuestion) {
        _uiState.update { state ->
            val alreadyExists = state.savedQuestions.any { it.id == question.id }
            val updatedList = if (alreadyExists) {
                state.savedQuestions.map { if (it.id == question.id) question else it }
            } else {
                state.savedQuestions + question
            }
            state.copy(savedQuestions = updatedList)
        }
        persist()
    }

    fun deleteQuestion(id: Long) {
        _uiState.update { state ->
            state.copy(savedQuestions = state.savedQuestions.filterNot { it.id == id })
        }
        persist()
    }

    private fun persist() {
        viewModelScope.launch {
            repository.save(subjectName, _uiState.value)
        }
    }

    /** Plain factory (no Hilt/DI in this app yet) — builds one ViewModel per subject. */
    class Factory(
        private val subjectName: String,
        private val repository: SubjectRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SubjectViewModel(subjectName, repository) as T
        }
    }
}
