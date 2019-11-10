package com.oast_projekt.utils

const val PERCENT_OF_BEST_CHROMOSOMES = 70f

fun newtonSymbol(n: Int, k: Int): Int {
    var result = 1
    for (i in 1..k)
        result = result * (n - i + 1) / i
    return result
}