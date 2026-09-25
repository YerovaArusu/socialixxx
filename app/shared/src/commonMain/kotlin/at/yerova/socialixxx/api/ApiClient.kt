package at.yerova.socialixxx.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

class ApiClient(
    private val client: HttpClient = socialixxxHttpClient,
    val baseUrl: String = "https://yerova.net/api"
) {

    suspend fun pingServer(): Boolean {
        return try {
            val response = client.get("$baseUrl/ping")
            response.status.isSuccess()
        } catch (e: Exception) {
            if (e is CancellationException) {
                currentCoroutineContext().ensureActive()
            }
            false
        }
    }

    private suspend inline fun <reified T> apiCall(
        apiCall: () -> HttpResponse
    ): NetworkResult<T> {
        return try {
            val response = apiCall()
            if (response.status.isSuccess()) {
                NetworkResult.Success(response.body())
            } else {
                NetworkResult.Error("API-Error: ${response.status.description}", response.status.value)
            }
        } catch (e: Exception) {
            if (e is CancellationException) {
                currentCoroutineContext().ensureActive()
            }
            NetworkResult.Error("NetworkError: ${e.message}")
        }
    }

    suspend fun login(request: LoginRequest): NetworkResult<UserDto> = apiCall {
        client.post("$baseUrl/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun register(request: RegisterRequest): NetworkResult<String> = apiCall {
        client.post("$baseUrl/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun getUsers(departmentId: Int? = null): NetworkResult<List<UserDto>> = apiCall {
        client.get("$baseUrl/users") {
            if (departmentId != null) {
                parameter("departmentId", departmentId)
            }
        }
    }

    suspend fun getUser(userId: Int): NetworkResult<UserDto> = apiCall {
        client.get("$baseUrl/users/$userId")
    }

    suspend fun getChats(userId: Int): NetworkResult<List<ChatDto>> = apiCall {
        client.get("$baseUrl/chats/$userId")
    }

    suspend fun getChat(chatId: Int, userId: Int): NetworkResult<ChatDto> = apiCall {
        client.get("$baseUrl/chat/$chatId") {
            parameter("userId", userId)
        }
    }

    suspend fun createChat(request: CreateChatRequest): NetworkResult<ChatDto> = apiCall {
        client.post("$baseUrl/chats/create") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun getMessages(chatId: Int, requesterId: Int): NetworkResult<List<MessageDto>> = apiCall {
        client.get("$baseUrl/chat/$chatId/messages") {
            parameter("userId", requesterId)
        }
    }

    suspend fun sendMessage(chatId: Int, request: SendMessageRequest): NetworkResult<MessageDto> = apiCall {
        client.post("$baseUrl/chat/$chatId/messages") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun markMessagesAsRead(chatId: Int, request: MarkReadRequest): NetworkResult<String> = apiCall {
        client.post("$baseUrl/chat/$chatId/read") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun updateChatStatus(chatId: Int, request: UpdateChatStatusRequest): NetworkResult<String> = apiCall {
        client.patch("$baseUrl/chat/$chatId/status") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun getEvents(userId: Int): NetworkResult<List<EventDto>> = apiCall {
        client.get("$baseUrl/events") {
            parameter("userId", userId)
        }
    }

    suspend fun getEvent(eventId: Int, userId: Int): NetworkResult<EventDto> = apiCall {
        client.get("$baseUrl/events/$eventId") {
            parameter("userId", userId)
        }
    }

    suspend fun createEvent(request: CreateEventRequest): NetworkResult<EventDto> = apiCall {
        client.post("$baseUrl/events") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun joinEvent(eventId: Int, request: EventActionRequest): NetworkResult<String> = apiCall {
        client.post("$baseUrl/events/$eventId/join") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun getUserStories(userId: Int): NetworkResult<List<StoryDto>> = apiCall {
        client.get("$baseUrl/users/$userId/stories")
    }

    suspend fun createStory(request: CreateStoryRequest): NetworkResult<StoryDto> = apiCall {
        client.post("$baseUrl/users/${request.userId}/stories") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun getEventComments(eventId: Int): NetworkResult<List<EventCommentDto>> = apiCall {
        client.get("$baseUrl/events/$eventId/comments")
    }

    suspend fun postEventComment(request: CreateEventCommentRequest): NetworkResult<EventCommentDto> = apiCall {
        client.post("$baseUrl/events/${request.eventId}/comments") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun getSpaces(userId: Int): NetworkResult<List<SpaceDto>> = apiCall {
        client.get("$baseUrl/spaces") {
            parameter("userId", userId)
        }
    }

    suspend fun getSpacePosts(spaceId: Int): NetworkResult<List<SpacePostDto>> = apiCall {
        client.get("$baseUrl/spaces/$spaceId/posts")
    }

    suspend fun createSpacePost(spaceId: Int, request: CreateSpacePostRequest): NetworkResult<SpacePostDto> = apiCall {
        client.post("$baseUrl/spaces/$spaceId/posts") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun getSpacePostComments(postId: Int): NetworkResult<List<SpacePostCommentDto>> = apiCall {
        client.get("$baseUrl/spaces/posts/$postId/comments")
    }

    suspend fun createSpacePostComment(
        postId: Int, request: CreateSpacePostCommentRequest
    ): NetworkResult<SpacePostCommentDto> = apiCall {
        client.post("$baseUrl/spaces/posts/$postId/comments") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun getSpaceQuestions(spaceId: Int): NetworkResult<List<QuestionDto>> = apiCall {
        client.get("$baseUrl/spaces/$spaceId/questions")
    }

    suspend fun createSpaceQuestion(spaceId: Int, request: CreateQuestionRequest): NetworkResult<QuestionDto> =

        apiCall {
            client.post("$baseUrl/spaces/$spaceId/questions") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    suspend fun getSpaceIdeas(departmentId: Int): NetworkResult<List<IdeaDto>> = apiCall {
        client.get("$baseUrl/spaces/$departmentId/ideas")
    }

    suspend fun createSpaceIdea(departmentId: Int, request: CreateIdeaRequest): NetworkResult<IdeaDto> = apiCall {
        client.post("$baseUrl/spaces/$departmentId/ideas") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun getDepartments(): NetworkResult<List<DepartmentBaseDto>> = apiCall {
        client.get("$baseUrl/departments")
    }
}