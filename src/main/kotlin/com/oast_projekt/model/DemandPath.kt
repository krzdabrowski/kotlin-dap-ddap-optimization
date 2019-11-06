package com.oast_projekt.model


data class DemandPath (
    private val params: List<String>
) {
    val id = params[0].toInt()
    var demandId: Int = 0
    var links: IntArray  // Contains only links' IDs (of the links that belong to the

    init {
        val tempLinks = IntArray(params.size - 1)
        for (i in 1 until params.size) {
            tempLinks[i - 1] = params[i].toInt()
        }
        links = tempLinks
    }
}