package com.oast_projekt

import com.google.common.collect.Lists
import com.oast_projekt.model.*
import com.oast_projekt.utils.fillLinkCapacitiesForNewSolutions
import com.oast_projekt.utils.getCombinationsOfOneDemand

import java.util.*

class BruteForceAlgorithm (
    val network: Network
) {
    val allSolutions: List<Solution>
        get() = fillLinkCapacitiesForNewSolutions(getSolutions(), network)

    private fun getSolutions(): List<Solution> {
        val allCombinations = network.demands.map { getCombinationsOfOneDemand(it) }

        val combinationOfIndexes = Lists.cartesianProduct(
            allCombinations.map { combination -> fillListWithSuccessiveIndexes(combination.size) }
        )

        return combinationOfIndexes.map { indexes -> getSolution(allCombinations, indexes) }
    }

    private fun getSolution(combinations: List<List<Solution>>, indexes: List<Int>): Solution {
        val solution = Solution(HashMap())
        for (i in combinations.indices) {
            val index = indexes[i]
            val list = combinations[i]
            val solution1 = list[index]
            val map = solution1.mapOfValues
            solution.mapOfValues.putAll(map)
        }
        return solution
    }

    private fun fillListWithSuccessiveIndexes(size: Int): List<Int> {
        val oneCombination = ArrayList<Int>()
        for (j in 0 until size) {
            oneCombination.add(j)
        }
        return oneCombination
    }

    fun computeDDAP(solutions: List<Solution>): Solution {
        var minimalizedFunctionCostValue = Double.MAX_VALUE
        var cost = 0.0
        lateinit var bestSolution: Solution

        for (solution in solutions) {
            val costsOfLinks = solution.capacitiesOfLinks
            for (j in costsOfLinks!!.indices) {
                cost += network.links[j].module.cost * costsOfLinks[j]  // sum delta(e) * y(e,x)
            }

            if (cost < minimalizedFunctionCostValue) {
                minimalizedFunctionCostValue = cost
                bestSolution = solution
            }
            cost = 0.0
        }
        println("Bruteforce DDAP best solution: $bestSolution\nMinimum cost: $minimalizedFunctionCostValue\n")

        return bestSolution
    }

    fun computeDAP(solutions: List<Solution>): Solution {
        var minimalizedFunctionCostValue = Int.MAX_VALUE
        var cost = 0
        lateinit var bestSolution: Solution

        for (solution in solutions) {
            val maxValues = mutableListOf<Int>()
            for (j in solution.capacitiesOfLinks!!.indices) {
                maxValues.add(solution.capacitiesOfLinks!![j] - network.links[j].numberOfModules)  // number of modules = number of fibre pairs in cable
            }

            cost = Collections.max(maxValues)  // max of link overload
            if (cost < minimalizedFunctionCostValue) {
                minimalizedFunctionCostValue = cost
                bestSolution = solution
            }
        }
        println("Bruteforce DAP best solution: $bestSolution\nMinimum cost: $minimalizedFunctionCostValue\n")

        return bestSolution
    }
}
