package com.smilehair.selfcapture

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    
    // Python API'mizin beklediği POST isteği
    @POST("/api/capture-sets") 
    suspend fun createCaptureSet(@Body patientData: PatientData): ApiResponse // ApiResponse, sunucudan dönen yanıtı temsil eder
}


