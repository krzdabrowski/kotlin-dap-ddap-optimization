package com.oast_projekt

import com.google.common.collect.Lists
import com.oast_projekt.model.*
import com.oast_projekt.utils.fillLinkCapacitiesForNewSolutions
import com.oast_projekt.utils.getCombinationsOfOneDemand

import kotlin.math.max

class BruteForceAlgorithm (
    val network: Network
) {
    val allSolutions: List<Solution>
        get() = fillLinkCapacitiesForNewSolutions(getSolutions(), network)

    private fun getSolutions(): List<Solution> {
        val allCombinations = network.demands.map { getCombinationsOfOneDemand(it) }

        val combinationOfIndexes = Lists.cartesianProduct(
            allCombinations.map { combination -> fillListWithConsecutiveIndexes(combination.size) }
        )

        return combinationOfIndexes.map { indexes -> getSolution(allCombinations, indexes) }
    }

    private fun fillListWithConsecutiveIndexes(size: Int): List<Int> {
        val oneCombination = mutableListOf<Int>()
        for (j in 0 until size) {
            oneCombination.add(j)
        }
        return oneCombination
    }

    private fun getSolution(combinations: List<List<Solution>>, indexes: List<Int>): Solution {
        val solution = Solution(mutableMapOf())
        for (i in combinations.indices) {
            val index = indexes[i]
            val listOfSolutions = combinations[i]

            solution.mapOfValues.putAll(listOfSolutions[index].mapOfValues)
        }
        return solution
    }

    fun computeDDAP(solutions: List<Solution>): Solution {
        var cost = 0.0
        var bestSolution = Solution(mutableMapOf())
        bestSolution.cost = Double.MAX_VALUE

        for (solution in solutions) {
            val costsOfLinks = solution.capacitiesOfLinks
            for (j in costsOfLinks!!.indices) {
                cost += network.links[j].module.cost * costsOfLinks[j]  // sum delta(e) * y(e,x)
            }
            solution.cost = cost

            if (solution.cost!! < bestSolution.cost!!) {
                bestSolution = solution
            }
            cost = 0.0
        }
        println("Bruteforce DDAP best solution: $bestSolution\nMinimum cost: ${bestSolution.cost}\n")

        return bestSolution
    }

    fun computeDAP(solutions: List<Solution>): Solution {
        var bestSolution = Solution(mutableMapOf())
        bestSolution.numberOfLinksWithExceededCapacity = Int.MAX_VALUE

        for (solution in solutions) {
            val maxValues = mutableListOf<Int>()
            for (j in solution.capacitiesOfLinks!!.indices) {
                maxValues.add(max(0, solution.capacitiesOfLinks!![j] - network.links[j].numberOfModules))  // number of modules = number of fibre pairs in cable
            }
            solution.numberOfLinksWithExceededCapacity = maxValues.filter { p -> p > 0 }.size

            if (solution.numberOfLinksWithExceededCapacity!! < bestSolution.numberOfLinksWithExceededCapacity!!) {
                bestSolution = solution
            }
        }
        println("Bruteforce DAP best solution: $bestSolution\nOverload of best solution: ${bestSolution.numberOfLinksWithExceededCapacity}\n")

        return bestSolution
    }
}
