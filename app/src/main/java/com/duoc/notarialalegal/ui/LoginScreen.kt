package com.duoc.notarialalegal.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.duoc.notarialalegal.R
import com.duoc.notarialalegal.viewmodel.LoginViewModel

@Composable
fun LoginScreen(viewModel: LoginViewModel, onRegister: () -> Unit, onLoginSuccess: () -> Unit) {

    // Cuando hay un usuario (recién ingresado o con sesión guardada en Room) pasamos al inicio
    LaunchedEffect(viewModel.usuario) {
        if (viewModel.usuario != null) {
            onLoginSuccess()
        }
    }

    // Animación de entrada: el logo y el formulario suben y aparecen suavemente al abrir la app
    val estadoEntrada = remember { MutableTransitionState(false).apply { targetState = true } }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visibleState = estadoEntrada,
                enter = fadeIn(animationSpec = tween(800)) +
                    slideInVertically(animationSpec = tween(800)) { altura -> altura / 4 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Image(
                        painter = painterResource(id = R.drawable.logo_la_legal),
                        contentDescription = "Logo Notaria La Legal",
                        modifier = Modifier.widthIn(max = 280.dp).fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Tus trámites notariales desde el celular",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    TarjetaSeccion(titulo = "Iniciar sesión") {
                        // Cada campo envía lo escrito al ViewModel.
                        OutlinedTextField(
                            value = viewModel.email,
                            onValueChange = { viewModel.updateEmail(it) },
                            label = { Text("Correo electrónico") },
                            singleLine = true,
                            isError = viewModel.emailError.isNotEmpty(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )
                        MensajeError(viewModel.emailError)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = viewModel.password,
                            onValueChange = { viewModel.updatePassword(it) },
                            label = { Text("Contraseña") },
                            singleLine = true,
                            isError = viewModel.passwordError.isNotEmpty(),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )
                        MensajeError(viewModel.passwordError)
                        MensajeError(viewModel.loginError)
                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { viewModel.login() },
                            enabled = !viewModel.revisandoSesion,
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            // Mientras se revisa si hay una sesión guardada se muestra un indicador de carga
                            if (viewModel.revisandoSesion) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text("Ingresar")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onRegister) {
                        Text("Crear cuenta")
                    }
                }
            }
        }
    }
}
