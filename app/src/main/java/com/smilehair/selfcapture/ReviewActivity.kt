package com.smilehair.selfcapture

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import retrofit2.HttpException
import kotlin.math.roundToInt
import java.io.File
import com.smilehair.selfcapture.localization.LanguageManager
import com.smilehair.selfcapture.localization.LocalizedText
import com.smilehair.selfcapture.localization.Texts

class ReviewActivity : AppCompatActivity() {
    
    private lateinit var photosContainer: LinearLayout
    private lateinit var submitButton: Button
    private lateinit var statusText: TextView
    private lateinit var warningText: TextView
    private lateinit var uploadProgressBar: android.widget.ProgressBar
    private lateinit var uploadStatusText: TextView
    private lateinit var uploadProgressContainer: LinearLayout
    private lateinit var uploadPercentText: TextView
    private lateinit var retryUploadButton: Button
    private lateinit var serverUrlInput: EditText
    private lateinit var serverApplyButton: Button
    private lateinit var serverHelpText: TextView
    private lateinit var serverConfigTitle: TextView
    
    private var photoPaths: Array<String>? = null
    private val photoStatus = mutableListOf<PhotoStatus>() // Her fotoğrafın durumu
    private val angleNameTexts = listOf(
        LocalizedText("1. Tam Yüz Karşıdan", "1. Full Face Front"),
        LocalizedText("2. 45° Sağa", "2. 45° Right"),
        LocalizedText("3. 45° Sola", "3. 45° Left"),
        LocalizedText("4. Tepe Kısmı", "4. Top View"),
        LocalizedText("5. Arka Donör", "5. Back Donor")
    )
    private var lastSubmissionInfo: SubmissionInfo? = null

    private fun getAngleName(index: Int): String {
        return angleNameTexts.getOrNull(index)?.get() ?: Texts.Review.photoLabel(index + 1)
    }

    private fun updateUploadProgress(progress: Int? = null, statusMessage: String? = null) {
        progress?.let {
            uploadProgressBar.progress = it
            uploadPercentText.text = "${it}%"
        }
        statusMessage?.let {
            uploadStatusText.text = it
        }
    }
    
    // ML Kit Face Detector
    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .enableTracking()
            .build()
    )
    
    // Yüz özellikleri (aynı kişi kontrolü için)
    private val faceFeatures = mutableListOf<FaceFeature>()
    private val clothingColors = mutableListOf<Int>()
    
    // Location services
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    
    // Location permission launcher
    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (fineLocationGranted || coarseLocationGranted) {
                getCurrentLocation()
            } else {
                Log.w("ReviewActivity", "Location permission denied")
                // Location olmadan da devam edebilir
            }
        }
    
    data class PhotoStatus(
        var isChecked: Boolean = false,
        var isApproved: Boolean = false,
        var errorMessage: String = ""
    )

data class SubmissionInfo(
    val name: String,
    val surname: String,
    val phone: String
)
    
    data class FaceFeature(
        val boundingBoxArea: Float,
        val headEulerAngleY: Float,
        val headEulerAngleX: Float,
        val headEulerAngleZ: Float
    )
    
    data class ValidationResult(val isValid: Boolean, val errorMessage: String)
    data class SamePersonResult(val isSamePerson: Boolean, val errorMessage: String)
    data class FinalValidationResult(val ok: Boolean, val reason: String = "")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageManager.init(applicationContext)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)
        
        photosContainer = findViewById(R.id.photosContainer)
        submitButton = findViewById(R.id.submitButton)
        statusText = findViewById(R.id.reviewStatusText)
        warningText = findViewById(R.id.reviewWarningText)
        uploadProgressBar = findViewById(R.id.uploadProgressBar)
        uploadStatusText = findViewById(R.id.uploadStatusText)
        uploadProgressContainer = findViewById(R.id.uploadProgressContainer)
        uploadPercentText = findViewById(R.id.uploadPercentText)
        retryUploadButton = findViewById(R.id.retryUploadButton)
        serverUrlInput = findViewById(R.id.inputServerUrlInline)
        serverApplyButton = findViewById(R.id.buttonApplyServerUrl)
        serverHelpText = findViewById(R.id.serverConfigHelp)
        serverConfigTitle = findViewById(R.id.serverConfigTitle)
        retryUploadButton.text = Texts.Review.retryButton()
        retryUploadButton.setOnClickListener {
            lastSubmissionInfo?.let {
                uploadSetToFirebase(it.name, it.surname, it.phone)
            }
        }
        
        warningText.text = Texts.Review.reviewWarning()
        statusText.text = Texts.Review.statusHint()
        submitButton.text = Texts.Review.uploadButton()
        serverConfigTitle.text = Texts.Review.serverSectionTitle()
        serverHelpText.text = Texts.Review.serverHelpText()
        serverApplyButton.text = Texts.Review.serverSaveButton()
        serverUrlInput.setText(RetrofitClient.getBaseUrl())
        serverUrlInput.hint = Texts.Review.serverHint()
        serverApplyButton.setOnClickListener {
            val value = serverUrlInput.text?.toString()?.trim().orEmpty()
            val success = ApiConfigManager.setBaseUrl(value)
            if (success) {
                Toast.makeText(this, Texts.Review.serverUpdateSuccess(), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, Texts.Review.serverUpdateInvalid(), Toast.LENGTH_LONG).show()
            }
        }
        
        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        photoPaths = intent.getStringArrayExtra("photo_paths")
        
        if (photoPaths != null && photoPaths!!.size == 5) {
            repeat(5) { photoStatus.add(PhotoStatus()) }
            displayPhotos()
        } else {
            statusText.text = Texts.Review.missingPhotos()
        }
        
        submitButton.setOnClickListener {
            // Final doğrulama geçmiş durumda aktif olacak
            promptUserInfoAndUpload()
        }
        
        // Request location permission and get location
        checkLocationPermissionAndGetLocation()
    }
    
    private fun checkLocationPermissionAndGetLocation() {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        if (fineLocationGranted || coarseLocationGranted) {
            getCurrentLocation()
        } else {
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    private fun getCurrentLocation() {
        lifecycleScope.launch {
            try {
                val fineLocationGranted = ContextCompat.checkSelfPermission(
                    this@ReviewActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                
                val coarseLocationGranted = ContextCompat.checkSelfPermission(
                    this@ReviewActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                
                if (!fineLocationGranted && !coarseLocationGranted) {
                    Log.w("ReviewActivity", "Location permission not granted")
                    return@launch
                }
                
                val cancellationTokenSource = CancellationTokenSource()
                val priority = if (fineLocationGranted) {
                    Priority.PRIORITY_HIGH_ACCURACY
                } else {
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY
                }
                
                val locationResult = fusedLocationClient.getCurrentLocation(
                    priority,
                    cancellationTokenSource.token
                ).await()
                
                currentLocation = locationResult
                Log.d("ReviewActivity", "Location obtained: ${locationResult?.latitude}, ${locationResult?.longitude}")
            } catch (e: Exception) {
                Log.e("ReviewActivity", "Error getting location", e)
                // Location olmadan da devam edebilir
            }
        }
    }
    
    private fun displayPhotos() {
        photosContainer.removeAllViews()
        
        photoPaths?.forEachIndexed { index, path ->
            val photoFile = File(path)
            if (!photoFile.exists()) return@forEachIndexed
            
            val photoLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(20), dp(20), dp(20), dp(20))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(16) }
                background = ContextCompat.getDrawable(this@ReviewActivity, R.drawable.bg_photo_card)
                elevation = 6f
            }
            
            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(260)
                ).apply { bottomMargin = dp(12) }
                scaleType = ImageView.ScaleType.CENTER_CROP
                clipToOutline = true
            }
            try {
                val originalBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                if (originalBitmap != null) {
                    // 5. foto (index 4) için 180 derece döndür (ters çekildiği için)
                    val displayBitmap = if (index == 4) {
                        val matrix = Matrix()
                        matrix.postRotate(180f)
                        Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
                    } else {
                        originalBitmap
                    }
                    imageView.setImageBitmap(displayBitmap)
                    // Fotoğrafa tıklayınca tam ekran göster (orijinal bitmap'i kullan, index'e göre döndür)
                    imageView.setOnClickListener {
                        showFullScreenImage(originalBitmap, index)
                    }
                } else {
                    imageView.setImageResource(android.R.drawable.ic_menu_report_image)
                }
            } catch (_: Exception) {
                imageView.setImageResource(android.R.drawable.ic_menu_report_image)
            }
            
            val angleText = TextView(this).apply {
                text = getAngleName(index)
                textSize = 18f
                setTextColor(0xFF000000.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(8) }
            }
            
            val statusTextView = TextView(this).apply {
                text = Texts.Review.photoUnchecked()
                textSize = 14f
                setTextColor(0xFF666666.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(12) }
            }
            
            val buttonLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, dp(4), 0, 0)
            }
            
            val checkButton = Button(this).apply {
                text = Texts.Review.buttonCheck()
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply { marginEnd = dp(8) }
                background = ContextCompat.getDrawable(this@ReviewActivity, R.drawable.bg_primary_button)
                setTextColor(Color.WHITE)
                isAllCaps = false
                setPadding(dp(12), dp(12), dp(12), dp(12))
            }
            checkButton.setOnClickListener {
                checkPhoto(index, photoFile, statusTextView, checkButton, buttonLayout)
            }
            
            val retakeButton = Button(this).apply {
                text = Texts.Review.buttonRetake()
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply { marginStart = dp(8) }
                visibility = View.GONE
                background = ContextCompat.getDrawable(this@ReviewActivity, R.drawable.bg_secondary_button)
                setTextColor(0xFF1A237E.toInt())
                isAllCaps = false
                setPadding(dp(12), dp(12), dp(12), dp(12))
                setOnClickListener { retakePhoto(index) }
            }
            
            buttonLayout.addView(checkButton)
            buttonLayout.addView(retakeButton)
            photoLayout.addView(imageView)
            photoLayout.addView(angleText)
            photoLayout.addView(statusTextView)
            photoLayout.addView(buttonLayout)
            photosContainer.addView(photoLayout)
        }
        
        statusText.text = Texts.Review.statusHint()
    }
    
    private fun checkPhoto(
        index: Int,
        photoFile: File,
        statusTextView: TextView,
        checkButton: Button,
        buttonLayout: LinearLayout
    ) {
        lifecycleScope.launch {
            statusTextView.text = Texts.Review.photoChecking()
            statusTextView.setTextColor(0xFFFF9800.toInt())
            checkButton.isEnabled = false
            
            try {
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                if (bitmap == null) {
                statusTextView.text = Texts.Review.photoLoadFailed()
                    statusTextView.setTextColor(0xFFFF0000.toInt())
                    checkButton.isEnabled = true
                    return@launch
                }
                
                val image = InputImage.fromBitmap(bitmap, 0)
                val faces = detector.process(image).await()
                
                val status = photoStatus[index]
                status.isChecked = true
                
                val validationResult = validatePhoto(index, faces, bitmap)
                if (validationResult.isValid) {
                    // Aynı kişi kontrolü - sadece ilk 3 foto için
                    if (index < 3) {
                        val samePersonResult = checkSamePerson(index, faces)
                        if (!samePersonResult.isSamePerson) {
                            status.isApproved = false
                            status.errorMessage = samePersonResult.errorMessage
                            statusTextView.text = Texts.Review.photoRejected(samePersonResult.errorMessage)
                            statusTextView.setTextColor(0xFFFF0000.toInt())
                            showRetakeButton(index, buttonLayout)
                            checkButton.isEnabled = true
                            updateSubmitButton()
                            return@launch
                        }
                    }
                    
                    status.isApproved = true
                    status.errorMessage = ""
                    statusTextView.text = Texts.Review.photoStatusApproved()
                    statusTextView.setTextColor(0xFF00AA00.toInt())
                    checkButton.text = Texts.Review.buttonChecked()
                    checkButton.isEnabled = false
                } else {
                    status.isApproved = false
                    status.errorMessage = validationResult.errorMessage
                    statusTextView.text = Texts.Review.photoRejected(validationResult.errorMessage)
                    statusTextView.setTextColor(0xFFFF0000.toInt())
                    showRetakeButton(index, buttonLayout)
                    checkButton.isEnabled = true
                }
                
                updateSubmitButton()
            } catch (e: Exception) {
                Log.e("ReviewActivity", "Photo check failed", e)
                statusTextView.text = Texts.Review.photoValidationError(e.message ?: "-")
                statusTextView.setTextColor(0xFFFF0000.toInt())
                checkButton.isEnabled = true
            }
        }
    }
    
    private fun validatePhoto(index: Int, faces: List<com.google.mlkit.vision.face.Face>, bitmap: Bitmap): ValidationResult {
        when (index) {
            0 -> {
                if (faces.isEmpty()) return ValidationResult(false, Texts.Directions.faceNotDetected.get())
                if (!isValidHumanHead(faces.first(), bitmap)) {
                    return ValidationResult(false, Texts.Review.headNotDetected())
                }
            }
            1 -> {
                if (faces.isEmpty()) return ValidationResult(false, Texts.Directions.faceNotDetected.get())
                val face = faces.first()
                val yaw = face.headEulerAngleY
                if (yaw !in -50f..-40f) return ValidationResult(false, Texts.Review.yawRightError(yaw.toInt()))
                if (!hasVisibleTemple(face, bitmap, isRight = true)) {
                    return ValidationResult(false, Texts.Review.templeRightMissing())
                }
                if (!isValidHumanHead(face, bitmap)) {
                    return ValidationResult(false, Texts.Review.headNotDetected())
                }
            }
            2 -> {
                if (faces.isEmpty()) return ValidationResult(false, Texts.Directions.faceNotDetected.get())
                val face = faces.first()
                val yaw = face.headEulerAngleY
                if (yaw !in 40f..50f) return ValidationResult(false, Texts.Review.yawLeftError(yaw.toInt()))
                if (!hasVisibleTemple(face, bitmap, isRight = false)) {
                    return ValidationResult(false, Texts.Review.templeLeftMissing())
                }
                if (!isValidHumanHead(face, bitmap)) {
                    return ValidationResult(false, Texts.Review.headNotDetected())
                }
            }
            3 -> {
                if (faces.isNotEmpty()) return ValidationResult(false, Texts.Directions.faceMustNotAppearTop.get())
                if (!hasValidTopHeadView(bitmap)) {
                    return ValidationResult(false, Texts.Review.topNotDetected())
                }
            }
            4 -> {
                if (faces.isNotEmpty()) return ValidationResult(false, Texts.Directions.noFaceBack.get())
                if (!hasValidBackHeadView(bitmap)) {
                    return ValidationResult(false, Texts.Review.backNotDetected())
                }
            }
        }
        return ValidationResult(true, "")
    }
    
    // Şakak görünüyor mu kontrolü
    private fun hasVisibleTemple(face: com.google.mlkit.vision.face.Face, bitmap: Bitmap, isRight: Boolean): Boolean {
        // Yüz bounding box'ın yan kısmında içerik var mı kontrol et
        val rect = face.boundingBox
        val w = bitmap.width
        val h = bitmap.height
        
        // Şakak bölgesi: yüzün yan tarafı
        val templeRegion = if (isRight) {
            // Sağ şakak: yüzün sağ tarafı
            val left = rect.right.coerceAtMost(w - 1)
            val right = (rect.right + (w - rect.right) * 0.3f).toInt().coerceAtMost(w - 1)
            val top = rect.top
            val bottom = rect.bottom
            Rect(left, top, right, bottom)
        } else {
            // Sol şakak: yüzün sol tarafı
            val left = (rect.left - (rect.left) * 0.3f).toInt().coerceAtLeast(0)
            val right = rect.left.coerceAtLeast(0)
            val top = rect.top
            val bottom = rect.bottom
            Rect(left, top, right, bottom)
        }
        
        // Şakak bölgesinde içerik var mı kontrol et
        val pixels = sampleRegion(bitmap,
            templeRegion.left.toFloat() / w,
            templeRegion.top.toFloat() / h,
            (templeRegion.width()).toFloat() / w,
            (templeRegion.height()).toFloat() / h
        )
        
        if (pixels.isEmpty()) return false
        
        // Varyans ve kontrast kontrolü - şakak bölgesinde texture olmalı
        var sumBrightness = 0L
        var sumSquared = 0L
        var minBrightness = 255
        var maxBrightness = 0
        
        for (px in pixels) {
            val r = (px shr 16) and 0xFF
            val g = (px shr 8) and 0xFF
            val b = px and 0xFF
            val brightness = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            sumBrightness += brightness
            sumSquared += brightness * brightness
            minBrightness = minOf(minBrightness, brightness)
            maxBrightness = maxOf(maxBrightness, brightness)
        }
        
        val avgBrightness = sumBrightness.toDouble() / pixels.size
        val avgSquared = sumSquared.toDouble() / pixels.size
        val variance = (avgSquared - (avgBrightness * avgBrightness)).toFloat()
        val contrast = maxBrightness - minBrightness
        
        // Şakak bölgesinde yeterli içerik var mı (saç veya deri)
        return avgBrightness > 20 && variance > 150 && contrast > 30
    }
    
    // Geçerli bir insan kafası mı kontrolü
    private fun isValidHumanHead(face: com.google.mlkit.vision.face.Face, bitmap: Bitmap): Boolean {
        val rect = face.boundingBox
        val area = rect.width() * rect.height()
        val totalArea = bitmap.width * bitmap.height
        
        // Yüz alanı çok küçük veya çok büyük olmamalı
        val areaRatio = area.toFloat() / totalArea
        if (areaRatio < 0.05f || areaRatio > 0.8f) return false
        
        // Yüz boyut oranı mantıklı olmalı (genişlik/yükseklik)
        val aspectRatio = rect.width().toFloat() / rect.height().toFloat()
        if (aspectRatio < 0.5f || aspectRatio > 2.0f) return false
        
        return true
    }
    
    // Üstten kafa görüntüsü kontrolü - dairesel yapı ve saç dokusu
    private fun hasValidTopHeadView(bitmap: Bitmap): Boolean {
        val w = bitmap.width
        val h = bitmap.height
        val centerX = w / 2
        val centerY = (h * 0.2f).toInt()
        
        val radius = minOf(w, h) * 0.2f
        val checkRadius = radius.toInt()
        
        var centerBrightness = 0L
        var centerCount = 0
        var edgeBrightness = 0L
        var edgeCount = 0
        var sumBrightness = 0L
        var sumSquared = 0L
        var minBrightness = 255
        var maxBrightness = 0
        
        for (y in maxOf(0, centerY - checkRadius) until minOf(h, centerY + checkRadius)) {
            for (x in maxOf(0, centerX - checkRadius) until minOf(w, centerX + checkRadius)) {
                val dx = x - centerX
                val dy = y - centerY
                val dist = kotlin.math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                
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
                    centerBrightness += brightness
                    centerCount++
                } else if (dist < radius * 1.2f) {
                    edgeBrightness += brightness
                    edgeCount++
                }
            }
        }
        
        if (centerCount == 0 || edgeCount == 0) return false
        
        val totalPixels = centerCount + edgeCount
        val avgBrightness = sumBrightness.toDouble() / totalPixels
        val avgSquared = sumSquared.toDouble() / totalPixels
        val variance = (avgSquared - (avgBrightness * avgBrightness)).toFloat()
        val contrast = maxBrightness - minBrightness
        
        // Sadece saç algılansın - dairesel yapı kontrolü kaldırıldı
        val hasTexture = variance > 180 && contrast > 40
        val reasonableBrightness = avgBrightness > 15 && avgBrightness < 235
        
        return hasTexture && reasonableBrightness
    }
    
    // Arkadan kafa görüntüsü kontrolü - sadece saç algılansın
    private fun hasValidBackHeadView(bitmap: Bitmap): Boolean {
        val w = bitmap.width
        val h = bitmap.height
        val centerX = w / 2
        val centerY = h / 2 // Tam merkeze - arka kafa tamamen görünsün
        
        val radius = minOf(w, h) * 0.22f
        val checkRadius = radius.toInt()
        
        var centerBrightness = 0L
        var centerCount = 0
        var edgeBrightness = 0L
        var edgeCount = 0
        var sumBrightness = 0L
        var sumSquared = 0L
        var minBrightness = 255
        var maxBrightness = 0
        
        for (y in maxOf(0, centerY - checkRadius) until minOf(h, centerY + checkRadius)) {
            for (x in maxOf(0, centerX - checkRadius) until minOf(w, centerX + checkRadius)) {
                val dx = x - centerX
                val dy = y - centerY
                val dist = kotlin.math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                
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
                    centerBrightness += brightness
                    centerCount++
                } else if (dist < radius * 1.2f) {
                    edgeBrightness += brightness
                    edgeCount++
                }
            }
        }
        
        if (centerCount == 0 || edgeCount == 0) return false
        
        val totalPixels = centerCount + edgeCount
        val avgBrightness = sumBrightness.toDouble() / totalPixels
        val avgSquared = sumSquared.toDouble() / totalPixels
        val variance = (avgSquared - (avgBrightness * avgBrightness)).toFloat()
        val contrast = maxBrightness - minBrightness
        
        // Sadece saç algılansın - dairesel yapı kontrolü kaldırıldı
        val hasTexture = variance > 180 && contrast > 40
        val reasonableBrightness = avgBrightness > 15 && avgBrightness < 235
        
        return hasTexture && reasonableBrightness
    }
    
    private fun checkSamePerson(index: Int, faces: List<com.google.mlkit.vision.face.Face>): SamePersonResult {
        // Sadece ilk 3 foto için aynı kişi kontrolü (yüz olan fotoğraflar)
        if (index >= 3) {
            return SamePersonResult(true, "") // Tepe ve arka için aynı kişi kontrolü yapılmıyor
        }
        
        if (faces.isEmpty()) {
            return SamePersonResult(false, Texts.Review.samePersonFaceMissing())
        }
        val face = faces.first()
        val rect = face.boundingBox
        val area = rect.width().toFloat() * rect.height().toFloat()
        val feature = FaceFeature(
            boundingBoxArea = area,
            headEulerAngleY = face.headEulerAngleY,
            headEulerAngleX = face.headEulerAngleX,
            headEulerAngleZ = face.headEulerAngleZ
        )
        if (index == 0) {
            faceFeatures.add(feature)
            return SamePersonResult(true, "")
        }
        if (faceFeatures.isEmpty()) {
            return SamePersonResult(false, Texts.Review.samePersonReferenceMissing())
        }
        val referenceFeature = faceFeatures[0]
        val areaDiff = kotlin.math.abs(area - referenceFeature.boundingBoxArea) / referenceFeature.boundingBoxArea
        // Daha toleranslı alan kontrolü - farklı açılardan çekimde boyut farkı olabilir
        if (areaDiff > 0.8f) return SamePersonResult(false, Texts.Review.samePersonMismatch())
        
        // Açı kontrolleri kaldırıldı - çekim sırasında zaten kontrol ediliyor
        faceFeatures.add(feature)
        return SamePersonResult(true, "")
    }
    
    // Üst kafa bölgesi içerik kontrolü (tepe fotoğrafı için)
    private fun hasHeadTopContent(bitmap: Bitmap): Boolean {
        val w = bitmap.width
        val h = bitmap.height
        
        // Üst bölgeyi kontrol et (üst %30-40)
        val topRegionStart = 0
        val topRegionEnd = (h * 0.4f).toInt()
        val leftMargin = (w * 0.25f).toInt()
        val rightMargin = (w * 0.75f).toInt()
        
        val pixels = sampleRegion(bitmap, 
            leftMargin.toFloat() / w, 
            topRegionStart.toFloat() / h,
            (rightMargin - leftMargin).toFloat() / w,
            (topRegionEnd - topRegionStart).toFloat() / h
        )
        
        // Parlaklık ve varyans hesapla
        var sumBrightness = 0L
        var sumSquared = 0L
        var minBrightness = 255
        var maxBrightness = 0
        
        for (px in pixels) {
            val r = (px shr 16) and 0xFF
            val g = (px shr 8) and 0xFF
            val b = px and 0xFF
            val brightness = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            
            sumBrightness += brightness
            sumSquared += brightness * brightness
            minBrightness = minOf(minBrightness, brightness)
            maxBrightness = maxOf(maxBrightness, brightness)
        }
        
        if (pixels.isEmpty()) {
            return false
        }
        
        val avgBrightness = sumBrightness.toDouble() / pixels.size
        val avgSquared = sumSquared.toDouble() / pixels.size
        val variance = (avgSquared - (avgBrightness * avgBrightness)).toFloat()
        val contrast = maxBrightness - minBrightness
        
        // Üst bölgede yeterli içerik var mı kontrol et (saç veya kafa derisi)
        return avgBrightness > 30 && variance > 200 && contrast > 40
    }
    
    // Arka kafa bölgesi içerik kontrolü (arka fotoğrafı için)
    private fun hasBackHeadContent(bitmap: Bitmap): Boolean {
        val w = bitmap.width
        val h = bitmap.height
        
        // Orta-alt bölgeyi kontrol et (arka kafa bölgesi)
        val backRegionStart = (h * 0.35f).toInt()
        val backRegionEnd = (h * 0.75f).toInt()
        val leftMargin = (w * 0.2f).toInt()
        val rightMargin = (w * 0.8f).toInt()
        
        val pixels = sampleRegion(bitmap, 
            leftMargin.toFloat() / w, 
            backRegionStart.toFloat() / h,
            (rightMargin - leftMargin).toFloat() / w,
            (backRegionEnd - backRegionStart).toFloat() / h
        )
        
        // Parlaklık ve varyans hesapla
        var sumBrightness = 0L
        var sumSquared = 0L
        var minBrightness = 255
        var maxBrightness = 0
        
        for (px in pixels) {
            val r = (px shr 16) and 0xFF
            val g = (px shr 8) and 0xFF
            val b = px and 0xFF
            val brightness = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            
            sumBrightness += brightness
            sumSquared += brightness * brightness
            minBrightness = minOf(minBrightness, brightness)
            maxBrightness = maxOf(maxBrightness, brightness)
        }
        
        if (pixels.isEmpty()) {
            return false
        }
        
        val avgBrightness = sumBrightness.toDouble() / pixels.size
        val avgSquared = sumSquared.toDouble() / pixels.size
        val variance = (avgSquared - (avgBrightness * avgBrightness)).toFloat()
        val contrast = maxBrightness - minBrightness
        
        // Arka bölgede yeterli içerik var mı kontrol et (saç veya kafa derisi)
        return avgBrightness > 30 && variance > 200 && contrast > 40
    }
    
    private fun extractDominantClothingColor(bitmap: Bitmap): Int {
        val w = bitmap.width
        val h = bitmap.height
        val left = (w * 0.35f).toInt()
        val right = (w * 0.65f).toInt()
        val top = (h * 0.70f).toInt()
        val bottom = (h * 0.90f).toInt()
        var sumR = 0L
        var sumG = 0L
        var sumB = 0L
        var count = 0L
        for (y in top until bottom) {
            for (x in left until right) {
                val c = bitmap.getPixel(x, y)
                sumR += (c shr 16) and 0xFF
                sumG += (c shr 8) and 0xFF
                sumB += c and 0xFF
                count++
            }
        }
        val r = (sumR / count).toInt().coerceIn(0, 255)
        val g = (sumG / count).toInt().coerceIn(0, 255)
        val b = (sumB / count).toInt().coerceIn(0, 255)
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }
    
    private fun sampleRegion(bitmap: Bitmap, leftRatio: Float, topRatio: Float, widthRatio: Float, heightRatio: Float): IntArray {
        val w = bitmap.width
        val h = bitmap.height
        val left = (w * leftRatio).toInt().coerceIn(0, w - 1)
        val top = (h * topRatio).toInt().coerceIn(0, h - 1)
        val width = (w * widthRatio).toInt().coerceAtLeast(1).coerceAtMost(w - left)
        val height = (h * heightRatio).toInt().coerceAtLeast(1).coerceAtMost(h - top)
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, left, top, width, height)
        return pixels
    }
    
    private fun validateSamePersonAcrossSet(): FinalValidationResult {
        // Kıyafet kontrolü kaldırıldı - farklı kıyafet olabilir
        // Sadece tüm fotoğrafların onaylandığını kontrol et
        if (photoStatus.size != 5 || !photoStatus.all { it.isChecked && it.isApproved }) {
            return FinalValidationResult(false, Texts.Review.finalValidationPending())
        }
        // Tüm fotoğraflar onaylandıysa geçer
        return FinalValidationResult(true, "")
    }
    
    private fun showRetakeButton(index: Int, buttonLayout: LinearLayout) {
        if (buttonLayout.childCount > 1) {
            val retakeButton = buttonLayout.getChildAt(1) as Button
            retakeButton.visibility = View.VISIBLE
        }
    }
    
    private fun retakePhoto(index: Int) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("retake_index", index)
        // Mevcut fotoğraf yollarını da gönder
        intent.putExtra("existing_photo_paths", photoPaths)
        startActivity(intent)
        finish()
    }
    
    private fun showFullScreenImage(bitmap: Bitmap?, index: Int) {
        if (bitmap == null) return
        
        // 5. foto için döndürülmüş bitmap'i kullan
        var displayBitmap = bitmap
        if (index == 4) {
            val matrix = Matrix()
            matrix.postRotate(180f)
            displayBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
        
        val fullScreenImageView = ImageView(this).apply {
            setImageBitmap(displayBitmap)
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            setOnClickListener {
                // Tıklayınca kapat
            }
        }
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(fullScreenImageView)
            .setTitle(getAngleName(index))
            .setPositiveButton(Texts.Review.closeButton(), null)
            .create()
        
        // Tam ekran yap
        dialog.window?.apply {
            setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundDrawableResource(android.R.color.black)
        }
        dialog.show()
    }
    
    private fun updateSubmitButton() {
        val allApproved = photoStatus.all { it.isChecked && it.isApproved }
        if (allApproved) {
            val final = validateSamePersonAcrossSet()
            submitButton.isEnabled = final.ok
            submitButton.alpha = if (final.ok) 1f else 0.5f
            if (final.ok) {
                statusText.text = Texts.Review.photoApproved()
                statusText.setTextColor(0xFF00AA00.toInt())
            } else {
                statusText.text = Texts.Review.photoRejected(final.reason)
                statusText.setTextColor(0xFFFF0000.toInt())
            }
        } else {
            submitButton.isEnabled = false
            submitButton.alpha = 0.5f
            val checkedCount = photoStatus.count { it.isChecked }
            val approvedCount = photoStatus.count { it.isApproved }
            statusText.text = Texts.Review.reviewProgress(checkedCount, approvedCount)
            statusText.setTextColor(0xFF000000.toInt())
        }
    }
    
    private fun promptUserInfoAndUpload() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_user_info, null)
        val nameInput = view.findViewById<EditText>(R.id.inputName).apply {
            hint = Texts.Review.nameHint()
        }
        val surnameInput = view.findViewById<EditText>(R.id.inputSurname).apply {
            hint = Texts.Review.surnameHint()
        }
        val phoneInput = view.findViewById<EditText>(R.id.inputPhone).apply {
            hint = Texts.Review.phoneHint()
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle(Texts.Review.infoDialogTitle())
            .setView(view)
            .setPositiveButton(Texts.Review.infoDialogConfirm()) { dialog, _ ->
                val name = nameInput.text?.toString()?.trim().orEmpty()
                val surname = surnameInput.text?.toString()?.trim().orEmpty()
                val phone = phoneInput.text?.toString()?.trim().orEmpty()
                if (name.isEmpty() || surname.isEmpty() || phone.isEmpty()) {
                    statusText.text = Texts.Review.missingInfo()
                    statusText.setTextColor(0xFFFF0000.toInt())
                    return@setPositiveButton
                }
                dialog.dismiss()
                uploadSetToFirebase(name, surname, phone)
            }
            .setNegativeButton(Texts.Review.infoDialogCancel(), null)
            .show()
    }
    
    private fun uploadSetToFirebase(name: String, surname: String, phone: String) {
        lastSubmissionInfo = SubmissionInfo(name, surname, phone)
        retryUploadButton.visibility = View.GONE
        statusText.text = Texts.Review.preparingPhotos()
        statusText.setTextColor(0xFF000000.toInt())
        submitButton.isEnabled = false
        submitButton.text = Texts.Review.loadingButton()
        uploadProgressContainer.visibility = android.view.View.VISIBLE
        updateUploadProgress(progress = 0, statusMessage = Texts.Review.preparingPhotos())
        
        lifecycleScope.launch {
            try {
                Log.d("ReviewActivity", "Starting API upload")
                Log.d("ReviewActivity", "Photo paths count: ${photoPaths?.size}")
                
                if (photoPaths == null || photoPaths!!.isEmpty()) {
                    throw Exception("Fotoğraf yolları bulunamadı")
                }
                
                // Fotoğraf sayısı kontrolü - tam 5 fotoğraf olmalı
                if (photoPaths!!.size != 5) {
                    throw Exception("Tam 5 fotoğraf gerekli, bulunan: ${photoPaths!!.size}")
                }
                
                // Fotoğraf tipleri (angleNames'e göre)
                val photoTypes = listOf("front", "side_right", "side_left", "top", "back")
                
                // Fotoğrafları base64'e çevir ve PhotoData listesi oluştur
                withContext(Dispatchers.Main) {
                    statusText.text = Texts.Review.uploadProcessing()
                }
                
                val photos = mutableListOf<PhotoData>()
                val totalPhotos = photoPaths!!.size
                photoPaths!!.forEachIndexed { idx, path ->
                    val photoFile = File(path)
                    if (!photoFile.exists()) {
                        Log.e("ReviewActivity", "Photo file not found: $path")
                        throw Exception("Fotoğraf ${idx + 1} bulunamadı: $path")
                    }
                    
                    // Progress bar güncelle (optimize etme aşaması: %0-60)
                    val optimizeProgress = (idx * 60 / totalPhotos)
                    withContext(Dispatchers.Main) {
                        val message = Texts.Review.processingPhoto(idx + 1)
                        statusText.text = message
                        updateUploadProgress(progress = optimizeProgress, statusMessage = message)
                    }
                    
                    // Fotoğrafı optimize et ve base64'e çevir
                    val optimizedBytes = withContext(Dispatchers.IO) {
                        try {
                            // Orijinal bitmap'i yükle
                            var originalBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                            if (originalBitmap == null) {
                                throw Exception("Fotoğraf yüklenemedi")
                            }
                            
                            val photoType = photoTypes.getOrNull(idx) ?: "unknown"
                            
                            // Back fotoğrafını 180° döndür
                            if (photoType == "back") {
                                val matrix = android.graphics.Matrix().apply {
                                    postRotate(180f)
                                }
                                val rotatedBitmap = Bitmap.createBitmap(
                                    originalBitmap, 
                                    0, 0, 
                                    originalBitmap.width, 
                                    originalBitmap.height, 
                                    matrix, 
                                    true
                                )
                                originalBitmap.recycle()
                                originalBitmap = rotatedBitmap
                                Log.d("ReviewActivity", "Back fotoğrafı 180° döndürüldü")
                            }
                            
                            // Fotoğrafı optimize et: maksimum 1280x1280 boyutunda, kalite %75 (daha küçük dosya)
                            val maxDimension = 1280
                            val width = originalBitmap.width
                            val height = originalBitmap.height
                            
                            val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                                val scale = minOf(
                                    maxDimension.toFloat() / width,
                                    maxDimension.toFloat() / height
                                )
                                val newWidth = (width * scale).toInt()
                                val newHeight = (height * scale).toInt()
                                Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
                            } else {
                                originalBitmap
                            }
                            
                            // JPEG olarak sıkıştır (kalite %75 - daha küçük dosya boyutu)
                            val outputStream = java.io.ByteArrayOutputStream()
                            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                            val compressedBytes = outputStream.toByteArray()
                            outputStream.close()
                            
                            // Orijinal bitmap'i temizle
                            if (scaledBitmap != originalBitmap) {
                                scaledBitmap.recycle()
                            }
                            originalBitmap.recycle()
                            
                            Log.d("ReviewActivity", "Photo ${idx + 1} optimized: ${compressedBytes.size / 1024}KB")
                            compressedBytes
                        } catch (e: Exception) {
                            Log.e("ReviewActivity", "Photo optimization failed for ${idx + 1}", e)
                            // Optimizasyon başarısız olursa orijinal dosyayı kullan
                            val inputStream = java.io.FileInputStream(photoFile)
                            val bytes = inputStream.readBytes()
                            inputStream.close()
                            bytes
                        }
                    }
                    
                    // Base64 encoding
                    val base64String = android.util.Base64.encodeToString(optimizedBytes, android.util.Base64.NO_WRAP)
                    
                    // Data URL formatı: "data:image/jpeg;base64,{base64_string}"
                    val dataUrl = "data:image/jpeg;base64,$base64String"
                    
                    val photoType = photoTypes.getOrNull(idx) ?: "unknown"
                    photos.add(PhotoData(
                        photo_url = dataUrl,
                        photo_type = photoType
                    ))
                    
                    Log.d("ReviewActivity", "Photo ${idx + 1} converted to base64, type: $photoType, size: ${dataUrl.length / 1024}KB")
                    
                    // Progress bar güncelle (base64 aşaması: %60-80)
                    val base64Progress = 60 + ((idx + 1) * 20 / totalPhotos)
                    withContext(Dispatchers.Main) {
                        updateUploadProgress(progress = base64Progress, statusMessage = null)
                    }
                }
                
                // PatientData oluştur
                val patientData = PatientData(
                    name = name,
                    surname = surname,
                    phone_number = phone,
                    photos = photos
                )
                
                // Diagnostic: JSON formatını logla
                DiagnosticHelper.logPatientData(patientData)
                
                currentLocation?.let { location ->
                    Log.d("ReviewActivity", "Location available: ${location.latitude}, ${location.longitude}")
                } ?: run {
                    Log.w("ReviewActivity", "Location not available")
                }
                
                // Toplam veri boyutunu hesapla
                val totalDataSize = photos.sumOf { it.photo_url.length } / 1024 / 1024 // MB cinsinden
                Log.d("ReviewActivity", "=== API REQUEST DETAILS ===")
                Log.d("ReviewActivity", "Endpoint: ${RetrofitClient.getBaseUrl()}/api/capture-sets")
                Log.d("ReviewActivity", "Name: $name")
                Log.d("ReviewActivity", "Surname: $surname")
                Log.d("ReviewActivity", "Phone: $phone")
                Log.d("ReviewActivity", "Photo count: ${photos.size}")
                Log.d("ReviewActivity", "Total data size: ${totalDataSize}MB")
                photos.forEachIndexed { idx, photo ->
                    Log.d("ReviewActivity", "Photo $idx: type=${photo.photo_type}, size=${photo.photo_url.length / 1024}KB")
                }
                Log.d("ReviewActivity", "PatientData JSON will be sent to API")
                
                // API'ye gönder (retry mekanizması ile)
                withContext(Dispatchers.Main) {
                    statusText.text = Texts.Review.sendingToApi(totalDataSize.toString())
                    updateUploadProgress(progress = 80, statusMessage = Texts.Review.apiInProgress())
                }
                
                Log.d("ReviewActivity", "=== API İSTEĞİ GÖNDERİLİYOR ===")
                Log.d("ReviewActivity", "Base URL: ${RetrofitClient.getBaseUrl()}")
                Log.d("ReviewActivity", "Endpoint: ${RetrofitClient.getBaseUrl()}/api/capture-sets")
                Log.d("ReviewActivity", "Request başlatılıyor...")
                
                var response: ApiResponse? = null
                var lastError: Exception? = null
                val maxRetries = 3 // 3 deneme yap
                
                for (attempt in 1..maxRetries) {
                    val startTime = System.currentTimeMillis()
                    try {
                        Log.d("ReviewActivity", "--- Deneme $attempt/$maxRetries ---")
                        
                        withContext(Dispatchers.Main) {
                            if (attempt > 1) {
                                val retryMessage = Texts.Review.retrying(attempt, maxRetries)
                                statusText.text = retryMessage
                                updateUploadProgress(progress = 80, statusMessage = retryMessage)
                            } else {
                                val message = Texts.Review.apiInProgress()
                                statusText.text = message
                                updateUploadProgress(progress = 80, statusMessage = message)
                            }
                        }
                        
                        Log.d("ReviewActivity", "Retrofit çağrısı yapılıyor...")
                        
                        response = withContext(Dispatchers.IO) {
                            try {
                                Log.d("ReviewActivity", "IO thread'de API çağrısı başlatıldı")
                                val result = RetrofitClient.apiService.createCaptureSet(patientData)
                                val duration = System.currentTimeMillis() - startTime
                                Log.d("ReviewActivity", "✅ API çağrısı başarılı! Süre: ${duration}ms")
                                result
                            } catch (e: Exception) {
                                val duration = System.currentTimeMillis() - startTime
                                Log.e("ReviewActivity", "❌ IO thread'de exception: ${e.javaClass.simpleName}", e)
                                Log.e("ReviewActivity", "Exception mesajı: ${e.message}")
                                Log.e("ReviewActivity", "Exception stack trace:")
                                e.printStackTrace()
                                throw e
                            }
                        }
                        
                        Log.d("ReviewActivity", "✅ Response alındı: ${response?.set_id}")
                        withContext(Dispatchers.Main) {
                            updateUploadProgress(progress = 100, statusMessage = Texts.Review.uploadSuccess())
                        }
                        break // Başarılı oldu, döngüden çık
                        
                    } catch (e: java.net.SocketTimeoutException) {
                        val duration = System.currentTimeMillis() - startTime
                        Log.e("ReviewActivity", "⏱️ SocketTimeoutException!")
                        Log.e("ReviewActivity", "Timeout süresi: ${duration}ms")
                        Log.e("ReviewActivity", "Exception detayı: ${e.message}")
                        e.printStackTrace()
                        lastError = Exception("Bağlantı zaman aşımı (Deneme $attempt/$maxRetries, ${duration}ms). Fotoğraflar çok büyük olabilir veya sunucu yanıt vermiyor.")
                        if (attempt < maxRetries) {
                            Log.w("ReviewActivity", "⏳ Timeout, retrying... ($attempt/$maxRetries)")
                            delay(2000) // 2 saniye bekle
                            continue
                        }
                    } catch (e: java.net.ConnectException) {
                        Log.e("ReviewActivity", "🔌 ConnectException!")
                        Log.e("ReviewActivity", "Exception mesajı: ${e.message}")
                        Log.e("ReviewActivity", "Sunucu adresi: ${RetrofitClient.getBaseUrl()}")
                        e.printStackTrace()
                        lastError = Exception("Sunucuya bağlanılamadı. API sunucusunun çalıştığından emin olun: ${RetrofitClient.getBaseUrl()}")
                        break // Bağlantı hatası için retry yapma
                    } catch (e: java.net.UnknownHostException) {
                        Log.e("ReviewActivity", "🌐 UnknownHostException!")
                        Log.e("ReviewActivity", "Host bulunamadı: ${e.message}")
                        e.printStackTrace()
                        lastError = Exception("Sunucu adresi bulunamadı: ${RetrofitClient.getBaseUrl()}")
                        break
                    } catch (e: java.io.IOException) {
                        Log.e("ReviewActivity", "📡 IOException!")
                        Log.e("ReviewActivity", "Exception mesajı: ${e.message}")
                        Log.e("ReviewActivity", "Exception tipi: ${e.javaClass.name}")
                        e.printStackTrace()
                        lastError = Exception("Ağ hatası: ${e.message}. İnternet bağlantınızı kontrol edin.")
                        if (attempt < maxRetries) {
                            Log.w("ReviewActivity", "🔄 IO error, retrying... ($attempt/$maxRetries)")
                            delay(2000)
                            continue
                        }
                    } catch (e: retrofit2.HttpException) {
                        val errorBody = try {
                            e.response()?.errorBody()?.string()
                        } catch (ex: Exception) {
                            "Error body okunamadı: ${ex.message}"
                        }
                        Log.e("ReviewActivity", "🚫 HTTP ERROR ===")
                        Log.e("ReviewActivity", "Status Code: ${e.code()}")
                        Log.e("ReviewActivity", "Error Body: $errorBody")
                        Log.e("ReviewActivity", "Error Message: ${e.message()}")
                        e.printStackTrace()
                        lastError = Exception("Sunucu hatası (${e.code()}): ${errorBody ?: e.message()}")
                        break // HTTP hatası için retry yapma
                    } catch (e: Exception) {
                        Log.e("ReviewActivity", "❌ Beklenmeyen Exception!")
                        Log.e("ReviewActivity", "Exception tipi: ${e.javaClass.name}")
                        Log.e("ReviewActivity", "Exception mesajı: ${e.message}")
                        Log.e("ReviewActivity", "Full stack trace:")
                        e.printStackTrace()
                        lastError = Exception("Beklenmeyen hata: ${e.message ?: e.javaClass.simpleName}")
                        break
                    }
                }
                
                Log.d("ReviewActivity", "=== API İSTEĞİ TAMAMLANDI ===")
                Log.d("ReviewActivity", "Response: ${if (response != null) "Başarılı" else "Başarısız"}")
                Log.d("ReviewActivity", "Last Error: ${lastError?.message}")
                
                val finalResponse = response ?: throw (lastError ?: Exception("Bilinmeyen hata"))
                
                // Diagnostic: Response JSON formatını logla
                DiagnosticHelper.logApiResponse(finalResponse)
                
                Log.d("ReviewActivity", "=== API RESPONSE ===")
                Log.d("ReviewActivity", "Message: ${finalResponse.message}")
                Log.d("ReviewActivity", "Set ID: ${finalResponse.set_id}")
                Log.d("ReviewActivity", "Response is null: ${finalResponse.set_id == null}")
                
                if (finalResponse.set_id != null) {
                    // Kayıt Başarılı
                    Log.d("ReviewActivity", "Successfully saved! New ID: ${finalResponse.set_id}")
                    withContext(Dispatchers.Main) {
                        statusText.text = Texts.Review.uploadComplete(finalResponse.set_id.toString())
                        statusText.setTextColor(0xFF00AA00.toInt())
                        submitButton.text = Texts.Review.uploadButton()
                        submitButton.isEnabled = false
                        uploadProgressContainer.visibility = android.view.View.GONE
                        retryUploadButton.visibility = View.GONE
                    }
                    lastSubmissionInfo = null
                } else {
                    // API'den Hata Geldi
                    Log.e("ReviewActivity", "API Error: ${finalResponse.message}")
                    throw Exception("Kayıt başarısız: ${finalResponse.message}")
                }
            } catch (e: Exception) {
                Log.e("ReviewActivity", "Upload failed", e)
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    uploadProgressContainer.visibility = android.view.View.GONE
                    val errorMessage = when {
                        e.message?.contains("timeout", ignoreCase = true) == true -> 
                            Texts.Review.errorTimeout()
                        e.message?.contains("connect", ignoreCase = true) == true -> 
                            Texts.Review.errorConnect()
                        e.message?.contains("network", ignoreCase = true) == true -> 
                            Texts.Review.errorNetwork()
                        else -> Texts.Review.errorUnknown(e.message ?: "Unknown error")
                    }
                    statusText.text = errorMessage
                    statusText.setTextColor(0xFFFF0000.toInt())
                    submitButton.text = Texts.Review.uploadButton()
                    submitButton.isEnabled = true
                    uploadProgressContainer.visibility = android.view.View.GONE
                    retryUploadButton.visibility = if (lastSubmissionInfo != null) View.VISIBLE else View.GONE
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        detector.close()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).roundToInt()
}