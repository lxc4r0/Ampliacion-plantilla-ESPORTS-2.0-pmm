package com.example.esports

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TemaTitans { PantallaEquipo() } }
    }
}

@Composable
fun TemaTitans(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color.Blue,
            background = Color.White,
            surface = Color.White
        ),
        content = content
    )
}

// Los jugadores son Serializable para que el Saver pueda guardarlos en el Bundle.
private val PlantillaSaver = listSaver<SnapshotStateList<Jugador>, Jugador>(
    save = { it.toList() },
    restore = { it.toMutableStateList() }
)

@Composable
fun PantallaEquipo() {
    val jugadores = rememberSaveable(saver = PlantillaSaver) {
        jugadoresIniciales().toMutableStateList()
    }
    var siguienteId by rememberSaveable { mutableStateOf(6) }
    var idEditar by rememberSaveable { mutableStateOf<Int?>(null) }
    var idEliminar by rememberSaveable { mutableStateOf<Int?>(null) }

    Surface(modifier = Modifier.fillMaxSize()) {
        // El formulario también se desplaza: sigue accesible con teclado o en horizontal.
        LazyColumn(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding().testTag("plantilla"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "cabecera") {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()) {
                    Text("TEAM TITANS", fontSize = 30.sp,
                        fontWeight = FontWeight.Bold, color = Color.Blue)
                    Text("Gestión de la plantilla", style = MaterialTheme.typography.titleMedium)
                }
            }
            item(key = "formulario") {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Nuevo jugador", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(12.dp))
                        FormularioJugador(onGuardar = { nombre, nickname, rol ->
                            jugadores.add(Jugador(siguienteId, nombre, nickname, rol))
                            siguienteId++
                        })
                    }
                }
            }
            item(key = "contador") {
                Text("Plantilla (${jugadores.size})", fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge)
            }
            if (jugadores.isEmpty()) {
                item(key = "vacio") { Text("No hay jugadores. Añade el primero con el formulario.") }
            }
            items(items = jugadores, key = { it.id }) { jugador ->
                FichaJugador(jugador,
                    onEditar = { idEditar = jugador.id },
                    onEliminar = { idEliminar = jugador.id })
                HorizontalDivider(Modifier.padding(top = 12.dp))
            }
        }
    }

    jugadores.firstOrNull { it.id == idEliminar }?.let { jugador ->
        AlertDialog(
            onDismissRequest = { idEliminar = null },
            title = { Text("Eliminar jugador") },
            text = { Text("¿Estás seguro de que deseas eliminar a ${jugador.nickname}?") },
            confirmButton = {
                TextButton(onClick = {
                    jugadores.removeAll { it.id == jugador.id }
                    idEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { idEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    jugadores.firstOrNull { it.id == idEditar }?.let { jugador ->
        key(jugador.id) {
            AlertDialog(
                onDismissRequest = { idEditar = null },
                title = { Text("Editar jugador") },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        FormularioJugador(jugador = jugador,
                            onGuardar = { nombre, nickname, rol ->
                                val indice = jugadores.indexOfFirst { it.id == jugador.id }
                                if (indice >= 0) {
                                    // copy conserva el identificador, la imagen y los demás datos.
                                    jugadores[indice] = jugadores[indice].copy(
                                        nombre = nombre, nickname = nickname, rol = rol)
                                }
                                idEditar = null
                            },
                            onCancelar = { idEditar = null })
                    }
                },
                confirmButton = {}
            )
        }
    }
}

@Composable
fun FormularioJugador(
    jugador: Jugador? = null,
    onGuardar: (String, String, String) -> Unit,
    onCancelar: (() -> Unit)? = null
) {
    var nombre by rememberSaveable(jugador?.id) { mutableStateOf(jugador?.nombre.orEmpty()) }
    var nickname by rememberSaveable(jugador?.id) { mutableStateOf(jugador?.nickname.orEmpty()) }
    var rol by rememberSaveable(jugador?.id) { mutableStateOf(jugador?.rol.orEmpty()) }
    var validar by rememberSaveable(jugador?.id) { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CampoJugador("Nombre", nombre, { nombre = it }, validar && nombre.isBlank())
        CampoJugador("Nickname", nickname, { nickname = it }, validar && nickname.isBlank())
        CampoJugador("Rol", rol, { rol = it }, validar && rol.isBlank())
        if (validar && (nombre.isBlank() || nickname.isBlank() || rol.isBlank())) {
            Text("Completa todos los campos.", color = MaterialTheme.colorScheme.error)
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                validar = true
                if (nombre.isNotBlank() && nickname.isNotBlank() && rol.isNotBlank()) {
                    onGuardar(nombre.trim(), nickname.trim(), rol.trim())
                    if (jugador == null) {
                        nombre = ""
                        nickname = ""
                        rol = ""
                        validar = false
                    }
                }
            }
        ) { Text(if (jugador == null) "Añadir jugador" else "Guardar cambios") }
        if (onCancelar != null) {
            TextButton(onClick = onCancelar, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}

@Composable
fun CampoJugador(etiqueta: String, valor: String, onCambio: (String) -> Unit, error: Boolean) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        isError = error,
        supportingText = if (error) { { Text("Este campo es obligatorio") } } else null,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun FichaJugador(jugador: Jugador, onEditar: () -> Unit, onEliminar: () -> Unit) {
    Column(Modifier.fillMaxWidth().testTag("jugador_${jugador.id}")) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(jugador.imagen),
                contentDescription = "Foto de ${jugador.nickname}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp).clip(CircleShape))
            Spacer(Modifier.width(15.dp))
            Column(Modifier.weight(1f)) {
                Text(jugador.nickname, fontSize = 20.sp,
                    fontWeight = FontWeight.Bold, color = Color.Blue)
                Text("Nombre: ${jugador.nombre}")
                Text("Rol: ${jugador.rol}")
                jugador.edad?.let { Text("Edad: $it años") }
                jugador.nivel?.let { Text("Nivel: $it") }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onEditar) { Text("Editar") }
            TextButton(onClick = onEliminar,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text("Eliminar") }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEquipo() {
    TemaTitans { PantallaEquipo() }
}
