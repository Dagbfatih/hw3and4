# Çokgen Üçgenleme Algoritması

# NOT:
Bu algoritma, dışbükey cisimlerde genellikle başarısız sonuç verebilmektedir. Projeye geç başladığım için daha iyisini yapamadım. Keyifli okumalar... 

## İçindekiler

1.  [Proje Tanımı](#1-proje-tanımı)
2.  [Problem Tanımı](#2-problem-tanımı)
3.  [Algoritma Yaklaşımı](#3-algoritma-yaklaşımı)
    * [Genel Bakış](#genel-bakış)
    * [Temel Adımlar](#temel-adımlar)
    * [Önemli Kontroller](#önemli-kontroller)
4.  [Kod Yapısı](#4-kod-yapısı)
    * [Ana Sınıflar](#ana-sınıflar)
    * [Yardımcı Geometrik Metotlar](#yardımcı-geometrik-metotlar)
5.  [Girdi ve Çıktı Formatları](#5-girdi-ve-çıktı-formatları)
6.  [Proje Nasıl Çalıştırılır?](#6-proje-nasıl-çalıştırılır)
7.  [Bilinen Sorunlar ve Kısıtlamalar](#7-bilinen-sorunlar-ve-kısıtlamalar)
8.  [Gelecek Geliştirmeler](#8-gelecek-geliştirmeler)

---

## 1. Proje Tanımı

Bu proje, dışbükey olmayan (non-convex) çokgenleri üçgenlere ayırmak için özyinelemeli bir algoritma geliştirmeyi amaçlamaktadır. Geliştirilen algoritma, "ince üçgenlerden" kaçınmaya çalışır ve harici üçgenleme kütüphaneleri veya bilinen algoritmaların (Ear Clipping, Delaunay Triangulation vb.) doğrudan kullanımını veya taklit edilmesini yasaklayan kısıtlamalara uyar.

## 2. Problem Tanımı

Verilen bir çokgenin (köşeleri saat yönünde sıralı), iç içe geçmeyen ve toplam alanı orijinal çokgenin alanına eşit olan bir dizi üçgene ayrılması gerekmektedir. Üçgenlerin kenarları, diğer üçgenlerin kenarlarıyla veya orijinal çokgenin kenarlarıyla kesişmemelidir. Özellikle ince (bir kenarı diğerlerine göre çok küçük) üçgenlerden kaçınılması istenir.

Girdi çokgenleri `.txt` dosyalarında (her satırda x ve y koordinatları) sağlanır ve çıktı, oluşturulan çokgen ve üçgenleri gösteren `.png` dosyaları olarak `outputImages` klasörüne kaydedilir.

## 3. Algoritma Yaklaşımı

### Genel Bakış

Algoritma, `naiveTriangulate2` olarak adlandırılan özyinelemeli bir yaklaşıma dayanmaktadır. Temel fikir, çokgeni adım adım daha küçük parçalara ayırarak (dörtgenler veya üçgenler) üçgenleme işlemini gerçekleştirmektir. Her adımda, çokgenin başından dört köşe alınır ve bu dört köşenin geçerli bir dışbükey dörtgen oluşturup oluşturmadığı kontrol edilir. Eğer geçerliyse, bu dörtgen üçgenlere ayrılır ve çokgenden ilgili köşeler çıkarılarak kalan çokgen için özyinelemeli olarak devam edilir.

### Temel Adımlar

1.  **Özyinelemeli Fonksiyon (`processQuadRecursive`):**
    * **Temel Durum (Base Case):** Eğer çokgende 3 nokta kalırsa, bu son üçgen olarak kabul edilir ve sonuç listesine eklenir. Eğer 3'ten az nokta kalırsa, üçgenleme durdurulur (dejenere durum).
    * **Özyinelemeli Adım:**
        * Mevcut çokgenin ilk dört köşesi (`p0, p1, p2, p3`) alınır.
        * Bu dört köşenin `isQuadrilateral` metodu ile geçerli bir dışbükey dörtgen oluşturup oluşturmadığı kontrol edilir.
        * **Eğer Geçerli Bir Dörtgen İse:**
            * Dörtgenin iki olası köşegeni (`p0-p2` ve `p1-p3`) değerlendirilir.
            * `isDiagonalInternalAndNonIntersecting` metodu kullanılarak her iki köşegenin de çokgenin içinde kalıp kalmadığı ve başka kenarları kesip kesmediği kontrol edilir.
            * Geçerli olan köşegenler arasında, `getSmallestAngleInRadians` metodu ile oluşturulacak üçgenlerin en küçük açısı hesaplanır. En büyük en küçük açıyı veren (yani en az ince üçgenler oluşturan) köşegen tercih edilir.
            * Seçilen köşegen ile oluşan iki üçgen, sonuç listesine eklenir.
            * Dörtgenin `p1` ve `p2` köşeleri (aradaki köşeler) çokgen listesinden çıkarılır.
            * `processQuadRecursive` metodu, kalan (küçültülmüş) çokgen listesiyle özyinelemeli olarak tekrar çağrılır.
        * **Eğer Geçerli Bir Dörtgen Değil İse:**
            * Çokgenin ilk köşesi (`p0`) listenin sonuna taşınır (rotasyon). Bu, bir sonraki özyinelemeli çağrıda farklı bir dörtlü setinin kontrol edilmesini sağlar.
            * Sonsuz döngüyü engellemek için bir `rotationCount` kullanılır. Eğer tüm noktalar bir tur döndürüldüğü halde geçerli bir dörtgen bulunamazsa, algoritma durdurulur.

### Önemli Kontroller

Algoritma, üçgenlemenin doğruluğunu sağlamak için çeşitli geometrik kontrolleri kullanır:

* **`orientation(p, q, r)`:** Üç noktanın doğrusal, saat yönü veya saat yönünün tersi olup olmadığını belirler. Kayan nokta hassasiyeti için bir eşik değeri (`1e-12`) kullanır.
* **`onSegment(p, q, r)`:** Bir noktanın bir doğru parçası üzerinde olup olmadığını kontrol eder (uç noktalar dahil).
* **`onSegmentInterior(p, q, r)`:** Bir noktanın bir doğru parçasının **iç kısmında** olup olmadığını kontrol eder (uç noktalar hariç).
* **`doProperSegmentsIntersect(p1, q1, p2, q2)`:** İki doğru parçasının **sadece iç kısımlarının** kesişip kesişmediğini kontrol eder. Uç nokta çakışmaları veya doğrusal çakışmalar "gerçek kesişim" olarak sayılmaz.
* **`isQuadrilateral(p0, p1, p2, p3)`:** Dört noktanın geçerli (farklı noktalar, ardışık doğrusal olmayan, kendi kendini kesmeyen ve dışbükey) bir dörtgen oluşturup oluşturmadığını kontrol eder.
* **`isConvex(p0, p1, p2, p3)`:** Dörtgenin dışbükey olup olmadığını, tüm iç açılarının 180 dereceden küçük olup olmadığını yön testleriyle kontrol ederek belirler.
* **`doesSegmentIntersectPolygonEdges(segmentToTest, polygonPoints)`:** Bir köşegen adayının, çokgenin *diğer* kenarlarını (kendi uç noktalarına bitişik olmayan) kesip kesmediğini kontrol eder. Bu, mavi çizgilerin kırmızı çokgen kenarlarını kesmesini engellemek için kritik bir kontrol noktasıdır.
* **`isPointInTriangle(testPoint, t1, t2, t3)`:** Bir noktanın bir üçgenin içinde (kenarlar dahil) olup olmadığını kontrol eder.
* **`getSmallestAngleInRadians()` (Triangle sınıfında):** Oluşturulan üçgenlerin kalitesini değerlendirmek ve "ince üçgenlerden kaçınmak" için kullanılır.

## 4. Kod Yapısı

Proje, nesne yönelimli prensiplere uygun olarak çeşitli sınıflara ayrılmıştır:

### Ana Sınıflar

* **`Main` (veya `PolygonTriangulator`):** Ana program akışını yönetir. Çokgenleri yükler, üçgenleme algoritmalarını çağırır ve çıktıları görselleştirir. `mainProgram`, `createTriangles`, `triangulate`, `naiveTriangulate1`, `naiveTriangulate2` ve `processQuadRecursive` metotlarını içerir.
* **`Point`:** İki boyutlu bir noktayı temsil eden immutable bir sınıftır.
* **`Geometry`:** `Point` listesi tutan ve `getArea()` gibi ortak metotları tanımlayan soyut bir sınıftır.
* **`Triangle`:** `Geometry`'den türetilmiş, 3 noktadan oluşan bir üçgeni temsil eder. Alan ve en küçük açı hesaplamaları yapar.
* **`Polygon`:** `Geometry`'den türetilmiş, çokgeni temsil eder. Noktaları saat yönünde sıralar ve alan hesaplar.
* **`Quadrilateral`:** `Geometry`'den türetilmiş, 4 noktadan oluşan bir dörtgeni temsil eder. Alan hesaplaması yapar.
* **`Segment`:** İki `Point`'ten oluşan bir doğru parçasını temsil eder.

### Yardımcı Geometrik Metotlar

`Main` (veya `PolygonTriangulator`) sınıfı içinde veya ayrı yardımcı sınıflar olarak tanımlanmış, yukarıda "Önemli Kontroller" başlığı altında açıklanan tüm geometrik yardımcı metotlar.

## 5. Girdi ve Çıktı Formatları

* **Girdi:** `inputData` klasöründe bulunan `.txt` dosyaları. Her dosya, bir çokgenin köşelerini temsil eden satır başına bir `x y` koordinat çifti içerir. (Örnek: `0.0 0.0\n4.0 0.0\n2.0 1.0\n...`)
* **Çıktı:** `outputImages` klasörüne kaydedilen `.png` dosyaları. Her çokgen için iki dosya oluşturulur: `[index]_polygon.png` (orijinal çokgen) ve `[index]_triangles.png` (üçgenlenmiş çokgen). (Görselleştirme `PolygonVisualizer` yer tutucu sınıfı tarafından yapılır, gerçek implementasyon harici bir grafik kütüphanesi gerektirir).

## 6. Proje Nasıl Çalıştırılır?

1.  **Java Geliştirme Ortamı:** Projeyi çalıştırmak için bir Java Geliştirme Kiti (JDK) kurulu olmalıdır.
2.  **Dosya Yapısı:**
    * `src/` klasörü altında tüm `.java` dosyalarınızı (Point.java, Triangle.java, Polygon.java, Quadrilateral.java, Segment.java, PolygonVisualizer.java, Main.java) yerleştirin.
    * `inputData/` adında bir klasör oluşturun ve içine test çokgenlerinizin `.txt` dosyalarını yerleştirin.
    * `outputImages/` adında boş bir klasör oluşturun. Algoritma çıktıları buraya kaydedecektir.
3.  **Derleme:** Komut istemcisini (terminal) `src/` klasörünün olduğu dizinde açın ve aşağıdaki komutu kullanarak Java dosyalarını derleyin:
    ```bash
    javac *.java
    ```
    veya eğer Main sınıfınızın adı `PolygonTriangulator` ise:
    ```bash
    javac PolygonTriangulator.java Point.java Triangle.java Polygon.java Quadrilateral.java Segment.java PolygonVisualizer.java
    ```
4.  **Çalıştırma:** Derleme başarılı olduktan sonra, uygulamayı aşağıdaki komutla çalıştırın:
    ```bash
    java Main
    ```
    veya eğer Main sınıfınızın adı `PolygonTriangulator` ise:
    ```bash
    java PolygonTriangulator
    ```
    Program, `inputData` klasöründeki çokgenleri işleyecek ve `outputImages` klasörüne sonuçları kaydedecektir. Konsol çıktısı, algoritmanın her adımını detaylı olarak gösterecektir.

## 7. Bilinen Sorunlar ve Kısıtlamalar

* **Görselleştirme:** `PolygonVisualizer` sınıfı sadece bir yer tutucudur. Gerçek görsel çıktıları (`.png` dosyaları) elde etmek için Java2D, AWT veya başka bir grafik kütüphanesi kullanarak bu sınıfın implementasyonunu tamamlamanız gerekmektedir.
* **Karmaşık İçbükey Çokgenler:** `naiveTriangulate2` algoritması, "ilk 4 noktayı alıp dörtgeni işle" prensibine dayanmaktadır. Bu yaklaşım, bazı karmaşık içbükey çokgenler için (örneğin, çok sayıda içbükey köşesi olan veya delikli çokgenler) her zaman optimal veya doğru bir üçgenleme üretemeyebilir. Algoritma, bazen geçerli bir dörtgen bulmakta zorlanabilir veya yanlış üçgenler oluşturabilir.
* **Kayan Nokta Hassasiyeti:** Geometrik hesaplamalardaki (özellikle `orientation` ve `doProperSegmentsIntersect` gibi metotlardaki) kayan nokta hassasiyeti sorunları, nadiren de olsa yanlış kesişim algılamalarına veya doğrusal olma hatalarına yol açabilir. Bu durumlar için `1e-12` gibi küçük bir tolerans kullanılmıştır.
* **Ödev Kısıtlamaları:** Algoritma, ödevdeki "bilinen yöntemleri kullanmayın veya taklit etmeyin" kısıtlamasına uymaya çalışırken, bazı durumlarda daha karmaşık geometrik durumları ele almakta zorlanabilir.

## 8. Gelecek Geliştirmeler

* **Daha Sağlam İçbükey Çokgen Triangülasyonu:** `naiveTriangulate2`'nin temel prensibini koruyarak, içbükey çokgenleri daha güvenilir bir şekilde işlemek için geliştirilebilir. Örneğin, her zaman bir "kulak" bulmaya odaklanmak yerine, çokgeni dışbükey parçalara ayıran veya daha karmaşık bir "kes-böl" stratejisi uygulayan hibrit bir yaklaşım düşünülebilir.
* **Üçgen Kalitesini İyileştirme:** `getSmallestAngleInRadians` kullanılarak ince üçgenlerden kaçınma çabası daha da geliştirilebilir. Örneğin, birden fazla geçerli kulak adayı olduğunda, sadece en küçük açıyı değil, aynı zamanda kenar oranlarını da dikkate alarak daha dengeli üçgenler üretecek bir metrik kullanılabilir.
* **Hata Yönetimi ve Geri Bildirim:** Algoritma bir çokgeni üçgenleyemediğinde daha açıklayıcı hata mesajları veya görsel geri bildirimler sağlanabilir.
* **Performans Optimizasyonu:** Büyük çokgenler için algoritmanın performansını artırmak amacıyla indeksleme yapıları veya daha verimli döngüler kullanılabilir.
* **Görselleştirme Modülünün Tamamlanması:** `PolygonVisualizer` sınıfının gerçek implementasyonu, çıktıları daha anlaşılır hale getirecektir.
