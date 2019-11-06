package com.oast_projekt

import com.google.common.collect.Lists
import com.oast_projekt.model.*

import java.util.*
import java.util.stream.Collectors
import kotlin.math.ceil

class BruteForceAlgorithm (
    val network: Network
) {

    private val solutions: List<Solution>
        get() {
            val allCombinations = network.demands
                .map { getCombinationsOfOneDemand(it) }

            val combinationOfIndexes = Lists.cartesianProduct(
                allCombinations
                    .map { combination -> fillListWithSuccessiveIndexes(combination.size) }
            )

            return combinationOfIndexes
                .map { indexes -> getSolution(allCombinations, indexes) }
        }

    val allSolutions: List<Solution>
        get() {
            val solutions = solutions
            val linksCapacities = solutions
                .map { computeLinksCapacitiesOfSolution(it) }

            for (i in solutions.indices) {
                solutions[i].capacitiesOfLinks = linksCapacities[i]
            }
            return solutions
        }

    fun computeDDAP(solutions: List<Solution>): Solution {
        var finalCost = Double.MAX_VALUE
        var cost = 0.0
        var bestSolution = 0

        for (i in solutions.indices) {
            val costsOfLinks = solutions[i].capacitiesOfLinks
            for (j in costsOfLinks!!.indices) {
                cost += network.links[j].module.cost * costsOfLinks[j]
            }
            if (cost < finalCost) {
                finalCost = cost
                bestSolution = i
            }
            cost = 0.0
        }
        println("BF DDAP minimum cost: $finalCost\n")

        return solutions[bestSolution]
    }

    fun computeDAP(solutions: List<Solution>): Solution {
        var minimalizedFunctionCostValue = 0
        lateinit var bestSolution: Solution
        for (solution in solutions) {
            val maxValues = ArrayList<Int>()
            for (j in solution.capacitiesOfLinks!!.indices) {
                maxValues.add(solution.capacitiesOfLinks!![j] - network.links[j].numberOfModules)
            }
            solution.numberOfLinksWithExceededCapacity =
                maxValues.filter { p -> p > 0 }.size

            if (Collections.max(maxValues) < minimalizedFunctionCostValue) {
                minimalizedFunctionCostValue = Collections.max(maxValues)
                bestSolution = solution
            }
        }
        println("BF DAP best solution: $bestSolution with cost of $minimalizedFunctionCostValue\n")
        return bestSolution
    }

    //    public Solution computeDAP(List<Solution> solutions) {
    //        for (Solution solution : solutions) {
    //            List<Integer> maxValues = new ArrayList();
    //            for (int j = 0; j < solution.getCapacitiesOfLinks().size(); j++) {
    //                maxValues.add(Math.max(0, solution.getCapacitiesOfLinks().get(j) - network.getLinks().get(j).getNumberOfModules()));
    //            }
    //            solution.setNumberOfLinksWithExceededCapacity(maxValues.stream().filter(p -> p > 0).collect(Collectors.toList()).size());
    //            if (Collections.max(maxValues) == 0) {
    //                System.out.println("BF DAP best solution: " + solution + "\n");
    //                return solution;
    //            }
    //        }
    //        return null;
    //    }

    private fun getCombinations(sum: Int, numberOfElements: Int): List<List<Int>> {
        val lists = ArrayList<List<Int>>()
        val list = ArrayList<Int>()

        for (i in 0..sum) {
            list.add(i)
        }

        for (i in 0 until numberOfElements) {
            lists.add(list)
        }

        return Lists.cartesianProduct<Int>(lists)
            .filter { product -> sum == product.map { it.toInt() } .sum() }
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

    private fun computeLinksCapacitiesOfSolution(solution: Solution): List<Int> {
        val linksCapacities = ArrayList<Int>()
        for (i in network.links.indices) {
            linksCapacities.add(0)
        }

        val paths = ArrayList<DemandPath>()
        for (demand in network.demands) {
            paths.addAll(demand.demandPaths)
        }

        for (j in network.links.indices) {
            var sum = 0.0
            for (path in paths) {
                val list = Arrays.stream(path.links).boxed().collect(Collectors.toList<Int>())
                if (list.contains(j + 1)) {
                    sum += solution.mapOfValues[Point(path.demandId, path.id)]!!.toDouble()
                }
            }
            linksCapacities[j] = ceil(sum / network.links[j].linkModule.toDouble()).toInt()
        }
        return linksCapacities
    }

    private fun getCombinationsOfOneDemand(demand: Demand): List<Solution> {
        val list = ArrayList<Solution>()
        val numberOfCombinations = calculateNewtonSymbol(demand.numberOfPaths + demand.volume - 1, demand.volume)
        val combinations = getCombinations(demand.volume, demand.numberOfPaths)
        for (i in 0 until numberOfCombinations) {
            val mapOfValuesForOneDemand = mutableMapOf<Point, Int>()
            for (j in 0 until demand.numberOfPaths) {
                val pathId = demand.demandPaths[j].id
                mapOfValuesForOneDemand.put(Point(demand.id, pathId), combinations[i][pathId - 1])
            }
            list.add(Solution(mapOfValuesForOneDemand))

        }
        return list
    }

    private fun calculateNewtonSymbol(n: Int, k: Int): Int {
        var result: Int? = 1
        for (i in 1..k)
            result = result!! * (n - i + 1) / i
        return result!!
    }

    private fun fillListWithSuccessiveIndexes(size: Int): List<Int> {
        val oneCombination = ArrayList<Int>()
        for (j in 0 until size) {
            oneCombination.add(j)
        }
        return oneCombination
    }
}
