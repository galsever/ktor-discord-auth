package org.srino.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import org.srino.dotenv

class Database() {

    val settings: MongoClientSettings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(dotenv["DATABASE_URL"]))
        .build()

    val client: MongoClient = MongoClients.create(settings)
    val database: MongoDatabase = client.getDatabase(dotenv["DATABASE_NAME"])
}