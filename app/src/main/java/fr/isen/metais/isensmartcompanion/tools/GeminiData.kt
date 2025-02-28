package fr.isen.metais.isensmartcompanion.tools

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

class GeminiViewModel : ViewModel() {
    private val _responseText = MutableStateFlow<String>("")
    val responseText: StateFlow<String> get() = _responseText

    private val apiKey = "YOUR API KEY" // Remplace par ta clé API

    fun sendMessage(message: String) {
        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(text = message))
                )
            )
        )

        RetrofitInstance.api.generateContent(apiKey, request).enqueue(object : Callback<GeminiResponse> {
            override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                if (response.isSuccessful) {
                    val geminiResponse = response.body()
                    val text = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Pas de réponse"
                    _responseText.value = text
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
        _responseText.value = "" // Réinitialise la réponse après ajout au chat
    }
}