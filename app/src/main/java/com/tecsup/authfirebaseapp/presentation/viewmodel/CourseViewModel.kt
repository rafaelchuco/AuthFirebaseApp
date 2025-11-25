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

data class FormState(
    val name: String = "",
    val teacher: String = "",
    val creditsText: String = ""
)

data class EditDialogState(
    val isOpen: Boolean = false,
    val course: Course? = null,
    val name: String = "",
    val teacher: String = "",
    val creditsText: String = ""
)

class CourseViewModel : ViewModel() {
    private val repository = CourseRepository()
    private val auth = FirebaseAuth.getInstance()

    // Estados
    private val _courseUiState = MutableStateFlow(CourseUiState())
    val courseUiState: StateFlow<CourseUiState> = _courseUiState.asStateFlow()

    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    private val _editDialogState = MutableStateFlow(EditDialogState())
    val editDialogState: StateFlow<EditDialogState> = _editDialogState.asStateFlow()

    init {
        loadCourses()
    }

    // ========== MÉTODOS DE CARGA ==========
    fun loadCourses() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _courseUiState.value = _courseUiState.value.copy(isLoading = true, error = null)
            val result = repository.getCoursesByUser(uid)
            result.onSuccess { courses ->
                _courseUiState.value = _courseUiState.value.copy(
                    courses = courses,
                    isLoading = false
                )
            }.onFailure { e ->
                _courseUiState.value = _courseUiState.value.copy(
                    error = e.message ?: "Error al cargar cursos",
                    isLoading = false
                )
            }
        }
    }

    // ========== MÉTODOS DE FORMULARIO ==========
    fun updateFormName(name: String) {
        _formState.value = _formState.value.copy(name = name)
    }

    fun updateFormTeacher(teacher: String) {
        _formState.value = _formState.value.copy(teacher = teacher)
    }

    fun updateFormCredits(credits: String) {
        _formState.value = _formState.value.copy(creditsText = credits)
    }

    fun clearForm() {
        _formState.value = FormState()
    }

    // ========== VALIDACIÓN ==========
    private fun validateForm(name: String, teacher: String, credits: String): String? {
        return when {
            name.isBlank() || teacher.isBlank() || credits.isBlank() -> 
                "Completa todos los campos"
            credits.toIntOrNull() == null -> 
                "Créditos debe ser un número"
            else -> null
        }
    }

    // ========== CREATE ==========
    fun saveCourse() {
        val uid = auth.currentUser?.uid ?: return
        val formState = _formState.value

        // Validar
        val error = validateForm(formState.name, formState.teacher, formState.creditsText)
        if (error != null) {
            _courseUiState.value = _courseUiState.value.copy(error = error)
            return
        }

        viewModelScope.launch {
            _courseUiState.value = _courseUiState.value.copy(isLoading = true, error = null)
            
            val course = Course(
                id = repository.javaClass.simpleName + System.currentTimeMillis(),
                name = formState.name,
                teacher = formState.teacher,
                credits = formState.creditsText.toInt(),
                userId = uid
            )

            val result = repository.createCourse(course)
            result.onSuccess {
                clearForm()
                loadCourses()
            }.onFailure { e ->
                _courseUiState.value = _courseUiState.value.copy(
                    error = e.message ?: "Error al guardar",
                    isLoading = false
                )
            }
        }
    }

    // ========== UPDATE ==========
    fun openEditDialog(course: Course) {
        _editDialogState.value = EditDialogState(
            isOpen = true,
            course = course,
            name = course.name,
            teacher = course.teacher,
            creditsText = course.credits.toString()
        )
    }

    fun closeEditDialog() {
        _editDialogState.value = EditDialogState()
    }

    fun updateEditName(name: String) {
        _editDialogState.value = _editDialogState.value.copy(name = name)
    }

    fun updateEditTeacher(teacher: String) {
        _editDialogState.value = _editDialogState.value.copy(teacher = teacher)
    }

    fun updateEditCredits(credits: String) {
        _editDialogState.value = _editDialogState.value.copy(creditsText = credits)
    }

    fun updateCourse() {
        val editState = _editDialogState.value
        val course = editState.course ?: return

        // Validar
        val error = validateForm(editState.name, editState.teacher, editState.creditsText)
        if (error != null) {
            _courseUiState.value = _courseUiState.value.copy(error = error)
            return
        }

        viewModelScope.launch {
            _courseUiState.value = _courseUiState.value.copy(isLoading = true, error = null)

            val updates = mapOf(
                "name" to editState.name,
                "teacher" to editState.teacher,
                "credits" to editState.creditsText.toInt()
            )

            val result = repository.updateCourse(course.id, updates)
            result.onSuccess {
                closeEditDialog()
                loadCourses()
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
                loadCourses()
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
