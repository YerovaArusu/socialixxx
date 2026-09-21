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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun Application.configureApi() {
    routing {
        route("/api") {

            post("/login") {
                val req = call.receive<LoginRequest>()
                val userDto = transaction {
                    val cred = Credential.find { CredentialsTable.username eq req.username }.firstOrNull()
                    if (cred != null && cred.passwordHash == req.passwordHash) {
                        val user = cred.user
                        val now = LocalDateTime.now()

                        val monthsBetween = ChronoUnit.MONTHS.between(user.entryDate, now)
                        val calculatedLehrjahr = (monthsBetween / 12).toInt() + 1

                        val yesterday = now.minusHours(24)
                        val hasActive = Story.find {
                            (StoriesTable.userId eq user.id) and (StoriesTable.isActive eq true)
                        }.any { it.timestamp.isAfter(yesterday) }

                        UserDto(
                            id = user.id.value,
                            displayName = user.displayName,
                            birthday = user.birthday,
                            gender = user.gender,
                            pronouns = user.pronouns,
                            profilePictureUrl = user.profilePictureUrl,
                            dw = user.dw,
                            kz = user.kz,
                            lehrjahr = calculatedLehrjahr,
                            hasActiveStory = hasActive
                        )
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
                        this.profilePictureUrl = "https://ui-avatars.com/api/?name=${req.displayName}&background=0D8ABC&color=fff&size=128"
                    }
                    Credential.new {
                        this.username = req.username
                        this.passwordHash = req.passwordHash
                        this.user = newUser
                    }

                    if (req.departmentId != null) {
                        val dept = Department.findById(req.departmentId!!)
                        if (dept != null) {
                            // Die n:m Beziehung über Exposed "via" befüllen
                            newUser.departments = org.jetbrains.exposed.v1.jdbc.SizedCollection(listOf(dept))
                        }
                    }
                    true
                }

                if (isSuccess) call.respond(HttpStatusCode.Created, "Account created successfully")
                else call.respond(HttpStatusCode.Conflict, "Username already exists")
            }

            get("/users") {
                val users = transaction {
                    val now = LocalDateTime.now()
                    val yesterday = now.minusHours(24)

                    User.all().map { user ->
                        val monthsBetween = ChronoUnit.MONTHS.between(user.entryDate, now)
                        val calculatedLehrjahr = (monthsBetween / 12).toInt() + 1

                        val hasActive = Story.find {
                            (StoriesTable.userId eq user.id) and (StoriesTable.isActive eq true)
                        }.any { it.timestamp.isAfter(yesterday) }

                        UserDto(
                            id = user.id.value,
                            displayName = user.displayName,
                            birthday = user.birthday,
                            gender = user.gender,
                            pronouns = user.pronouns,
                            profilePictureUrl = user.profilePictureUrl,
                            dw = user.dw,
                            kz = user.kz,
                            lehrjahr = calculatedLehrjahr,
                            hasActiveStory = hasActive
                        )
                    }
                }
                call.respond(HttpStatusCode.OK, users)
            }
            get("/users/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)

                val user = transaction {
                    User.findById(id)?.let { userEntity ->
                        val now = LocalDateTime.now()

                        val monthsBetween = ChronoUnit.MONTHS.between(userEntity.entryDate, now)
                        val calculatedLehrjahr = (monthsBetween / 12).toInt() + 1

                        val yesterday = now.minusHours(24)
                        val hasActive = Story.find {
                            (StoriesTable.userId eq userEntity.id) and (StoriesTable.isActive eq true)
                        }.any { it.timestamp.isAfter(yesterday) }

                        UserDto(
                            id = userEntity.id.value,
                            displayName = userEntity.displayName,
                            birthday = userEntity.birthday,
                            gender = userEntity.gender,
                            pronouns = userEntity.pronouns,
                            profilePictureUrl = userEntity.profilePictureUrl,
                            dw = userEntity.dw,
                            kz = userEntity.kz,
                            lehrjahr = calculatedLehrjahr,
                            hasActiveStory = hasActive
                        )
                    }
                }

                if (user != null) call.respond(HttpStatusCode.OK, user)
                else call.respond(HttpStatusCode.NotFound, "User not found")
            }

            route("/users/{id}/stories") {
                get {
                    val userId =
                        call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)

                    val activeStories = transaction {
                        val yesterday = LocalDateTime.now().minusHours(24)

                        Story.find {
                            (StoriesTable.userId eq userId) and
                                    (StoriesTable.isActive eq true)
                        }.filter {
                            it.timestamp.isAfter(yesterday)
                        }.map { story ->
                            StoryDto(
                                id = story.id.value,
                                userId = story.user.id.value,
                                mediaUrl = story.mediaUrl,
                                caption = story.caption,
                                timestamp = story.timestamp.toString()
                            )
                        }.sortedByDescending { it.timestamp }
                    }
                    call.respond(HttpStatusCode.OK, activeStories)
                }

                post {
                    val req = call.receive<CreateStoryRequest>()
                    val newStory = transaction {
                        val userEntity = User.findById(req.userId) ?: throw Exception("User not found")
                        val story = Story.new {
                            this.user = userEntity
                            this.mediaUrl = req.mediaUrl
                            this.caption = req.caption
                            this.isActive = true
                        }
                        StoryDto(
                            story.id.value,
                            story.user.id.value,
                            story.mediaUrl,
                            story.caption,
                            story.timestamp.toString()
                        )
                    }
                    call.respond(HttpStatusCode.Created, newStory)
                }
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
                            val commentsCount =
                                EventComment.find { EventCommentsTable.eventId eq event.id }.count().toInt()

                            EventDto(
                                id = event.id.value,
                                title = event.title,
                                description = event.description,
                                eventTime = event.eventTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                participantCount = event.participants.count().toInt(),
                                isParticipating = requesterId != null && event.participants.any { it.id.value == requesterId },
                                commentCount = commentsCount
                            )
                        }.sortedBy { it.eventTime }
                    }
                    call.respond(HttpStatusCode.OK, events)
                }

                get("/{id}") {
                    val eventId =
                        call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val requesterId = call.request.queryParameters["userId"]?.toIntOrNull()

                    val eventDto = transaction {
                        Event.findById(eventId)?.let { event ->
                            val commentsCount =
                                EventComment.find { EventCommentsTable.eventId eq event.id }.count().toInt()

                            EventDto(
                                id = event.id.value,
                                title = event.title,
                                description = event.description,
                                eventTime = event.eventTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                participantCount = event.participants.count().toInt(),
                                isParticipating = requesterId != null && event.participants.any { it.id.value == requesterId },
                                commentCount = commentsCount
                            )
                        }
                    }

                    if (eventDto != null) call.respond(HttpStatusCode.OK, eventDto)
                    else call.respond(HttpStatusCode.NotFound, "Event not found")
                }

                route("/{eventId}/comments") {
                    get {
                        val eventId = call.parameters["eventId"]?.toIntOrNull() ?: return@get call.respond(
                            HttpStatusCode.BadRequest
                        )
                        val comments = transaction {
                            EventComment.find { EventCommentsTable.eventId eq eventId }
                                .sortedBy { it.timestamp }
                                .map {
                                    EventCommentDto(
                                        id = it.id.value,
                                        userId = it.user.id.value,
                                        userName = it.user.displayName,
                                        userProfilePic = it.user.profilePictureUrl,
                                        content = it.content,
                                        timestamp = it.timestamp.toString()
                                    )
                                }
                        }
                        call.respond(HttpStatusCode.OK, comments)
                    }

                    post {
                        val eventId = call.parameters["eventId"]?.toIntOrNull() ?: return@post call.respond(
                            HttpStatusCode.BadRequest
                        )
                        val req = call.receive<CreateEventCommentRequest>()

                        val newComment = transaction {
                            val eventEntity = Event.findById(eventId) ?: throw Exception("Event not found")
                            val userEntity = User.findById(req.userId) ?: throw Exception("User not found")

                            val comment = EventComment.new {
                                this.event = eventEntity
                                this.user = userEntity
                                this.content = req.content
                            }
                            EventCommentDto(
                                id = comment.id.value,
                                userId = comment.user.id.value,
                                userName = comment.user.displayName,
                                userProfilePic = comment.user.profilePictureUrl,
                                content = comment.content,
                                timestamp = comment.timestamp.toString()
                            )
                        }
                        call.respond(HttpStatusCode.Created, newComment)
                    }
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
                            isParticipating = true,
                            commentCount = 0
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

                get {
                    val chatId =
                        call.parameters["chatId"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val requesterId = call.request.queryParameters["userId"]?.toIntOrNull() ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        "Missing userId"
                    )

                    val chatDto = transaction {
                        DirectChat.findById(chatId)?.let { chat ->
                            if (chat.user1.id.value != requesterId && chat.user2.id.value != requesterId) return@let null

                            val partner = if (chat.user1.id.value == requesterId) chat.user2 else chat.user1
                            val unread = chat.messages.count { it.sender.id.value != requesterId && !it.isRead }
                            val lastMsg = chat.messages.maxByOrNull { it.timestamp }?.content

                            ChatDto(chat.id.value, partner.id.value, partner.displayName, chat.status, unread, lastMsg)
                        }
                    }

                    if (chatDto != null) call.respond(HttpStatusCode.OK, chatDto)
                    else call.respond(HttpStatusCode.NotFound, "Chat not found or access denied")
                }

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
            route("/spaces") {

                // Gibt alle Abteilungen als Kacheln zurück und berechnet die individuellen Schreibrechte
                get {
                    val userId = call.request.queryParameters["userId"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing userId")

                    val spaces = transaction {
                        val user = User.findById(userId)
                        val userDepartmentIds = user?.departments?.map { it.id.value }?.toSet() ?: emptySet()

                        Department.all().map { dept ->
                            SpaceDto(
                                id = dept.id.value,
                                name = dept.name,
                                kz = dept.kz,
                                description = dept.description,
                                // Prüft via n:m-Beziehung, ob der Nutzer dieser Abteilung zugewiesen ist
                                isAssigned = userDepartmentIds.contains(dept.id.value)
                            )
                        }.sortedByDescending { it.isAssigned } // Zuweisungen wandern im UI automatisch nach oben
                    }
                    call.respond(HttpStatusCode.OK, spaces)
                }

                route("/{spaceId}/posts") {

                    // Lädt den Feed ("Erfahrungsberichte, Tips & Tricks") einer spezifischen Abteilung
                    get {
                        val spaceId = call.parameters["spaceId"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)

                        val posts = transaction {
                            SpacePost.find { SpacePostsTable.departmentId eq spaceId }
                                .sortedByDescending { it.timestamp }
                                .map { post ->
                                    SpacePostDto(
                                        id = post.id.value,
                                        departmentId = post.department.id.value,
                                        authorId = post.author.id.value,
                                        authorName = post.author.displayName,
                                        authorProfilePic = post.author.profilePictureUrl,
                                        content = post.content,
                                        mediaUrl = post.mediaUrl,
                                        timestamp = post.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                        commentCount = post.comments.count().toInt()
                                    )
                                }
                        }
                        call.respond(HttpStatusCode.OK, posts)
                    }

                    // Erstellt einen neuen Post, zwingt aber eine Autorisierungsprüfung auf (RBAC)
                    post {
                        val spaceId = call.parameters["spaceId"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
                        val req = call.receive<CreateSpacePostRequest>()

                        var errorStatus: HttpStatusCode? = null
                        var errorMessage: String? = null
                        var responseDto: SpacePostDto? = null

                        transaction {
                            val departmentEntity = Department.findById(spaceId)
                            if (departmentEntity == null) {
                                errorStatus = HttpStatusCode.NotFound
                                errorMessage = "Abteilung nicht gefunden"
                                return@transaction
                            }

                            val authorEntity = User.findById(req.authorId)
                            if (authorEntity == null) {
                                errorStatus = HttpStatusCode.NotFound
                                errorMessage = "Nutzer nicht gefunden"
                                return@transaction
                            }

                            // SECURITY CHECK: Ein Posting ist ausnahmslos nur im eigenen Space erlaubt
                            val isAssigned = authorEntity.departments.any { it.id.value == spaceId }
                            if (!isAssigned) {
                                errorStatus = HttpStatusCode.Forbidden
                                errorMessage = "Keine Berechtigung: Posting nur im eigenen Space erlaubt."
                                return@transaction
                            }

                            val newPost = SpacePost.new {
                                this.department = departmentEntity
                                this.author = authorEntity
                                this.content = req.content
                                this.mediaUrl = req.mediaUrl
                                this.timestamp = LocalDateTime.now()
                            }

                            responseDto = SpacePostDto(
                                id = newPost.id.value,
                                departmentId = newPost.department.id.value,
                                authorId = newPost.author.id.value,
                                authorName = newPost.author.displayName,
                                authorProfilePic = newPost.author.profilePictureUrl,
                                content = newPost.content,
                                mediaUrl = newPost.mediaUrl,
                                timestamp = newPost.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                commentCount = 0
                            )
                        }

                        if (responseDto != null) {
                            call.respond(HttpStatusCode.Created, responseDto!!)
                        } else {
                            call.respond(errorStatus ?: HttpStatusCode.InternalServerError, errorMessage ?: "Fehler beim Erstellen des Posts")
                        }
                    }
                }
                route("/posts/{postId}/comments") {

                    get {
                        val postId = call.parameters["postId"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)

                        val comments = transaction {
                            SpacePostComment.find { SpacePostCommentsTable.postId eq postId }
                                .sortedBy { it.timestamp }
                                .map { comment ->
                                    SpacePostCommentDto(
                                        id = comment.id.value,
                                        postId = comment.post.id.value,
                                        authorId = comment.author.id.value,
                                        authorName = comment.author.displayName,
                                        authorProfilePic = comment.author.profilePictureUrl,
                                        content = comment.content,
                                        timestamp = comment.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                    )
                                }
                        }
                        call.respond(HttpStatusCode.OK, comments)
                    }

                    post {
                        val postId = call.parameters["postId"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
                        val req = call.receive<CreateSpacePostCommentRequest>()

                        var errorStatus: HttpStatusCode? = null
                        var errorMessage: String? = null
                        var responseDto: SpacePostCommentDto? = null

                        transaction {
                            val postEntity = SpacePost.findById(postId)
                            if (postEntity == null) {
                                errorStatus = HttpStatusCode.NotFound
                                errorMessage = "Post nicht gefunden"
                                return@transaction
                            }

                            val authorEntity = User.findById(req.authorId)
                            if (authorEntity == null) {
                                errorStatus = HttpStatusCode.NotFound
                                errorMessage = "Nutzer nicht gefunden"
                                return@transaction
                            }

                            // SECURITY CHECK: Auch kommentieren darf man nur in eigenen zugewiesenen Spaces
                            val departmentId = postEntity.department.id.value
                            val isAssigned = authorEntity.departments.any { it.id.value == departmentId }
                            if (!isAssigned) {
                                errorStatus = HttpStatusCode.Forbidden
                                errorMessage = "Keine Berechtigung zum Kommentieren in abteilungsfremden Spaces."
                                return@transaction
                            }

                            val newComment = SpacePostComment.new {
                                this.post = postEntity
                                this.author = authorEntity
                                this.content = req.content
                                this.timestamp = LocalDateTime.now()
                            }

                            responseDto = SpacePostCommentDto(
                                id = newComment.id.value,
                                postId = newComment.post.id.value,
                                authorId = newComment.author.id.value,
                                authorName = newComment.author.displayName,
                                authorProfilePic = newComment.author.profilePictureUrl,
                                content = newComment.content,
                                timestamp = newComment.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            )
                        }

                        if (responseDto != null) {
                            call.respond(HttpStatusCode.Created, responseDto!!)
                        } else {
                            call.respond(errorStatus ?: HttpStatusCode.InternalServerError, errorMessage ?: "Fehler")
                        }
                    }
                }

                route("/{spaceId}/questions") {

                    get {
                        val spaceId = call.parameters["spaceId"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)

                        val questions = transaction {
                            Question.find { QuestionsTable.departmentId eq spaceId }
                                .sortedByDescending { it.timestamp }
                                .map { q ->
                                    QuestionDto(
                                        id = q.id.value,
                                        departmentId = q.department.id.value,
                                        authorId = q.author.id.value,
                                        authorName = q.author.displayName,
                                        questionTitle = q.questionTitle,
                                        answerText = q.answerText,
                                        timestamp = q.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                    )
                                }
                        }
                        call.respond(HttpStatusCode.OK, questions)
                    }

                    post {
                        val spaceId = call.parameters["spaceId"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
                        val req = call.receive<CreateQuestionRequest>()

                        var errorStatus: HttpStatusCode? = null
                        var errorMessage: String? = null
                        var responseDto: QuestionDto? = null

                        transaction {
                            val departmentEntity = Department.findById(spaceId)
                            if (departmentEntity == null) {
                                errorStatus = HttpStatusCode.NotFound
                                errorMessage = "Abteilung nicht gefunden"
                                return@transaction
                            }

                            val authorEntity = User.findById(req.authorId)
                            if (authorEntity == null) {
                                errorStatus = HttpStatusCode.NotFound
                                errorMessage = "Nutzer nicht gefunden"
                                return@transaction
                            }

                            val isAssigned = authorEntity.departments.any { it.id.value == spaceId }
                            if (!isAssigned) {
                                errorStatus = HttpStatusCode.Forbidden
                                errorMessage = "Keine Berechtigung: Einträge in den Fragenkatalog erfordern Space-Zuweisung."
                                return@transaction
                            }

                            val newQuestion = Question.new {
                                this.department = departmentEntity
                                this.author = authorEntity
                                this.questionTitle = req.questionTitle
                                this.answerText = req.answerText
                                this.timestamp = LocalDateTime.now()
                            }

                            responseDto = QuestionDto(
                                id = newQuestion.id.value,
                                departmentId = newQuestion.department.id.value,
                                authorId = newQuestion.author.id.value,
                                authorName = newQuestion.author.displayName,
                                questionTitle = newQuestion.questionTitle,
                                answerText = newQuestion.answerText,
                                timestamp = newQuestion.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            )
                        }

                        if (responseDto != null) {
                            call.respond(HttpStatusCode.Created, responseDto!!)
                        } else {
                            call.respond(errorStatus ?: HttpStatusCode.InternalServerError, errorMessage ?: "Fehler")
                        }
                    }
                }
            }
            get("/departments") {
                val deps = transaction {
                    Department.all().map { DepartmentBaseDto(it.id.value, it.name, it.kz) }
                }
                call.respond(HttpStatusCode.OK, deps)
            }
            get("/ping") {
                call.respond(HttpStatusCode.OK, "pong")
            }
        }
    }
}