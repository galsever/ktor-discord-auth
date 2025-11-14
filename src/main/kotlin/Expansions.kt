package org.srino

import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration

fun CoroutineScope.every(period: Duration, block: suspend () -> Unit) = launch {
    delay(period)
    while (isActive) {
        block()
        delay(period)
    }
}

fun Application.onStop(block: suspend () -> Unit) {
    monitor.subscribe(ApplicationStopping) {
        runBlocking {
            block()
        }
    }
}