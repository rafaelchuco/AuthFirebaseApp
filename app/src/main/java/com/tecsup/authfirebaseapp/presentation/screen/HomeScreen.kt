package com.tecsup.authfirebaseapp.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.tecsup.authfirebaseapp.presentation.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    viewModel: EventViewModel = viewModel()
) {
    val auth = FirebaseAuth.getInstance()
    val email = auth.currentUser?.email ?: "Usuario"

    // Observar estados
    val eventUiState by viewModel.eventUiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val editDialogState by viewModel.editDialogState.collectAsState()

    // Diálogo de edición
    if (editDialogState.isOpen && editDialogState.event != null) {
        Dialog(
            onDismissRequest = { viewModel.closeEditDialog() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Editar Evento ✏️",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF1976D2)
                    )

                    OutlinedTextField(
                        value = editDialogState.title,
                        onValueChange = { viewModel.updateEditTitle(it) },
                        label = { Text("Título *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = editDialogState.date,
                        onValueChange = { viewModel.updateEditDate(it) },
                        label = { Text("Fecha *") },
                        placeholder = { Text("DD/MM/YYYY") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = editDialogState.description,
                        onValueChange = { viewModel.updateEditDescription(it) },
                        label = { Text("Descripción (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3,
                        maxLines = 5
                    )

                    eventUiState.error?.let {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(it, color = Color(0xFFC62828))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.closeEditDialog() },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = { viewModel.updateEvent() },
                            enabled = !eventUiState.isLoading,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (eventUiState.isLoading) "Guardando..." else "Actualizar")
                        }
                    }
                }
            }
        }
    }

    // ---------- UI PRINCIPAL ----------

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Eventos 📅") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1976D2),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(
                        onClick = {
                            try {
                                // Detener listeners antes de cerrar sesión
                                viewModel.stopListening()
                                auth.signOut()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                onLogout()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bienvenida
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                "Bienvenido 👋",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                // ----- Formulario nuevo evento -----
                item {
                    Text(
                        "Crear nuevo evento",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = formState.title,
                                onValueChange = { viewModel.updateFormTitle(it) },
                                label = { Text("Título *") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = formState.date,
                                onValueChange = { viewModel.updateFormDate(it) },
                                label = { Text("Fecha *") },
                                placeholder = { Text("DD/MM/YYYY") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = formState.description,
                                onValueChange = { viewModel.updateFormDescription(it) },
                                label = { Text("Descripción (opcional)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                minLines = 3,
                                maxLines = 5
                            )

                            Button(
                                onClick = { viewModel.saveEvent() },
                                enabled = !eventUiState.isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    if (eventUiState.isLoading) "Guardando..." else "Guardar evento",
                                    fontSize = 14.sp
                                )
                            }

                            if (eventUiState.isLoading) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            eventUiState.error?.let {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = Color(0xFFFFEBEE),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Text(it, color = Color(0xFFC62828))
                                }
                            }
                        }
                    }
                }

                // ----- Lista de eventos -----
                item {
                    Text(
                        "Mis eventos (${eventUiState.events.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (eventUiState.events.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No hay eventos registrados",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                items(eventUiState.events) { event ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Título
                            Text(
                                event.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF1976D2),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            
                            // Fecha
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "📅",
                                    fontSize = 16.sp
                                )
                                Text(
                                    event.date,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }

                            // Descripción (si existe)
                            if (event.description.isNotBlank()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                
                                Text(
                                    "Descripción:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                                
                                Text(
                                    event.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            // Botones de acción
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.openEditDialog(event) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Editar", color = Color.White, fontSize = 13.sp)
                                }

                                Button(
                                    onClick = { viewModel.deleteEvent(event.id) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCDD2)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Eliminar", color = Color(0xFFC62828), fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
