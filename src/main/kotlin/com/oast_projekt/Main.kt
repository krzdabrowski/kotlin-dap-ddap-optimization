package com.oast_projekt

import com.oast_projekt.utils.Constants.CHOOSE_ALGORITHM
import com.oast_projekt.utils.Constants.CHOOSE_INPUT_FILE_TO_PARSE
import com.oast_projekt.input_output.OutputWriter
import com.oast_projekt.input_output.InputParser
import com.oast_projekt.model.Network
import com.oast_projekt.model.Solution
import com.oast_projekt.utils.*
import java.util.Scanner
import kotlin.system.exitProcess

private val scanner = Scanner(System.`in`)
private lateinit var inputParser: InputParser
private lateinit var path: String
private lateinit var dapOrDdap: String

fun main() {
    do {
        println(CHOOSE_INPUT_FILE_TO_PARSE)
        var choiceMenu = scanner.nextInt()
        when (choiceMenu) {
            1 -> path = "./net/net4.txt"
            2 -> path = "./net/net12_1.txt"
            3 -> path = "./net/net12_2.txt"
            4 -> exitProcess(0)
            else -> println("It's a wrong input. Please type a correct one.")
        }
        if (choiceMenu in 1..3) inputParser = InputParser(path)

        println(CHOOSE_ALGORITHM)
        choiceMenu = scanner.nextInt()
        when (choiceMenu) {
            1 -> bruteforceAlgorithm()
            2 -> evolutionaryAlgorithm()
            else -> println("It's a wrong input. Please type a correct one.")
        }

        val endChoice = scanner.askUserForStringWhenChoiceIs("Do you want to run program again or exit? (A/E)", "A", "E")
        choiceMenu = if (endChoice == "A") 0 else 4

    } while (choiceMenu != 4)
}

private fun bruteforceAlgorithm() {
    dapOrDdap = scanner.askUserForStringWhenChoiceIs("Choose problem to solve (DAP/DDAP): ", "DAP", "DDAP")

    val startTime = System.nanoTime()
    println("Working, please wait...")
    val bruteForceAlgorithm = BruteForceAlgorithm(inputParser.getNetworkFromFile())
    val solution = try {
        if (dapOrDdap == "DAP")
            bruteForceAlgorithm.computeDAP(bruteForceAlgorithm.allSolutions)
        else
            bruteForceAlgorithm.computeDDAP(bruteForceAlgorithm.allSolutions)
    } catch (e: Exception) {
        println("Exception caught: ${e.message}, check if your network is small enough for BF algorithm.")
        null
    }

    val endTime = System.nanoTime()
    showTotalTimeAndSaveToFile("bruteforce", solution, bruteForceAlgorithm.allSolutions, bruteForceAlgorithm.network, startTime, endTime)
}

private fun evolutionaryAlgorithm() {
    with (scanner) {
        val population = askUserForInt("Choose start population size (chromosomes count): ")
        val crossoverProb = askUserForFloatBetweenZeroAndOne("Choose crossover probability from 0 to 1: ")
        val mutationProb = askUserForFloatBetweenZeroAndOne("Choose mutation probability from 0 to 1: ")

        println("******* Stop criteria *******")
        val maxTime = askUserForInt("Choose max work time of the algorithm [s]: ")
        val maxNumberOfGenerations = askUserForInt("Choose max number of generations: ")
        val maxNumberOfMutations = askUserForInt("Choose max number of mutations: ")
        val maxNumberOfContinuousNonBetterSolutions = askUserForInt("Choose max number of the best solution improvement attempts: ")
        val seed = askUserForLong("Choose random number generator seed: ")
        dapOrDdap = askUserForStringWhenChoiceIs("Choose problem to solve (DAP/DDAP): ", "DAP", "DDAP")

        val startTime = System.nanoTime()
        println("Working, please wait...")
        val evolutionaryAlgorithm = EvolutionaryAlgorithm(crossoverProb, mutationProb, maxTime, population, PERCENT_OF_BEST_CHROMOSOMES, maxNumberOfGenerations, maxNumberOfMutations, maxNumberOfContinuousNonBetterSolutions, seed, inputParser.getNetworkFromFile())

        val solution = if (dapOrDdap == "DAP")
            evolutionaryAlgorithm.computeDAP()
        else
            evolutionaryAlgorithm.computeDDAP()

        val endTime = System.nanoTime()
        showTotalTimeAndSaveToFile("evolutionary", solution, null, evolutionaryAlgorithm.network, startTime, endTime)
    }
}

private fun showTotalTimeAndSaveToFile(typeOfAlgorithm: String, solution: Solution?, solutions: List<Solution>?, network: Network, startTime: Long, endTime: Long) {
    println("Time of optimization: " + (endTime - startTime) / 1_000_000_000 + "," + ((endTime - startTime) / 1_000_000 - (endTime - startTime) / 1_000_000_000 * 1_000) + " s")
    OutputWriter().writeSolutionToFile(path + "_solution_${typeOfAlgorithm}_${dapOrDdap}", solution, network)
    if (solutions != null) {
        println("Saving all results to a file, this may take some additional time...")
        OutputWriter().writeAllSolutionsToFile(path + "_allsolutions_${typeOfAlgorithm}_${dapOrDdap}", solutions, network)
    }
}