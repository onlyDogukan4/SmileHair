package com.smilehair.selfcapture

data class ApiResponse(
    val message: String,
    val set_id: Int? = null // Sunucu başarılı olursa set_id dönecek
)


