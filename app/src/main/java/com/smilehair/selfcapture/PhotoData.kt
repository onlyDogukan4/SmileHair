package com.smilehair.selfcapture

data class PhotoData(
    val photo_url: String, // Base64 encoded image string veya URL
    val photo_type: String // Örn: "front", "side_right", "side_left", "top", "back"
)

