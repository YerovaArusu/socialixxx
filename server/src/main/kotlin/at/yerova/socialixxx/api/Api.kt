package at.yerova.socialixxx.api

import at.yerova.socialixxx.database.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.SizedCollection
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.format.DateTimeFormatter

fun Application.configureApi() {
    routing {
        route("/api") {

            post("/login") {
                val req = call.receive<LoginRequest>()
                val userDto = transaction {
                    val cred = Credential.find { CredentialsTable.username eq req.username }.firstOrNull()
                    if (cred != null && cred.passwordHash == req.passwordHash) {
                        val user = cred.user
                        UserDto(user.id.value, user.displayName, user.department)
                    } else null
                }

                if (userDto != null) call.respond(HttpStatusCode.OK, userDto)
                else call.respond(HttpStatusCode.Unauthorized, "Wrong Username or Password")
            }

            post("/register") {
                val req = call.receive<RegisterRequest>()
                val isSuccess = transaction {
                    val existing = Credential.find { CredentialsTable.username eq req.username }.empty().not()
                    if (existing) return@transaction false

                    val newUser = User.new {
                        this.displayName = req.displayName
                        this.department = req.department
                    }
                    Credential.new {
                        this.username = req.username
                        this.passwordHash = req.passwordHash
                        this.user = newUser
                    }
                    true
                }

                if (isSuccess) call.respond(HttpStatusCode.Created, "Account created successfully")
                else call.respond(HttpStatusCode.Conflict, "Username already exists")
            }

            get("/users") {
                val users = transaction {
                    User.all().map { UserDto(it.id.value, it.displayName, it.department) }
                }
                call.respond(HttpStatusCode.OK, users)
            }


            route("/chats") {
                get("/{userId}") {
                    val userId =
                        call.parameters["userId"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)

                    val myChats = transaction {
                        DirectChat.find {
                            ((DirectChatsTable.user1Id eq userId) or (DirectChatsTable.user2Id eq userId)) and
                                    (DirectChatsTable.status neq 4)
                        }.map { chat ->
                            val partner = if (chat.user1.id.value == userId) chat.user2 else chat.user1
                            val unread = chat.messages.count { it.sender.id.value != userId && !it.isRead }
                            val lastMsg = chat.messages.maxByOrNull { it.timestamp }?.content

                            ChatDto(chat.id.value, partner.id.value, partner.displayName, chat.status, unread, lastMsg)
                        }
                    }
                    call.respond(HttpStatusCode.OK, myChats)
                }

                post("/create") {
                    val req = call.receive<CreateChatRequest>()
                    val u1Id = if (req.myUserId < req.partnerId) req.myUserId else req.partnerId
                    val u2Id = if (req.myUserId < req.partnerId) req.partnerId else req.myUserId

                    val chatDto = transaction {
                        val existingChat = DirectChat.find {
                            (DirectChatsTable.user1Id eq u1Id) and (DirectChatsTable.user2Id eq u2Id)
                        }.firstOrNull()

                        if (existingChat != null) {
                            val partner =
                                if (existingChat.user1.id.value == req.myUserId) existingChat.user2 else existingChat.user1
                            return@transaction ChatDto(
                                existingChat.id.value,
                                partner.id.value,
                                partner.displayName,
                                existingChat.status
                            )
                        }

                        val user1Entity = User.findById(u1Id) ?: throw Exception("User 1 missing")
                        val user2Entity = User.findById(u2Id) ?: throw Exception("User 2 missing")

                        val newChat = DirectChat.new {
                            this.user1 = user1Entity
                            this.user2 = user2Entity
                            this.status = 1
                        }

                        val partner = if (newChat.user1.id.value == req.myUserId) newChat.user2 else newChat.user1
                        ChatDto(newChat.id.value, partner.id.value, partner.displayName, newChat.status)
                    }
                    call.respond(HttpStatusCode.OK, chatDto)
                }
            }
            route("/events") {

                get {
                    val requesterId = call.request.queryParameters["userId"]?.toIntOrNull()

                    val events = transaction {
                        Event.all().map { event ->
                            EventDto(
                                id = event.id.value,
                                title = event.title,
                                description = event.description,
                                eventTime = event.eventTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                participantCount = event.participants.count().toInt(),
                                isParticipating = requesterId != null && event.participants.any { it.id.value == requesterId }
                            )
                        }.sortedBy { it.eventTime }
                    }
                    call.respond(HttpStatusCode.OK, events)
                }

                post {
                    val req = call.receive<CreateEventRequest>()

                    val newEvent = transaction {
                        val creatorEntity = User.findById(req.creatorId) ?: throw Exception("Creator not found")

                        val event = Event.new {
                            this.title = req.title
                            this.description = req.description
                            this.eventTime = java.time.LocalDateTime.parse(req.eventTime)
                            this.creator = creatorEntity
                        }
                        event.participants = SizedCollection(listOf(creatorEntity))

                        EventDto(
                            id = event.id.value,
                            title = event.title,
                            description = event.description,
                            eventTime = event.eventTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            participantCount = 1,
                            isParticipating = true
                        )
                    }
                    call.respond(HttpStatusCode.Created, newEvent)
                }

                post("/{eventId}/join") {
                    val eventId =
                        call.parameters["eventId"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val req = call.receive<EventActionRequest>()

                    val isSuccess = transaction {
                        val event = Event.findById(eventId) ?: return@transaction false
                        val user = User.findById(req.userId) ?: return@transaction false

                        val currentParticipants = event.participants.toMutableList()
                        if (!currentParticipants.any { it.id.value == req.userId }) {
                            currentParticipants.add(user)
                            event.participants = SizedCollection(currentParticipants)
                        }
                        true
                    }

                    if (isSuccess) call.respond(HttpStatusCode.OK, "Joined event")
                    else call.respond(HttpStatusCode.NotFound, "Event or User not found")
                }
            }

            route("/chat/{chatId}") {

                post("/read") {
                    val chatId =
                        call.parameters["chatId"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val req = call.receive<MarkReadRequest>()

                    transaction {
                        val unreadMessages = Message.find {
                            (MessagesTable.chatId eq chatId) and
                                    (MessagesTable.senderId neq req.readerId) and
                                    (MessagesTable.isRead eq false)
                        }
                        unreadMessages.forEach { it.isRead = true }
                    }
                    call.respond(HttpStatusCode.OK, "Messages marked as read")
                }

                patch("/status") {
                    val chatId =
                        call.parameters["chatId"]?.toIntOrNull() ?: return@patch call.respond(HttpStatusCode.BadRequest)
                    val req = call.receive<UpdateChatStatusRequest>()

                    val isSuccess = transaction {
                        val chat = DirectChat.findById(chatId) ?: return@transaction false
                        if (chat.user1.id.value != req.requesterId && chat.user2.id.value != req.requesterId) return@transaction false

                        chat.status = req.newStatus
                        true
                    }

                    if (isSuccess) call.respond(HttpStatusCode.OK, "Status updated")
                    else call.respond(HttpStatusCode.Forbidden, "Action failed or not allowed")
                }

                route("/messages") {
                    get {
                        val chatId = call.parameters["chatId"]?.toIntOrNull()
                            ?: return@get call.respond(HttpStatusCode.BadRequest)
                        // Lese die userId aus der URL, z.B. /api/chat/1/messages?userId=2
                        val requesterId =
                            call.request.queryParameters["userId"]?.toIntOrNull() ?: return@get call.respond(
                                HttpStatusCode.BadRequest,
                                "Missing userId parameter"
                            )

                        var errorStatus: HttpStatusCode? = null
                        var errorMessage: String? = null
                        var resultMessages: List<MessageDto>? = null

                        transaction {
                            val chat = DirectChat.findById(chatId)
                            if (chat == null) {
                                errorStatus = HttpStatusCode.NotFound
                                errorMessage = "Chat not found"
                                return@transaction
                            }

                            if (chat.user1.id.value != requesterId && chat.user2.id.value != requesterId) {
                                errorStatus = HttpStatusCode.Forbidden
                                errorMessage = "Action Blocked: Not part of conversation."
                                return@transaction
                            }

                            resultMessages = chat.messages.map { msg ->
                                MessageDto(
                                    id = msg.id.value,
                                    senderId = msg.sender.id.value,
                                    content = msg.content,
                                    timestamp = msg.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                    isRead = msg.isRead
                                )
                            }
                        }

                        if (resultMessages != null) {
                            call.respond(HttpStatusCode.OK, resultMessages)
                        } else {
                            call.respond(errorStatus ?: HttpStatusCode.InternalServerError, errorMessage ?: "Error")
                        }
                    }

                    // Neue Nachricht senden (mit Sicherheitsprüfung)
                    post {
                        val chatId = call.parameters["chatId"]?.toIntOrNull()
                            ?: return@post call.respond(HttpStatusCode.BadRequest)
                        val req = call.receive<SendMessageRequest>()

                        var errorStatus: HttpStatusCode? = null
                        var errorMessage: String? = null
                        var responseDto: MessageDto? = null

                        transaction {
                            val chatEntity = DirectChat.findById(chatId)
                            if (chatEntity == null) {
                                errorStatus = HttpStatusCode.NotFound
                                errorMessage = "Chat not found"
                                return@transaction
                            }

                            val senderEntity = User.findById(req.senderId)
                            if (senderEntity == null) {
                                errorStatus = HttpStatusCode.NotFound
                                errorMessage = "Sender not found"
                                return@transaction
                            }

                            if (chatEntity.user1.id.value != req.senderId && chatEntity.user2.id.value != req.senderId) {
                                errorStatus = HttpStatusCode.Forbidden
                                errorMessage = "Action Blocked: Not part of conversation."
                                return@transaction
                            }

                            val msg = Message.new {
                                this.chat = chatEntity
                                this.sender = senderEntity
                                this.content = req.content
                                this.isRead = false
                            }

                            responseDto = MessageDto(
                                id = msg.id.value,
                                senderId = msg.sender.id.value,
                                content = msg.content,
                                timestamp = msg.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                isRead = msg.isRead
                            )
                        }

                        if (responseDto != null) {
                            call.respond(HttpStatusCode.Created, responseDto)
                        } else {
                            call.respond(
                                errorStatus ?: HttpStatusCode.InternalServerError,
                                errorMessage ?: "An Error Occurred!"
                            )
                        }
                    }
                }
            }
        }
    }
}