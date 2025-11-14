package org.srino

import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import org.srino.database.Database
import org.srino.debug.Debug
import org.srino.managers.GsonManager
import org.srino.managers.SessionManager
import org.srino.managers.UserManager
import org.srino.modules.configureRouting
import org.srino.modules.configureSecurity
import org.srino.modules.configureSerialization

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    application = this

    database = Database()

    GsonManager.init()

    userManager = UserManager()
    sessionManager = SessionManager()

    configureSecurity()
    configureSerialization()
    configureRouting()

    onStop {
        Debug.send("Shutting down Application")

        userManager.shutdown()
        sessionManager.shutdown()
    }
}

lateinit var application: Application
lateinit var database: Database

lateinit var userManager: UserManager
lateinit var sessionManager: SessionManager

val dotenv = dotenv()