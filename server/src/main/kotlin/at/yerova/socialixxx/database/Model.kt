package at.yerova.socialixxx.database

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime

object UsersTable : IntIdTable("users") {
    val displayName = varchar("display_name", 100)
    val lu = varchar("lu", 50).nullable()
    val zk = varchar("zk", 50).nullable()
    val dw = varchar("dw", 50).nullable()
    val kz = varchar("kz", 50).nullable()
    val entryDate = datetime("entry_date").default(LocalDateTime.now())

    val birthday = varchar("birthday", 10).nullable() // Format YYYY-MM-DD
    val gender = varchar("gender", 50).nullable()
    val pronouns = varchar("pronouns", 50).nullable()
    val profilePictureUrl = varchar("profile_picture_url", 500).nullable()
}

object CredentialsTable : IntIdTable("credentials") {
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val userId = reference("user_id", UsersTable)
}

object DirectChatsTable : IntIdTable("direct_chats") {
    val user1Id = reference("user1_id", UsersTable)
    val user2Id = reference("user2_id", UsersTable)
    val status = integer("status").default(0)

    init {
        uniqueIndex(user1Id, user2Id)
    }
}

object MessagesTable : IntIdTable("messages") {
    val chatId = reference("chat_id", DirectChatsTable)
    val senderId = reference("sender_id", UsersTable)
    val content = text("content")
    val timestamp = datetime("timestamp").default(LocalDateTime.now())
    val isRead = bool("is_read").default(false)
}

object EventsTable : IntIdTable("events") {
    val title = varchar("title", 150)
    val description = text("description").nullable()
    val eventTime = datetime("event_time")
    val creatorId = reference("creator_id", UsersTable)
}

object EventParticipantsTable : Table("event_participants") {
    val eventId = reference("event_id", EventsTable)
    val userId = reference("user_id", UsersTable)
    override val primaryKey = PrimaryKey(eventId, userId)
}

object EventCommentsTable : IntIdTable("event_comments") {
    val eventId = reference("event_id", EventsTable)
    val userId = reference("user_id", UsersTable)
    val content = text("content")
    val timestamp = datetime("timestamp").default(java.time.LocalDateTime.now())
}

object StoriesTable : IntIdTable("stories") {
    val userId = reference("user_id", UsersTable)
    val mediaUrl = varchar("media_url", 500)
    val caption = text("caption").nullable()
    val timestamp = datetime("timestamp").default(LocalDateTime.now())
    val isActive = bool("is_active").default(true)
}

object DepartmentsTable : IntIdTable("departments") {
    val name = varchar("name", 100)
    val kz = varchar("kz", 20) // z.B. "IT", "HR", "LOG"
    val description = text("description").nullable()
}

object UserDepartmentsTable : Table("user_departments") {
    val userId = reference("user_id", UsersTable)
    val departmentId = reference("department_id", DepartmentsTable)
    override val primaryKey = PrimaryKey(userId, departmentId)
}

object SpacePostsTable : IntIdTable("space_posts") {
    val departmentId = reference("department_id", DepartmentsTable)
    val authorId = reference("author_id", UsersTable)
    val content = text("content")
    val mediaUrl = varchar("media_url", 500).nullable() // Für Bilder/Links im Post
    val timestamp = datetime("timestamp").default(LocalDateTime.now())
}

object SpacePostCommentsTable : IntIdTable("space_post_comments") {
    val postId = reference("post_id", SpacePostsTable)
    val authorId = reference("author_id", UsersTable)
    val content = text("content")
    val timestamp = datetime("timestamp").default(LocalDateTime.now())
}

object QuestionsTable : IntIdTable("questions") {
    val departmentId = reference("department_id", DepartmentsTable)
    val authorId = reference("author_id", UsersTable) // Wer hat den Eintrag erstellt?

    val questionTitle = varchar("question_title", 255) // Die eigentliche Frage
    val answerText = text("answer_text") // Die detaillierte Anleitung/Antwort

    val timestamp = datetime("timestamp").default(LocalDateTime.now())
}