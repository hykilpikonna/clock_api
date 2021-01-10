package org.hydev.clock_api

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.*

fun main() {
    println(listOf("A0101", "A0102"))
    println()

    val testJsonArray = "[\"A1\", \"A2\"]"
    val objectMapper = ObjectMapper()
    val stringArray = objectMapper.readValue(testJsonArray, Array<String>::class.java)
    println(Arrays.toString(stringArray))

    println()
}
