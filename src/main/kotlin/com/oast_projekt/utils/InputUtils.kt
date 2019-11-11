package com.oast_projekt.utils

import java.util.*

fun Scanner.askUserForInt(message: String): Int {
    var answer: Int

    do {
        println(message)
        while (!this.hasNextInt()) {
            println("It's a wrong input. Please type a correct one.")
            this.next()
        }
        answer = this.nextInt()
    } while (answer <= 0)

    return answer
}

fun Scanner.askUserForFloatBetweenZeroAndOne(message: String): Float {
    var answer: Float

    do {
        println(message)
        while (!this.hasNext()) {
            println("It's a wrong input. Please type a correct one.")
            this.next()
        }
        answer = this.nextFloat()
    } while (answer < 0 || answer > 1)

    return answer
}

fun Scanner.askUserForLong(message: String): Long {
    var answer: Long

    do {
        println(message)
        while (!this.hasNextLong()) {
            println("It's a wrong input. Please type a correct one.")
            this.next()
        }
        answer = this.nextLong()
    } while (answer <= 0)

    return answer
}

fun Scanner.askUserForStringWhenChoiceIs(message: String, firstChoice: String, secondChoice: String): String {
    var answer: String
    println(message)

    do {
        while (!this.hasNextLine()) {
            println("It's a wrong input. Please type a correct one.")
            this.next()
        }
        answer = this.nextLine()
    } while (answer != firstChoice && answer != secondChoice)

    return answer
}

fun String.parseInput(): List<String> {
    return split(" ".toRegex()).dropLastWhile { it.isEmpty() }
}
