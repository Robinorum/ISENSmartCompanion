package fr.isen.metais.isensmartcompanion.tools

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

class GeminiViewModel(private val context: Context) : ViewModel() {
    private val _responseText = MutableStateFlow<String>("")
    val responseText: StateFlow<String> get() = _responseText

    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val db = AppDatabase.getDatabase(context)
    private val chatDao = db.chatMessageDao()

    fun sendMessage(message: String) {
        if (apiKey.isEmpty()) {
            _responseText.value = "Erreur : Clé API manquante"
            return
        }

        viewModelScope.launch {
            val userMessage = ChatMessage(text = message, isFromUser = true)
            chatDao.insert(userMessage)
            Log.d("GeminiViewModel", "Message utilisateur sauvegardé : ${userMessage.text}, ID : ${userMessage.id}")
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
                        val aiMessage = ChatMessage(text = text, isFromUser = false)
                        chatDao.insert(aiMessage)
                        Log.d("GeminiViewModel", "Réponse IA sauvegardée : ${aiMessage.text}, ID : ${aiMessage.id}")
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

    fun clearConversation() {
        viewModelScope.launch {
            chatDao.deleteAll()
            Log.d("GeminiViewModel", "Conversation effacée")
        }
    }

    fun getChatMessages(): Flow<List<ChatMessage>> { // Retour à Flow
        return chatDao.getAllMessages()
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
}