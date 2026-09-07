package at.yerova.socialixxx.database

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime

object UsersTable : IntIdTable("users") {
    val displayName = varchar("display_name", 100)
    val lu = varchar("lu", 50).nullable()
    val zk = varchar("zk", 50).nullable()
    val department = varchar("department", 50).nullable()
    val dw = varchar("dw", 50).nullable()
    val kz = varchar("kz", 50).nullable()
    val entryDate = datetime("entry_date").default(LocalDateTime.now())
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