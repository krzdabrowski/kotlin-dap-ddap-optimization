package com.oast_projekt.model

data class DemandPath (
    private val params: List<String>
) {
    val id = params[0].toInt()
    var demandId: Int = 0
    var links = params.drop(1).map { it.toInt() }  // wez kolejne krawedzie pomijajac 1szy element porzadkowy w wierszu
}