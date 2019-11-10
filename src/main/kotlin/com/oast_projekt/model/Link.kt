package com.oast_projekt.model

data class Link (
    private val params: List<String>,
    private val id: Int
) {
    val numberOfModules = params[2].toInt()
    val module = Module(params[3].toDouble())
    val linkModule = params[4].toInt()
}