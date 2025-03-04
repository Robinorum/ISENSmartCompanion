package fr.isen.metais.isensmartcompanion.gemini

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.isen.metais.isensmartcompanion.BuildConfig
import fr.isen.metais.isensmartcompanion.data.AppDatabase
import fr.isen.metais.isensmartcompanion.data.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?
)

class GeminiViewModel(context: Context) : ViewModel() {
    private val _responseText = MutableStateFlow<String>("")
    val responseText: StateFlow<String> get() = _responseText

    private val _currentConversationId = MutableStateFlow(1)
    val currentConversationId: StateFlow<Int> get() = _currentConversationId

    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val db = AppDatabase.getDatabase(context)
    private val chatDao = db.chatMessageDao()

    fun sendMessage(message: String) {
        if (apiKey.isEmpty()) {
            _responseText.value = "Erreur : Clé API manquante"
            return
        }

        viewModelScope.launch {
            val userMessage = ChatMessage(
                text = message,
                isFromUser = true,
                conversationId = _currentConversationId.value
            )
            chatDao.insert(userMessage)
            Log.d("GeminiViewModel", "Message utilisateur sauvegardé : ${userMessage.text}, ConvID : ${_currentConversationId.value}")
        }

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = message))))
        )

        RetrofitInstance.api.generateContent(apiKey, request).enqueue(object : Callback<GeminiResponse> {
            override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                if (response.isSuccessful) {
                    val geminiResponse = response.body()
                    val text = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Pas de réponse"
                    _responseText.value = text
                    viewModelScope.launch {
                        val aiMessage = ChatMessage(
                            text = text,
                            isFromUser = false,
                            conversationId = _currentConversationId.value
                        )
                        chatDao.insert(aiMessage)
                        Log.d("GeminiViewModel", "Réponse IA sauvegardée : ${aiMessage.text}, ConvID : ${_currentConversationId.value}")
                    }
                } else {
                    _responseText.value = "Erreur : ${response.code()} - ${response.message()}"
                }
            }

            override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                _responseText.value = "Erreur réseau : ${t.message}"
            }
        })
    }

    fun clearResponse() {
        _responseText.value = ""
    }

    fun startNewConversation() {
        viewModelScope.launch {
            val existingIds = chatDao.getAllConversationIds().firstOrNull() ?: emptyList()
            val newId = (existingIds.maxOrNull() ?: 0) + 1
            _currentConversationId.value = newId
            Log.d("GeminiViewModel", "Nouvelle conversation démarrée : $newId, Nombre de convs existantes : ${existingIds.size}")
        }
    }

    fun resumeConversation(conversationId: Int) {
        _currentConversationId.value = conversationId
        Log.d("GeminiViewModel", "Conversation reprise immédiatement : $conversationId")
    }

    fun getChatMessages(conversationId: Int): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForConversation(conversationId)
    }

    fun getAllConversationIds(): Flow<List<Int>> {
        return chatDao.getAllConversationIds()
    }

    suspend fun getLastMessageTimestamp(conversationId: Int): Long? {
        return chatDao.getLastMessageTimestamp(conversationId)
    }

    fun deleteMessagePair(questionId: Long, answerId: Long?) {
        viewModelScope.launch {
            chatDao.deleteById(questionId)
            Log.d("GeminiViewModel", "Message supprimé, ID : $questionId")
            answerId?.let {
                chatDao.deleteById(it)
                Log.d("GeminiViewModel", "Réponse supprimée, ID : $it")
            }
        }
    }

    fun deleteConversation(conversationId: Int) {
        viewModelScope.launch {
            chatDao.deleteConversation(conversationId)
            Log.d("GeminiViewModel", "Conversation supprimée : $conversationId")
            if (_currentConversationId.value == conversationId) {
                _currentConversationId.value = chatDao.getAllConversationIds().firstOrNull()?.maxOrNull() ?: 1
            }
        }
    }
}