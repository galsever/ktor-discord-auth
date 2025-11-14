package org.srino.managers

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.server.sessions.SessionSerializer

lateinit var gson: Gson

class GsonManager { companion object {
    fun init() {
        gson = extendBuilder(GsonBuilder()).create()
    }
    private fun extendBuilder(builder: GsonBuilder): GsonBuilder {
        return builder
    }
}}

class GsonSessionSerializer<T: Any>(
    val clazz: Class<T>
): SessionSerializer<T> {
    override fun serialize(session: T): String = gson.toJson(session)
    override fun deserialize(text: String): T = gson.fromJson(text, clazz)
}