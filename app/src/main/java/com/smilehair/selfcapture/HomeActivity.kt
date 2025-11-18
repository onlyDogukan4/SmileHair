package com.smilehair.selfcapture

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.tabs.TabLayout
import com.smilehair.selfcapture.localization.AppLanguage
import com.smilehair.selfcapture.localization.LanguageManager
import com.smilehair.selfcapture.localization.Texts

class HomeActivity : AppCompatActivity() {
    
    private lateinit var tabLayout: TabLayout
    private lateinit var contentContainer: LinearLayout
    private lateinit var startCaptureButton: MaterialButton
    private lateinit var languageToggleGroup: MaterialButtonToggleGroup
    private lateinit var languageTrButton: MaterialButton
    private lateinit var languageEnButton: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageManager.init(applicationContext)
        super.onCreate(savedInstanceState)
        
        // Status bar'ı lacivert yap (tema ile uyumlu)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = 0xFF1A237E.toInt() // Lacivert tema rengi
        }
        
        // Status bar içeriğini beyaz yap (lacivert arka plan için)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and 
                android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        
        setContentView(R.layout.activity_home)
        
        tabLayout = findViewById(R.id.tabLayout)
        contentContainer = findViewById(R.id.contentContainer)
        startCaptureButton = findViewById(R.id.startCaptureButton)
        languageToggleGroup = findViewById(R.id.languageToggleGroup)
        languageTrButton = findViewById(R.id.buttonLanguageTr)
        languageEnButton = findViewById(R.id.buttonLanguageEn)
        
        startCaptureButton.text = Texts.Home.startCaptureButton()
        
        val currentLang = LanguageManager.currentLanguage
        languageToggleGroup.check(
            if (currentLang == AppLanguage.EN) R.id.buttonLanguageEn else R.id.buttonLanguageTr
        )
        
        languageToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val newLanguage = if (checkedId == R.id.buttonLanguageEn) AppLanguage.EN else AppLanguage.TR
            if (newLanguage != LanguageManager.currentLanguage) {
                LanguageManager.setLanguage(this, newLanguage)
                recreate()
            }
        }
        
        // Logo'yu yükle
        val logoImageView = findViewById<ImageView>(R.id.logoImageView)
        val logoResourceId = resources.getIdentifier("smile_hair_logo", "drawable", packageName)
        if (logoResourceId != 0) {
            logoImageView.setImageResource(logoResourceId)
        } else {
            // Logo bulunamazsa placeholder göster
            logoImageView.setBackgroundColor(0xFF0D47A1.toInt())
        }
        
        // Tab'ları oluştur
        tabLayout.addTab(tabLayout.newTab().setText(Texts.Home.analyzeTabTitle()))
        tabLayout.addTab(tabLayout.newTab().setText(Texts.Home.aboutTabTitle()))
        
        // İlk tab'ı göster
        showCaptureTab()
        
        // Tab değişim listener'ı
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showCaptureTab()
                    1 -> showAboutTab()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        
        // Analiz Ettir butonuna tıklama
        startCaptureButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun showCaptureTab() {
        contentContainer.removeAllViews()
        
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }
        
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(0xFFF5F5F5.toInt())
        }
        
        // Başlık
        val titleText = TextView(this).apply {
            text = Texts.Home.captureTitle()
            textSize = 24f
            setTextColor(0xFF1A237E.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }
        
        // Açıklama
        val descriptionText = TextView(this).apply {
            text = Texts.Home.captureDescription()
            textSize = 16f
            setTextColor(0xFF424242.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8 }
        }
        
        // Dikkat edilmesi gerekenler başlığı
        val attentionHeader = TextView(this).apply {
            text = Texts.Home.attentionTitle()
            textSize = 18f
            setTextColor(0xFF1A237E.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }
        
        content.addView(titleText)
        content.addView(descriptionText)
        content.addView(attentionHeader)
        
        // 5 Fotoğraf ve Açıklamaları
        val photoSteps = listOf(
            Pair("front_photo", Texts.Home.stepDescription(0)),
            Pair("right_side_photo", Texts.Home.stepDescription(1)),
            Pair("left_side_photo", Texts.Home.stepDescription(2)),
            Pair("top_side_photo", Texts.Home.stepDescription(3)),
            Pair("back_side_photo", Texts.Home.stepDescription(4))
        )
        
        photoSteps.forEachIndexed { index, (imageName, description) ->
            val stepCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { 
                    bottomMargin = 20
                }
                setBackgroundColor(0xFFFFFFFF.toInt())
                elevation = 2f
            }
            
            val stepImage = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { 
                    bottomMargin = 12
                }
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.FIT_CENTER  // Tam görünür yapmak için
                maxHeight = 400  // Maksimum yükseklik
                val resourceId = resources.getIdentifier(imageName, "drawable", packageName)
                if (resourceId != 0) {
                    setImageResource(resourceId)
                } else {
                    setBackgroundColor(0xFFE0E0E0.toInt())
                }
            }
            
            val stepDescription = TextView(this).apply {
                text = description
                textSize = 14f
                setTextColor(0xFF424242.toInt())
                setLineSpacing(2f, 1f)
            }.apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            
            stepCard.addView(stepImage)
            stepCard.addView(stepDescription)
            content.addView(stepCard)
        }
        scrollView.addView(content)
        contentContainer.addView(scrollView)
    }
    
    private fun showAboutTab() {
        contentContainer.removeAllViews()
        
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }
        
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(0xFFF5F5F5.toInt())
        }
        
        // Smile Hair Clinic Tanıtım Kartı
        val clinicIntroCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { 
                bottomMargin = 32
            }
            setBackgroundColor(0xFFFFFFFF.toInt())
            elevation = 4f
        }
        
        val clinicTitle = TextView(this).apply {
            text = Texts.Home.clinicTitle()
            textSize = 24f
            setTextColor(0xFF1A237E.toInt()) // Lacivert
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }
        
        val clinicSubtitle = TextView(this).apply {
            text = Texts.Home.clinicSubtitle()
            textSize = 18f
            setTextColor(0xFF1A237E.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }
        
        val clinicInfo = TextView(this).apply {
            text = Texts.Home.clinicInfo()
            textSize = 14f
            setTextColor(0xFF424242.toInt())
            setLineSpacing(4f, 1f)
        }.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        clinicIntroCard.addView(clinicTitle)
        clinicIntroCard.addView(clinicSubtitle)
        clinicIntroCard.addView(clinicInfo)
        
        content.addView(clinicIntroCard)
        
        // Doktorlar Başlığı
        val doctorsHeader = TextView(this).apply {
            text = Texts.Home.doctorsHeader()
            textSize = 22f
            setTextColor(0xFF1A237E.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { 
                bottomMargin = 16
                topMargin = 8
            }
        }
        
        content.addView(doctorsHeader)
        
        // Dr. Gökay Bilgin
        content.addView(createDoctorCard(
            "gokay_bilgin",
            "Dr. Gökay Bilgin",
            Texts.Home.doctorTitle("gokay_bilgin"),
            Texts.Home.doctorDescription("gokay_bilgin")
        ))
        
        // Dr. Mehmet Erdoğan
        content.addView(createDoctorCard(
            "mehmet_erdogan",
            "Dr. Mehmet Erdoğan",
            Texts.Home.doctorTitle("mehmet_erdogan"),
            Texts.Home.doctorDescription("mehmet_erdogan")
        ))
        
        // Dr. Firdavs Ahmedov
        content.addView(createDoctorCard(
            "firdavs_ahmedov",
            "Dr. Firdavs Ahmedov",
            Texts.Home.doctorTitle("firdavs_ahmedov"),
            Texts.Home.doctorDescription("firdavs_ahmedov")
        ))
        
        // Dr. Ali Osman SOLUK
        content.addView(createDoctorCard(
            "ali_osman_soluk",
            "Dr. Ali Osman SOLUK",
            Texts.Home.doctorTitle("ali_osman_soluk"),
            Texts.Home.doctorDescription("ali_osman_soluk")
        ))
        
        // Dr. M. Reşat Arpacı
        content.addView(createDoctorCard(
            "resat_arpaci",
            "Dr. M. Reşat Arpacı",
            Texts.Home.doctorTitle("resat_arpaci"),
            Texts.Home.doctorDescription("resat_arpaci")
        ))
        
        // Footer - Sosyal Medya (En altta, doktorlar bitince)
        val footer = createSocialMediaFooter()
        content.addView(footer)
        
        scrollView.addView(content)
        contentContainer.addView(scrollView)
    }
    
    private fun createDoctorCard(imageResourceName: String, name: String, title: String, info: String): LinearLayout {
        // Doktor kartı container
        val doctorCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { 
                bottomMargin = 24
            }
            setBackgroundColor(0xFFFFFFFF.toInt())
            elevation = 4f
        }
        
        // Doktor fotoğrafı - Tam görünür olacak şekilde, geniş alan
        val doctorImage = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { 
                bottomMargin = 16
            }
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER  // Tam görünür yapmak için
            maxHeight = 800  // Daha geniş alan için maksimum yükseklik artırıldı
            // Doktor fotoğrafı için drawable resource
            val resourceId = resources.getIdentifier(imageResourceName, "drawable", packageName)
            if (resourceId != 0) {
                setImageResource(resourceId)
            } else {
                setBackgroundColor(0xFFE0E0E0.toInt())
            }
        }
        
        // Doktor adı ve unvan
        val doctorName = TextView(this).apply {
            text = name
            textSize = 22f
            setTextColor(0xFF1A237E.toInt()) // Lacivert
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8 }
        }
        
        val doctorTitle = TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(0xFF424242.toInt())
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }
        
        // Doktor bilgileri
        val doctorInfo = TextView(this).apply {
            text = info
            textSize = 14f
            setTextColor(0xFF424242.toInt())
            setLineSpacing(4f, 1f)
        }.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        doctorCard.addView(doctorImage)
        doctorCard.addView(doctorName)
        doctorCard.addView(doctorTitle)
        doctorCard.addView(doctorInfo)
        
        return doctorCard
    }
    
    private fun createSocialMediaFooter(): LinearLayout {
        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 32)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 40
            }
            setBackgroundColor(0xFF1A237E.toInt()) // Lacivert tema
        }
        
        val footerTitle = TextView(this).apply {
            text = Texts.Home.footerTitle()
            textSize = 20f
            setTextColor(0xFFFFFFFF.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
        }
        
        val socialMediaContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val socialMediaLinks = listOf(
            Pair("https://www.facebook.com/smilehairclinic", "ic_facebook"),
            Pair("https://www.instagram.com/smilehairclinic/", "ic_instagram"),
            Pair("https://www.youtube.com/@SmileHairClinic", "ic_youtube"),
            Pair("https://www.tiktok.com/@smilehairclinic", "ic_tiktok")
        )
        
        socialMediaLinks.forEach { (url, iconName) ->
            val buttonContainer = FrameLayout(this).apply {
                val size = dp(56)
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    val margin = dp(8)
                    setMargins(margin, margin, margin, margin)
                }
                background = ContextCompat.getDrawable(this@HomeActivity, R.drawable.social_icon_circle_bg)
                clipToOutline = true
                isClickable = true
                isFocusable = true
                setOnClickListener { openSocialMediaUrl(url) }
            }
            
            val iconView = ImageView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                val padding = dp(6)
                setPadding(padding, padding, padding, padding)
                scaleType = ImageView.ScaleType.FIT_CENTER
                
                val resourceId = resources.getIdentifier(iconName, "drawable", packageName)
                if (resourceId != 0) {
                    setImageResource(resourceId)
                } else {
                    setBackgroundColor(0xFF0D47A1.toInt())
                }
            }
            
            buttonContainer.addView(iconView)
            socialMediaContainer.addView(buttonContainer)
        }
        
        footer.addView(footerTitle)
        footer.addView(socialMediaContainer)
        
        return footer
    }
    
    private fun openSocialMediaUrl(url: String) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "${Texts.Home.linkError()}: $url", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}

