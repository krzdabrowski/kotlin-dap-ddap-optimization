package com.oast_projekt.utils

import com.google.common.collect.Lists
import com.oast_projekt.model.*
import kotlin.math.ceil

fun getCombinationsOfOneDemand(demand: Demand): List<Solution> {
    val list = mutableListOf<Solution>()
    val numberOfFlowPatternsX = newtonSymbol(demand.numberOfPaths + demand.volume - 1, demand.numberOfPaths - 1)  // newton symbol of (h(d)+r(d)-1, r(d)-1)
    val combinations = getCombinations(demand.volume, demand.numberOfPaths)
    for (i in 0 until numberOfFlowPatternsX) {
        val mapOfValuesForOneDemand = mutableMapOf<Point, Int>()
        for (j in 0 until demand.numberOfPaths) {
            val pathId = demand.demandPaths[j].id
            mapOfValuesForOneDemand.put(Point(demand.id, pathId), combinations[i][pathId - 1])
        }
        list.add(Solution(mapOfValuesForOneDemand))

    }
    return list
}

private fun getCombinations(sum: Int, numberOfElements: Int): List<List<Int>> {
    val lists = mutableListOf<List<Int>>()
    val list = mutableListOf<Int>()

    for (i in 0..sum) {
        list.add(i)
    }

    for (i in 0 until numberOfElements) {
        lists.add(list)
    }

    return Lists.cartesianProduct<Int>(lists)
        .filter { product -> sum == product.sum() }
}

fun fillLinkCapacitiesForNewSolutions(solutions: List<Solution>, network: Network): List<Solution> {
    val linksCapacities = solutions.map { computeLinksCapacitiesOfSolution(it, network) }

    for (i in solutions.indices) {
        if (solutions[i].capacitiesOfLinks == null)
            solutions[i].capacitiesOfLinks = linksCapacities[i]
    }
    return solutions
}

fun computeLinksCapacitiesOfSolution(solution: Solution, network: Network): List<Int> {
    val linksCapacities = MutableList(network.links.size) { 0 }  // inicjalizacja zerami
    val paths = mutableListOf<DemandPath>()
    network.demands.map { paths.addAll(it.demandPaths) }

    for (j in network.links.indices) {
        var sum = 0
        for (path in paths) {
            if (path.links.contains(j + 1)) {  // czy krawedz nalezy do sciezki (delta)
                sum += solution.mapOfValues[Point(path.demandId, path.id)]!!
            }
        }
        linksCapacities[j] = ceil(sum / network.links[j].linkModule.toDouble()).toInt()  // link size y(e,x) = ceil(l(e,x)/M)
    }
    return linksCapacities
}