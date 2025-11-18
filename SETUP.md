# 🚀 Hızlı Kurulum Rehberi

## Android Studio'da Projeyi Açma

1. **Android Studio'yu açın** (Hedgehog | 2023.1.1 veya daha yeni)

2. **Projeyi açın:**
   - `File` -> `Open`
   - `SmileHairSelfCapture` klasörünü seçin
   - `OK` butonuna tıklayın

3. **Gradle Sync:**
   - Android Studio otomatik olarak Gradle sync başlatacak
   - İlk açılışta 2-5 dakika sürebilir (bağımlılıklar indirilir)
   - Sync tamamlandığında alt kısımda "Gradle build finished" mesajını göreceksiniz

4. **SDK Kontrolü:**
   - `File` -> `Project Structure` -> `SDK Location`
   - Android SDK yüklü olduğundan emin olun
   - `Tools` -> `SDK Manager` -> `SDK Platforms` sekmesinden **API 34** yüklü olmalı

5. **Emülatör Oluşturma:**
   - `Tools` -> `Device Manager` -> `Create Device`
   - **Pixel 5** seçin
   - **System Image:** API 30 (Android 11) veya daha yeni seçin
   - `Finish` butonuna tıklayın
   - Emülatörü başlatın

6. **Uygulamayı Çalıştırma:**
   - Üst menüden emülatörü seçin
   - Yeşil `Run` butonuna (▶️) tıklayın
   - Uygulama derlenecek ve emülatöre yüklenecektir

## ⚠️ Olası Sorunlar ve Çözümleri

### Gradle Sync Hatası
- **Sorun:** "Gradle sync failed"
- **Çözüm:** 
  - `File` -> `Invalidate Caches / Restart` -> `Invalidate and Restart`
  - Internet bağlantınızı kontrol edin
  - `gradle.properties` dosyasında proxy ayarları gerekebilir

### SDK Bulunamadı
- **Sorun:** "SDK location not found"
- **Çözüm:**
  - `File` -> `Project Structure` -> `SDK Location`
  - Android SDK yolunu manuel olarak belirtin (örn: `C:\Users\YourName\AppData\Local\Android\Sdk`)

### Emülatörde Kamera Çalışmıyor
- **Sorun:** Emülatörde kamera açılmıyor
- **Çözüm:**
  - Emülatör ayarlarından kamerayı etkinleştirin
  - Fiziksel cihaz kullanmayı deneyin (daha iyi sonuç)

### Build Hatası
- **Sorun:** "Build failed"
- **Çözüm:**
  - `Build` -> `Clean Project`
  - `Build` -> `Rebuild Project`
  - `File` -> `Sync Project with Gradle Files`

## 📱 Fiziksel Cihazda Test

1. **USB Debugging:**
   - Cihazınızda: `Ayarlar` -> `Telefon Hakkında` -> `Yapı Numarası`'na 7 kez dokunun
   - `Ayarlar` -> `Geliştirici Seçenekleri` -> `USB Debugging`'i açın

2. **Cihazı Bağlama:**
   - USB kablosu ile cihazı bilgisayara bağlayın
   - Cihazda "USB Debugging" iznini onaylayın
   - Android Studio'da cihazınız görünecektir

3. **Çalıştırma:**
   - Cihazı seçin ve `Run` butonuna tıklayın

## ✅ Kontrol Listesi

- [ ] Android Studio yüklü
- [ ] JDK 17 veya üzeri yüklü
- [ ] Android SDK API 34 yüklü
- [ ] Gradle sync başarılı
- [ ] Emülatör veya fiziksel cihaz hazır
- [ ] Uygulama başarıyla çalışıyor
- [ ] Kamera izni verildi

## 🎯 Hackathon İçin Hazır!

Proje Android Studio'da açıldıktan sonra hackathon için hazırdır. Tüm bağımlılıklar yüklü, yapılandırma tamamlanmış ve uygulama çalışır durumdadır.

**İyi şanslar! 🚀**

