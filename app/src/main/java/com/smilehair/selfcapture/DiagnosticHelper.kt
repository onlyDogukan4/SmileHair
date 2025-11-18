package com.smilehair.selfcapture

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder

object DiagnosticHelper {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    
    fun logPatientData(patientData: PatientData) {
        try {
            val json = gson.toJson(patientData)
            Log.d("DiagnosticHelper", "=== PATIENT DATA JSON ===")
            Log.d("DiagnosticHelper", json)
            Log.d("DiagnosticHelper", "=== END JSON ===")
        } catch (e: Exception) {
            Log.e("DiagnosticHelper", "Failed to serialize PatientData", e)
        }
    }
    
    fun logApiResponse(response: ApiResponse) {
        try {
            val json = gson.toJson(response)
            Log.d("DiagnosticHelper", "=== API RESPONSE JSON ===")
            Log.d("DiagnosticHelper", json)
            Log.d("DiagnosticHelper", "=== END JSON ===")
        } catch (e: Exception) {
            Log.e("DiagnosticHelper", "Failed to serialize ApiResponse", e)
        }
    }
}

