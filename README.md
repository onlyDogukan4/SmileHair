# SmileHair Self-Capture - Hackathon Projesi

## 📱 Proje Hakkında

SmileHair Self-Capture, saç ekimi için gerekli 5 farklı açıdan (Tam Yüz, 45° Sağa, 45° Sola, Tepe, Arka Donör) otomatik fotoğraf çekimi yapan bir Android uygulamasıdır.

### Özellikler
- **CameraX** tabanlı modern kamera entegrasyonu
- **ML Kit Face Detection** ile yüz algılama ve ortalama kontrolü
- Otomatik çekim: Yüz ortalandığında geri sayım ve ses ile fotoğraf alma
- 5 açı akışı: Sırasıyla farklı pozisyonlar için yönlendirme
- Ön kamera kullanımı: Self-capture için optimize edilmiş

## 🚀 Android Studio'da Açma ve Çalıştırma

### Gereksinimler
- **Android Studio** (Hedgehog | 2023.1.1 veya daha yeni önerilir)
- **JDK 17** veya daha yeni
- **Android SDK** (API 34 - Android 14)
- **Minimum SDK:** Android 7.0 (API 24)
- **Target SDK:** Android 14 (API 34)

### Kurulum Adımları

1. **Projeyi Android Studio'da Açma**
   - Android Studio'yu açın
   - `File` -> `Open` menüsünden `SmileHairSelfCapture` klasörünü seçin
   - Android Studio projeyi senkronize edecek (Gradle sync)

2. **SDK ve Bağımlılıklar**
   - Android Studio otomatik olarak gerekli SDK'ları ve bağımlılıkları indirecektir
   - Eğer hata alırsanız: `Tools` -> `SDK Manager` -> `SDK Platforms` sekmesinden **Android 14 (API 34)** yükleyin
   - `SDK Tools` sekmesinden **Android SDK Build-Tools 34.0.0** yükleyin

3. **Gradle Sync**
   - Proje açıldığında otomatik sync başlar
   - Hata alırsanız: `File` -> `Sync Project with Gradle Files`
   - İlk sync işlemi birkaç dakika sürebilir (bağımlılıklar indirilir)

4. **Emülatör veya Fiziksel Cihaz**
   
   **Emülatör Kullanımı:**
   - `Tools` -> `Device Manager` -> `Create Device`
   - **Pixel 5** veya benzeri bir cihaz seçin
   - **System Image:** API 30 (Android 11) veya daha yeni seçin
   - Emülatörü başlatın
   - **Önemli:** Emülatörde kamera çalışması için emülatör ayarlarından kamerayı etkinleştirin
   
   **Fiziksel Cihaz:**
   - USB Debugging'i etkinleştirin (Ayarlar -> Geliştirici Seçenekleri -> USB Debugging)
   - Cihazı USB ile bilgisayara bağlayın
   - Android Studio'da cihazınız görünecektir

5. **Uygulamayı Çalıştırma**
   - Üst menüden cihaz/emülatör seçin
   - Yeşil `Run` butonuna (▶️) tıklayın veya `Shift + F10` tuşlarına basın
   - Uygulama derlenecek ve seçili cihaza yüklenecektir

6. **İzinler**
   - Uygulama ilk açılışta **Kamera izni** isteyecektir
   - İzni kabul edin
   - Emülatörde kamera izni için emülatör ayarlarını kontrol edin

## 📂 Proje Yapısı

```
SmileHairSelfCapture/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/smilehair/selfcapture/
│   │       │   └── MainActivity.kt          # Ana aktivite
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml    # Ana ekran layout
│   │       │   ├── values/
│   │       │   │   ├── strings.xml
│   │       │   │   ├── colors.xml
│   │       │   │   └── styles.xml
│   │       │   └── mipmap-*/                # Uygulama ikonları
│   │       └── AndroidManifest.xml
│   └── build.gradle                          # App seviyesi bağımlılıklar
├── build.gradle                              # Proje seviyesi yapılandırma
├── settings.gradle                           # Proje ayarları
├── gradle.properties                         # Gradle yapılandırması
└── README.md
```

## 🔧 Teknik Detaylar

### Kullanılan Teknolojiler
- **Kotlin** - Programlama dili
- **CameraX** - Kamera API'si
- **ML Kit Face Detection** - Yüz algılama
- **Coroutines** - Asenkron işlemler
- **Material Components** - UI bileşenleri

### Bağımlılıklar
- `androidx.camera:camera-core:1.3.1`
- `androidx.camera:camera-camera2:1.3.1`
- `androidx.camera:camera-lifecycle:1.3.1`
- `androidx.camera:camera-view:1.3.1`
- `com.google.mlkit:face-detection:16.1.6`
- `kotlinx-coroutines-android:1.7.3`

## 🎯 Kullanım

1. Uygulamayı açın
2. Kamera iznini verin
3. Ön kameradan yüzünüzü görüntüleyin
4. Uygulama sırasıyla 5 farklı açı için yönlendirme yapacaktır:
   - Tam Yüz (Karşıdan)
   - 45° Sağa
   - 45° Sola
   - Tepe (Vertex)
   - Arka Donör
5. Yüz ortalandığında otomatik olarak geri sayım başlar (3-2-1)
6. Fotoğraf otomatik olarak çekilir ve kaydedilir
7. Manuel çekim için "Manuel Çekim" butonunu kullanabilirsiniz

## 📸 Fotoğrafların Konumu

Fotoğraflar cihazınızın şu konumuna kaydedilir:
- **Android 10 ve üzeri:** `/Android/data/com.smilehair.selfcapture/files/`
- **Android 9 ve altı:** `/Pictures/SmileHair Self-Capture/`

## ⚠️ Önemli Notlar

- Bu proje bir **hackathon prototipidir**
- Emülatörde kamera testi için emülatörün kamera özelliklerini etkinleştirin
- Fiziksel cihazda test etmek daha iyi sonuçlar verecektir
- İleri seviye özellikler için:
  - UI/UX iyileştirmeleri (animasyonlar, görsel şablonlar)
  - Tepe ve arka çekimler için 3D rehber silüetleri
  - Görüntü kalitesi kontrolü
  - Pose stabilizasyonu

## 🐛 Bilinen Sorunlar

- Emülatörde kamera bazen çalışmayabilir (fiziksel cihaz önerilir)
- Yüz algılama bazen yanlış pozisyon algılayabilir (geliştirilebilir)
- Çoklu çekim önleme mekanizması basit seviyededir

## 📝 Lisans

Bu proje hackathon amaçlı geliştirilmiştir.

## 👥 Geliştirici

SmileHair Self-Capture Hackathon Ekibi

---

**Hackathon için hazır! 🚀**
