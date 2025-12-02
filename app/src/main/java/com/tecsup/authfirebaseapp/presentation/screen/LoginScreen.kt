package com.tecsup.authfirebaseapp.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.tecsup.authfirebaseapp.R

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // ---------- HEADER CON GRADIENTE ----------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    color = Color(0xFF1976D2),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img),
                    contentDescription = "Logo Tecsup",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Event Planner ",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }

        // ---------- CONTENIDO PRINCIPAL ----------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Bienvenido 👋",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp),
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                "Inicia sesión con tu cuenta",
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

            // ---------- BOTÓN DE LOGIN ----------
            Button(
                onClick = {
                    errorMessage = ""
                    
                    when {
                        email.isBlank() -> {
                            errorMessage = "Por favor ingresa tu correo electrónico"
                            return@Button
                        }
                        password.isBlank() -> {
                            errorMessage = "Por favor ingresa tu contraseña"
                            return@Button
                        }
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                            errorMessage = "Formato de correo inválido"
                            return@Button
                        }
                    }

                    isLoading = true
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                Toast.makeText(context, "¡Bienvenido! ✅", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            } else {
                                errorMessage = when (task.exception?.message) {
                                    "The email address is badly formatted." -> "El formato del correo es incorrecto"
                                    "The supplied auth credential is incorrect, malformed or has expired." -> "Credenciales incorrectas"
                                    "There is no user record corresponding to this identifier. The user may have been deleted." -> "Usuario no encontrado"
                                    "The password is invalid or the user does not have a password." -> "Contraseña incorrecta"
                                    "Access to this account has been temporarily disabled due to many failed login attempts. You can immediately restore it by resetting your password or you can try again later." -> "Cuenta temporalmente bloqueada por múltiples intentos fallidos"
                                    "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> "Error de conexión. Verifica tu internet"
                                    else -> task.exception?.message ?: "Error al iniciar sesión"
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
                    if (isLoading) "Cargando..." else "Ingresar",
                    fontSize = 16.sp
                )
            }

            // ---------- REDIRECCIÓN A REGISTRO ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "¿No tienes cuenta? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text("Regístrate", color = Color(0xFF1976D2))
                }
            }
        }
    }
}
