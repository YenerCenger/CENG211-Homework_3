# Sliding Penguins Puzzle Game App 🐧❄️

**Ders:** CENG211 - Programming Fundamentals  
**Ödev:** #3

## 📋 Proje Tanımı
Bu proje, 3 penguenin 10x10 boyutundaki buzlu bir arazide (grid) yiyecek toplamak için rekabet ettiği Java tabanlı bir simülasyon oyunudur. Uygulama, **Kalıtım (Inheritance)**, **Çok Biçimlilik (Polymorphism)**, **Arayüzler (Interfaces)** ve **Soyut Sınıflar (Abstract Classes)** gibi temel Nesne Yönelimli Programlama (OOP) prensiplerini kapsamlı bir şekilde uygular.

Oyun, nesnelerin buz üzerinde kaydığı, tehlikelerle etkileşime girdiği (sektirme, delik tıkama, buz bloğu kaydırma) ve farklı ağırlıklardaki yiyeceklerin toplandığı gelişmiş bir fizik motoruna sahiptir.

## 🚀 Projeyi Çalıştırma
Bu proje **Visual Studio Code Java Projesi** olarak tasarlanmıştır.

1.  **Projeyi Açın:** Proje klasörünü VS Code ile açın.
2.  **Ana Sınıfı Bulun:** `src/app/SlidingPuzzleApp.java` dosyasına gidin.
3.  **Çalıştırın:** "Run" butonuna tıklayın veya `F5` tuşuna basın.
4.  **Kontroller:**
    * Konsol ekranındaki yönergeleri takip edin.
    * **Özel Yetenek:** `Y` (Evet) veya `N` (Hayır).
    * **Yön Seçimi:** `U` (Yukarı), `D` (Aşağı), `L` (Sol), `R` (Sağ).
    * *Not:* Girişlerde büyük/küçük harf duyarlılığı yoktur.

## 🏗️ Mimari ve Tasarım Kararları

Proje, kodun okunabilirliğini ve yönetilebilirliğini artırmak için modüler bir paket yapısına sahiptir:

```text
src/
└── com/ceng211/hw3/
    ├── app/          # Main sınıfı (Giriş Noktası)
    ├── core/         # Oyun motoru (IcyTerrain, Cell, Oyun Döngüsü)
    ├── entities/     # Varlık sınıfları (Penguin, Hazard, Food)
    │   ├── penguins/ # Özelleşmiş Penguen sınıfları (King, Royal vb.)
    │   └── hazards/  # Özelleşmiş Tehlike sınıfları (SeaLion, LightIceBlock vb.)
    ├── enums/        # Tip güvenliği için Enum'lar (Direction, HazardType vb.)
    └── interfaces/   # Davranış sözleşmeleri (ISlidable, ICollidable, ITerrainObject)
````

### Önemli Tasarım Detayları

1.  **`Cell` Sınıfı Stratejisi:**

      * Basit bir nesne dizisi kullanmak yerine, harita `ArrayList<ArrayList<Cell>>` olarak tasarlanmıştır.
      * Her `Cell` (Hücre), içinde `List<ITerrainObject>` barındırır. Bu sayede, ödev gereksinimlerine uygun olarak **birden fazla nesne** (örneğin bir Penguen ve bir Yemek) aynı karede çakışmadan bulunabilir.

2.  **Polimorfik Çarpışma Yönetimi (`ICollidable`):**

      * Tehlikeler (`Hazard`), `ICollidable` arayüzünü implemente eder.
      * Bir penguen bir engele çarptığında, `if-else` blokları yerine polimorfik `onCollision(Penguin p, IcyTerrain t)` metodu çağrılır.
      * Bu sayede her nesne kendi fizik kuralını uygular:
          * **SeaLion:** Pengueni geri sektirir ve kendisi momentum kazanarak kaymaya başlar.
          * **HoleInIce:** Pengueni oyun dışı bırakır (veya bir buz bloğu düşerse tıkanır).
          * **LightIceBlock:** Pengueni sersemletir (Stun) ve aksi yönde kaymaya başlar.

3.  **Özyinelemeli (Recursive) Kayma Fiziği:**

      * `ISlidable` arayüzünü kullanan nesneler (Penguenler, SeaLion, LightIceBlock), `slide()` metodunda **recursion** kullanır. Nesne, bir engele veya yemeğe çarpana kadar adım adım ilerler.

4.  **Yapay Zeka (AI) Mantığı:**

      * Oyuncu olmayan penguenler (AI), şu öncelik sırasına göre karar verir:
        1.  Yemek olan yöne git.
        2.  Güvenli (boş) alana git.
        3.  Mecbursa Tehlikeye git.
        4.  Son çare olarak Suya git.

## 🎮 Uygulanan Oyun Özellikleri

### 🐧 Penguen Türleri ve Özel Yetenekler

  * **Royal Penguin:** Kaymaya başlamadan önce **1 kare** güvenli adım atarak konumunu ayarlayabilir.
  * **Rockhopper Penguin:** Yolundaki ilk engelin üzerinden **zıplayabilir**.
  * **King Penguin:** Kayarken tam olarak **5. karede** durmayı (fren yapmayı) seçebilir.
  * **Emperor Penguin:** Kayarken tam olarak **3. karede** durmayı (fren yapmayı) seçebilir.

### ⚠️ Tehlikeler ve Fizik Kuralları

  * **Sabit Tehlikeler:**
      * `HeavyIceBlock (HB)`: Duvar görevi görür. Çarpan penguenin en hafif yemeğini düşürmesine neden olur.
      * `HoleInIce (HI)`: Ölümcül tuzak. İçine bir `LightIceBlock` veya `SeaLion` düşerse tıkanır **(PH)** ve geçilebilir hale gelir.
  * **Dinamik Tehlikeler (ISlidable):**
      * `LightIceBlock (LB)`: Tekmelediğinde kayar. Pengueni sersemletir (Stun). Yolundaki yemekleri ezer.
      * `SeaLion (SL)`: Pengueni geri sektirir (Bounce). Çarpışma anında kendisi de kaymaya başlar.

## 📝 Hata Denetimi (Input Validation)

Oyun, kullanıcı deneyimini iyileştirmek için tüm girişleri doğrular. Kullanıcı geçersiz bir karakter girdiğinde (örn: Yön için 'Z'), geçerli bir giriş (U/D/L/R) yapılana kadar oyun tekrar sorar.

```
```
