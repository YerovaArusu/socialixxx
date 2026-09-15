package at.yerova.socialixxx.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*

class ApiClient(
    private val client: HttpClient = socialixxxHttpClient,
    private val baseUrl: String = "http://127.0.0.1:8081/api" // 10.0.2.2 ist localhost für den Android Emulator. Für Web: http://localhost:8080/api
) {
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

    suspend fun getUsers(): NetworkResult<List<UserDto>> = apiCall {
        client.get("$baseUrl/users")
    }

    suspend fun getChats(userId: Int): NetworkResult<List<ChatDto>> = apiCall {
        client.get("$baseUrl/chats/$userId")
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
}