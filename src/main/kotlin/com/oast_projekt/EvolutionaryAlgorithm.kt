package com.oast_projekt

import com.google.common.collect.Lists
import com.oast_projekt.model.*
import java.lang.Math.*

import java.util.*
import java.util.stream.Collectors

class EvolutionaryAlgorithm(
    private val pCross: Float,
    private val pMutate: Float,
    private val maxTime: Int,  // czas w sekundach
    private val numberOfChromosomes: Int,
    private val percentOfBestChromosomes: Float,
    private val numberOfGenerations: Int,
    private val maxMutationNumber: Int,
    private val maxNumberOfContinuousNonBetterSolutions: Int,
    private val seed: Long,
    val network: Network
) {
    private val random: Random = Random(seed)

    // ograniczenia
    private var endTime = 0L

    // aktualny stan
    private var currentGeneration = 0
    private var currentMutation = 0
    private var currentNumberOfContinuousNonBetterSolutions = 0

    /**
     * @return True if the loop should go on
     */
    private fun computeStopCriterion(): Boolean {

        if (System.currentTimeMillis() >= endTime)
            return false

        if (currentGeneration >= numberOfGenerations)
            return false

        if (currentMutation >= maxMutationNumber)
            return false

        return currentNumberOfContinuousNonBetterSolutions < maxNumberOfContinuousNonBetterSolutions
    }

    fun computeDDAP(): Solution {
        //Początkowa pula rozwiązań - chromosomy
        var population: MutableList<Solution> = getInitialRandomPopulation(numberOfChromosomes, seed)

        //Startowe najlepsze rozwiązania - koszt = infinity
        var bestSolution = Solution(HashMap())
        bestSolution.cost = Float.MAX_VALUE

        //maxTime w sec
        endTime = System.currentTimeMillis() + maxTime * 1000
        while (computeStopCriterion()) {
            currentGeneration++

            var bestSolutionOfGeneration = Solution(HashMap())
            bestSolutionOfGeneration.cost = Float.MAX_VALUE

            for (i in population.indices) {

                var cost = 0f
                //Obliczamy koszt dla każdego z chromosomów
                val costsOfLinks = population[i].capacitiesOfLinks
                for (j in population[i].capacitiesOfLinks!!.indices) {
                    cost += (network.links[j].module.cost * costsOfLinks!![j]).toFloat()

                }
                population[i].cost = cost

                //zapisujemy najlepsze rozwiazanie w generacji
                if (population[i].cost!! < bestSolutionOfGeneration.cost!!)
                    bestSolutionOfGeneration = population[i]
            }

            // zapisujemy najlepsze rozwiazanie w historii
            if (bestSolutionOfGeneration.cost!! < bestSolution.cost!!) {
                bestSolution = bestSolutionOfGeneration
                currentNumberOfContinuousNonBetterSolutions = 0
            } else
                currentNumberOfContinuousNonBetterSolutions++


            population = takeBestDDAP(population, percentOfBestChromosomes)
            population = crossover(population, seed, pCross)
            population = mutation(population, seed, pMutate)
            population = fillLinkCapacitiesForNewSolutions(population)
            // nie możemy w tym momencie wybrac najlepszych bo nie są obliczone koszta (dlatego przed mutacja)

            println("Cost of generation " + currentGeneration + ": " + bestSolutionOfGeneration.cost)
        }
        println("Cost of best solution: " + bestSolution.cost!!)

        return bestSolution
    }

    private fun takeBestDDAP(solutions: List<Solution>, percentOfBestChromosomes: Float): MutableList<Solution> {
        // Wybieramy x procent najlepszych
        val subListEnd = round(solutions.size * (percentOfBestChromosomes / 100))
        val list0 = solutions
            .sortedWith(Comparator.comparing<Solution, Float> { it.cost })

        val list = solutions
            .sortedWith(Comparator.comparing<Solution, Float> { it.cost })
            .subList(0, round(solutions.size * (percentOfBestChromosomes / 100)))
            .toMutableList()

        // Dopełniamy najlepszymi, aby populacja nie zmalała
        list.addAll(list0.subList(0, solutions.size - subListEnd))

        return list
    }

    private fun crossover(
        parents: MutableList<Solution>,
        seed: Long,
        probabilityOfCrossover: Float
    ): MutableList<Solution> {
        val children = ArrayList<Solution>()

        val parentsSize = parents.size
        //w jednej iteracji krzyżowanie 2 rodziców z listy, wiec liczba iteracji / 2
        // wywalamy rodzicow z listy i bierzemy kolejnych 2
        for (i in 0 until parentsSize / 2) {
            children.addAll(
                crossParents(
                    parents.removeAt(random.nextInt(parents.size)),
                    parents.removeAt(random.nextInt(parents.size)),
                    probabilityOfCrossover, seed
                )
            )
        }
        return children
    }

    private fun crossParents(parent0: Solution, parent1: Solution, probabilityOfCrossover: Float, seed: Long): List<Solution> {
        var children: MutableList<Solution> = ArrayList()
        var rand = random.nextDouble()

        // albo krzyżujemy rodziców i zwracamy dzieci, albo zwracamy rodziców
        if (rand < probabilityOfCrossover) {
            children = ArrayList()
            children.add(Solution(HashMap()))
            children.add(Solution(HashMap()))

            for (i in 0 until parent0.numberOfGenes) {
                rand = random.nextDouble()

                if (rand > 0.5) {
                    children[0]
                        .mapOfValues.putAll(parent0.getGene(i + 1))
                    children[1]
                        .mapOfValues
                        .putAll(parent1.getGene(i + 1))
                } else {
                    children[1]
                        .mapOfValues
                        .putAll(parent0.getGene(i + 1))
                    children[0]
                        .mapOfValues
                        .putAll(parent1.getGene(i + 1))
                }
            }
            return children
        }

        val solutions = ArrayList<Solution>()
        solutions.add(parent0)
        solutions.add(parent1)

        return solutions
    }

    private fun mutation(population: List<Solution>, seed: Long, probabilityOfMutation: Float): MutableList<Solution> {
        val mutants = ArrayList<Solution>()

        val rand = random.nextDouble()

        for (i in population.indices) {
            // Losowe wystąpienie mutacji
            if (rand < probabilityOfMutation) {

                currentMutation++
                val genes = HashMap<Point, Int>()

                for (j in 0 until population[i].numberOfGenes) {
                    genes.putAll(mutateGene(population[i].getGene(j + 1), seed))
                }
                mutants.add(Solution(genes))
            } else {
                mutants.add(population[i])
            }
        }
        return mutants
    }

    private fun mutateGene(gene: Map<Point, Int>, seed: Long): Map<Point, Int> {

        val mutatedGene = HashMap<Point, Int>()
        val points = ArrayList<Point>()
        val values = ArrayList<Int>()

        for ((key, value) in gene) {
            points.add(key)
            values.add(value)
        }

        for (i in values.indices) {

            val i0 = random.nextInt(values.size)
            val i1 = random.nextInt(values.size)

            if (values[i0] != 0) {
                values[i0] = values[i0] - 1
                values[i1] = values[i1] + 1
                break
            }
        }

        for (i in 0 until gene.size) {
            mutatedGene[points.removeAt(0)] = values.removeAt(0)
        }

        return mutatedGene
    }


    private fun fillLinkCapacitiesForNewSolutions(solutions: MutableList<Solution>): MutableList<Solution> {

        val linksCapacities = solutions
            .map { computeLinksCapacitiesOfSolution(it) }

        for (i in solutions.indices) {
            if (solutions[i].capacitiesOfLinks == null)
                solutions[i].capacitiesOfLinks = linksCapacities[i]
        }
        return solutions
    }


    private fun getInitialRandomPopulation(numberOfChromosomes: Int, seed: Long): MutableList<Solution> {

        //Ze wszystkich możliwych kombinacji routingu (nie obliczone koszta itp) wybieramy losowe N chromosomow (numberOfChromosomes)
        val allCombinations = network.demands
            .map { getCombinationsOfOneDemand(it) }

        val routingPossibilities = ArrayList<Solution>()

        for (i in 0 until numberOfChromosomes) {
            val chromosome = Solution(HashMap())
            for (j in allCombinations.indices) {
                chromosome.mapOfValues.putAll(allCombinations[j][random.nextInt(allCombinations[j].size)].mapOfValues)
            }
            routingPossibilities.add(chromosome)
        }

        val linksCapacities = routingPossibilities
            .map { computeLinksCapacitiesOfSolution(it) }

        val list = ArrayList<Solution>()

        for (i in 0 until numberOfChromosomes) {
            val rand = random.nextInt(routingPossibilities.size)
            routingPossibilities[rand].capacitiesOfLinks = linksCapacities[rand]
            list.add(routingPossibilities[rand])
        }
        return list
    }


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
            .filter { product -> sum == product.map { it.toInt() }.sum() }
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
            linksCapacities[j] = kotlin.math.ceil(sum / network.links[j].linkModule.toDouble()).toInt()
        }
        return linksCapacities
    }

    private fun calculateNewtonSymbol(n: Int, k: Int): Int {
        var result: Int? = 1
        for (i in 1..k)
            result = result!! * (n - i + 1) / i
        return result!!
    }

    fun computeDAP(): Solution {
        //Początkowa pula rozwiązań - chromosomy
        var population: MutableList<Solution> = getInitialRandomPopulation(numberOfChromosomes, seed)

        //Startowe najlepsze rozwiązania - koszt = infinity
        var bestSolution = Solution(HashMap())
        bestSolution.numberOfLinksWithExceededCapacity = Integer.MAX_VALUE

        //maxTime w sec
        endTime = System.currentTimeMillis() + maxTime * 1000

        while (computeStopCriterion()) {
            currentGeneration++

            var bestSolutionOfGeneration = Solution(HashMap())
            bestSolutionOfGeneration.numberOfLinksWithExceededCapacity = Integer.MAX_VALUE

            for (i in population.indices) {

                val maxValues = ArrayList<Int>()
                for (j in population[i].capacitiesOfLinks!!.indices) {
                    maxValues.add(
                        max(
                            0,
                            population[i].capacitiesOfLinks!![j] - network.links[j].numberOfModules
                        )
                    )
                }
                population[i].numberOfLinksWithExceededCapacity =
                    maxValues.filter { p -> p > 0 }.size


                //zapisujemy najlepsze rozwiazanie w generacji
                if (population[i].numberOfLinksWithExceededCapacity!! < bestSolutionOfGeneration.numberOfLinksWithExceededCapacity!!)
                    bestSolutionOfGeneration = population[i]


            }

            // zapisujemy najlepsze rozwiazanie w historii
            if (bestSolutionOfGeneration.numberOfLinksWithExceededCapacity!! < bestSolution.numberOfLinksWithExceededCapacity!!) {
                bestSolution = bestSolutionOfGeneration
                currentNumberOfContinuousNonBetterSolutions = 0
            } else
                currentNumberOfContinuousNonBetterSolutions++


            population = takeBestDAP(population, percentOfBestChromosomes)
            population = crossover(population, seed, pCross)
            population = mutation(population, seed, pMutate)
            population = fillLinkCapacitiesForNewSolutions(population)
            // nie możemy w tym momencie wybrac najlepszych bo nie są obliczone koszta (dlatego przed mutacja)

            println("Overload of generation " + currentGeneration + ": " + bestSolutionOfGeneration.numberOfLinksWithExceededCapacity)
        }
        println("Overload of best solution: " + bestSolution.numberOfLinksWithExceededCapacity!!)

        return bestSolution
    }

    private fun takeBestDAP(solutions: List<Solution>, percentOfBestChromosomes: Float): MutableList<Solution> {
        // Wybieramy x procent najlepszych
        val subListEnd = round(solutions.size * (percentOfBestChromosomes / 100))
        val list0 = solutions
            .sortedWith(Comparator.comparing<Solution, Int> { it.numberOfLinksWithExceededCapacity })

        val list = solutions
            .sortedWith(Comparator.comparing<Solution, Int> { it.numberOfLinksWithExceededCapacity })
            .subList(0, round(solutions.size * (percentOfBestChromosomes / 100)))
            .toMutableList()

        // Dopełniamy najlepszymi, aby populacja nie zmalała
        list.addAll(list0.subList(0, solutions.size - subListEnd))

        return list
    }
}