package com.example.esports

import androidx.annotation.DrawableRes
import java.io.Serializable

data class Jugador(
    val id: Int,
    val nombre: String,
    val nickname: String,
    val rol: String,
    @param:DrawableRes val imagen: Int = R.drawable.jugador4,
    val edad: Int? = null,
    val nivel: Int? = null
) : Serializable

fun jugadoresIniciales() = listOf(
    Jugador(1, "Alex", "AlexPro", "Atacante", R.drawable.jugador1, 21, 87),
    Jugador(2, "David", "ShadowX", "Defensor", R.drawable.jugador2, 24, 91),
    Jugador(3, "Marta", "MartaGG", "Soporte", R.drawable.jugador3, 20, 84),
    Jugador(4, "Daniel", "Destroyer", "Atacante", R.drawable.jugador4, 23, 89),
    Jugador(5, "Albert", "Einstein", "Estratega", R.drawable.jugador5, 22, 88)
)
