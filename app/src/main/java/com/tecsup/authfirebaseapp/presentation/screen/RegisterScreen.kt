package com.tecsup.authfirebaseapp.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // ---------- TOP APP BAR ----------
        TopAppBar(
            title = { Text("Crear Cuenta") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1976D2),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        // ---------- CONTENIDO PRINCIPAL ----------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Únete a nosotros 🚀",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp)
            )

            Text(
                "Crea tu cuenta para acceder",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(Modifier.height(8.dp))

            // Tarjeta de campos
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
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmar contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Mensaje de error
            if (errorMessage.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFC62828),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // ----------- BOTÓN DE REGISTRO -------------
            Button(
                onClick = {
                    errorMessage = ""
                    
                    when {
                        email.isBlank() -> {
                            errorMessage = "Por favor ingresa tu correo electrónico"
                            return@Button
                        }
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                            errorMessage = "Formato de correo inválido"
                            return@Button
                        }
                        password.isBlank() -> {
                            errorMessage = "Por favor ingresa una contraseña"
                            return@Button
                        }
                        password.length < 6 -> {
                            errorMessage = "La contraseña debe tener al menos 6 caracteres"
                            return@Button
                        }
                        confirmPassword.isBlank() -> {
                            errorMessage = "Por favor confirma tu contraseña"
                            return@Button
                        }
                        password != confirmPassword -> {
                            errorMessage = "Las contraseñas no coinciden"
                            return@Button
                        }
                    }

                    isLoading = true
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    context,
                                    "¡Cuenta creada exitosamente! ✔️",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onRegisterSuccess()
                            } else {
                                errorMessage = when {
                                    task.exception?.message?.contains("email address is already in use") == true -> 
                                        "Este correo ya está registrado"
                                    task.exception?.message?.contains("email address is badly formatted") == true -> 
                                        "El formato del correo es incorrecto"
                                    task.exception?.message?.contains("network error") == true -> 
                                        "Error de conexión. Verifica tu internet"
                                    task.exception?.message?.contains("Password should be at least 6 characters") == true -> 
                                        "La contraseña debe tener al menos 6 caracteres"
                                    else -> task.exception?.message ?: "Error al crear la cuenta"
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            isLoading = false
                            errorMessage = "Error inesperado: ${exception.localizedMessage}"
                        }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (isLoading) "Creando..." else "Registrarse",
                    fontSize = 16.sp
                )
            }
        }
    }
}
