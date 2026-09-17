package at.yerova.socialixxx.database

import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun Application.configureDatabase() {

    Database.connect(
        url = "jdbc:h2:file:./local_db/socialixxx_db;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver",
        user = "root",
        password = ""
    )

    transaction {
        SchemaUtils.create(
            UsersTable,
            CredentialsTable,
            DirectChatsTable,
            MessagesTable,
            EventsTable,
            EventParticipantsTable
        )
    }
}