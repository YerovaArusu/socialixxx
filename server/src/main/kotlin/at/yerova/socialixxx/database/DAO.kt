package at.yerova.socialixxx.database

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass


class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(UsersTable)

    var displayName by UsersTable.displayName
    var lu by UsersTable.lu
    var zk by UsersTable.zk
    var dw by UsersTable.dw
    var kz by UsersTable.kz
    var entryDate by UsersTable.entryDate

    var birthday by UsersTable.birthday
    var gender by UsersTable.gender
    var pronouns by UsersTable.pronouns
    var profilePictureUrl by UsersTable.profilePictureUrl
    var departments by Department via UserDepartmentsTable

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

class EventComment(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<EventComment>(EventCommentsTable)

    var event by Event referencedOn EventCommentsTable.eventId
    var user by User referencedOn EventCommentsTable.userId
    var content by EventCommentsTable.content
    var timestamp by EventCommentsTable.timestamp
}

class Story(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Story>(StoriesTable)

    var user by User referencedOn StoriesTable.userId
    var mediaUrl by StoriesTable.mediaUrl
    var caption by StoriesTable.caption
    var timestamp by StoriesTable.timestamp
    var isActive by StoriesTable.isActive
}

class Department(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Department>(DepartmentsTable)

    var name by DepartmentsTable.name
    var kz by DepartmentsTable.kz
    var description by DepartmentsTable.description

    var members by User via UserDepartmentsTable

    val posts by SpacePost referrersOn SpacePostsTable.departmentId
    val questions by Question referrersOn QuestionsTable.departmentId
}

class SpacePost(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SpacePost>(SpacePostsTable)

    var department by Department referencedOn SpacePostsTable.departmentId
    var author by User referencedOn SpacePostsTable.authorId
    var content by SpacePostsTable.content
    var mediaUrl by SpacePostsTable.mediaUrl
    var timestamp by SpacePostsTable.timestamp

    val comments by SpacePostComment referrersOn SpacePostCommentsTable.postId
}

class SpacePostComment(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SpacePostComment>(SpacePostCommentsTable)

    var post by SpacePost referencedOn SpacePostCommentsTable.postId
    var author by User referencedOn SpacePostCommentsTable.authorId
    var content by SpacePostCommentsTable.content
    var timestamp by SpacePostCommentsTable.timestamp
}

class Question(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Question>(QuestionsTable)

    var department by Department referencedOn QuestionsTable.departmentId
    var author by User referencedOn QuestionsTable.authorId
    var questionTitle by QuestionsTable.questionTitle
    var answerText by QuestionsTable.answerText
    var timestamp by QuestionsTable.timestamp
}