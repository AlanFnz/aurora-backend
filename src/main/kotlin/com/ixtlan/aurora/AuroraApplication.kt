package com.ixtlan.aurora

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AuroraApplication

fun main(args: Array<String>) {
	runApplication<AuroraApplication>(*args)
}
