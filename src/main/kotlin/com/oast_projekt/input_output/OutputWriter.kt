package com.oast_projekt.input_output

import com.oast_projekt.model.*

import java.io.FileNotFoundException
import java.io.PrintWriter
import java.io.UnsupportedEncodingException
import kotlin.math.ceil

class OutputWriter {
    fun writeSolutionToFile(fileName: String, solution: Solution?, network: Network) {
        val resultToBeSavedToFile = if (solution == null) "No solution found"
        else getFormattedSolution(network, solution)
        
        try {
            PrintWriter("$fileName.txt", "UTF-8").use { writer -> writer.println(resultToBeSavedToFile) }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    fun writeAllSolutionsToFile(fileName: String, solutions: List<Solution>?, network: Network) {
        val allResultsToBeSavedToFile = StringBuilder()

        if (solutions != null) {
            allResultsToBeSavedToFile.append("Number of results: ${solutions.size} \n\n")
            for (i in solutions.indices) {
                allResultsToBeSavedToFile.append("Result no ${i+1}\n")
                allResultsToBeSavedToFile.append(getFormattedSolution(network, solutions[i]))
            }
        } else {
            allResultsToBeSavedToFile.append("No solutions found")
        }

        try {
            PrintWriter("$fileName.txt", "UTF-8").use { writer -> writer.println(allResultsToBeSavedToFile) }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    private fun getFormattedSolution(network: Network, solution: Solution): String {
        val signals = MutableList(network.links.size) { 0 } // inicjalizacja zerami
        val fibers = MutableList(network.links.size) { 0 }
        val paths = mutableListOf<DemandPath>()
        network.demands.map { paths.addAll(it.demandPaths) }

        // czesc krawedziowa (link part)
        for (j in network.links.indices) {
            var sum = 0
            for (path in paths) {
                if (path.links.contains(j + 1)) {  // czy krawedz nalezy do sciezki (delta)
                    sum += solution.mapOfValues[Point(path.demandId, path.id)]!!
                }
            }
            signals[j] = sum  // link load l(e,x) = sum x(d,p)
            fibers[j] = ceil(sum / network.links[j].linkModule.toDouble()).toInt()  // link size y(e,x) = ceil(l(e,x)/M)
        }

        // tworzenie wyjsciowego txt
        var result = signals.size.toString() + "\n\n"
        for (i in signals.indices) {
            result += (i + 1).toString() + " " + signals[i] + " " + fibers[i] + "\n"
        }
        result += "\n"

        // czesc zapotrzebowaniowa (demand part)
        result += network.demands.size.toString() + "\n\n"

        for (element in network.demands) {
            val demandId = element.id

            var demandPaths = 0
            for ((key, value) in solution.mapOfValues) {
                if (key.demandId == demandId && value != 0)
                    demandPaths++
            }

            var demandPathFlowList = ""
            for ((key, value) in solution.mapOfValues) {
                if (key.demandId == demandId && value != 0)
                    demandPathFlowList += key.pathId.toString() + " " + value + "\n"
            }

            result += "$demandId $demandPaths\n"
            result += demandPathFlowList + "\n"
        }

        return result
    }
}
