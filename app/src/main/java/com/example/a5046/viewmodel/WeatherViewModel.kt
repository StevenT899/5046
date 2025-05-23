package com.example.a5046.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherData(
    val temperature: Double = 0.0,
    val humidity: Int = 0,
    val uvIndex: Double = 0.0
)
//sealed class for UI state
sealed interface WeatherState {
    data object Loading : WeatherState
    data class Success(val weatherData: WeatherData) : WeatherState
    data class Error(val message: String) : WeatherState
}
//Retrofit API definition

interface WeatherApi {
    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    @GET("data/2.5/uvi")
    suspend fun getUVIndex(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): UVResponse
}

data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>
)

data class Main(
    val temp: Double,
    val humidity: Int
)

data class Weather(
    val description: String,
    val icon: String
)

data class UVResponse(
    val value: Double
)
//Calls OpenWeatherMap APIs, combines temperature + humidity + UV index
class WeatherViewModel : ViewModel() {
    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weatherState: StateFlow<WeatherState> = _weatherState

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherApi = retrofit.create(WeatherApi::class.java)
    //reference from AI
    /** Fetch current weather + UV index, then update StateFlow */
    fun updateWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                _weatherState.value = WeatherState.Loading
                

                val weatherResponse = weatherApi.getWeather(
                    lat = latitude,
                    lon = longitude,
                    apiKey = "6a43ad4ad589a4b49e92e04b71ea79b6"
                )


                val uvResponse = weatherApi.getUVIndex(
                    lat = latitude,
                    lon = longitude,
                    apiKey = "6a43ad4ad589a4b49e92e04b71ea79b6"
                )

                val weatherData = WeatherData(
                    temperature = weatherResponse.main.temp,
                    humidity = weatherResponse.main.humidity,
                    uvIndex = uvResponse.value
                )

                _weatherState.value = WeatherState.Success(weatherData)
            } catch (e: Exception) {
                _weatherState.value = WeatherState.Error(e.message ?: "Failed to fetch weather data")
            }
        }
    }
} 