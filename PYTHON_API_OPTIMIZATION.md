# 🚀 Python API Optimizasyon Rehberi

## ⚠️ Mevcut Sorun: Timeout

Mobil uygulamadan gelen istekler **60 saniye sonra timeout** oluyor. Bu, Python API'nin veriyi işlemesi çok uzun sürdüğü için oluyor.

## 🔧 Çözümler

### 1. Flask Timeout Ayarları

Python Flask uygulamanızda timeout ayarlarını artırın:

```python
from flask import Flask
app = Flask(__name__)

# Timeout ayarları
app.config['SEND_FILE_MAX_AGE_DEFAULT'] = 0
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # 16MB max

# Gunicorn kullanıyorsanız:
# gunicorn --timeout 180 app:app

# Veya Flask'ı direkt çalıştırıyorsanız:
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, threaded=True, timeout=180)
```

### 2. Base64 Decode Optimizasyonu

Base64 decode işlemini optimize edin:

```python
import base64
import io
from PIL import Image

@app.route('/api/capture-sets', methods=['POST'])
def add_capture_set():
    try:
        data = request.get_json()
        photos = data.get('photos', [])
        
        for photo in photos:
            photo_url = photo.get('photo_url')
            photo_type = photo.get('photo_type')
            
            # Base64'ten fotoğrafı çıkar (optimize edilmiş)
            if photo_url.startswith('data:image'):
                # "data:image/jpeg;base64," kısmını kaldır
                base64_data = photo_url.split(',')[1]
                
                # Base64 decode
                image_data = base64.b64decode(base64_data)
                
                # PIL ile aç ve optimize et
                image = Image.open(io.BytesIO(image_data))
                
                # Dosyayı kaydet
                filename = f"{set_id}_{photo_type}.jpg"
                filepath = os.path.join('uploaded_captures', filename)
                
                # JPEG olarak kaydet (optimize edilmiş)
                image.save(filepath, 'JPEG', quality=85, optimize=True)
                
    except Exception as e:
        return jsonify({"message": f"Error: {str(e)}", "set_id": None}), 500
```

### 3. Veritabanı İşlemlerini Optimize Edin

```python
# Tek bir transaction'da tüm işlemleri yapın
try:
    cursor = db.cursor()
    
    # Ana kaydı ekle
    cursor.execute(
        "INSERT INTO capture_sets (name, surname, phone_number) VALUES (%s, %s, %s)",
        (name, surname, phone_number)
    )
    set_id = cursor.lastrowid
    
    # Fotoğrafları toplu olarak ekle (daha hızlı)
    photo_values = []
    for photo in photos:
        photo_values.append((set_id, photo['photo_type'], photo_path))
    
    if photo_values:
        cursor.executemany(
            "INSERT INTO photos (capture_set_id, photo_type, photo_path) VALUES (%s, %s, %s)",
            photo_values
        )
    
    db.commit()
    
    return jsonify({"message": "Success", "set_id": set_id}), 200
    
except Exception as e:
    db.rollback()
    return jsonify({"message": f"Error: {str(e)}", "set_id": None}), 500
```

### 4. Async İşlem (Opsiyonel)

Eğer hala yavaşsa, fotoğraf kaydetme işlemini async yapın:

```python
import threading

def save_photo_async(set_id, photo_type, base64_data):
    # Fotoğraf kaydetme işlemi
    pass

@app.route('/api/capture-sets', methods=['POST'])
def add_capture_set():
    # Veritabanına kaydet
    set_id = cursor.lastrowid
    
    # Fotoğrafları async kaydet
    for photo in photos:
        thread = threading.Thread(
            target=save_photo_async,
            args=(set_id, photo['photo_type'], photo['photo_url'])
        )
        thread.start()
    
    # Hemen response döndür (fotoğraflar arka planda kaydedilir)
    return jsonify({"message": "Success", "set_id": set_id}), 200
```

## 📊 Beklenen İyileştirmeler

Mobil uygulamada yaptığımız değişiklikler:
- ✅ Fotoğraf boyutu: 1920x1920 → **1280x1280**
- ✅ JPEG kalitesi: %85 → **%75**
- ✅ Timeout: 120s → **180s**
- ✅ Retry sayısı: 2 → **3**

Bu değişikliklerle:
- Dosya boyutu: ~1MB → **~500-600KB** (yaklaşık %40-50 azalma)
- Gönderme süresi: Daha hızlı
- Timeout riski: Daha düşük

## ✅ Test

1. Mobil uygulamayı yeniden derleyin
2. Python API'yi optimize edin (yukarıdaki kodları uygulayın)
3. Test edin ve logları kontrol edin

