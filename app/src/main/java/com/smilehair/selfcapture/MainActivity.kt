package com.smilehair.selfcapture

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.RectF
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.common.InputImage
import com.smilehair.selfcapture.localization.AppLanguage
import com.smilehair.selfcapture.localization.LanguageManager
import com.smilehair.selfcapture.localization.LocalizedText
import com.smilehair.selfcapture.localization.Texts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.common.util.concurrent.ListenableFuture
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

// Açı tanımları
enum class CaptureAngle(
    private val title: LocalizedText,
    val tiltRange: Pair<Float, Float>,
    val headYawRange: Pair<Float, Float>?
) {
    FRONT(LocalizedText("Tam Yüz Karşıdan", "Full Face Front"), Pair(-25f, 25f), Pair(-25f, 25f)),
    RIGHT_45(LocalizedText("45° Sağ (Kendi Sağınız)", "45° Right (Your Right)"), Pair(-30f, 30f), Pair(20f, 70f)),
    LEFT_45(LocalizedText("45° Sola (Kendi Solunuz)", "45° Left (Your Left)"), Pair(-30f, 30f), Pair(-70f, -20f)),
    VERTEX(LocalizedText("Tepe Kısmı", "Top View"), Pair(45f, 120f), null),
    BACK_DONOR(LocalizedText("Arka Donör", "Back Donor"), Pair(-10f, 55f), null);

    fun displayName(): String = title.get()
}

class MainActivity : AppCompatActivity(), SensorEventListener {
    
    private lateinit var previewView: PreviewView
    private lateinit var faceOverlayView: FaceOverlayView
    private lateinit var statusText: TextView
    private lateinit var angleText: TextView
    private lateinit var guidanceText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var yawText: TextView // Real-time yaw değeri için
    
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var isCapturing = false
    private var currentAngleIndex = 0
    private var isCameraPaused = false // Kamera duraklatıldı mı?
    private val TAG = "MainActivity"
    
    // Sensör yönetimi
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    
    // Sensör verileri
    private var phoneTilt: Float = 0f // Telefonun yere göre eğimi (0-180 derece)
    
    // Fotoğraf yönetimi
    private val capturedPhotos = mutableListOf<File>()
    private val angles = listOf(
        CaptureAngle.FRONT,
        CaptureAngle.RIGHT_45,
        CaptureAngle.LEFT_45,
        CaptureAngle.VERTEX,
        CaptureAngle.BACK_DONOR
    )
    
    // Radar sesi için
    private var lastBeepTime = 0L
    private var stablePositionCount = 0
    private val STABLE_POSITION_THRESHOLD = 2 // 2 kere üst üste doğru pozisyon (daha hızlı)
    private var pendingCaptureJob: Job? = null
    private var isAutoCapturePending = false
    private val BACK_FACE_MAX_RATIO = 0.25f
    private val TOP_FACE_MAX_RATIO = 0.2f
    
    // Text-to-Speech
    private var tts: TextToSpeech? = null
    private var lastVoiceTime = 0L
    private val VOICE_INTERVAL = 2000L // 2 saniyede bir sesli yönlendirme
    private var lastCaptureAnnouncementTime = 0L
    private val CAPTURE_ANNOUNCEMENT_INTERVAL = 2500L
    
    // ML Kit Face Detector - Head pose estimation için gerekli
    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE) // Daha doğru head pose için
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .enableTracking()
            .build()
    )
    
    // İzin isteme mekanizması
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            val cameraGranted = perms[Manifest.permission.CAMERA] == true
            if (cameraGranted) {
                setupSensors()
                // Retake modunda ise direkt ilgili talimatı göster
                val retakeIndex = intent.getIntExtra("retake_index", -1)
                val isRetakeMode = retakeIndex in 0 until angles.size
                if (isRetakeMode) {
                    currentAngleIndex = retakeIndex
                    Log.d(TAG, "Retake mode: İlgili talimat gösteriliyor, index: $retakeIndex")
                    showStepInstructionForCurrentAngle()
                } else {
                    showFirstInstruction()
                }
            } else {
                statusText.text = Texts.Capture.permissionRequired()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageManager.init(applicationContext)
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Başlatılıyor...")
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        faceOverlayView = findViewById(R.id.faceOverlayView)
        statusText = findViewById(R.id.statusText)
        angleText = findViewById(R.id.angleText)
        guidanceText = findViewById(R.id.guidanceText)
        progressBar = findViewById(R.id.progressBar)
        yawText = findViewById(R.id.yawText)
        
        Log.d(TAG, "UI elementleri başlatıldı")
        
        // Retake index kontrolü
        val retakeIndex = intent.getIntExtra("retake_index", -1)
        val isRetakeMode = retakeIndex in 0 until angles.size
        if (isRetakeMode) {
            Log.d(TAG, "Retake mode: index $retakeIndex")
            currentAngleIndex = retakeIndex
            // Retake modunda mevcut fotoğrafları yükle
            val existingPaths = intent.getStringArrayExtra("existing_photo_paths")
            if (existingPaths != null && existingPaths.size == angles.size) {
                capturedPhotos.clear()
                existingPaths.forEach { path ->
                    if (path.isNotEmpty()) {
                        val file = File(path)
                        if (file.exists()) {
                            capturedPhotos.add(file)
                        } else {
                            capturedPhotos.add(File("")) // Placeholder
                        }
                    } else {
                        capturedPhotos.add(File("")) // Placeholder
                    }
                }
                Log.d(TAG, "Retake mode: Mevcut ${capturedPhotos.size} fotoğraf yüklendi")
            } else {
                capturedPhotos.clear()
            }
        }
        
        // Sensör yöneticisini başlat
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer == null) {
            Log.w(TAG, "Accelerometer sensörü bulunamadı")
        } else {
            Log.d(TAG, "Accelerometer sensörü bulundu")
        }
        
        // İlerleme çubuğunu güncelle
        updateProgress()
        updateAngleText()
        
        // Text-to-Speech başlat
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val targetLocale = if (LanguageManager.currentLanguage == AppLanguage.EN) {
                    Locale.ENGLISH
                } else {
                    Locale("tr", "TR")
                }
                val result = tts?.setLanguage(targetLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "TTS: Selected language not supported, falling back to English")
                    tts?.setLanguage(Locale.ENGLISH)
                } else {
                    Log.d(TAG, "TTS: Language initialized for ${LanguageManager.currentLanguage}")
                }
            } else {
                Log.e(TAG, "TTS: Başlatılamadı, status: $status")
            }
        }
        
        // İzinleri kontrol et ve iste
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Kamera izni mevcut")
            setupSensors()
            // Retake modunda ise direkt ilgili talimatı göster, değilse ilk talimatı göster
            if (isRetakeMode) {
                Log.d(TAG, "Retake mode: İlgili talimat gösteriliyor, index: $retakeIndex")
                showStepInstructionForCurrentAngle()
            } else {
                showFirstInstruction()
            }
        } else {
            Log.d(TAG, "Kamera izni yok, izin istiyor...")
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }
    
    private fun setupSensors() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    
                    // Eğim açısını hesapla (0-180 derece)
                    phoneTilt = Math.toDegrees(atan2(sqrt(x * x + y * y).toDouble(), z.toDouble())).toFloat()
                }
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Gerekirse işlem yapılabilir
    }
    
    override fun onResume() {
        super.onResume()
        setupSensors()
    }
    
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun startCamera() {
        if (isCameraPaused) {
            Log.d(TAG, "Kamera duraklatılmış, başlatılmıyor")
            return
        }
        Log.d(TAG, "startCamera: Kamerayı başlatıyor...")
        lifecycleScope.launch {
            try {
                val provider = getCameraProvider()
                cameraProvider = provider
                Log.d(TAG, "CameraProvider alındı")

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                Log.d(TAG, "Preview oluşturuldu")

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                Log.d(TAG, "ImageCapture oluşturuldu")

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                provider.unbindAll()
                provider.bindToLifecycle(this@MainActivity, cameraSelector, preview, imageCapture)
                Log.d(TAG, "Kamera başarıyla bağlandı")
                
                statusText.text = Texts.Capture.cameraReady(angles[currentAngleIndex].displayName())
                startDetectionLoop()
                Log.d(TAG, "Detection loop başlatıldı")
            } catch (exc: Exception) {
                Log.e(TAG, "Kamera başlatılamadı", exc)
                statusText.text = Texts.Capture.cameraFailed(exc.message ?: "")
            }
        }
    }
    
    private fun stopCamera() {
        Log.d(TAG, "stopCamera: Kamera durduruluyor...")
        cameraProvider?.unbindAll()
        cameraProvider = null
        imageCapture = null
        previewView.visibility = android.view.View.GONE
        faceOverlayView.visibility = android.view.View.GONE
    }
    
    private fun resumeCamera() {
        Log.d(TAG, "resumeCamera: Kamera devam ettiriliyor...")
        isCameraPaused = false
        previewView.visibility = android.view.View.VISIBLE
        faceOverlayView.visibility = android.view.View.VISIBLE
        startCamera()
    }
    
    private suspend fun getCameraProvider(): ProcessCameraProvider = suspendCancellableCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                continuation.resume(cameraProvider)
            } catch (exc: Exception) {
                continuation.resumeWithException(exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startDetectionLoop() {
        Log.d(TAG, "startDetectionLoop: Detection loop başlatılıyor, currentAngleIndex: $currentAngleIndex")
        lifecycleScope.launch {
            while (currentAngleIndex < angles.size && !isFinishing) {
                // Kamera duraklatılmışsa bekle
                if (isCameraPaused) {
                    delay(500)
                    continue
                }
                
                val bitmap = withContext(Dispatchers.Main) {
                    previewView.bitmap
                }

                if (bitmap != null && !isCapturing) {
                    // currentAngle'ı try bloğunun dışında tanımla
                    val currentAngle = angles[currentAngleIndex]
                    
                    try {
                        val image = InputImage.fromBitmap(bitmap, 0)
                        val faces = detector.process(image).await()
                        
                        // Pozisyon kontrolü
                        val positionCheck = checkPosition(faces, bitmap, currentAngle)
                        val tiltCheck = checkTilt(currentAngle)
                        val headPoseCheck = checkHeadPose(faces, currentAngle)
                        
                        // Arka donör için pozisyon + telefon açısı kontrolü
                        val isValid = when (currentAngle) {
                            CaptureAngle.BACK_DONOR -> {
                                // Sadece pozisyon kontrolü - tilt kontrolü gevşetildi (sadece saç algılansın)
                                positionCheck.isValid
                            }
                            CaptureAngle.VERTEX -> {
                                // Sadece pozisyon kontrolü - tilt kontrolü gevşetildi (sadece saç algılansın)
                                positionCheck.isValid
                            }
                            else -> {
                                // İlk 3 açı için pozisyon + head pose kontrolü (40-50 derece sıkı kontrol)
                                positionCheck.isValid && headPoseCheck.isValid
                            }
                        }
                        
                        // Yaw değerini logla ve göster
                        var currentYaw: Float? = null
                        if (faces.isNotEmpty() && (currentAngleIndex in 1..2)) {
                            val face = faces.first()
                            currentYaw = face.headEulerAngleY
                            Log.v(TAG, "Angle: ${currentAngle.displayName()}, Yaw: ${"%.2f".format(currentYaw)}, Faces: ${faces.size}, Valid: $isValid")
                        }
                        
                        withContext(Dispatchers.Main) {
                            updateOverlay(faces, bitmap, positionCheck, isValid, currentAngle)
                            updateStatus(positionCheck, tiltCheck, headPoseCheck, currentAngle, isValid, currentYaw)
                            
                            // Pozisyon stabil olduğunda çekim yap
                            if (isValid) {
                                stablePositionCount++
                                val threshold = if (currentAngleIndex < 2) 1 else STABLE_POSITION_THRESHOLD
                                if (stablePositionCount >= threshold && !isCapturing && !isAutoCapturePending) {
                                    beginAutoCapture(currentAngle)
                                }
                            } else {
                                stablePositionCount = 0
                                cancelPendingCapture()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Face detection failed", e)
                        withContext(Dispatchers.Main) {
                            if (currentAngle == CaptureAngle.BACK_DONOR) {
                                // Arka donör için yüz algılanamaması normal
                                val tiltCheck = checkTilt(currentAngle)
                                if (tiltCheck.isValid) {
                                    statusText.text = Texts.Capture.positionPerfect()
                                    statusText.setTextColor(0xFF00FF00.toInt())
                                } else {
                                    statusText.text = tiltCheck.message
                                    statusText.setTextColor(0xFFFF0000.toInt())
                                }
                            } else {
                                statusText.text = Texts.Capture.faceDetecting()
                            }
                        }
                    }
                } else if (bitmap == null) {
                    Log.v(TAG, "Bitmap null, bekleniyor...")
                }
                delay(300) // Daha hızlı algılama
            }
            Log.d(TAG, "Detection loop sonlandı, currentAngleIndex: $currentAngleIndex")
        }
    }
    
    private fun updateOverlay(
        faces: List<com.google.mlkit.vision.face.Face>,
        bitmap: android.graphics.Bitmap,
        positionCheck: PositionCheck,
        isValid: Boolean,
        angle: CaptureAngle
    ) {
        val targetRect = getTargetRect(bitmap, angle)
        val overlayFaces = when (angle) {
            CaptureAngle.VERTEX, CaptureAngle.BACK_DONOR -> emptyList()
            else -> faces
        }
        faceOverlayView.updateFaces(overlayFaces, targetRect, isValid, angle, bitmap.width, bitmap.height)
    }
    
    private fun getTargetRect(bitmap: android.graphics.Bitmap, angle: CaptureAngle): RectF {
        val w = bitmap.width.toFloat()
        val h = bitmap.height.toFloat()
        
        return when (angle) {
            CaptureAngle.FRONT, CaptureAngle.RIGHT_45, CaptureAngle.LEFT_45 -> {
                val targetWidth = w * 0.65f
                val targetHeight = h * 0.75f
                val left = (w - targetWidth) / 2
                val top = (h - targetHeight) / 2
                RectF(left, top, left + targetWidth, top + targetHeight)
            }
            CaptureAngle.VERTEX -> {
                val targetWidth = w * 0.5f
                val targetHeight = h * 0.5f
                val left = (w - targetWidth) / 2
                val top = h * 0.08f
                RectF(left, top, left + targetWidth, top + targetHeight)
            }
            CaptureAngle.BACK_DONOR -> {
                val targetWidth = w * 0.7f
                val targetHeight = h * 0.6f
                val left = (w - targetWidth) / 2
                val top = (h - targetHeight) / 2
                RectF(left, top, left + targetWidth, top + targetHeight)
            }
        }
    }
    
    data class PositionCheck(val isValid: Boolean, val distance: Float, val message: String)
    data class TiltCheck(val isValid: Boolean, val message: String)
    data class HeadPoseCheck(val isValid: Boolean, val message: String)
    
    private fun checkHeadPose(faces: List<com.google.mlkit.vision.face.Face>, angle: CaptureAngle): HeadPoseCheck {
        // Head pose kontrolü sadece ilk 3 açı için
        if (angle.headYawRange == null) {
            return HeadPoseCheck(true, "") // Kontrol yok
        }
        
        if (faces.isEmpty()) {
            return HeadPoseCheck(false, Texts.Directions.faceNotDetected.get())
        }
        
        val face = faces.first()
        val yaw = face.headEulerAngleY // ML Kit için pozitif değer kullanıcı sağa döndüğünde artıyor
        val (minYaw, maxYaw) = angle.headYawRange
        val relaxedMin = minYaw - 5f
        val relaxedMax = maxYaw + 5f
        
        return when {
            yaw in minYaw..maxYaw -> {
                HeadPoseCheck(true, Texts.Capture.positionPerfect())
            }
            yaw in relaxedMin..relaxedMax -> {
                HeadPoseCheck(true, Texts.Capture.adjustPosition())
            }
            yaw < minYaw -> {
                when (angle) {
                    CaptureAngle.RIGHT_45 -> HeadPoseCheck(false, Texts.Directions.yawRightMore.get())
                    CaptureAngle.LEFT_45 -> HeadPoseCheck(false, Texts.Directions.yawLeftLess.get())
                    else -> HeadPoseCheck(false, if (yaw > 0) Texts.Directions.yawLeftMore.get() else Texts.Directions.yawRightMore.get())
                }
            }
            else -> {
                when (angle) {
                    CaptureAngle.RIGHT_45 -> HeadPoseCheck(false, Texts.Directions.yawRightLess.get())
                    CaptureAngle.LEFT_45 -> HeadPoseCheck(false, Texts.Directions.yawLeftMore.get())
                    else -> HeadPoseCheck(false, if (yaw > 0) Texts.Directions.yawLeftMore.get() else Texts.Directions.yawRightMore.get())
                }
            }
        }
    }
    
    private fun checkPosition(faces: List<com.google.mlkit.vision.face.Face>, bitmap: android.graphics.Bitmap, angle: CaptureAngle): PositionCheck {
        val w = bitmap.width.toFloat()
        val h = bitmap.height.toFloat()
        
        // VERTEX için özel kontrol - yüz görünmemeli, üst kafa bölgesi kontrolü
            if (angle == CaptureAngle.VERTEX) {
            return checkHeadTopPosition(bitmap, faces)
            }
        
        // BACK_DONOR için arka kafa tespiti - yüz görünmemeli
            if (angle == CaptureAngle.BACK_DONOR) {
            return checkBackHeadPosition(bitmap, faces)
            }
        
        if (faces.isEmpty()) {
            return PositionCheck(false, Float.MAX_VALUE, Texts.Directions.faceNotDetected.get())
        }
        
        val face = faces.first()
        val rect = face.boundingBox
        val centerX = rect.centerX().toFloat()
        val centerY = rect.centerY().toFloat()
        
        // Merkeze uzaklık hesapla
        val distanceX = abs(centerX - w / 2)
        val distanceY = abs(centerY - h / 2)
        val distance = sqrt(distanceX * distanceX + distanceY * distanceY)
        
        // Her açı için özel kontrol (daha toleranslı)
        when (angle) {
            CaptureAngle.FRONT -> {
                // Yüz ortalanmış olmalı (daha geniş tolerans)
                val centered = (centerX in (w * 0.25)..(w * 0.75)) &&
                        (centerY in (h * 0.2)..(h * 0.8))
                
                if (centered && distance < w * 0.4f) {
                    return PositionCheck(true, distance, Texts.Directions.hairDetected.get())
                } else {
                    val message = when {
                        centerX < w * 0.25 -> Texts.Directions.moveRight.get()
                        centerX > w * 0.75 -> Texts.Directions.moveLeft.get()
                        centerY < h * 0.2 -> Texts.Directions.moveDown.get()
                        centerY > h * 0.8 -> Texts.Directions.moveUp.get()
                        else -> Texts.Directions.moveCloser.get()
                    }
                    return PositionCheck(false, distance, message)
                }
            }
            CaptureAngle.RIGHT_45, CaptureAngle.LEFT_45 -> {
                val centered = (centerX in (w * 0.25)..(w * 0.75)) &&
                        (centerY in (h * 0.2)..(h * 0.8))
                
                if (centered && distance < w * 0.4f) {
                    return PositionCheck(true, distance, Texts.Directions.hairDetected.get())
                } else {
                    val message = when {
                        centerX < w * 0.25 -> Texts.Directions.moveRight.get()
                        centerX > w * 0.75 -> Texts.Directions.moveLeft.get()
                        centerY < h * 0.2 -> Texts.Directions.moveDown.get()
                        centerY > h * 0.8 -> Texts.Directions.moveUp.get()
                        else -> Texts.Directions.centerFace.get()
                    }
                    return PositionCheck(false, distance, message)
                }
            }
            else -> {
                return PositionCheck(false, 0f, Texts.Directions.unknownAngle.get())
            }
        }
    }
    
    // Üst kafa bölgesi tespiti - dairesel yapı ve saç dokusu kontrolü
    private fun checkHeadTopPosition(bitmap: android.graphics.Bitmap, faces: List<com.google.mlkit.vision.face.Face> = emptyList()): PositionCheck {
        // Üstten çekimde yüz görünmemeli
        val totalImagePixels = (bitmap.width * bitmap.height).toFloat()
        if (faces.isNotEmpty()) {
            val significantFace = faces.firstOrNull { face ->
                val area = face.boundingBox.width().toFloat() * face.boundingBox.height().toFloat()
                val ratio = area / totalImagePixels
                ratio > TOP_FACE_MAX_RATIO
            }
            if (significantFace != null) {
                return PositionCheck(false, Float.MAX_VALUE, Texts.Directions.faceMustNotAppearTop.get())
            }
        }
        
        val w = bitmap.width
        val h = bitmap.height
        val centerX = w / 2
        val centerY = (h * 0.2f).toInt() // Üst bölgenin merkezi
        
        // Merkez bölgede dairesel yapı kontrolü (kafa üstten yuvarlak görünür)
        val radius = minOf(w, h) * 0.2f // Daha geniş rehber
        val checkRadius = radius.toInt()
        
        // Merkez bölgede gradient ve varyans kontrolü
        var centerBrightness = 0L
        var centerCount = 0
        var edgeBrightness = 0L
        var edgeCount = 0
        var sumBrightness = 0L
        var sumSquared = 0L
        var minBrightness = 255
        var maxBrightness = 0
        
        // Merkez ve kenar bölgelerini analiz et
        for (y in maxOf(0, centerY - checkRadius) until minOf(h, centerY + checkRadius)) {
            for (x in maxOf(0, centerX - checkRadius) until minOf(w, centerX + checkRadius)) {
                val dx = x - centerX
                val dy = y - centerY
                val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val brightness = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                
                sumBrightness += brightness
                sumSquared += brightness * brightness
                minBrightness = minOf(minBrightness, brightness)
                maxBrightness = maxOf(maxBrightness, brightness)
                
                if (dist < radius * 0.6f) {
                    // Merkez bölge
                    centerBrightness += brightness
                    centerCount++
                } else if (dist < radius * 1.2f) {
                    // Kenar bölge
                    edgeBrightness += brightness
                    edgeCount++
                }
            }
        }
        
        if (centerCount == 0 || edgeCount == 0) {
            return PositionCheck(false, Float.MAX_VALUE, Texts.Directions.headNotVisibleTop.get())
        }
        
        val totalPixels = centerCount + edgeCount
        val avgBrightness = sumBrightness.toDouble() / totalPixels
        val avgSquared = sumSquared.toDouble() / totalPixels
        val variance = (avgSquared - (avgBrightness * avgBrightness)).toFloat()
        val contrast = maxBrightness - minBrightness
        
        // Üstten kafa tespiti kriterleri - sadece saç algılansın:
        // 1. Yüksek varyans (saç dokusu - tişört düz, saç dokulu) - daha toleranslı
        // 2. Yeterli kontrast
        // 3. Ortalama parlaklık makul aralıkta (tişört çok parlak olabilir)
        // Dairesel yapı kontrolü kaldırıldı - sadece saç algılansın yeter
        val hasTexture = variance > 180 && contrast > 40
        val reasonableBrightness = avgBrightness > 15 && avgBrightness < 235
        
        val isValidHead = hasTexture && reasonableBrightness
        
        return when {
            isValidHead -> {
                PositionCheck(true, 0f, Texts.Directions.hairDetected.get())
            }
            !hasTexture -> {
                PositionCheck(false, Float.MAX_VALUE, Texts.Directions.hairNotDetectedTop.get())
            }
            else -> {
                PositionCheck(false, Float.MAX_VALUE, Texts.Directions.keepTopInstruction.get())
            }
        }
    }
    
    // Arka kafa bölgesi tespiti - dairesel yapı ve saç dokusu kontrolü
    private fun checkBackHeadPosition(bitmap: android.graphics.Bitmap, faces: List<com.google.mlkit.vision.face.Face> = emptyList()): PositionCheck {
        val w = bitmap.width
        val h = bitmap.height
        val totalImagePixels = (w * h).toFloat()
        
        if (faces.isNotEmpty()) {
            val significantFace = faces.firstOrNull { face ->
                val area = face.boundingBox.width().toFloat() * face.boundingBox.height().toFloat()
                val ratio = area / totalImagePixels
                ratio > BACK_FACE_MAX_RATIO
            }
            if (significantFace != null) {
                return PositionCheck(false, Float.MAX_VALUE, Texts.Directions.noFaceBack.get())
            }
        }
        val centerX = w / 2
        val centerY = h / 2 // Tam merkeze - arka kafa tamamen görünsün
        
        // Merkez bölgede dairesel yapı kontrolü (kafa arkadan yuvarlak görünür)
        val radius = minOf(w, h) * 0.22f
        val checkRadius = radius.toInt()
        
        // Merkez bölgede gradient ve varyans kontrolü
        var centerBrightness = 0L
        var centerCount = 0
        var edgeBrightness = 0L
        var edgeCount = 0
        var sumBrightness = 0L
        var sumSquared = 0L
        var minBrightness = 255
        var maxBrightness = 0
        
        // Merkez ve kenar bölgelerini analiz et
        for (y in maxOf(0, centerY - checkRadius) until minOf(h, centerY + checkRadius)) {
            for (x in maxOf(0, centerX - checkRadius) until minOf(w, centerX + checkRadius)) {
                val dx = x - centerX
                val dy = y - centerY
                val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val brightness = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                
                sumBrightness += brightness
                sumSquared += brightness * brightness
                minBrightness = minOf(minBrightness, brightness)
                maxBrightness = maxOf(maxBrightness, brightness)
                
                if (dist < radius * 0.6f) {
                    // Merkez bölge
                    centerBrightness += brightness
                    centerCount++
                } else if (dist < radius * 1.2f) {
                    // Kenar bölge
                    edgeBrightness += brightness
                    edgeCount++
                }
            }
        }
        
        if (centerCount == 0 || edgeCount == 0) {
            return PositionCheck(false, Float.MAX_VALUE, Texts.Directions.headNotDetectedBack.get())
        }
        
        val sampleCount = centerCount + edgeCount
        val avgBrightness = sumBrightness.toDouble() / sampleCount
        val avgSquared = sumSquared.toDouble() / sampleCount
        val variance = (avgSquared - (avgBrightness * avgBrightness)).toFloat()
        val contrast = maxBrightness - minBrightness
        
        val centerAvg = centerBrightness.toDouble() / centerCount
        val edgeAvg = edgeBrightness.toDouble() / edgeCount
        
        // Arkadan kafa tespiti kriterleri - sadece saç algılansın:
        // 1. Yüksek varyans (saç dokusu - tişört düz, saç dokulu) - daha toleranslı
        // 2. Yeterli kontrast
        // 3. Ortalama parlaklık makul aralıkta (tişört çok parlak olabilir)
        // Dairesel yapı kontrolü kaldırıldı - sadece saç algılansın yeter
        val hasTexture = variance > 180 && contrast > 40
        val reasonableBrightness = avgBrightness > 15 && avgBrightness < 235
        
        val isValidHead = hasTexture && reasonableBrightness
        
        return when {
            isValidHead -> {
                PositionCheck(true, 0f, Texts.Directions.hairDetected.get())
            }
            !hasTexture -> {
                PositionCheck(false, Float.MAX_VALUE, Texts.Directions.hairNotDetectedBack.get())
            }
            else -> {
                PositionCheck(false, Float.MAX_VALUE, Texts.Directions.keepDonorInstruction.get())
            }
        }
    }
    
    private fun checkTilt(angle: CaptureAngle): TiltCheck {
        val (minTilt, maxTilt) = angle.tiltRange
        
        return when {
            phoneTilt in minTilt..maxTilt -> {
                TiltCheck(true, Texts.Directions.tiltOk.get())
            }
            phoneTilt < minTilt -> {
                TiltCheck(false, Texts.Directions.tiltMore.get())
            }
            else -> {
                TiltCheck(false, Texts.Directions.tiltLess.get())
            }
        }
    }
    
    private fun updateStatus(
        positionCheck: PositionCheck, 
        tiltCheck: TiltCheck, 
        headPoseCheck: HeadPoseCheck,
        angle: CaptureAngle, 
        isValid: Boolean,
        currentYaw: Float? = null
    ) {
        // Yaw değerini göster (sadece 45 derece açılar için)
        if (::yawText.isInitialized) {
            if (angle == CaptureAngle.RIGHT_45 || angle == CaptureAngle.LEFT_45) {
                if (currentYaw != null) {
                    yawText.visibility = android.view.View.VISIBLE
                    val (minYaw, maxYaw) = angle.headYawRange!!
                    val targetYaw = (minYaw + maxYaw) / 2f
                    val yawDiff = abs(currentYaw - targetYaw)
                    yawText.text = Texts.Capture.yawLabel(currentYaw.toInt(), targetYaw.toInt())
                    if (yawDiff < 10f) {
                        yawText.setTextColor(0xFF00FF00.toInt()) // Yeşil - hedefe yakın
                    } else if (yawDiff < 20f) {
                        yawText.setTextColor(0xFFFFFF00.toInt()) // Sarı - orta
                    } else {
                        yawText.setTextColor(0xFFFF0000.toInt()) // Kırmızı - uzak
                    }
                } else {
                    yawText.visibility = android.view.View.GONE
                }
            } else {
                yawText.visibility = android.view.View.GONE
            }
        }
        
        if (isValid) {
            statusText.text = Texts.Capture.positionPerfect()
            statusText.setTextColor(0xFF00FF00.toInt()) // Yeşil
            guidanceText.setTextColor(0xCCFFFFFF.toInt())
        } else {
            val messages = mutableListOf<String>()
            
            // Arka ve tepe donör pozları için özel yönlendirme - sadece pozisyon kontrolü
            if (angle == CaptureAngle.BACK_DONOR || angle == CaptureAngle.VERTEX) {
                // Tilt kontrolü kaldırıldı - sadece saç algılansın
                if (!positionCheck.isValid && positionCheck.message.isNotEmpty()) {
                    messages.add(positionCheck.message)
                }
                // Sesli yönlendirme
                speakVoiceGuidance(angle, positionCheck, tiltCheck, headPoseCheck)
            } else {
                // Diğer açılar için tüm kontroller
                if (!positionCheck.isValid && positionCheck.message.isNotEmpty()) {
                    messages.add(positionCheck.message)
                }
                if (!headPoseCheck.isValid && headPoseCheck.message.isNotEmpty()) {
                    messages.add(headPoseCheck.message)
                    // Head pose için sesli yönlendirme
                    speakVoiceGuidance(angle, positionCheck, tiltCheck, headPoseCheck)
                }
                // Tilt kontrolü sadece VERTEX için kritik
                if (!tiltCheck.isValid && angle == CaptureAngle.VERTEX) {
                    messages.add(tiltCheck.message)
                }
            }
            
            statusText.text = if (messages.isEmpty()) {
                Texts.Capture.adjustPosition()
            } else {
                messages.joinToString(" | ")
            }
            statusText.setTextColor(0xFFFF0000.toInt()) // Kırmızı
        }
        
        // Radar sesi (yaklaştıkça frekans artar) - sadece arka donör dışındaki açılar için
        if (!isValid && angle != CaptureAngle.BACK_DONOR && positionCheck.distance < Float.MAX_VALUE && positionCheck.distance > 0) {
            playRadarBeep(positionCheck.distance)
        }
        
        updateAngleText()
    }
    
    private fun speakVoiceGuidance(
        angle: CaptureAngle,
        positionCheck: PositionCheck,
        tiltCheck: TiltCheck,
        headPoseCheck: HeadPoseCheck
    ) {
        val now = System.currentTimeMillis()
        if (now - lastVoiceTime < VOICE_INTERVAL) {
            return // Çok sık ses çıkmasını önle
        }
        
        val message = when (angle) {
            CaptureAngle.BACK_DONOR -> {
                // Arka donör için özel yönlendirme - sadece saç algılansın
                if (!positionCheck.isValid && positionCheck.message.isNotEmpty()) {
                    positionCheck.message
                } else {
                    null
                }
            }
            CaptureAngle.RIGHT_45 -> {
                if (!headPoseCheck.isValid) {
                    headPoseCheck.message
                } else if (!positionCheck.isValid) {
                    Texts.Voice.rightInstruction()
                } else {
                    null
                }
            }
            CaptureAngle.LEFT_45 -> {
                if (!headPoseCheck.isValid) {
                    headPoseCheck.message
                } else if (!positionCheck.isValid) {
                    Texts.Voice.leftInstruction()
                } else {
                    null
                }
            }
            CaptureAngle.FRONT -> {
                if (!headPoseCheck.isValid) {
                    Texts.Voice.frontInstruction()
                } else if (!positionCheck.isValid) {
                    Texts.Voice.keepCentered()
                } else {
                    null
                }
            }
            CaptureAngle.VERTEX -> {
                // Tilt kontrolü kaldırıldı - sadece saç algılansın
                if (!positionCheck.isValid && positionCheck.message.isNotEmpty()) {
                    positionCheck.message
                } else {
                    null
                }
            }
        }
        
        message?.let {
            speak(it)
            lastVoiceTime = now
        }
    }
    
    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun announceCapture() {
        val now = System.currentTimeMillis()
        if (now - lastCaptureAnnouncementTime > CAPTURE_ANNOUNCEMENT_INTERVAL) {
            speak(Texts.Capture.holdStillVoice())
            lastCaptureAnnouncementTime = now
        }
    }
    
    private fun updateAngleText() {
        if (currentAngleIndex < angles.size) {
            val currentAngle = angles[currentAngleIndex]
            angleText.text = "${currentAngleIndex + 1}/5: ${currentAngle.displayName()}"
            updateGuidanceMessage(currentAngle)
        }
    }

    private fun updateGuidanceMessage(angle: CaptureAngle) {
        val message = when (angle) {
            CaptureAngle.FRONT -> Texts.Capture.angleInstruction("front")
            CaptureAngle.RIGHT_45 -> Texts.Capture.angleInstruction("right")
            CaptureAngle.LEFT_45 -> Texts.Capture.angleInstruction("left")
            CaptureAngle.VERTEX -> Texts.Capture.angleInstruction("top")
            CaptureAngle.BACK_DONOR -> Texts.Capture.angleInstruction("back")
        }
        guidanceText.text = message
    }
    
    private fun playRadarBeep(distance: Float) {
        val now = System.currentTimeMillis()
        // Mesafeye göre beep sıklığı (yaklaştıkça daha sık)
        val interval = (distance / 30).toLong().coerceIn(200, 1500)
        
        if (now - lastBeepTime > interval) {
            playBeep()
            lastBeepTime = now
        }
    }
    
    private fun beginAutoCapture(angle: CaptureAngle) {
        cancelPendingCapture()
        isAutoCapturePending = true
        pendingCaptureJob = lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                announceCapture()
                statusText.text = Texts.Capture.autoCapturePrompt()
                statusText.setTextColor(0xFFFFFF00.toInt()) // Sarı
                guidanceText.text = Texts.Capture.autoCapturePrompt()
            }
            delay(650)
            withContext(Dispatchers.Main) {
                isAutoCapturePending = false
                isCapturing = true
                takePhoto(angle)
            }
        }.also { job ->
            job.invokeOnCompletion {
                pendingCaptureJob = null
                if (it != null) {
                    isAutoCapturePending = false
                    if (!isFinishing) {
                        runOnUiThread {
                            statusText.text = Texts.Capture.maintainPosition()
                            statusText.setTextColor(0xFFFF6F00.toInt())
                        }
                    }
                }
            }
        }
    }

    private fun cancelPendingCapture() {
        if (pendingCaptureJob != null) {
            pendingCaptureJob?.cancel()
            pendingCaptureJob = null
            isAutoCapturePending = false
        }
    }

    private fun playBeep() {
        try {
            val tg = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            tg.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
            tg.release()
        } catch (e: Exception) {
            Log.e(TAG, "Beep failed", e)
        }
    }

    private fun takePhoto(angle: CaptureAngle) {
        val imageCapture = imageCapture ?: return

        val outputDir = getOutputDirectory()
        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val photoFile = File(outputDir, "angle_${currentAngleIndex + 1}_${angle.name}_${name}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        guidanceText.text = Texts.Capture.savingPhoto()
        guidanceText.setTextColor(0xCCFFFFFF.toInt())

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    runOnUiThread { 
                    statusText.text = Texts.Capture.captureError(exc.message ?: "")
                        statusText.setTextColor(0xFFFF0000.toInt())
                        isCapturing = false
                    }
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Photo saved: ${photoFile.absolutePath}")
                    
                    // Retake modunda mı kontrol et
                    val retakeIndex = intent.getIntExtra("retake_index", -1)
                    val isRetakeMode = retakeIndex in 0 until angles.size
                    
                    if (isRetakeMode) {
                        // Retake modunda: fotoğrafı doğru index'e ekle
                        if (capturedPhotos.size <= retakeIndex) {
                            // Eksik olan fotoğraflar için null placeholder ekle
                            while (capturedPhotos.size < retakeIndex) {
                                capturedPhotos.add(File("")) // Placeholder
                            }
                        }
                        if (capturedPhotos.size > retakeIndex) {
                            capturedPhotos[retakeIndex] = photoFile
                        } else {
                            capturedPhotos.add(photoFile)
                        }
                        
                        // Retake modunda: ReviewActivity'ye dön
                        runOnUiThread {
                            statusText.text = Texts.Capture.retakeReturn()
                            statusText.setTextColor(0xFF00FF00.toInt())
                            lifecycleScope.launch {
                                delay(1000)
                                navigateToReviewAfterRetake()
                            }
                        }
                    } else {
                        // Normal mod: fotoğrafı ekle ve başarılı mesajı göster
                    capturedPhotos.add(photoFile)
                    runOnUiThread { 
                        // Başarılı mesajı göster
                        showSuccessFeedback(angle.displayName())
                        
                        // Kamera kapat
                        stopCamera()
                        isCameraPaused = true
                        
                        // Kısa bir gecikme sonrası sonraki talimata geç
                        lifecycleScope.launch {
                            delay(2000) // 2 saniye başarı mesajı göster
                            moveToNextAngle()
                            }
                        }
                    }
                }
            })
    }
    
    private fun moveToNextAngle() {
        Log.d(TAG, "moveToNextAngle: Şu anki index: $currentAngleIndex")
        isCapturing = false
        stablePositionCount = 0
        currentAngleIndex++
        Log.d(TAG, "moveToNextAngle: Yeni index: $currentAngleIndex, Toplam: ${angles.size}")
        
        if (currentAngleIndex < angles.size) {
            updateProgress()
            updateAngleText()
            Log.d(TAG, "Sıradaki açı: ${angles[currentAngleIndex].displayName()}")
            
            // Her adım için talimat göster
            showStepInstructionForCurrentAngle()
        } else {
            // Tüm açılar tamamlandı - Başarı mesajı göster
            Log.d(TAG, "Tüm açılar tamamlandı, başarı mesajı gösteriliyor...")
            showSuccessMessage()
        }
    }
    
    private fun showFirstInstruction() {
        currentAngleIndex = 0
        showStepInstructionForCurrentAngle()
    }
    
    private fun showStepInstructionForCurrentAngle() {
        val angle = angles[currentAngleIndex]
        val stepNumber = currentAngleIndex + 1
        val (stepTitle, instruction, imageName) = when (angle) {
            CaptureAngle.FRONT -> Triple(
                angle.displayName(),
                Texts.Capture.angleInstruction("front"),
                "front_photo"
            )
            CaptureAngle.RIGHT_45 -> Triple(
                angle.displayName(),
                Texts.Capture.angleInstruction("right"),
                "right_side_photo"
            )
            CaptureAngle.LEFT_45 -> Triple(
                angle.displayName(),
                Texts.Capture.angleInstruction("left"),
                "left_side_photo"
            )
            CaptureAngle.VERTEX -> Triple(
                angle.displayName(),
                Texts.Capture.angleInstruction("top"),
                "top_side_photo"
            )
            CaptureAngle.BACK_DONOR -> Triple(
                angle.displayName(),
                Texts.Capture.angleInstruction("back"),
                "back_side_photo"
            )
        }
        
        showStepInstructionDialog(stepNumber, stepTitle, instruction, imageName) {
            // Onayla butonuna tıklanınca kamera aç
            resumeCamera()
        }
    }
    
    private fun showStepInstructionDialog(stepNumber: Int, stepTitle: String, instruction: String, imageName: String, onDismiss: (() -> Unit)? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_step_instruction, null)
        
        val titleText = dialogView.findViewById<TextView>(R.id.instructionTitle)
        val exampleImage = dialogView.findViewById<ImageView>(R.id.exampleImageView)
        val instructionText = dialogView.findViewById<TextView>(R.id.instructionText)
        val okButton = dialogView.findViewById<Button>(R.id.okButton)
        
        titleText.text = Texts.Capture.dialogTitle(stepNumber, stepTitle)
        instructionText.text = instruction
        
        // Talimat fotoğrafını yükle
        val resourceId = resources.getIdentifier(imageName, "drawable", packageName)
        if (resourceId != 0) {
            exampleImage.setImageResource(resourceId)
        } else {
            // Placeholder - görsel bulunamazsa göster
            exampleImage.setImageResource(android.R.drawable.ic_menu_camera)
        }
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        
        okButton.text = Texts.Capture.confirmButton()
        okButton.setBackgroundColor(0xFF1A237E.toInt()) // Lacivert tema
        okButton.setOnClickListener {
            dialog.dismiss()
            onDismiss?.invoke()
        }
        
        dialog.show()
    }
    
    private fun updateProgress() {
        val progress = if (angles.isNotEmpty()) {
            ((currentAngleIndex + 1) * 100 / angles.size).coerceAtMost(100)
        } else {
            0
        }
        progressBar.progress = progress
    }
    
    private fun showSuccessFeedback(angleName: String) {
        runOnUiThread {
            statusText.text = Texts.Capture.successStatus(angleName)
            statusText.setTextColor(0xFF00FF00.toInt())
            guidanceText.text = Texts.Capture.successGuidance()
            guidanceText.setTextColor(0xFF00FF00.toInt())
            
            // "Başarılı" sesi çal
            speak(Texts.Capture.successVoice())
        }
    }
    
    private fun showSuccessMessage() {
        runOnUiThread {
            statusText.text = Texts.Capture.completionStatus()
            statusText.setTextColor(0xFF00AA00.toInt())
            guidanceText.text = Texts.Capture.completionGuidance()
            guidanceText.setTextColor(0xFF00AA00.toInt())
            
            // 2 saniye sonra ReviewActivity'ye geç
            lifecycleScope.launch {
                delay(2000)
                navigateToReview()
            }
        }
    }
    
    private fun navigateToReview() {
        Log.d(TAG, "navigateToReview: Çekilen fotoğraf sayısı: ${capturedPhotos.size}, Beklenen: ${angles.size}")
        if (capturedPhotos.size == angles.size) {
            val photoPaths = capturedPhotos.map { it.absolutePath }.toTypedArray()
            Log.d(TAG, "Tüm fotoğraflar çekildi, ReviewActivity'ye geçiliyor. Fotoğraflar:")
            photoPaths.forEachIndexed { index, path ->
                Log.d(TAG, "  Fotoğraf ${index + 1}: $path")
            }
            val intent = Intent(this, ReviewActivity::class.java)
            intent.putExtra("photo_paths", photoPaths)
            startActivity(intent)
            finish()
        } else {
            Log.w(TAG, "Tüm fotoğraflar çekilmedi: ${capturedPhotos.size}/${angles.size}")
            statusText.text = Texts.Capture.notAllPhotos()
            statusText.setTextColor(0xFFFF0000.toInt())
        }
    }
    
    private fun navigateToReviewAfterRetake() {
        // Retake sonrası: mevcut fotoğrafları al ve ReviewActivity'ye dön
        val retakeIndex = intent.getIntExtra("retake_index", -1)
        val existingPaths = intent.getStringArrayExtra("existing_photo_paths")
        
        if (existingPaths != null && existingPaths.size == angles.size) {
            // Mevcut fotoğrafları kullan, sadece retake edilen fotoğrafı güncelle
            val updatedPaths = existingPaths.toMutableList()
            if (capturedPhotos.isNotEmpty() && retakeIndex in 0 until updatedPaths.size) {
                val updatedPhoto = capturedPhotos.getOrNull(retakeIndex) ?: capturedPhotos.last()
                updatedPaths[retakeIndex] = updatedPhoto.absolutePath
            }
            
            val intent = Intent(this, ReviewActivity::class.java)
            intent.putExtra("photo_paths", updatedPaths.toTypedArray())
            startActivity(intent)
            finish()
        } else {
            // Fallback: normal navigasyon
            navigateToReview()
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return when {
            mediaDir != null && mediaDir.exists() -> mediaDir
            else -> filesDir
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        detector.close()
        sensorManager.unregisterListener(this)
        tts?.stop()
        tts?.shutdown()
    }
}