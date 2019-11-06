package com.oast_projekt.model

/**
 * @param params Line in order: <start node ID> <end node ID> <number of modules> <module cost> <link module></link>
 * @param id ID of the link
</module></number></end></start> */

data class Link (
    private val params: List<String>,
    private val id: Int
) {
    val numberOfModules = params[2].toInt()
    val module = Module(params[3].toDouble())
    val linkModule = params[4].toInt()
}