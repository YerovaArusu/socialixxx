package at.yerova.socialixxx.database

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass


class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(UsersTable)

    var displayName by UsersTable.displayName
    var lu by UsersTable.lu
    var zk by UsersTable.zk
    var department by UsersTable.department
    var dw by UsersTable.dw
    var kz by UsersTable.kz
    var entryDate by UsersTable.entryDate

    var birthday by UsersTable.birthday
    var gender by UsersTable.gender
    var pronouns by UsersTable.pronouns
    var profilePictureUrl by UsersTable.profilePictureUrl
    val credentials by Credential referrersOn CredentialsTable.userId
}

class Credential(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Credential>(CredentialsTable)

    var username by CredentialsTable.username
    var passwordHash by CredentialsTable.passwordHash

    var user by User referencedOn CredentialsTable.userId
}

class DirectChat(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<DirectChat>(DirectChatsTable)

    var user1 by User referencedOn DirectChatsTable.user1Id
    var user2 by User referencedOn DirectChatsTable.user2Id
    var status by DirectChatsTable.status

    val messages by Message referrersOn MessagesTable.chatId
}

class Message(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Message>(MessagesTable)

    var chat by DirectChat referencedOn MessagesTable.chatId

    var sender by User referencedOn MessagesTable.senderId

    var content by MessagesTable.content
    var timestamp by MessagesTable.timestamp
    var isRead by MessagesTable.isRead
}

class Event(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Event>(EventsTable)

    var title by EventsTable.title
    var description by EventsTable.description
    var eventTime by EventsTable.eventTime
    var creator by User referencedOn EventsTable.creatorId

    var participants by User via EventParticipantsTable
}