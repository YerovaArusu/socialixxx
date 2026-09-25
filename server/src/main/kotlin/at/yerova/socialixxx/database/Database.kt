package at.yerova.socialixxx.database

import io.ktor.server.application.*
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.LocalDateTime

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
            SpaceIdeasTable
        )

        if (Department.count() == 0L) {
            Department.new {
                name = "Informationstechnologie"; kz = "IT"; description = "Softwareentwicklung & Support"
            }
            Department.new { name = "Personalwesen"; kz = "HR"; description = "Recruiting & Mitarbeiterbetreuung" }
            Department.new { name = "Logistik"; kz = "LOG"; description = "Lagerhaltung & Versand" }
            Department.new { name = "Marketing"; kz = "MKT"; description = "Werbung & Social Media" }
            Department.new { name = "Vertrieb"; kz = "VER"; description = "Sales & Kundenbetreuung" }
        }
        if (User.count() == 0L) {
            val itDept = Department.find { DepartmentsTable.kz eq "IT" }.firstOrNull()
            val hrDept = Department.find { DepartmentsTable.kz eq "HR" }.firstOrNull()
            val mktDept = Department.find { DepartmentsTable.kz eq "MKT" }.firstOrNull()
            val logDept = Department.find { DepartmentsTable.kz eq "LOG" }.firstOrNull()

            // 1. Max Mustermann - IT (2. Lehrjahr)
            val user1 = User.new {
                displayName = "Max Mustermann"
                lu = "LU-01"
                zk = "ZK-A"
                dw = "+43 660 1234567" // Realistische Telefonnummer
                kz = "MM"
                entryDate = LocalDateTime.now().minusYears(1)
                birthday = "2005-04-12"
                gender = "Männlich"
                pronouns = "Er/Ihm"
                profilePictureUrl =
                    "https://ui-avatars.com/api/?name=Max+Mustermann&background=0D8ABC&color=fff&size=128"
            }
            Credential.new {
                username = "max"
                passwordHash = "1234"
                user = user1
            }
            if (itDept != null) user1.departments = org.jetbrains.exposed.v1.jdbc.SizedCollection(listOf(itDept))

            // 2. Anna Schmidt - HR (1. Lehrjahr - KEIN TELEFON FÜR UI TEST)
            val user2 = User.new {
                displayName = "Anna Schmidt"
                lu = "LU-02"
                zk = "ZK-B"
                dw = null
                kz = "AS"
                entryDate = LocalDateTime.now().minusMonths(3)
                birthday = "2006-08-25"
                gender = "Weiblich"
                pronouns = "Sie/Ihr"
                profilePictureUrl = "https://ui-avatars.com/api/?name=Anna+Schmidt&background=E1306C&color=fff&size=128"
            }
            Credential.new {
                username = "anna"
                passwordHash = "1234"
                user = user2
            }
            if (hrDept != null) user2.departments = org.jetbrains.exposed.v1.jdbc.SizedCollection(listOf(hrDept))

            // 3. Leon Weber - Marketing (3. Lehrjahr)
            val user3 = User.new {
                displayName = "Leon Weber"
                lu = "LU-03"
                zk = "ZK-C"
                dw = "+43 676 9876543"
                kz = "LW"
                entryDate = LocalDateTime.now().minusYears(2)
                birthday = "2004-11-03"
                gender = "Divers"
                pronouns = "Dey/Deren"
                profilePictureUrl = "https://ui-avatars.com/api/?name=Leon+Weber&background=F77737&color=fff&size=128"
            }
            Credential.new {
                username = "leon"
                passwordHash = "1234"
                user = user3
            }
            if (mktDept != null) user3.departments = org.jetbrains.exposed.v1.jdbc.SizedCollection(listOf(mktDept))

            // 4. Sophie Müller - IT (Zweite Person in IT, 1. Lehrjahr)
            val user4 = User.new {
                displayName = "Sophie Müller"
                lu = "LU-04"
                zk = "ZK-A"
                dw = "+43 664 1122334"
                kz = "SM"
                entryDate = LocalDateTime.now().minusMonths(6)
                birthday = "2005-09-14"
                gender = "Weiblich"
                pronouns = "Sie/Ihr"
                profilePictureUrl =
                    "https://ui-avatars.com/api/?name=Sophie+Müller&background=9C27B0&color=fff&size=128"
            }
            Credential.new {
                username = "sophie"
                passwordHash = "1234"
                user = user4
            }
            if (itDept != null) user4.departments = org.jetbrains.exposed.v1.jdbc.SizedCollection(listOf(itDept))

            // 5. Felix Wagner - HR (Zweite Person in HR, 2. Lehrjahr)
            val user5 = User.new {
                displayName = "Felix Wagner"
                lu = "LU-05"
                zk = "ZK-B"
                dw = "+43 680 4455667"
                kz = "FW"
                entryDate = LocalDateTime.now().minusYears(1).minusMonths(2)
                birthday = "2005-01-30"
                gender = "Männlich"
                pronouns = "Er/Ihm"
                profilePictureUrl = "https://ui-avatars.com/api/?name=Felix+Wagner&background=4CAF50&color=fff&size=128"
            }
            Credential.new {
                username = "felix"
                passwordHash = "1234"
                user = user5
            }
            if (hrDept != null) user5.departments = org.jetbrains.exposed.v1.jdbc.SizedCollection(listOf(hrDept))

            // 6. Laura Fischer - Logistik (Neue Abteilung, 3. Lehrjahr)
            val user6 = User.new {
                displayName = "Laura Fischer"
                lu = "LU-06"
                zk = "ZK-D"
                dw = "+43 699 1239874"
                kz = "LF"
                entryDate = LocalDateTime.now().minusYears(2).minusMonths(5)
                birthday = "2004-05-18"
                gender = "Weiblich"
                pronouns = "Sie/Ihr"
                profilePictureUrl =
                    "https://ui-avatars.com/api/?name=Laura+Fischer&background=FFC107&color=fff&size=128"
            }
            Credential.new {
                username = "laura"
                passwordHash = "1234"
                user = user6
            }
            if (logDept != null) user6.departments = org.jetbrains.exposed.v1.jdbc.SizedCollection(listOf(logDept))
        }
    }
}