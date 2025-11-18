package com.smilehair.selfcapture

data class PatientData(
    val name: String,
    val surname: String,
    val phone_number: String,
    val photos: List<PhotoData> // Fotoğraf listesi
)


