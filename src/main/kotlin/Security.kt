package org.srino

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable

@Serializable
data class UserSession(val state: String, val token: String)

fun Application.configureSecurity() {

    install(Sessions) {
        cookie<UserSession>("user_session")
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
                    clientId = this@configureSecurity.environment.config.property("ktor.authentication.discord.clientId").getString(),
                    clientSecret = this@configureSecurity.environment.config.property("ktor.authentication.discord.clientSecret").getString(),
                    defaultScopes = listOf("email"),
                )
            }
            client = HttpClient(Apache)
        }
    }

    routing {
        authenticate("auth-oauth-discord") {
            get("/login") {
                // Redirects to 'authorizeUrl' automatically
            }

            get("/callback") {
                val currentPrincipal: OAuthAccessTokenResponse.OAuth2 = call.principal() ?: return@get
                val state = currentPrincipal.state ?: return@get
                call.sessions.set(UserSession(state, currentPrincipal.accessToken))
                call.respondRedirect("/")
            }
        }
    }
}
