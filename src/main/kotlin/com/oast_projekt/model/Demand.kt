package com.oast_projekt.model

data class Demand (
    private val params: List<String>,  // start node, end node, volume
    val demandPaths: List<DemandPath>,
    val id: Int = 0
) {
    val volume = params[2].toInt()
    val numberOfPaths = demandPaths.size
}
