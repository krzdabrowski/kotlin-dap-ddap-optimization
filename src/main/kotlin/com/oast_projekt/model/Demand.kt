package com.oast_projekt.model

/**
 * @param params Parameters from input: start node, end node, volume.
 * @param demandPaths List of DemandPath objects
 * @param id
 */

data class Demand (
    private val params: List<String>,
    val demandPaths: List<DemandPath>,
    val id: Int = 0
) {
    val volume = params[2].toInt()
    val numberOfPaths = demandPaths.size
}
