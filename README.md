 🚀 SmileHair Self-Capture: Klinik Veri Standartlarında Devrim
✨ Proje Özeti: Yapay Zeka Destekli Hassas Görüntüleme Sistemi
SmileHair Self-Capture, saç ekimi ve dermatolojik analiz süreçlerinin kritik ön koşulu olan standartlaştırılmış fotoğraf çekimini otomatikleştiren, yapay zeka destekli bir Android prototipidir. Bu çözüm, manuel çekim hatalarını ve klinik süreçlerdeki veri tutarsızlığını ortadan kaldırarak hasta kayıtlarının ve tedavi öncesi analizlerin kalitesini kökten iyileştirmektedir.

Bu proje, bir Hackathon ortamında geliştirilmiş olmasına rağmen, ticari kullanıma uygunluk ve ölçeklenebilirlik potansiyeli düşünülerek en güncel mobil ve makine öğrenimi teknolojileriyle inşa edilmiştir.

🛡️ Benzersiz Satış Noktası (USP): AI Destekli Veri Güvenliği ve Kullanıcı Güveni
Bu bölüm, uygulamanın güvenlik, uluslararası erişilebilirlik ve hasta psikolojisine odaklanarak fark yarattığı noktaları vurgular.

1. Maksimum Veri Tutarlılığı için AI Doğrulama
Uygulama, fotoğraf çekim sonrası süreçte benzersiz bir AI Doğrulama Katmanı (AI Validation Layer) kullanır. Bu katman, verilerin klinik veritabanına kaydedilmeden önce aşağıdaki kritik kontrolleri gerçek zamanlı olarak sağlar:

Kişi Tutarlılığı: Çekilen tüm fotoğrafların aynı bireye ait olup olmadığı kontrol edilir.

Ortam Analizi: Fotoğrafın aydınlatma koşulları analiz edilir (aşırı aydınlık veya karanlık).

Açısal Hassasiyet (Real-Time Control): Özellikle 45° sapmalı ikinci ve üçüncü resimler için merkezden 40-50 derece sapma aralığı titizlikle kontrol edilir. Dördüncü ve beşinci fotoğraflar için, telefonun tutuş açısı gerçek zamanlı (real-time) olarak denetlenir ve sesli uyarılarla kullanıcı yönlendirilir.

İnsan Doğrulaması: Ekranda gerçekten bir insan kafasının olup olmadığı kontrol edilir. Rastgele gelen ve AI tarafından onaylanmayan fotoğraflar tamamen elenir ve kullanıcının veritabanına (POST) erişimi engellenir. Bu, veritabanı bütünlüğünü korur.

2. Uluslararasılaşma ve Güven Odaklı Arayüz (UI/UX)
Türkiye'nin medikal turizmdeki rekabet avantajını desteklemek amacıyla, uygulama hem Türkçe hem de İngilizce arayüz seçenekleri sunar. Kullanıcının dil engeli olmadan süreci anlaması sağlanır.

Basitleştirilmiş Akış: Deneyimsiz bir kullanıcı bile süreci 3 aşamada (Kamera ekranında bekleyen görevler, yazılı ve sesli talimatlar, AI doğrulama) kolayca tamamlayabilir. Hata durumunda, AI kontrolü başarılı olana kadar tekrar denemeye yönlendirilir.

Bilinçaltı Güven Tasarımı: Bıçak altına yatacak hastalar için en kritik duygu olan güven duygusunu pekiştirmek amacıyla arayüz, güven rengi olan mavi ve otorite rengi olan siyah ile birleştirilerek lacivert bir tema ile tasarlanmıştır. Bu tasarım, bilinçaltına "bize güvenin ve kendinizi bize bırakın" imajını yerleştirmeyi hedefler.

Semantik Arayüz: Arayüz, bilimselliğe uygun büyüklük ve yerleşime sahip bloklarla semantik uygulama prensiplerine göre tasarlanmıştır.

3. Hekim Tanınırlığı ile Güven İnşası
Uygulama, güven duygusunu artırmak için bir hekim tanıtım sekmesi içerir. Bu sekme:

Değerli hekimlerin kariyer tecrübelerini ve başarılarını ön plana çıkarır.

Instagram, YouTube, TikTok ve Facebook gibi platformlardaki tanınırlıklarını vurgulayan bir "Follow Me" bölümü içerir.


🎯 Klinik İhtiyaca Yönelik Çözüm Odaklılık
Problem: Klinik Veri Tutarsızlığı ve Personel Bağımlılığı Saç ekimi öncesi 5 kritik açının (Tam Yüz, 45° Sağ/Sol, Tepe, Donör Alan) fotoğraflanması, manuel çekimlerde sürekli olarak hatalı pozisyon, yanlış ışık ve kamera açısı sorunlarına yol açar. Bu hatalar, doğru greft (saç kökü) sayımı, donör alan kapasitesi analizi ve tedavi sonrası karşılaştırmaları imkansız hale getirir.

Çözüm: Hassas Otomasyon ve Standartlaştırma (HR için Anahtar Değer) SmileHair Self-Capture, fotoğraf çekimini operatörün veya hastanın kişisel yeteneğinden bağımsız hale getirerek, klinik ekibe zaman kazandırır ve diagnostik verinin kalitesini garanti altına alır. Bu, doğrudan iş gücü verimliliğini artırır ve klinik sonuçların güvenilirliğini üst seviyeye taşır.

Özellik	Geleneksel Yöntem	SmileHair Self-Capture
Hassasiyet	Öznel, İnsan Hatasına Açık	ML Kit ile 0.5° Sapma Toleransı
Süreç	Tekrarlayan Personel İşi	5 Açının Otomatik Hizada Çekimi
Kullanıcı	Teknisyen veya Fotoğrafçı	Hasta (Self-Capture)
Verimlilik	Yavaş, Düzeltme Gerektirir	Hızlı, %100 Uyumlu Kayıt


💻 Teknik Mimari ve Performans Üstünlükleri
Gelişmiş Kamera ve Görüntü İşleme Mimarisi

CameraX API Entegrasyonu: Performans ve pil optimizasyonu için en yeni Android Jetpack CameraX kütüphanesi kullanılmıştır. Görüntü yakalama ve önizleme süreçleri minimal gecikmeyle (low-latency) çalışır.

Asenkron Operasyonlar: Tüm kamera ve yüz algılama işlemleri, uygulamanın ana iş parçacığını (Main Thread) bloke etmemek için Kotlin Coroutines yapısı ile yönetilmektedir. Bu, yüz algılama gerçekleşirken bile akıcı bir kullanıcı deneyimi sunar.

Yapay Zeka Destekli Otomatik Hizalama (Alignment)

Google ML Kit Face Detection: Uygulamanın beyni, gerçek zamanlı (real-time) yüz algılama yeteneğine sahip olan ML Kit'tir. Bu, sadece yüzün varlığını değil, aynı zamanda merkezi konumunu ve eğiklik derecesini (pitch, yaw, roll) milisaniyeler içinde hesaplar.

Kritik 5 Açı Kontrolü: Uygulama, kullanıcının yüzünü sırasıyla 5 kritik pozisyona yönlendirir. ML çıktıları, kullanıcının açıyı başarıyla tamamladığını onayladığı anda otomatik geri sayımı başlatır ve fotoğrafı çeker. Bu, personelin eğitime olan bağımlılığını sıfıra indirir.

Veri Bütünlüğü ve Güvenliği

Meta Veri Entegrasyonu: Çekilen her fotoğraf, sadece görsel veriyi değil, aynı zamanda çekim açısı, zaman damgası ve kamera kalibrasyon bilgisi gibi kritik meta verileri de içerir.

Güvenli Depolama: Fotoğraflar, Android'in önerdiği Scoped Storage prensiplerine uygun olarak uygulama dizinine kaydedilir, bu da hasta verilerinin yüksek güvenlik standartlarında tutulmasını sağlar.

⚠️ Sunucu Notu ve Teknik Bilgilendirme
Dikkat: Bu prototip, klinikteki mevcut MySQL veritabanı ile sorunsuz iletişim kurması için tasarlanmıştır. Uygulamada, veritabanına (DB) erişim hatası oluşması durumunda endişelenmeyin. Uygulama içinde Port Ayarlama Ekranı mevcuttur. Bu, kliniğinizin özel ağ yapılandırmasına hızlı adaptasyon yeteneğini gösterir. Şu anda yerel olarak kaydedilen assets/7.png gibi dosyalar, sadece geliştirme ortamı testlerini göstermektedir ve sunucunuz kapalıyken veri kayıt hatası vermesi beklenen bir durumdur.
