package fr.isen.metais.isensmartcompanion.events
import fr.isen.metais.isensmartcompanion.screens.Event
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface EventApiService {
    @GET
        ("events.json")
    suspend fun getEvents(): Response<List<Event>>
}object RetrofitInstance {
    private const val BASE_URL = "https://isen-smart-companion-default-rtdb.europe-west1.firebasedatabase.app/"

    val api: EventApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EventApiService::class.java)
    }

}

