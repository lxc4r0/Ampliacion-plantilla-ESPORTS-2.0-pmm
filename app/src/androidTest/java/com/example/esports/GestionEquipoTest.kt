package com.example.esports

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class GestionEquipoTest {
    @get:Rule val compose = createComposeRule()

    private fun iniciar() {
        compose.setContent { TemaTitans { PantallaEquipo() } }
    }

    private fun irAlJugador(id: Int) {
        compose.onNodeWithTag("plantilla").performScrollToNode(hasTestTag("jugador_$id"))
    }

    private fun accion(id: Int, texto: String) = compose.onNode(
        hasText(texto) and hasAnyAncestor(hasTestTag("jugador_$id"))
    )

    private fun enDialogo(texto: String) = compose.onNode(
        hasText(texto) and hasAnyAncestor(isDialog())
    )

    @Test fun altaValidaCamposYAnadeJugador() {
        iniciar()
        compose.onNodeWithText("Nombre").performTextInput("   ")
        compose.onNodeWithText("Añadir jugador").performClick()
        compose.onNodeWithText("Completa todos los campos.").assertIsDisplayed()
        compose.onNodeWithText("Nombre").performTextReplacement("Carlos")
        compose.onNodeWithText("Nickname").performTextInput("Flash")
        compose.onNodeWithText("Rol").performTextInput("Soporte")
        compose.onNodeWithText("Añadir jugador").performClick()
        compose.onNodeWithText("Nombre").assertTextEquals("Nombre", "")
        irAlJugador(6)
        compose.onNodeWithText("Flash").assertIsDisplayed()
        compose.onNodeWithText("Nombre: Carlos").assertIsDisplayed()
    }

    @Test fun eliminarExigeConfirmacionYPermiteCancelar() {
        iniciar()
        irAlJugador(1)
        accion(1, "Eliminar").performClick()
        enDialogo("Cancelar").performClick()
        compose.onNodeWithTag("jugador_1").assertExists()
        accion(1, "Eliminar").performClick()
        enDialogo("Eliminar").performClick()
        compose.onNodeWithTag("jugador_1").assertDoesNotExist()
        irAlJugador(2)
        compose.onNodeWithText("ShadowX").assertIsDisplayed()
    }

    @Test fun editarValidaCancelaYConservaIdentificador() {
        iniciar()
        irAlJugador(1)
        accion(1, "Editar").performClick()
        enDialogo("Nickname").performTextReplacement("Descartado")
        enDialogo("Cancelar").performClick()
        compose.onNodeWithText("AlexPro").assertExists()
        accion(1, "Editar").performClick()
        enDialogo("Nombre").performTextClearance()
        enDialogo("Guardar cambios").performClick()
        enDialogo("Completa todos los campos.").assertExists()
        enDialogo("Nombre").performTextInput("Alejandro")
        enDialogo("Nickname").performTextReplacement("AlexNuevo")
        enDialogo("Guardar cambios").performClick()
        compose.onNodeWithTag("jugador_1").assertExists()
        compose.onNodeWithText("AlexNuevo").assertExists()
        compose.onNodeWithText("Nombre: Alejandro").assertExists()
    }

    @Test fun restauracionConservaPlantillaYFormulario() {
        val restauracion = StateRestorationTester(compose)
        restauracion.setContent { TemaTitans { PantallaEquipo() } }
        compose.onNodeWithText("Nombre").performTextInput("Carlos")
        compose.onNodeWithText("Nickname").performTextInput("Flash")
        compose.onNodeWithText("Rol").performTextInput("Soporte")
        compose.onNodeWithText("Añadir jugador").performClick()
        compose.onNodeWithText("Nombre").performTextInput("Pendiente")
        restauracion.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Nombre").assertTextEquals("Nombre", "Pendiente")
        irAlJugador(6)
        compose.onNodeWithText("Flash").assertExists()
    }
}
