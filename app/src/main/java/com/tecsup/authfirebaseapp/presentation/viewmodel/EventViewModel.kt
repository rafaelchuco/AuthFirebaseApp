package com.tecsup.authfirebaseapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.tecsup.authfirebaseapp.domain.model.Event
import com.tecsup.authfirebaseapp.data.repository.EventRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

// Estados UI
data class EventUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class FormState(
    val title: String = "",
    val date: String = "",
    val description: String = ""
)

data class EditDialogState(
    val isOpen: Boolean = false,
    val event: Event? = null,
    val title: String = "",
    val date: String = "",
    val description: String = ""
)

class EventViewModel : ViewModel() {
    private val repository = EventRepository()
    private val auth = FirebaseAuth.getInstance()

    // Estados
    private val _eventUiState = MutableStateFlow(EventUiState())
    val eventUiState: StateFlow<EventUiState> = _eventUiState.asStateFlow()

    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    private val _editDialogState = MutableStateFlow(EditDialogState())
    val editDialogState: StateFlow<EditDialogState> = _editDialogState.asStateFlow()

    // Job para controlar el listener de Firestore
    private var listenerJob: Job? = null

    init {
        loadEventsRealtime()
    }

    // ========== MÉTODOS DE CARGA ==========
    fun loadEventsRealtime() {
        // Cancelar el listener anterior si existe
        listenerJob?.cancel()
        
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _eventUiState.value = EventUiState(events = emptyList())
            return
        }
        
        listenerJob = viewModelScope.launch {
            _eventUiState.value = _eventUiState.value.copy(isLoading = true, error = null)
            try {
                repository.getEventsByUserRealtime(uid)
                    .catch { exception ->
                        // Manejo de errores de permisos o conexión
                        if (exception.message?.contains("PERMISSION_DENIED") == true) {
                            _eventUiState.value = EventUiState(
                                events = emptyList(),
                                isLoading = false,
                                error = null // No mostrar error si el usuario cerró sesión
                            )
                        } else {
                            _eventUiState.value = _eventUiState.value.copy(
                                isLoading = false,
                                error = "Error de conexión: ${exception.message}"
                            )
                        }
                    }
                    .collect { events ->
                        _eventUiState.value = _eventUiState.value.copy(
                            events = events,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                if (e.message?.contains("PERMISSION_DENIED") != true) {
                    _eventUiState.value = _eventUiState.value.copy(
                        isLoading = false,
                        error = "Error: ${e.message}"
                    )
                }
            }
        }
    }
    
    // Método para limpiar listeners cuando se cierra sesión
    fun stopListening() {
        listenerJob?.cancel()
        listenerJob = null
        _eventUiState.value = EventUiState(events = emptyList())
        _formState.value = FormState()
        _editDialogState.value = EditDialogState()
    }
    
    override fun onCleared() {
        super.onCleared()
        stopListening()
    }

    // ========== MÉTODOS DE FORMULARIO ==========
    fun updateFormTitle(title: String) {
        _formState.value = _formState.value.copy(title = title)
    }

    fun updateFormDate(date: String) {
        _formState.value = _formState.value.copy(date = date)
    }

    fun updateFormDescription(description: String) {
        _formState.value = _formState.value.copy(description = description)
    }

    fun clearForm() {
        _formState.value = FormState()
    }

    // ========== VALIDACIÓN ==========
    private fun validateForm(title: String, date: String): String? {
        return when {
            title.isBlank() -> "El título es obligatorio"
            date.isBlank() -> "La fecha es obligatoria"
            else -> null
        }
    }

    // ========== CREATE ==========
    fun saveEvent() {
        val uid = auth.currentUser?.uid ?: return
        val formState = _formState.value

        // Validar
        val error = validateForm(formState.title, formState.date)
        if (error != null) {
            _eventUiState.value = _eventUiState.value.copy(error = error)
            return
        }

        viewModelScope.launch {
            _eventUiState.value = _eventUiState.value.copy(isLoading = true, error = null)
            
            val event = Event(
                id = "event_${System.currentTimeMillis()}",
                title = formState.title,
                date = formState.date,
                description = formState.description,
                userId = uid
            )

            val result = repository.createEvent(event)
            result.onSuccess {
                clearForm()
                // No necesitamos recargar manualmente, el snapshot listener lo hará
            }.onFailure { e ->
                _eventUiState.value = _eventUiState.value.copy(
                    error = e.message ?: "Error al guardar evento",
                    isLoading = false
                )
            }
        }
    }

    // ========== UPDATE ==========
    fun openEditDialog(event: Event) {
        _editDialogState.value = EditDialogState(
            isOpen = true,
            event = event,
            title = event.title,
            date = event.date,
            description = event.description
        )
    }

    fun closeEditDialog() {
        _editDialogState.value = EditDialogState()
    }

    fun updateEditTitle(title: String) {
        _editDialogState.value = _editDialogState.value.copy(title = title)
    }

    fun updateEditDate(date: String) {
        _editDialogState.value = _editDialogState.value.copy(date = date)
    }

    fun updateEditDescription(description: String) {
        _editDialogState.value = _editDialogState.value.copy(description = description)
    }

    fun updateEvent() {
        val editState = _editDialogState.value
        val event = editState.event ?: return

        // Validar
        val error = validateForm(editState.title, editState.date)
        if (error != null) {
            _eventUiState.value = _eventUiState.value.copy(error = error)
            return
        }

        viewModelScope.launch {
            _eventUiState.value = _eventUiState.value.copy(isLoading = true, error = null)

            val updates = mapOf(
                "title" to editState.title,
                "date" to editState.date,
                "description" to editState.description
            )

            val result = repository.updateEvent(event.id, updates)
            result.onSuccess {
                closeEditDialog()
                // No necesitamos recargar manualmente, el snapshot listener lo hará
                _eventUiState.value = _eventUiState.value.copy(isLoading = false)
            }.onFailure { e ->
                _eventUiState.value = _eventUiState.value.copy(
                    error = e.message ?: "Error al actualizar",
                    isLoading = false
                )
            }
        }
    }

    // ========== DELETE ==========
    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _eventUiState.value = _eventUiState.value.copy(isLoading = true, error = null)

            val result = repository.deleteEvent(eventId)
            result.onSuccess {
                // No necesitamos recargar manualmente, el snapshot listener lo hará
                _eventUiState.value = _eventUiState.value.copy(isLoading = false)
            }.onFailure { e ->
                _eventUiState.value = _eventUiState.value.copy(
                    error = e.message ?: "Error al eliminar",
                    isLoading = false
                )
            }
        }
    }

    // ========== UTILIDADES ==========
    fun clearError() {
        _eventUiState.value = _eventUiState.value.copy(error = null)
    }
}
