package org.srino.debug

import org.srino.application

class Debug { companion object {
    fun send(message: String) {
        if (!isDebuggingEnabled) return
        println("DEBUG | $message")
    }

    val isDebuggingEnabled get() = application.environment.config.property("ktor.debugging").getString().toBooleanStrict()
}}