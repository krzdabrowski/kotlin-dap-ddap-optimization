package com.oast_projekt

import com.oast_projekt.model.*
import com.oast_projekt.utils.computeLinksCapacitiesOfSolution
import com.oast_projekt.utils.addLinkCapacitiesForSolutions
import com.oast_projekt.utils.getCombinationsOfOneDemand

import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

class EvolutionaryAlgorithm(
    private val crossoverProb: Float,
    private val mutationProb: Float,
    private val maxTime: Int,  // secs
    private val numberOfChromosomes: Int,
    private val percentOfBestChromosomes: Float,
    private val numberOfGenerations: Int,
    private val maxMutationNumber: Int,
    private val maxNumberOfContinuousNonBetterSolutions: Int,
    private val seed: Long,
    val network: Network
) {
    private val random = Random(seed)

    // ograniczenia (stop criteria)
    private var endTime = 0L
    private var currentGeneration = 0
    private var currentMutation = 0
    private var currentNumberOfContinuousNonBetterSolutions = 0

    private fun checkStopCriteria(): Boolean {
        if (System.currentTimeMillis() >= endTime) return false
        if (currentGeneration >= numberOfGenerations) return false
        if (currentMutation >= maxMutationNumber) return false
        return currentNumberOfContinuousNonBetterSolutions < maxNumberOfContinuousNonBetterSolutions
    }

    private fun getInitialRandomPopulation(numberOfChromosomes: Int): List<Solution> {
        val allCombinations = network.demands.map { getCombinationsOfOneDemand(it) }
        val allRoutingPossibilities = mutableListOf<Solution>()
        val population = mutableListOf<Solution>()

        // dodaj wszystkie mozliwe chromosomy z kombinacji routingu sieci
        for (i in 0 until numberOfChromosomes) {
            val chromosome = Solution(mutableMapOf())
            for (j in allCombinations.indices) {
                chromosome.mapOfValues.putAll(allCombinations[j][random.nextInt(allCombinations[j].size)].mapOfValues)
            }
            allRoutingPossibilities.add(chromosome)
        }

        val linksCapacities = allRoutingPossibilities.map { computeLinksCapacitiesOfSolution(it, network) }

        // wybierz losowo z wszystkich numberOfChromosomes tworzacych populacje
        for (i in 0 until numberOfChromosomes) {
            val rand = random.nextInt(allRoutingPossibilities.size)
            allRoutingPossibilities[rand].capacitiesOfLinks = linksCapacities[rand]
            population.add(allRoutingPossibilities[rand])
        }
        return population
    }

    fun computeDDAP(): Solution {
        var population = getInitialRandomPopulation(numberOfChromosomes)
        var bestSolution = Solution(mutableMapOf())
        bestSolution.cost = Double.MAX_VALUE

        endTime = System.currentTimeMillis() + maxTime * 1000
        while (checkStopCriteria()) {
            currentGeneration++

            var bestSolutionOfEachGeneration = Solution(mutableMapOf())
            bestSolutionOfEachGeneration.cost = Double.MAX_VALUE

            for (i in population.indices) {
                var cost = 0.0
                val costsOfLinks = population[i].capacitiesOfLinks
                for (j in population[i].capacitiesOfLinks!!.indices) {
                    cost += network.links[j].module.cost * costsOfLinks!![j]  // sum delta(e) * y(e,x)
                }
                population[i].cost = cost

                if (population[i].cost!! < bestSolutionOfEachGeneration.cost!!)
                    bestSolutionOfEachGeneration = population[i]  // najlepsze rozwiazanie w generacji
            }

            if (bestSolutionOfEachGeneration.cost!! < bestSolution.cost!!) {
                bestSolution = bestSolutionOfEachGeneration  // najlepsze rozwiazanie ze wszystkich
                currentNumberOfContinuousNonBetterSolutions = 0
            } else
                currentNumberOfContinuousNonBetterSolutions++

            population = takeBestDDAP(population, percentOfBestChromosomes)
            population = crossover(population, crossoverProb)
            population = mutation(population, mutationProb)
            population = addLinkCapacitiesForSolutions(population, network)

            println("Best cost for generation $currentGeneration: ${bestSolutionOfEachGeneration.cost}")
        }
        println("Cost of total best solution: ${bestSolution.cost!!}")

        return bestSolution
    }

    fun computeDAP(): Solution {
        var population = getInitialRandomPopulation(numberOfChromosomes)
        var bestSolution = Solution(mutableMapOf())
        bestSolution.numberOfLinksWithExceededCapacity = Integer.MAX_VALUE

        endTime = System.currentTimeMillis() + maxTime * 1000
        while (checkStopCriteria()) {
            currentGeneration++

            var bestSolutionOfEachGeneration = Solution(mutableMapOf())
            bestSolutionOfEachGeneration.numberOfLinksWithExceededCapacity = Integer.MAX_VALUE

            for (i in population.indices) {
                val maxValues = mutableListOf<Int>()
                for (j in population[i].capacitiesOfLinks!!.indices) {
                    maxValues.add(max(0, population[i].capacitiesOfLinks!![j] - network.links[j].numberOfModules))  // number of modules = number of fibre pairs in cable
                }
                population[i].numberOfLinksWithExceededCapacity = maxValues.filter { p -> p > 0 }.size

                if (population[i].numberOfLinksWithExceededCapacity!! < bestSolutionOfEachGeneration.numberOfLinksWithExceededCapacity!!)
                    bestSolutionOfEachGeneration = population[i]  // najlepsze rozwiazanie z generacji
            }

            if (bestSolutionOfEachGeneration.numberOfLinksWithExceededCapacity!! < bestSolution.numberOfLinksWithExceededCapacity!!) {
                bestSolution = bestSolutionOfEachGeneration  // najlepsze rozwiazanie z wszystkich
                currentNumberOfContinuousNonBetterSolutions = 0
            } else
                currentNumberOfContinuousNonBetterSolutions++

            population = takeBestDAP(population, percentOfBestChromosomes)
            population = crossover(population, crossoverProb)
            population = mutation(population, mutationProb)
            population = addLinkCapacitiesForSolutions(population, network)

            println("Overload of generation $currentGeneration: ${bestSolutionOfEachGeneration.numberOfLinksWithExceededCapacity}")
        }
        println("Overload of best solution: ${bestSolution.numberOfLinksWithExceededCapacity!!}")

        return bestSolution
    }

    private fun takeBestDDAP(solutions: List<Solution>, percentOfBestChromosomes: Float): MutableList<Solution> {
        val subListEnd = (solutions.size * (percentOfBestChromosomes / 100)).roundToInt()

        return solutions.sortedBy { it.cost }
            .subList(0, subListEnd)  // wybieramy n procent najlepszych rozwiazan
            .toMutableList()
            .apply { addAll(subList(0, solutions.size - subListEnd)) }  // uzupelniamy populacje najlepszymi
    }

    private fun takeBestDAP(solutions: List<Solution>, percentOfBestChromosomes: Float): MutableList<Solution> {
        val subListEnd = (solutions.size * (percentOfBestChromosomes / 100)).roundToInt()

        return solutions.sortedBy { it.numberOfLinksWithExceededCapacity }
            .subList(0, subListEnd)  // wybieramy n procent najlepszych rozwiazan
            .toMutableList()
            .apply { addAll(subList(0, solutions.size - subListEnd)) }  // uzupelniamy populacje najlepszymi
    }

    // region Genetic operators
    private fun crossover(parents: MutableList<Solution>, probabilityOfCrossover: Float): List<Solution> {
        val children = mutableListOf<Solution>()

        // 2 rodzicow krzyzowanych naraz, wiec iteracji jest polowa
        for (i in 0 until parents.size / 2) {
            children.addAll(crossParents(parents.removeAt(random.nextInt(parents.size)), parents.removeAt(random.nextInt(parents.size)), probabilityOfCrossover))
        }
        return children
    }

    private fun crossParents(firstParent: Solution, secondParent: Solution, probabilityOfCrossover: Float): List<Solution> {
        var rand = random.nextDouble()

        return if (rand < probabilityOfCrossover) {
            val children = mutableListOf<Solution>()
            children.addAll(listOf(Solution(mutableMapOf()), Solution(mutableMapOf())))

            for (i in 0 until firstParent.numberOfGenes) {
                rand = random.nextDouble()

                if (rand > 0.5) {
                    children[0].mapOfValues.putAll(firstParent.getGene(i + 1))
                    children[1].mapOfValues.putAll(secondParent.getGene(i + 1))
                } else {
                    children[1].mapOfValues.putAll(firstParent.getGene(i + 1))
                    children[0].mapOfValues.putAll(secondParent.getGene(i + 1))
                }
            }
            children  // zwracanie dzieci po skrzyzowaniu
        } else listOf(firstParent, secondParent)  // w innym przypadku zwracanie rodzicow
    }

    private fun mutation(population: List<Solution>, probabilityOfMutation: Float): MutableList<Solution> {
        val rand = random.nextDouble()

        return if (rand < probabilityOfMutation) {
            val mutants = mutableListOf<Solution>()

            for (i in population.indices) {
                currentMutation++
                val genes = mutableMapOf<Point, Int>()

                for (j in 0 until population[i].numberOfGenes) {
                    genes.putAll(mutateGene(population[i].getGene(j + 1)))
                }
                mutants.add(Solution(genes))
            }
            mutants  // zwracanie zmutowanych rozwiazan
        } else population.toMutableList()  // w innym przypadku brak mutacji
    }

    private fun mutateGene(gene: Map<Point, Int>): Map<Point, Int> {
        val mutatedGene = mutableMapOf<Point, Int>()
        val points = mutableListOf<Point>()
        val values = mutableListOf<Int>()

        for ((key, value) in gene) {
            points.add(key)
            values.add(value)
        }

        for (i in values.indices) {
            val first = random.nextInt(values.size)
            val second = random.nextInt(values.size)

            if (values[first] != 0) {
                values[first] = values[first] - 1
                values[second] = values[second] + 1
                break
            }
        }

        for (i in 0 until gene.size) {
            mutatedGene[points.removeAt(0)] = values.removeAt(0)
        }

        return mutatedGene
    }
    // endregion
}