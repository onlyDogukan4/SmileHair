package com.smilehair.selfcapture.localization

object Texts {

    private fun l(tr: String, en: String) = LocalizedText(tr, en)

    object Home {
        private val analyzeTab = l("Analiz Ettir", "Capture Guide")
        private val aboutTab = l("Biz Kimiz", "About Us")
        private val startCapture = l("Analiz Yap", "Start Capture")
        private val captureTitle = l("Saç Ekimi Analizi", "Hair Transplant Analysis")
        private val captureDescription = l(
            "5 farklı açıdan fotoğraf çekerek saç ekimi analizi için gerekli görüntüleri oluşturun.",
            "Capture five guided angles to create the visuals required for your hair transplant analysis."
        )
        private val attentionTitle = l("Dikkat edilmesi gerekenler:", "Important notes:")

        private val stepDescriptions = listOf(
            l(
                "1. Tam Yüz Karşıdan\nYüzünüzü dairesel şablonda ortalayın ve göz hizasında tutun. Yüz algılanınca otomatik çekim yapılacak.",
                "1. Full Face Front\nCenter your face inside the circular guide at eye level. The photo will be taken automatically once detected."
            ),
            l(
                "2. 45° Sağa (Kendi Sağınız)\nKendi sağınıza doğru 45° dönün ve başınız sabitken şablonu doldurun. Yüz algılanınca otomatik çekim yapılacak.",
                "2. 45° Right (Your Right)\nTurn 45° toward your right, keep your head steady inside the guide. The capture will start automatically."
            ),
            l(
                "3. 45° Sola (Kendi Solunuz)\nKendi solunuza doğru 45° dönün ve yüzünüz şablonun içinde kalsın. Yüz algılanınca otomatik çekim yapılacak.",
                "3. 45° Left (Your Left)\nTurn 45° toward your left and keep your face within the guide. The capture will start automatically."
            ),
            l(
                "4. Tepe Kısmı\nTelefonu yatay tutup kafanızın tepesine getirin. Telefonu başınızın üzerinde tutun ve saç bölgesini görüntüleyin. Saç algılanınca otomatik çekim yapılacak.",
                "4. Top View\nHold the phone horizontally above your head to show the crown. The photo will be taken automatically once hair texture is detected."
            ),
            l(
                "5. Arka Donör\nTelefonu ters dik tutup kafanızın arkasına getirin. Telefonu başınızın arkasında tutun ve arka saç bölgesini görüntüleyin. Saç algılanınca otomatik çekim yapılacak.",
                "5. Back Donor Area\nHold the phone vertically behind your head to show the donor area. The capture will start automatically once your hair is detected."
            )
        )

        private val clinicTitle = l("Smile Hair Clinic Nedir?", "What is Smile Hair Clinic?")
        private val clinicSubtitle = l("İki Mükemmeliyetçinin Gelişim Yolculuğu!", "The Growth Journey of Two Perfectionists!")
        private val clinicDescription = l(
            """
Her şey Dr. M. Erdoğan ve Dr. G. Bilgin'in saç ekiminde ilerleme tutkusu ile başladı. Yılların tecrübesini FUE saç ekimi tekniklerine adapte ederek 2018 yılında Smile Hair Clinic'i kurdular ve medikal estetikte farklarını ortaya koydular.

Bölgedeki sınıfının en iyisi olarak tıbbi saç ekimi klinikleri için küresel bir ölçüt olan Smile Hair Clinic, BBC Sağlık Turizmi Belgesel Dizisi dahil olmak üzere birçok otorite tarafından da takdir görmüştür.

Doğu İstanbul'un finans bölgesine yeniden konumlandırılan Smile Hair Clinic, şimdi 100'den fazla iyi eğitimli personelin çalıştığı 7 katlı, sofistike tasarımlı, yüksek teknolojili klinik binasında faaliyet göstermektedir. Kalitesi ve mükemmelliği, yoğun eğitim prosedürlerine bağlıdır; operasyonlarda görev alan her ekip üyesi TrueTM Philosophy kriterlerine göre test edilir.

Doktorlar tarafından kuruldu, doktorlar tarafından yönetiliyor!

Kurucuların önceliği üç bileşenden oluşur: Estetik, operasyon konforu ve uzun vadeli dönüşüm. Smile Hair Clinic saç ekimini bütüncül bir şekilde ele alır; bu sadece tıbbi bir operasyon değil, hastanın hayatında yeni bir sayfa açan kapsamlı bir deneyimdir.

10.000'den fazla hastanın dönüşüm yolculuğunda Smile Hair Clinic, tutkulu ve mükemmeliyetçi insanların buluşma noktasıdır. Araştırma, inovasyon ve sürekli iyileştirme, nihai mükemmeliyetçilik vizyonunu besler.
            """.trimIndent(),
            """
Everything began with Dr. M. Erdoğan and Dr. G. Bilgin’s passion for progress in hair transplantation. They adapted years of experience to advanced FUE techniques, founded Smile Hair Clinic in 2018, and set a new benchmark in medical aesthetics.

Recognized as a best-in-class medical hair clinic and featured by authorities such as the BBC Health Tourism Documentary Series, Smile Hair Clinic has become a global reference point.

Relocated to the financial district on Istanbul’s Anatolian side, Smile Hair now operates in a seven-storey, high-tech facility where more than 100 well-trained professionals work. Quality and excellence are safeguarded through intensive training; every team member involved in procedures is tested against the TrueTM Philosophy criteria.

Founded and led by doctors!

The founders prioritize three pillars: aesthetics, maximum comfort, and long-term transformation. Smile Hair Clinic approaches hair transplantation holistically; it is not only a medical procedure but an end-to-end experience that opens a new chapter in the patient’s life.

Having guided more than 10,000 gentlemen through life-changing journeys, Smile Hair Clinic is the meeting point for passionate perfectionists. Research, innovation, and continuous improvement nurture its vision of ultimate excellence.
            """.trimIndent()
        )

        private val doctorsHeader = l("Doktorlarımız", "Our Doctors")

        private val doctorTitles = mapOf(
            "gokay_bilgin" to l("Saç Ekimi Cerrahı, M.D.\nMedikal Estetik Hekimi", "Hair Transplant Surgeon, M.D.\nMedical Aesthetic Physician"),
            "mehmet_erdogan" to l("Saç Ekimi Cerrahı, M.D.\nMedikal Estetik Hekimi", "Hair Transplant Surgeon, M.D.\nMedical Aesthetic Physician"),
            "firdavs_ahmedov" to l("Saç Ekimi Cerrahı, M.D.\nMedikal Estetik Hekimi", "Hair Transplant Surgeon, M.D.\nMedical Aesthetic Physician"),
            "ali_osman_soluk" to l("Saç Ekimi Cerrahı, M.D.\nMedikal Estetik Hekimi", "Hair Transplant Surgeon, M.D.\nMedical Aesthetic Physician"),
            "resat_arpaci" to l("Saç Ekimi Cerrahı, M.D.\nMedikal Estetik Hekimi", "Hair Transplant Surgeon, M.D.\nMedical Aesthetic Physician")
        )

        private val doctorDescriptions = mapOf(
            "gokay_bilgin" to l(
                """
Dr. Gökay Bilgin, Smile Hair Clinic'in kurucu ortağıdır.

Kariyerinin ilk yıllarına Medical Park Sağlık Grubu'nda başladı. Medikal estetik ve saç ekimi eğitimlerini tamamlayarak uzmanlaştı. Medicana Sağlık Grubuna Saç Ekimi Cerrahı olarak katıldı ve kısa sürede Başhekim Yardımcılığına yükseldi. 4000'den fazla saç ekimi operasyonunu tamamladı.

Hastalarına 360 derecelik bir tedavi yaklaşımı sunarak medikal ve cerrahi planlamayı birleştirir. Mikromotor greft ekstraksiyonu ve Safir FUE tekniklerini kullanmayı tercih eder. Sağlık personelinin eğitimi konusunda tutkuludur ve meslektaşı Dr. Mehmet Erdoğan ile TrueTM Philosophy'yi geliştirmiştir.

Operasyonlarında mizah ve pozitif iletişimi ile tanınır. Fantastik filmlere ilgi duyar, evli ve bir çocuk babasıdır.
                """.trimIndent(),
                """
Dr. Gökay Bilgin is the co-founder of Smile Hair Clinic.

He began his career at Medical Park Health Group, completed his medical aesthetic and hair transplant training, and later joined Medicana Health Group as a hair transplant surgeon, quickly becoming deputy chief physician. He has completed more than 4,000 hair transplant operations.

He believes in providing a 360-degree treatment journey that blends medical and surgical planning. He favors micromotor graft extraction and Sapphire FUE techniques. Passionate about professional education, he co-created the TrueTM Philosophy with Dr. Mehmet Erdoğan.

Known for his sense of humor in the operating room, he enjoys fantastic movies, is married, and has one child.
                """.trimIndent()
            ),
            "mehmet_erdogan" to l(
                """
Dr. Mehmet Erdoğan, Smile Hair Clinic'in kurucu ortağıdır.

Acıbadem Sağlık Grubu'nda tıp doktoru olarak çalıştıktan sonra Medicana Sağlık Grubuna transfer oldu ve burada Başhekim Yardımcısı ve Uluslararası Hasta Merkezi Direktörü olarak görev yaptı. 2013'ten bu yana Saç Ekimi Cerrahı ve Medikal Estetik Hekimi olarak 4000'den fazla operasyon gerçekleştirmiştir.

Planlamanın başarının anahtarı olduğuna inanır; donör bölge analizi, saç çizgisi tasarımı ve yüz özelliklerine göre detaylı planlama yapar. Micromotor Greft Extraction ve Sapphire FUE yöntemlerini kullanır.

Şık tarzı, puro merakı ve sanata ilgisi ile tanınır. Golf oynamayı sever, yeni evlidir ve İngilizce ile Almanca dillerinde uzmandır.
                """.trimIndent(),
                """
Dr. Mehmet Erdoğan is the co-founder of Smile Hair Clinic.

After working as a physician within Acıbadem Health Group, he joined Medicana Health Group, serving as Deputy Chief Physician and Director of the International Patient Center. Since 2013 he has performed more than 4,000 hair transplant operations as a surgeon and medical aesthetic physician.

He believes meticulous planning ensures success—analyzing donor areas, designing hairlines, and tailoring plans to facial features. He applies Micromotor Graft Extraction and Sapphire FUE techniques.

Recognized for his sophisticated style, passion for cigars, and appreciation of art, he enjoys golf, is newly married, and is fluent in English and German.
                """.trimIndent()
            ),
            "firdavs_ahmedov" to l(
                """
Dr. Firdavs Ahmedov, Smile Hair Clinic'te Saç Ekimi Cerrahıdır.

Medikal estetik tutkusunu Ege Üniversitesi'nde pekiştirdi ve onur derecesi ile mezun oldu. Ege Üniversitesi Hastanesi, Mount Sinai Hospital ve James J. Peters Bronx Veterans Hospital gibi merkezlerde cerrahi deneyim kazandı.

2015'ten bu yana saç ekimi ve medikal estetik alanlarında uzmanlaşarak FUE, DHI ve revizyon saç ekimlerinde 3500'den fazla operasyon gerçekleştirdi. Mikromotor ve safir bıçak tekniklerini kombinleyerek doğal sonuçlar elde eder.

Uluslararası vizyona sahip, etik değerlere bağlı ve sürekli öğrenmeye açık bir cerrahtır.
                """.trimIndent(),
                """
Dr. Firdavs Ahmedov is a hair transplant surgeon at Smile Hair Clinic.

His passion for medical aesthetics took shape at Ege University, where he graduated with honors. He gained extensive clinical experience at Ege University Hospital, Mount Sinai Hospital, and the James J. Peters Bronx Veterans Hospital.

Since 2015 he has specialized in hair transplantation and medical aesthetics, completing more than 3,500 FUE, DHI, and revision procedures. He blends micromotor and sapphire blade techniques to deliver natural results.

He has an international vision, upholds high ethical standards, and remains a lifelong learner and mentor.
                """.trimIndent()
            ),
            "ali_osman_soluk" to l(
                """
Dr. Ali Osman SOLUK, Smile Hair Clinic'te saç ekimi cerrahıdır.

İstanbul Tıp Fakültesi mezunudur. Kariyerine İstanbul Büyükşehir Belediyesi'nde saha hekimi olarak başladı ve İSPER'de üst düzey yönetici olarak görev yaptı. Sağlık turizmini geliştirmek için ulusal ve uluslararası organizasyonlarda aktif rol aldı.

Hastane ve Sağlık Kurumları Yönetimi yüksek lisansı ile eğitimini pekiştirdi, Adalet bölümünü tamamladı ve birçok sağlık kuruluşunda işyeri hekimliği ile acil servis hekimliği yaptı. Saç ekimi ve saç tedavilerine olan ilgisi onu bu alanda uzmanlaşmaya yöneltti.

İş dışında ailesi ile vakit geçirmeyi, sinemayı, kitapları ve koleksiyon hobilerini sever.
                """.trimIndent(),
                """
Dr. Ali Osman SOLUK is a hair transplant surgeon at Smile Hair Clinic.

He graduated from Istanbul Faculty of Medicine and began his career as a field physician for Istanbul Metropolitan Municipality, later serving as a senior executive at İSPER. He actively contributed to national and international initiatives that promote health tourism.

He completed a master’s degree in Hospital and Healthcare Management, studied law, and worked as an occupational and emergency physician across many institutions. His curiosity for continuous learning led him to specialize in hair transplantation and therapies.

Outside the clinic he enjoys spending time with his family, going to the cinema, reading, and collecting watches and fountain pens.
                """.trimIndent()
            ),
            "resat_arpaci" to l(
                """
Dr. M. Reşat Arpacı, 2000 yılında Dokuz Eylül Üniversitesi Tıp Fakültesi'nden mezun oldu ve estetik tıbba olan ilgisiyle saç ekimine odaklandı.

Biofibre Sentetik Saç Ekimi sertifikasını aldı, Kuşadası ve İzmir'de klinikler kurdu, FUE tekniğini erkenden benimsedi ve geliştirdi. 2007 yılında saç grefti kapasitesini artıran gelişmiş bir FUE mikromotor tekniği icat ederek patent aldı.

2010'dan bu yana İstanbul'daki prestijli merkezlerde saç ekimi başkanlığı yaptı. Eğitimci ruhu ile Milli Eğitim Bakanlığı onaylı Güzellik ve Estetik Okulu'nun kurucusu ve baş eğitmeni olarak görev yaptı. 2025 itibariyle Smile Hair Clinic'te saç ekimi cerrahı olarak görevine devam etmektedir.
                """.trimIndent(),
                """
Dr. M. Reşat Arpacı graduated from Dokuz Eylül University Faculty of Medicine in 2000 and gradually focused on hair transplantation through his passion for aesthetic medicine.

He earned the Biofibre Synthetic Hair Implant certificate, founded clinics in Kuşadası and İzmir, adopted the FUE technique early, and perfected it. In 2007 he invented and patented an advanced FUE micromotor method that increased graft capacity.

Since 2010 he has led hair transplant units in prestigious Istanbul clinics. As an educator, he founded and directed a Ministry of Education–approved School of Beauty and Aesthetics. As of 2025 he continues his career as a hair transplant surgeon at Smile Hair Clinic.
                """.trimIndent()
            )
        )

        private val footerTitle = l("Bizi Takip Edin", "Follow Us")
        private val linkError = l("Link açılamadı", "Unable to open link")

        fun analyzeTabTitle() = analyzeTab.get()
        fun aboutTabTitle() = aboutTab.get()
        fun startCaptureButton() = startCapture.get()
        fun captureTitle() = captureTitle.get()
        fun captureDescription() = captureDescription.get()
        fun attentionTitle() = attentionTitle.get()
        fun stepDescription(index: Int) = stepDescriptions[index].get()
        fun clinicTitle() = clinicTitle.get()
        fun clinicSubtitle() = clinicSubtitle.get()
        fun clinicInfo() = clinicDescription.get()
        fun doctorsHeader() = doctorsHeader.get()
        fun doctorTitle(key: String) = doctorTitles[key]?.get().orEmpty()
        fun doctorDescription(key: String) = doctorDescriptions[key]?.get().orEmpty()
        fun footerTitle() = footerTitle.get()
        fun linkError() = linkError.get()
    }

    object Capture {
        private val autoCapturePrompt = l(
            "Şablonda sabit kalın, otomatik çekim başlıyor.",
            "Hold steady inside the guide, capture starting."
        )
        private val savingPhoto = l("Fotoğraf kaydediliyor...", "Saving photo...")
        private val retakeReturn = l(
            "Fotoğraf kaydedildi. Kontrol ekranına dönülüyor...",
            "Photo saved. Returning to review screen..."
        )
        private val successStatus = l(
            "✓ Fotoğraf başarıyla çekildi: %s",
            "✓ Photo captured successfully: %s"
        )
        private val successGuidance = l(
            "Başarılı! Fotoğraf kaydedildi.",
            "Great! Photo has been saved."
        )
        private val completionStatus = l(
            "✓ Fotoğraflarınız başarıyla tamamlandı!",
            "✓ All photos captured successfully!"
        )
        private val completionGuidance = l(
            "Tüm fotoğraflar çekildi. Kontrol ekranına yönlendiriliyorsunuz...",
            "All photos are ready. Redirecting you to the review screen..."
        )
        private val confirmButton = l("Onayla", "Confirm")
        private val dialogTitle = l("%d. Adım: %s", "Step %d: %s")
        private val adjustPosition = l("Pozisyonu ayarlayın...", "Adjust your position...")
        private val holdStillVoice = l("Kıpırdamayın, fotoğrafınız çekiliyor.", "Hold still, capturing now.")
        private val successVoice = l("Başarılı", "Success")
        private val cameraPausedInfo = l(
            "Kamera hazır. %s için hazırlanın.",
            "Camera ready. Prepare for %s."
        )
        private val cameraFailed = l(
            "Kamera başlatılamadı: %s",
            "Camera could not start: %s"
        )
        private val captureError = l("Çekim hatası: %s", "Capture error: %s")
        private val permissionRequired = l("Kamera izni gerekli.", "Camera permission is required.")

        private val maintainPosition = l(
            "Pozisyonu koruyun, tekrar hizalayın.",
            "Hold position and align again."
        )
        private val faceDetecting = l("Yüz algılanıyor...", "Detecting your face...")
        private val positionPerfect = l("✓ Pozisyon doğru! Sabit kalın...", "✓ Position looks great! Hold still...")
        private val notAllPhotos = l("Tüm fotoğraflar çekilmedi. Lütfen tekrar deneyin.", "Not all photos were captured. Please try again.")
        private val yawLabel = l("Yaw: %d° (Hedef: %d°)", "Yaw: %d° (Target: %d°)")

        private val instructions = mapOf(
            "front" to l(
                "Yüzünüzü dairesel şablonda ortalayın ve göz hizasında tutun.",
                "Center your face inside the circle at eye level."
            ),
            "right" to l(
                "Kendi sağınıza doğru 45° dönün ve başınız sabitken şablonu doldurun.",
                "Turn 45° toward your right and fill the guide while keeping your head steady."
            ),
            "left" to l(
                "Kendi solunuza doğru 45° dönün ve yüzünüz şablonun içinde kalsın.",
                "Turn 45° toward your left and keep your face inside the guide."
            ),
            "top" to l(
                "Telefonu yatay tutup kafanızın tepesine getirin. Saç bölgesini görüntüleyin.",
                "Hold the phone horizontally above your head and show the crown."
            ),
            "back" to l(
                "Telefonu ters dik tutup kafanızın arkasına getirin. Donör bölgeyi gösterin.",
                "Hold the phone vertically behind your head to show the donor area."
            )
        )

        fun autoCapturePrompt() = autoCapturePrompt.get()
        fun savingPhoto() = savingPhoto.get()
        fun retakeReturn() = retakeReturn.get()
        fun successStatus(angleName: String) = String.format(successStatus.get(), angleName)
        fun successGuidance() = successGuidance.get()
        fun completionStatus() = completionStatus.get()
        fun completionGuidance() = completionGuidance.get()
        fun confirmButton() = confirmButton.get()
        fun dialogTitle(stepNumber: Int, stepTitle: String): String {
            return if (LanguageManager.currentLanguage == AppLanguage.EN) {
                String.format("Step %d: %s", stepNumber, stepTitle)
            } else {
                String.format("%d. Adım: %s", stepNumber, stepTitle)
            }
        }
        fun adjustPosition() = adjustPosition.get()
        fun holdStillVoice() = holdStillVoice.get()
        fun successVoice() = successVoice.get()
        fun cameraReady(angleName: String) = String.format(cameraPausedInfo.get(), angleName)
        fun cameraFailed(message: String) = String.format(cameraFailed.get(), message)
        fun captureError(message: String) = String.format(captureError.get(), message)
        fun permissionRequired() = permissionRequired.get()
        fun maintainPosition() = maintainPosition.get()
        fun faceDetecting() = faceDetecting.get()
        fun positionPerfect() = positionPerfect.get()
        fun notAllPhotos() = notAllPhotos.get()
        fun angleInstruction(key: String) = instructions[key]?.get().orEmpty()
        fun yawLabel(current: Int, target: Int) = String.format(yawLabel.get(), current, target)
    }

    object Directions {
        val faceNotDetected = l("Yüz algılanamadı - Lütfen kameraya bakın", "Face not detected - please look at the camera")
        val moveRight = l("Sağa kaydırın", "Move right")
        val moveLeft = l("Sola kaydırın", "Move left")
        val moveDown = l("Aşağı kaydırın", "Move down")
        val moveUp = l("Yukarı kaydırın", "Move up")
        val moveCloser = l("Biraz daha yaklaşın", "Move a little closer")
        val centerFace = l("Ortaya alın", "Center your face")
        val unknownAngle = l("Bilinmeyen açı", "Unknown angle")
        val faceMustNotAppearTop = l("Yüzünüz görünmemeli, telefonu kafanızın üzerine alın", "Your face should not be visible, lift the phone above your head")
        val headNotVisibleTop = l("Telefonu başınızın üstüne getirin, kafa görünmüyor", "Move the phone above your head, the scalp is not visible")
        val hairDetected = l("Saç algılandı, pozisyon doğru!", "Hair detected, position looks good!")
        val hairNotDetectedTop = l("Telefonu yatay tutup kafanızın tepesine getirin, saç görünmüyor", "Hold the phone horizontally above your head, hair is not detected")
        val headNotDetectedBack = l("Telefonu kafanızın arkasına getirin, kafa görünmüyor", "Move the phone behind your head, the donor area is missing")
        val hairNotDetectedBack = l("Telefonu ters dik tutup kafanızın arkasına getirin, saç görünmüyor", "Hold the phone vertically behind your head, hair is not detected")
        val keepPhoneStraight = l("Telefonu yatay tutup kafanızın tepesine getirin", "Hold the phone above your head, keep it level")
        val noFaceBack = l("Yüzünüz görünmemeli, telefonu kafanızın arkasına alın", "Your face shouldn't be visible, move the phone to the back")
        val tiltOk = l("Açı doğru!", "Angle looks good!")
        val tiltMore = l("Telefonu daha fazla eğin", "Tilt the phone a bit more")
        val tiltLess = l("Telefonu daha az eğin", "Tilt the phone a bit less")
        val keepDonorInstruction = l(
            "Telefonu ters dik tutup kafanızın arkasına getirin",
            "Hold the phone vertically behind your head"
        )
        val keepTopInstruction = l(
            "Telefonu yatay tutup kafanızın tepesine getirin",
            "Hold the phone horizontally above your head"
        )
        val yawRightMore = l("Lütfen yüzünüzü kendi sağınıza doğru çevirin", "Please turn your face more to your right")
        val yawRightLess = l("Biraz daha az kendi sağınıza dönün", "Turn slightly less to your right")
        val yawLeftMore = l("Lütfen yüzünüzü kendi solunuza doğru çevirin", "Please turn your face more to your left")
        val yawLeftLess = l("Biraz daha az kendi solunuza dönün", "Turn slightly less to your left")
    }

    object Review {
        private val statusHint = l(
            "Lütfen her fotoğrafı kontrol edin",
            "Please review each photo"
        )
        private val uploadButton = l("Analize Gönder", "Submit for Analysis")
        private val loadingButton = l("Yükleniyor...", "Uploading...")
        private val preparingPhotos = l("Fotoğraflar hazırlanıyor...", "Preparing photos...")
        private val processingPhoto = l("Fotoğraf %d/5 optimize ediliyor...", "Optimizing photo %d/5...")
        private val uploadProcessing = l("Fotoğraflar işleniyor...", "Processing photos...")
        private val sendingApi = l("Veriler gönderiliyor... (%sMB)", "Sending data... (%sMB)")
        private val retrying = l("Tekrar deneniyor... (Deneme %d/%d)", "Retrying... (Attempt %d/%d)")
        private val apiInProgress = l("API'ye gönderiliyor...", "Sending to API...")
        private val uploadComplete = l("Başarıyla kaydedildi! ID: %s", "Uploaded successfully! ID: %s")
        private val missingInfo = l("Lütfen tüm bilgileri doldurun", "Please fill in all fields")
        private val missingPhotos = l("Hata: Tüm fotoğraflar bulunamadı", "Error: Required photos are missing.")
        private val errorTimeout = l("⏱️ Bağlantı zaman aşımı. API sunucusunun çalıştığından emin olun.", "⏱️ Connection timed out. Please make sure the API server is running.")
        private val errorConnect = l("🔌 Sunucuya bağlanılamadı. IP adresini ve port numarasını kontrol edin.", "🔌 Unable to connect to the server. Check the IP address and port.")
        private val errorNetwork = l("📡 Ağ hatası. İnternet bağlantınızı kontrol edin.", "📡 Network error. Please check your internet connection.")
        private val errorUnknown = l("❌ %s", "❌ %s")
        private val uploadProgressLabel = l("Fotoğraflar gönderiliyor...", "Uploading photos...")
        private val uploadSuccess = l("Başarıyla gönderildi!", "Uploaded successfully!")
        private val uploadFailed = l("Gönderim başarısız, tekrar deneyin.", "Upload failed, please try again.")
        private val infoDialogTitle = l("Kullanıcı Bilgileri", "User Information")
        private val infoDialogConfirm = l("Gönder", "Submit")
        private val infoDialogCancel = l("İptal", "Cancel")
        private val closeButton = l("Kapat", "Close")
        private val nameHint = l("İsim", "First Name")
        private val surnameHint = l("Soyisim", "Last Name")
        private val phoneHint = l("Telefon", "Phone")
        private val reviewWarning = l(
            "❗ UYARI: Analize Gönderme Şartları\n\n1) Çekimleri tamamladıktan sonra her fotoğraf kartındaki “Kontrol Et” butonuna dokunarak kaliteyi yapay zekâ ile doğrulayın.\n2) Kontrol bittiğinde listedeki onay durumlarını takip edin. “Onaylandı” yazısını görmeden devam etmeyin.\n3) Eğer örneğin “5. Arka Donör” fotoğrafı reddedilirse karttaki “Tekrar Çek / Yeniden Dene” butonu ile aynı açıyı yeniden çekmeniz zorunludur.\n\nTüm fotoğraflar onaylanmadan “Analize Gönder” butonu aktif olmayacaktır.",
            "❗ WARNING: Submission Requirements\n\n1) After finishing all shots, tap “Review” on each card so AI can verify quality.\n2) Track the approval badges and make sure every photo is marked Approved before moving on.\n3) If, for example, “Photo 5 - Back Donor” is rejected, you must use the Retake button on that card and capture it again.\n\nThe “Submit for Analysis” button stays disabled until all five photos are approved."
        )
        private val photoApproved = l("✓ Tüm kontroller geçti! Analize gönderebilirsiniz.", "✓ All checks passed! Ready to submit.")
        private val photoRejected = l("❌ %s", "❌ %s")
        private val reviewProgress = l("Kontrol edildi: %d/5, Onaylandı: %d/5", "Checked: %d/5, Approved: %d/5")
        private val photoLabel = l("Fotoğraf %d", "Photo %d")
        private val photoUnchecked = l("Kontrol edilmedi", "Not checked yet")
        private val photoChecking = l("Kontrol ediliyor...", "Checking...")
        private val photoLoadFailed = l("Fotoğraf yüklenemedi", "Photo could not be loaded")
        private val photoValidationError = l("Kontrol hatası: %s", "Check failed: %s")
        private val buttonCheck = l("Kontrol Et", "Review")
        private val buttonRetake = l("Tekrar Çek", "Retake")
        private val photoStatusApproved = l("✓ Onaylandı", "✓ Approved")
        private val buttonChecked = l("✓ Kontrol Edildi", "✓ Reviewed")
        private val headNotDetected = l("Geçerli bir insan kafası algılanamadı", "A valid head could not be detected.")
        private val yawRightError = l("Yüz -40° ile -50° arasında sağa dönmemiş (%d°)", "Face is not turned between -40° and -50° to the right (%d°)")
        private val yawLeftError = l("Yüz 40° ile 50° arasında sola dönmemiş (%d°)", "Face is not turned between 40° and 50° to the left (%d°)")
        private val templeRightMissing = l("Sağ şakak görünmüyor", "Right temple is not visible")
        private val templeLeftMissing = l("Sol şakak görünmüyor", "Left temple is not visible")
        private val photoTooDark = l("Fotoğraf çok karanlık", "Photo is too dark")
        private val photoTooBright = l("Fotoğraf çok parlak", "Photo is too bright")
        private val topNotDetected = l("Üstten kafa görüntüsü algılanamadı", "Top of head could not be detected")
        private val backNotDetected = l("Arkadan kafa görüntüsü algılanamadı", "Back of head could not be detected")
        private val samePersonFaceMissing = l("Yüz algılanamadı", "Face could not be detected.")
        private val samePersonReferenceMissing = l("Referans fotoğraf bulunamadı", "Reference photo not found.")
        private val samePersonMismatch = l("Yüz boyutu çok farklı - aynı kişi değil olabilir", "Face size differs too much - may not be the same person.")
        private val finalValidationPending = l("Tüm fotoğraflar onaylanmadı", "Not all photos are approved.")
        private val retryButton = l("Tekrar Gönder", "Resend")
        private val serverSectionTitle = l("Sunucu Adresi", "Server Address")
        private val serverHelpText = l("Sunucu adresini girerek tüm cihazların aynı API'yi kullanmasını sağlayın.", "Enter the API address so that every device uses the same backend.")
        private val serverSaveButton = l("Kaydet", "Save")
        private val serverHint = l(
            "https://unfreakish-hottish-selene.ngrok-free.dev/",
            "https://unfreakish-hottish-selene.ngrok-free.dev/"
        )
        private val serverUpdateSuccess = l("Sunucu adresi güncellendi.", "Server URL updated.")
        private val serverUpdateInvalid = l("Geçersiz adres. Lütfen http veya https ile başlayan bir URL girin.", "Invalid address. Please provide a URL starting with http or https.")

        fun statusHint() = statusHint.get()
        fun uploadButton() = uploadButton.get()
        fun loadingButton() = loadingButton.get()
        fun preparingPhotos() = preparingPhotos.get()
        fun processingPhoto(index: Int) = String.format(processingPhoto.get(), index)
        fun uploadProcessing() = uploadProcessing.get()
        fun sendingToApi(sizeMb: String) = String.format(sendingApi.get(), sizeMb)
        fun retrying(attempt: Int, max: Int) = String.format(retrying.get(), attempt, max)
        fun apiInProgress() = apiInProgress.get()
        fun uploadComplete(id: String) = String.format(uploadComplete.get(), id)
        fun missingInfo() = missingInfo.get()
        fun missingPhotos() = missingPhotos.get()
        fun errorTimeout() = errorTimeout.get()
        fun errorConnect() = errorConnect.get()
        fun errorNetwork() = errorNetwork.get()
        fun errorUnknown(message: String) = String.format(errorUnknown.get(), message)
        fun uploadProgressLabel() = uploadProgressLabel.get()
        fun uploadSuccess() = uploadSuccess.get()
        fun uploadFailed() = uploadFailed.get()
        fun infoDialogTitle() = infoDialogTitle.get()
        fun infoDialogConfirm() = infoDialogConfirm.get()
        fun infoDialogCancel() = infoDialogCancel.get()
        fun closeButton() = closeButton.get()
        fun nameHint() = nameHint.get()
        fun surnameHint() = surnameHint.get()
        fun phoneHint() = phoneHint.get()
        fun reviewWarning() = reviewWarning.get()
        fun photoApproved() = photoApproved.get()
        fun photoRejected(reason: String) = String.format(photoRejected.get(), reason)
        fun reviewProgress(checked: Int, approved: Int) = String.format(reviewProgress.get(), checked, approved)
        fun photoLabel(index: Int) = String.format(photoLabel.get(), index)
        fun photoUnchecked() = photoUnchecked.get()
        fun photoChecking() = photoChecking.get()
        fun photoLoadFailed() = photoLoadFailed.get()
        fun photoValidationError(reason: String) = String.format(photoValidationError.get(), reason)
        fun buttonCheck() = buttonCheck.get()
        fun buttonRetake() = buttonRetake.get()
        fun photoStatusApproved() = photoStatusApproved.get()
        fun buttonChecked() = buttonChecked.get()
        fun headNotDetected() = headNotDetected.get()
        fun yawRightError(angle: Int) = String.format(yawRightError.get(), angle)
        fun yawLeftError(angle: Int) = String.format(yawLeftError.get(), angle)
        fun templeRightMissing() = templeRightMissing.get()
        fun templeLeftMissing() = templeLeftMissing.get()
        fun photoTooDark() = photoTooDark.get()
        fun photoTooBright() = photoTooBright.get()
        fun topNotDetected() = topNotDetected.get()
        fun backNotDetected() = backNotDetected.get()
        fun samePersonFaceMissing() = samePersonFaceMissing.get()
        fun samePersonReferenceMissing() = samePersonReferenceMissing.get()
        fun samePersonMismatch() = samePersonMismatch.get()
        fun finalValidationPending() = finalValidationPending.get()
        fun retryButton() = retryButton.get()
        fun serverSectionTitle() = serverSectionTitle.get()
        fun serverHelpText() = serverHelpText.get()
        fun serverSaveButton() = serverSaveButton.get()
        fun serverHint() = serverHint.get()
        fun serverUpdateSuccess() = serverUpdateSuccess.get()
        fun serverUpdateInvalid() = serverUpdateInvalid.get()
    }

    object Voice {
        private val rightInstruction = l(
            "Yüzünüzü kendi sağınıza çevirip şablonun ortasına yerleştirin",
            "Turn your face toward your right and center it in the guide"
        )
        private val leftInstruction = l(
            "Yüzünüzü kendi solunuza çevirip şablonun ortasına yerleştirin",
            "Turn your face toward your left and center it in the guide"
        )
        private val frontInstruction = l(
            "Lütfen kameraya karşıdan bakın",
            "Please face the camera directly"
        )
        private val keepCentered = l(
            "Yüzünüzü ortaya alın",
            "Keep your face centered"
        )

        fun rightInstruction() = rightInstruction.get()
        fun leftInstruction() = leftInstruction.get()
        fun frontInstruction() = frontInstruction.get()
        fun keepCentered() = keepCentered.get()
    }
}

