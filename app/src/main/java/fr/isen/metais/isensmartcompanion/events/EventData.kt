package fr.isen.metais.isensmartcompanion.events
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.isen.metais.isensmartcompanion.screens.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class EventViewModel : ViewModel() {
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> get() = _events

    init {
        fetchEvents()
    }

    private fun fetchEvents() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getEvents()
                if (response.isSuccessful) {
                    val jsonEvents = response.body() ?: emptyList()
                    val eventList = jsonEvents.map { Event.fromJsonEvent(it) }
                    Log.d("EventViewModel", "Événements récupérés : $eventList")
                    _events.value = eventList
                } else {
                    Log.e("EventViewModel", "Erreur HTTP : ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("EventViewModel", "Exception : ${e.message}")
                e.printStackTrace()
            }
        }
    }

}

