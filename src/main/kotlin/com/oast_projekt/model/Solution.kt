package com.oast_projekt.model

data class Solution (
    val mapOfValues: MutableMap<Point, Int>
) {
    var cost: Float? = null
    var capacitiesOfLinks: List<Int>? = null
    var numberOfLinksWithExceededCapacity: Int? = null

    val numberOfGenes: Int
        get() = mapOfValues.entries
            .map { entry -> entry.key.demandId }
            .size

    fun getGene(geneId: Int): Map<Point, Int> {
        val gene = mutableMapOf<Point, Int>()
        for ((key, value) in mapOfValues) {
            if (key.demandId == geneId) {
                gene[key] = value
            }
        }

        return gene
    }
}

