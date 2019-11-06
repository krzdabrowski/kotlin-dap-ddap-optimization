package com.oast_projekt.input_output

import com.oast_projekt.model.Demand
import com.oast_projekt.model.DemandPath
import com.oast_projekt.model.Link
import com.oast_projekt.model.Network
import com.oast_projekt.utils.parseInput

import java.io.FileReader
import java.util.Scanner

class InputParser (
    private val filename: String
) {
    private val fileLines = mutableListOf<String>()
    private var currentLine: Int = 0

    fun getNetworkFromFile(): Network {
        try {
            val `in` = Scanner(FileReader(filename))

            while (`in`.hasNext()) {
                fileLines.add(`in`.nextLine())
            }
            `in`.close()
        } catch (e: Exception) {
            println("Error reading from file: $filename")
        }

        val links = parseLinks()
        if (fileLines[currentLine] == "-1") currentLine += 2
        else if (fileLines[currentLine] == "") currentLine++
        val demands = parseDemands()

        return Network(links, demands)
    }

    private fun parseLinks(): List<Link> {
        currentLine = 0
        val links = mutableListOf<Link>()
        var linkId = 1

        try {
            currentLine = 1
            while (true) {
                val line = fileLines[currentLine]
                if (line.contains("-1")) break
                else {
                    val link = parseSingleLink(line, linkId)
                    if (link != null) {
                        links.add(link)
                        linkId++
                    } else
                        println("Couldn't parse link")
                }
                currentLine++
            }
        } catch (e: Exception) {
            println("Error while reading a line with link: " + e.message)
        }

        return links
    }

    private fun parseDemands(): List<Demand> {
        currentLine++
        val demands = mutableListOf<Demand>()
        var demandId = 1

        try {
            while (currentLine < fileLines.size) {
                val line = fileLines[currentLine]
                val regexParserResult = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }

                if (regexParserResult.size == 3) {
                    val demand = parseSingleDemand(line, demandId)
                    if (demand != null) {
                        demands.add(demand)
                        demandId++
                    } else
                        println("Couldn't parse demand")
                } else if (line == "") {
                    currentLine++
                    continue
                } else
                    println("Wrong number of elements (" + regexParserResult.size + "), should be 3")
                currentLine++
            }
        } catch (e: Exception) {
            println("Error while parsing a line with demand: " + e.message)
        }

        return demands
    }

    private fun parseSingleLink(line: String, linkId: Int): Link? {
        val regexParserResult = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
        return if (regexParserResult.size == 5) {
            Link(regexParserResult, linkId)
        } else {
            println("Wrong number of elements (" + regexParserResult.size + "), should be 5")
            null
        }
    }

    private fun parseSingleDemand(line: String, demandId: Int): Demand? {
        val regexParserResult = line.parseInput()
        lateinit var tempLine: String

        return if (regexParserResult.size == 3) {
            currentLine++
            val numberOfDemandPaths = fileLines[currentLine].toInt()
            val demandPaths = mutableListOf<DemandPath>()

            currentLine++
            while (currentLine < currentLine + numberOfDemandPaths && currentLine < fileLines.size) {
                tempLine = fileLines[currentLine]
                if (tempLine == "") {
                    break
                } else {
                    val demandPath = parseSingleDemandPath(tempLine)
                    demandPath?.demandId = demandId
                    if (demandPath != null) {
                        demandPaths.add(demandPath)
                    } else
                        println("Error while parsing demand path")
                }
                currentLine++
            }

            Demand(regexParserResult, demandPaths, demandId)
        } else {
            println("Wrong number of elements (" + regexParserResult.size + "), should be 3")
            null
        }
    }

    private fun parseSingleDemandPath(line: String): DemandPath? {
        return if (line.parseInput().size >= 0) {
            DemandPath(line.parseInput())
        } else {
            println("Wrong number of elements, should be >= 0")
            null
        }
    }
}