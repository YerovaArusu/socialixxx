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
            EventParticipantsTable,
            EventCommentsTable,
            StoriesTable,
            DepartmentsTable,
            UserDepartmentsTable,
            SpacePostsTable,
            SpacePostCommentsTable,
            QuestionsTable,
        )

        // IDK which test departments... also... This should do for now.
        if (Department.count() == 0L) {
            Department.new { name = "Informationstechnologie"; kz = "IT"; description = "Softwareentwicklung & Support" }
            Department.new { name = "Personalwesen"; kz = "HR"; description = "Recruiting & Mitarbeiterbetreuung" }
            Department.new { name = "Logistik"; kz = "LOG"; description = "Lagerhaltung & Versand" }
            Department.new { name = "Marketing"; kz = "MKT"; description = "Werbung & Social Media" }
            Department.new { name = "Vertrieb"; kz = "VER"; description = "Sales & Kundenbetreuung" }
        }
    }
}