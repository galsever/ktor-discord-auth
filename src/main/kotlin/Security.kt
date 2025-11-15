package org.srino

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
data class UserSession(val sessionId: String)

@Serializable
data class User(
    val id: String,
    val username: String,
    val avatar: String,
    val global_name: String,
    val email: String,
)

fun Application.configureSecurity() {

    val signingKey = hex(this@configureSecurity.environment.config.property("ktor.authentication.keys.sign").getString())
    val encryptKey = hex(this@configureSecurity.environment.config.property("ktor.authentication.keys.encrypt").getString())

    install(Sessions) {
        cookie<UserSession>("user_session") {
            cookie.httpOnly = true
            cookie.extensions["SameSite"] = "lax"

            transform(SessionTransportTransformerEncrypt(encryptKey, signingKey))
        }
    }

    authentication {
        oauth("auth-oauth-discord") {
            urlProvider = { "http://localhost:8080/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "discord",
                    authorizeUrl = "https://discord.com/api/oauth2/authorize",
                    accessTokenUrl = "https://discord.com/api/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = this@configureSecurity.environment.config.property("ktor.authentication.discord.clientId")
                        .getString(),
                    clientSecret = this@configureSecurity.environment.config.property("ktor.authentication.discord.clientSecret")
                        .getString(),
                    defaultScopes = listOf("email", "identify"),
                )
            }
            client = HttpClient(Apache)
        }
        session<UserSession>("session-auth") {
            validate { session -> session }
            challenge { call.respondRedirect("/login") }
        }
    }

    routing {

        val httpClient = HttpClient(Apache) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        authenticate("auth-oauth-discord") {
            get("/login") {
                // Redirects to 'authorizeUrl' automatically
            }

            get("/callback") {
                val currentPrincipal: OAuthAccessTokenResponse.OAuth2 = call.principal() ?: return@get

                val user = httpClient.get("https://discord.com/api/users/@me") {
                    bearerAuth(currentPrincipal.accessToken)
                }.body<User>()

                val session = UserSession(UUID.randomUUID().toString())

                println(Json.encodeToString(user))

                call.sessions.set(session)
                call.respondRedirect("/check")
            }
        }

        authenticate("session-auth") {
            get("/check") {

                val session = call.sessions.get<UserSession>()
                    ?: return@get call.respondText("You are not logged in, my friend")

                call.respondText("You are logged in! Session ID: ${session.sessionId}")
            }
        }
    }
}
