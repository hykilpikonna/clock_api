package org.hydev.clock_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class ClockApiApplication

fun main(args: Array<String>)
{
    runApplication<ClockApiApplication>(*args)
}
