package com.duoc.notarialalegal.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duoc.notarialalegal.model.Usuario

@Composable
fun HomeScreen(usuario: Usuario, onLogout: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .padding(24.dp)
        ) {
            Text("Notaria La Legal", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("¡Bienvenido!", style = MaterialTheme.typography.titleLarge)
            Text("Hola, ${usuario.nombre} (${usuario.rol})")
            Spacer(modifier = Modifier.height(24.dp))
            Text("Mis trámites", style = MaterialTheme.typography.titleLarge)
            Text("Todavía no hay trámites registrados.")
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onLogout) {
                Text("Cerrar sesión")
            }
        }
    }
}
