package com.smilehair.selfcapture

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.face.Face
import kotlin.math.min

class FaceOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var faces: List<Face> = emptyList()
    private var targetRect: RectF? = null
    private var isPositionValid = false
    private var currentAngle: CaptureAngle = CaptureAngle.FRONT
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0
    private val overlayVerticalScale = 0.82f
    
    // Paint nesneleri
    private val darkOverlayPaint = Paint().apply {
        color = Color.argb(180, 10, 12, 28) // Karanlık overlay
        isAntiAlias = true
    }
    
    private val validPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
        alpha = 255
    }
    
    private val invalidPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
        alpha = 200
    }
    
    private val checkmarkPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 10f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }
    
    private val cutoutBorderPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
        color = Color.argb(220, 255, 255, 255)
    }
    
    private val glowPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 16f
        isAntiAlias = true
        color = Color.argb(90, 0, 200, 255)
    }
    
    // Erase mode için paint (yüz bölgesini aydınlatmak için)
    private val erasePaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        isAntiAlias = true
    }

    fun updateFaces(
        faces: List<Face>, 
        targetRect: RectF?, 
        isValid: Boolean, 
        angle: CaptureAngle,
        imageWidth: Int = 0,
        imageHeight: Int = 0
    ) {
        this.faces = faces
        this.targetRect = targetRect
        this.isPositionValid = isValid
        this.currentAngle = angle
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        
        if (viewWidth <= 0 || viewHeight <= 0) return
        
        // Scale faktörlerini hesapla (bitmap koordinatlarından view koordinatlarına)
        val scaleX = if (imageWidth > 0) viewWidth / imageWidth else 1f
        val scaleY = if (imageHeight > 0) viewHeight / imageHeight else 1f
        
        // Hedef alanı hesapla
        val scaledTargetRect = targetRect?.let { rect ->
            RectF(
                rect.left * scaleX,
                rect.top * scaleY,
                rect.right * scaleX,
                rect.bottom * scaleY
            )
        }
        
        // Template için oval path çalışması
        val cutoutOval = scaledTargetRect?.let { rect ->
            val centerY = rect.centerY()
            val halfHeight = rect.height() / 2f * overlayVerticalScale
            RectF(
                rect.left,
                centerY - halfHeight,
                rect.right,
                centerY + halfHeight
            )
        }
        
        // Karanlık overlay çiz - oval alanı kes
        val overlayPath = android.graphics.Path().apply {
            addRect(0f, 0f, viewWidth, viewHeight, android.graphics.Path.Direction.CW)
            cutoutOval?.let { addOval(it, android.graphics.Path.Direction.CCW) }
        }
        canvas.drawPath(overlayPath, darkOverlayPaint)
        
        // Oval rehber çizgileri
        cutoutOval?.let { oval ->
            if (isPositionValid) {
                canvas.drawOval(oval, glowPaint)
                canvas.drawOval(oval, validPaint)
            } else {
                canvas.drawOval(oval, cutoutBorderPaint)
            }
        }
        
        // Yüz algılandıysa çiz (üstte görünsün)
        faces.forEach { face ->
            val boundingBox = face.boundingBox
            val faceRect = RectF(
                boundingBox.left * scaleX,
                boundingBox.top * scaleY,
                boundingBox.right * scaleX,
                boundingBox.bottom * scaleY
            )
            
            val paint = if (isPositionValid) validPaint else invalidPaint
            canvas.drawRoundRect(faceRect, 32f, 32f, paint)
            
            // Pozisyon doğruysa yeşil checkmark göster
            if (isPositionValid) {
                drawCheckmark(canvas, faceRect)
            }
        }
        
        // Merkez çizgileri (rehber için) - sadece hedef alan içinde
        if (!isPositionValid && scaledTargetRect != null) {
            val centerGuidePaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 2f
                alpha = 80
                isAntiAlias = true
            }
            val centerX = viewWidth / 2
            val centerY = viewHeight / 2
            canvas.drawLine(centerX, centerY - viewHeight * 0.15f, centerX, centerY + viewHeight * 0.15f, centerGuidePaint)
            canvas.drawLine(centerX - viewWidth * 0.15f, centerY, centerX + viewWidth * 0.15f, centerY, centerGuidePaint)
        }
    }
    
    private fun drawCheckmark(canvas: Canvas, rect: RectF) {
        val centerX = rect.centerX()
        val centerY = rect.centerY()
        val size = min(rect.width(), rect.height()) * 0.25f
        
        val path = android.graphics.Path().apply {
            moveTo(centerX - size * 0.5f, centerY)
            lineTo(centerX - size * 0.1f, centerY + size * 0.3f)
            lineTo(centerX + size * 0.5f, centerY - size * 0.2f)
        }
        
        canvas.drawPath(path, checkmarkPaint)
        
        // Yeşil ışık efekti (daire)
        val glowPaint = Paint().apply {
            color = Color.argb(100, 0, 255, 0)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(centerX, centerY, size * 0.8f, glowPaint)
    }
}