package org.srino.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import io.ktor.server.application.*

class Database(application: Application) {

    val settings: MongoClientSettings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(application.environment.config.property("ktor.database.url").getString()))
        .build()

    val client: MongoClient = MongoClients.create(settings)
    val database: MongoDatabase = client.getDatabase(application.environment.config.property("ktor.database.name").getString())
}