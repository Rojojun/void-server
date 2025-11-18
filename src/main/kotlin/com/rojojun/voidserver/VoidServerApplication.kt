package com.rojojun.voidserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VoidServerApplication

fun main(args: Array<String>) {
    runApplication<VoidServerApplication>(*args)
}
