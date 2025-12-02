package com.tecsup.authfirebaseapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.tecsup.authfirebaseapp.domain.model.Course
import com.tecsup.authfirebaseapp.data.repository.CourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estados UI
data class CourseUiState(
    val courses: List<Course> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class CourseFormState(
    val title: String = "",
    val description: String = "",
    val duration: String = ""
)

data class CourseEditDialogState(
    val isOpen: Boolean = false,
    val course: Course? = null,
    val title: String = "",
    val description: String = "",
    val duration: String = ""
)

class CourseViewModel : ViewModel() {
    private val repository = CourseRepository()
    private val auth = FirebaseAuth.getInstance()

    // Estados
    private val _courseUiState = MutableStateFlow(CourseUiState())
    val courseUiState: StateFlow<CourseUiState> = _courseUiState.asStateFlow()

    private val _formState = MutableStateFlow(CourseFormState())
    val formState: StateFlow<CourseFormState> = _formState.asStateFlow()

    private val _editDialogState = MutableStateFlow(CourseEditDialogState())
    val editDialogState: StateFlow<CourseEditDialogState> = _editDialogState.asStateFlow()

    init {
        loadCoursesRealtime()
    }

    // ========== MÉTODOS DE CARGA ==========
    fun loadCoursesRealtime() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _courseUiState.value = _courseUiState.value.copy(isLoading = true, error = null)
            repository.getCoursesByUserRealtime(uid).collect { courses ->
                _courseUiState.value = _courseUiState.value.copy(
                    courses = courses,
                    isLoading = false
                )
            }
        }
    }

    // ========== MÉTODOS DE FORMULARIO ==========
    fun updateFormTitle(title: String) {
        _formState.value = _formState.value.copy(title = title)
    }

    fun updateFormDescription(description: String) {
        _formState.value = _formState.value.copy(description = description)
    }

    fun updateFormDuration(duration: String) {
        _formState.value = _formState.value.copy(duration = duration)
    }

    fun clearForm() {
        _formState.value = CourseFormState()
    }

    // ========== VALIDACIÓN ==========
    private fun validateForm(title: String, duration: String): String? {
        return when {
            title.isBlank() -> "El título es obligatorio"
            duration.isBlank() -> "La duración es obligatoria"
            else -> null
        }
    }

    // ========== CREATE ==========
    fun saveCourse() {
        val uid = auth.currentUser?.uid ?: return
        val formState = _formState.value

        // Validar
        val error = validateForm(formState.title, formState.duration)
        if (error != null) {
            _courseUiState.value = _courseUiState.value.copy(error = error)
            return
        }

        viewModelScope.launch {
            _courseUiState.value = _courseUiState.value.copy(isLoading = true, error = null)
            
            val course = Course(
                id = "course_${System.currentTimeMillis()}",
                title = formState.title,
                description = formState.description,
                duration = formState.duration,
                userId = uid
            )

            val result = repository.createCourse(course)
            result.onSuccess {
                clearForm()
            }.onFailure { e ->
                _courseUiState.value = _courseUiState.value.copy(
                    error = e.message ?: "Error al guardar curso",
                    isLoading = false
                )
            }
        }
    }

    // ========== UPDATE ==========
    fun openEditDialog(course: Course) {
        _editDialogState.value = CourseEditDialogState(
            isOpen = true,
            course = course,
            title = course.title,
            description = course.description,
            duration = course.duration
        )
    }

    fun closeEditDialog() {
        _editDialogState.value = CourseEditDialogState()
    }

    fun updateEditTitle(title: String) {
        _editDialogState.value = _editDialogState.value.copy(title = title)
    }

    fun updateEditDescription(description: String) {
        _editDialogState.value = _editDialogState.value.copy(description = description)
    }

    fun updateEditDuration(duration: String) {
        _editDialogState.value = _editDialogState.value.copy(duration = duration)
    }

    fun updateCourse() {
        val editState = _editDialogState.value
        val course = editState.course ?: return

        // Validar
        val error = validateForm(editState.title, editState.duration)
        if (error != null) {
            _courseUiState.value = _courseUiState.value.copy(error = error)
            return
        }

        viewModelScope.launch {
            _courseUiState.value = _courseUiState.value.copy(isLoading = true, error = null)

            val updates = mapOf(
                "title" to editState.title,
                "description" to editState.description,
                "duration" to editState.duration
            )

            val result = repository.updateCourse(course.id, updates)
            result.onSuccess {
                closeEditDialog()
                _courseUiState.value = _courseUiState.value.copy(isLoading = false)
            }.onFailure { e ->
                _courseUiState.value = _courseUiState.value.copy(
                    error = e.message ?: "Error al actualizar",
                    isLoading = false
                )
            }
        }
    }

    // ========== DELETE ==========
    fun deleteCourse(courseId: String) {
        viewModelScope.launch {
            _courseUiState.value = _courseUiState.value.copy(isLoading = true, error = null)

            val result = repository.deleteCourse(courseId)
            result.onSuccess {
                _courseUiState.value = _courseUiState.value.copy(isLoading = false)
            }.onFailure { e ->
                _courseUiState.value = _courseUiState.value.copy(
                    error = e.message ?: "Error al eliminar",
                    isLoading = false
                )
            }
        }
    }

    // ========== UTILIDADES ==========
    fun clearError() {
        _courseUiState.value = _courseUiState.value.copy(error = null)
    }
}
