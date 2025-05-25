import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public enum TriangulationAlgorithmType {
        NAIVE_1,
        NAIVE_2,
    }

    public static void main(String[] args) {
        try {
            mainProgram();
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace(); 
        }
    }

    private static void mainProgram() throws Exception {
        deleteAllFiles("outputImages");

        // loadAllPolygons ve Polygon sınıfı bu kod bloğunda tanımlı değildir.
        // Bu yüzden, örnek bir boş liste ile devam ediyoruz.
        // Gerçek kullanım için bu metotların ve sınıfların tanımlanması gereklidir.
        // Eğer bu kısım derleme hatası verirse, lütfen Point ve Polygon sınıflarınızın
        // bu dosyanın dışında veya üstünde tanımlı olduğundan emin olun.
        List<Polygon> polygons = loadAllPolygons("inputData"); // Bu satır, dışarıdan Polygon ve Point bekler.


        int index = 1;
        for (Polygon polygon : polygons.subList(0, polygons.size())) { 
            createTriangles(polygon, index);

            if (index == 20)
                break; 
            index++;
        }
    }

    private static void createTriangles(Polygon polygon, int index) {
        // Polygon, Point ve Triangle sınıfları bu kod bloğunda tanımlı değildir.
        // Bu yüzden, bu metot şu an için çalışmayacaktır.
        // Derleme hatası verirse, ilgili sınıfların tanımlı olduğundan emin olun.
        List<Point> points = polygon.getPoints();
        List<Triangle> triangles = triangulate(points, TriangulationAlgorithmType.NAIVE_2);
        
        // PolygonVisualizer sınıfı tanımlı değilse bu satır hata verir.
        PolygonVisualizer.drawPolygon(
                "outputImages/polygon_" + index + ".png", polygon, triangles);
    }

    public static List<Triangle> triangulate(List<Point> points, TriangulationAlgorithmType type) {
        if (points == null || points.size() < 3) {
            return new ArrayList<>();
        }

        switch (type) {
            case NAIVE_1:
                return naiveTriangulate1(points);
            case NAIVE_2:
                return naiveTriangulate2(points);
            default:
                return null;
        }
    }

    private static List<Triangle> naiveTriangulate1(List<Point> points) {
        // Point ve Triangle sınıfları bu kod bloğunda tanımlı değildir.
        // Derleme hatası verirse, ilgili sınıfların tanımlı olduğundan emin olun.
        List<Triangle> triangles = new ArrayList<>();

        if (points.size() < 3)
            return triangles;

        Point anchor = points.get(0);

        for (int i = 1; i < points.size() - 1; i++) {
            Point b = points.get(i);
            Point c = points.get(i + 1);

            if (anchor.equals(b) || anchor.equals(c) || b.equals(c))
                continue;

            Triangle triangle = new Triangle(List.of(anchor, b, c));
            if (triangle.getArea() > 1e-10) {
                triangles.add(triangle);
                System.out.println("✅ Üçgen oluşturuldu: " + triangle.getPoints());
            } else {
                System.out.println("⚠️ Atlandı: alan çok küçük veya sıfır");
            }
        }

        return triangles;
    }

    /**
     * İkinci üçgenleme algoritması.
     * Bu metot, orijinal "ilk 4 noktayı al, dörtgeni işle, 2 noktayı çıkar" mantığını
     * recursive olarak uygular. Algoritma mantığına müdahale edilmemiştir.
     *
     * @param points Çokgenin köşe noktaları.
     * @return Oluşturulan üçgenlerin listesi.
     */
    private static List<Triangle> naiveTriangulate2(List<Point> points) {
        List<Triangle> triangles = new ArrayList<>();
        // List<Quadrilateral> processedQuadrilaterals = new ArrayList<>(); // Orijinal mantıkta bu liste kullanılmıyor.

        // Orijinal listeyi doğrudan değiştirmemek için bir kopya oluştur.
        List<Point> currentPoints = new ArrayList<>(points);

        System.out.println("Naive Triangulate 2 (recursive dörtgen işleme) algoritması çalıştırılıyor.");
        System.out.println("Başlangıç nokta sayısı: " + currentPoints.size());

        // Recursive yardımcı metodu çağır
        // rotationCount'u da recursive çağrılar arasında taşıyoruz.
        // Bu metot, orijinal while döngüsünün içindeki mantığı ve rotationCount'u yönetir.
        processQuadRecursive(currentPoints, triangles, 0); 
        
        System.out.println("naiveTriangulate2 algoritması tamamlandı. Toplam üçgen: " + triangles.size());
        return triangles;
    }

    /**
     * Orijinal naiveTriangulate2 algoritmasının döngü mantığını recursive olarak uygular.
     * Bu metot, Point, Triangle, Quadrilateral ve Segment sınıflarına,
     * isQuadrilateral, isDiagonalInternalAndNonIntersecting, orientation, onSegment, getSmallestAngleInRadians
     * gibi yardımcı metotlara bağımlıdır.
     *
     * @param currentPoints Çokgenin mevcut köşe noktaları (bu liste recursive çağrılarda değiştirilebilir).
     * @param resultTriangles Bulunan üçgenlerin ekleneceği liste.
     * @param rotationCount Mevcut recursive seviyede yapılan döndürme sayısı (sonsuz döngüyü engellemek için).
     */
    private static void processQuadRecursive(List<Point> currentPoints, List<Triangle> resultTriangles, int rotationCount) {
        // Temel Durum (Base Case): Çokgende 4'ten az nokta kaldıysa, dörtgen oluşturamayız.
        if (currentPoints.size() < 4) {
            if (currentPoints.size() == 3) {
                // Kalan 3 nokta son üçgeni oluşturur.
                resultTriangles.add(new Triangle(currentPoints));
                System.out.println("✅ Son üçgen oluşturuldu: " + currentPoints);
            } else {
                System.out.println("⚠️ Kalan nokta sayısı üçgen oluşturmak için yeterli değil (" + currentPoints.size() + " nokta).");
            }
            return; // Özyinelemeyi sonlandır
        }

        // --- Recursive Adım: Orijinal while döngüsünün içindeki mantık ---

        // Mevcut çokgenin ilk dört noktasını al.
        Point p0 = currentPoints.get(0);
        Point p1 = currentPoints.get(1);
        Point p2 = currentPoints.get(2);
        Point p3 = currentPoints.get(3);

        System.out.println("Kontrol edilen dörtlü: " + p0 + ", " + p1 + ", " + p2 + ", " + p3);

        // Bu dört noktanın bir dörtgen oluşturup oluşturmadığını kontrol et.
        // isQuadrilateral metodu ve onun bağımlılıkları tanımlı olmalıdır.
        if (isQuadrilateral(p0, p1, p2, p3)) {
            System.out.println("Dörtlü geçerli bir dörtgen oluşturuyor. İşleniyor...");

            // --- İnce üçgenlerden kaçınma ve köşegen seçimi ---
            // isDiagonalInternalAndNonIntersecting metodu ve onun bağımlılıkları da tanımlı olmalı.
            boolean diagonal1_valid = isDiagonalInternalAndNonIntersecting(p0, p2, p1, p3, currentPoints);
            boolean diagonal2_valid = isDiagonalInternalAndNonIntersecting(p1, p3, p0, p2, currentPoints);

            List<Triangle> tempTriangles1 = new ArrayList<>();
            List<Triangle> tempTriangles2 = new ArrayList<>();

            if (diagonal1_valid) {
                tempTriangles1.add(new Triangle(List.of(p0, p1, p2)));
                tempTriangles1.add(new Triangle(List.of(p0, p2, p3)));
            }
            if (diagonal2_valid) {
                tempTriangles2.add(new Triangle(List.of(p0, p1, p3)));
                tempTriangles2.add(new Triangle(List.of(p1, p2, p3)));
            }

            if (diagonal1_valid && diagonal2_valid) {
                double minAngle1 = Math.min(tempTriangles1.get(0).getSmallestAngleInRadians(), tempTriangles1.get(1).getSmallestAngleInRadians());
                double minAngle2 = Math.min(tempTriangles2.get(0).getSmallestAngleInRadians(), tempTriangles2.get(1).getSmallestAngleInRadians());

                if (minAngle1 >= minAngle2) {
                    resultTriangles.addAll(tempTriangles1);
                    System.out.println("    Her iki köşegen de geçerli. İlk köşegen (p0, p2) seçildi (daha az ince üçgen).");
                } else {
                    resultTriangles.addAll(tempTriangles2);
                    System.out.println("    Her iki köşegen de geçerli. İkinci köşegen (p1, p3) seçildi (daha az ince üçgen).");
                }
            } else if (diagonal1_valid) {
                resultTriangles.addAll(tempTriangles1);
                System.out.println("    Sadece köşegen (p0, p2) geçerli. Bu köşegen kullanılıyor.");
            } else if (diagonal2_valid) {
                resultTriangles.addAll(tempTriangles2);
                System.out.println("    Sadece köşegen (p1, p3) geçerli. Bu köşegen kullanılıyor.");
            } else {
                System.out.println("    ⚠️ Hata: Geçerli bir dörtgen olmasına rağmen geçerli iç köşegen bulunamadı. Üçgenlenemedi.");
                return; // Bu dörtgen işlenemiyor, recursive çağrıyı sonlandır.
            }

            // Dörtgen işlendikten ve üçgenler eklendikten sonra, aradaki noktaları çokgenden çıkar.
            // Talimat: "dışarda kalan 2 noktayı listeden çıkararak yapabilirsin"
            // Bu, P1 ve P2 noktalarını listeden çıkarmak anlamına gelir.
            currentPoints.remove(2); // p2'yi çıkar
            currentPoints.remove(1); // p1'i çıkar

            System.out.println("Noktalar çıkarıldı. Güncel nokta sayısı: " + currentPoints.size());
            System.out.println("Kalan noktalar: " + currentPoints);

            // Başarılı bir işlem sonrası, rotationCount'u sıfırla ve kalan çokgeni recursive işle.
            processQuadRecursive(currentPoints, resultTriangles, 0);

        } else {
            // Eğer ilk dörtlü bir dörtgen oluşturmuyorsa (kendi kendini kesiyor veya dejenere),
            // bu dörtlüden bir dörtgen koparamıyoruz.
            // Bu durumda, bir sonraki dörtlü setini denemek için listenin başındaki noktayı
            // listenin sonuna taşıyarak noktaları döndürüyoruz.
            // Orijinal rotationCount kontrolü
            if (rotationCount < currentPoints.size()) { 
                Point firstPoint = currentPoints.remove(0);
                currentPoints.add(firstPoint);
                System.out.println("Dörtlü geçerli bir dörtgen oluşturmuyor. Noktalar döndürüldü. Döndürme Sayısı: " + (rotationCount + 1));
                System.out.println("Yeni başlangıç: " + currentPoints.get(0) + ". Kalan nokta sayısı: " + currentPoints.size());

                // Recursive çağrı ile döndürülmüş noktaları işle, rotationCount'u artır.
                processQuadRecursive(currentPoints, resultTriangles, rotationCount + 1);
            } else {
                System.out.println("⚠️ Tüm noktalar döndürüldü ancak geçerli bir dörtgen bulunamadı. Döngü sonlandırılıyor.");
                // Bu çokgen, bu metot ile üçgenlenemedi.
            }
        }
    }

    // --- isQuadrilateral metodu (Main sınıfı içinde veya dışarıda tanımlı olmalı) ---
    /**
     * Verilen dört sıralı noktanın geçerli (kendi kendini kesmeyen ve dışbükey)
     * bir dörtgen oluşturup oluşturmadığını kontrol eder.
     *
     * @param p0 İlk nokta.
     * @param p1 İkinci nokta.
     * @param p2 Üçüncü nokta.
     * @param p3 Dördüncü nokta.
     * @return Noktalar geçerli bir dörtgen oluşturuyorsa true, aksi takdirde false.
     */
    public static boolean isQuadrilateral(Point p0, Point p1, Point p2, Point p3) {
        // Not: Bu metodun ve bağımlılıklarının (isConvex, doProperSegmentsIntersect, orientation, onSegment, onSegmentInterior)
        // bu dosyanın üstünde veya dışında tanımlı olduğundan emin olun.
        
        // 1. Noktaların birbirinden farklı olduğunu kontrol et
        if (p0.equals(p1) || p0.equals(p2) || p0.equals(p3) ||
            p1.equals(p2) || p1.equals(p3) ||
            p2.equals(p3)) {
            System.out.println("   [isQuadrilateral] Dörtgen oluşturulamadı: Noktalar aynı.");
            return false;
        }

        // 2. Ardışık üç noktanın doğrusal olup olmadığını kontrol et
        if (orientation(p0, p1, p2) == 0 ||
            orientation(p1, p2, p3) == 0 ||
            orientation(p2, p3, p0) == 0 ||
            orientation(p3, p0, p1) == 0) {
            System.out.println("   [isQuadrilateral] Ardışık üç nokta doğrusal.");
            return false;
        }

        // 3. Çapraz kenarların kesişip kesişmediğini kontrol et (Kendi kendini kesen dörtgenler için)
        if (doProperSegmentsIntersect(p0, p1, p2, p3) || doProperSegmentsIntersect(p1, p2, p3, p0)) {
            System.out.println("   [isQuadrilateral] Dörtgen kendi kendini kesiyor: Çapraz kenar kesişimi.");
            return false;
        }

        // 4. Dörtgenin Dışbükeyliğini Kontrol Etme
        if (!isConvex(p0, p1, p2, p3)) {
             System.out.println("   [isQuadrilateral] Dörtgen içbükeydir ve bu algoritmada dörtgen olarak kabul edilmez.");
             return false;
        }
        System.out.println("   [isQuadrilateral] Noktalar geçerli bir dörtgen oluşturuyor.");
        return true;
    }

    // --- isConvex metodu (Ana sınıf içinde veya dışarıda tanımlı olmalı) ---
    /**
     * Verilen dört sıralı noktanın dışbükey bir dörtgen oluşturup oluşturmadığını kontrol eder.
     * Tüm iç açılar 180 dereceden küçükse dışbükeydir.
     *
     * @param p0 İlk nokta.
     * @param p1 İkinci nokta.
     * @param p2 Üçüncü nokta.
     * @param p3 Dördüncü nokta.
     * @return Dörtgen dışbükey ise true, aksi takdirde false.
     */
    public static boolean isConvex(Point p0, Point p1, Point p2, Point p3) {
        // orientation 0 döndürmediği varsayılır (isQuadrilateral tarafından kontrol edilir).
        int firstOrientation = orientation(p0, p1, p2);
        if (firstOrientation == 0) { return false; } // Zaten isQuadrilateral tarafından ele alınmalıydı

        if (orientation(p1, p2, p3) != firstOrientation) { return false; }
        if (orientation(p2, p3, p0) != firstOrientation) { return false; }
        if (orientation(p3, p0, p1) != firstOrientation) { return false; }

        return true; // Tüm yönler aynıysa dışbükeydir.
    }

    // --- isDiagonalInternalAndNonIntersecting metodu (Ana sınıf içinde veya dışarıda tanımlı olmalı) ---
    /**
     * Bir köşegenin dörtgenin içinde olup olmadığını ve çokgenin kenarlarını kesip kesmediğini kontrol eder.
     *
     * @param dP1 Köşegenin birinci noktası.
     * @param dP2 Köşegenin ikinci noktası.
     * @param otherP1 Köşegenin ait olmadığı dörtgenin diğer noktalarından ilki.
     * @param otherP2 Köşegenin ait olmadığı dörtgenin diğer noktalarından ikincisi.
     * @param currentPolygonPoints Köşegenin çokgenin kenarlarını kesip kesmediğini kontrol etmek için mevcut çokgenin tüm noktaları.
     * @return Köşegen dörtgenin içindeyse ve çokgenin kenarlarını kesmiyorsa true, aksi takdirde false.
     */
    private static boolean isDiagonalInternalAndNonIntersecting(Point dP1, Point dP2, Point otherP1, Point otherP2, List<Point> currentPolygonPoints) {
        int orient_otherP1 = orientation(dP1, dP2, otherP1);
        int orient_otherP2 = orientation(dP1, dP2, otherP2);

        boolean isInternalByOrientation = false;
        if ((orient_otherP1 != 0 && orient_otherP2 != 0) && (orient_otherP1 != orient_otherP2)) {
            isInternalByOrientation = true;
        } else if (orient_otherP1 == 0 && onSegment(dP1, otherP1, dP2)) {
            isInternalByOrientation = true;
        } else if (orient_otherP2 == 0 && onSegment(dP1, otherP2, dP2)) {
            isInternalByOrientation = true;
        }

        if (!isInternalByOrientation) {
            System.out.println("    Köşegen (" + dP1 + ", " + dP2 + ") dörtgenin içinde değil (yön testi başarısız).");
            return false;
        }

        if (doesSegmentIntersectPolygonEdges(new Segment(dP1, dP2), currentPolygonPoints)) {
            System.out.println("    Köşegen (" + dP1 + ", " + dP2 + ") çokgenin kenarlarını kesiyor.");
            return false;
        }
        
        for (Point p : currentPolygonPoints) {
            if (p.equals(dP1) || p.equals(dP2)) {
                continue;
            }
            if (orientation(dP1, dP2, p) == 0 && onSegment(dP1, p, dP2)) {
                System.out.println("    Köşegen (" + dP1 + ", " + dP2 + ") üzerinde başka bir çokgen noktası (" + p + ") var.");
                return false;
            }
        }
        System.out.println("    Köşegen (" + dP1 + ", " + dP2 + ") içeride ve çokgen kenarlarını kesmiyor.");
        return true;
    }

    // --- doesSegmentIntersectPolygonEdges metodu (Ana sınıf içinde veya dışarıda tanımlı olmalı) ---
    /**
     * Verilen bir doğru parçasının (segmentToTest), belirtilen çokgenin mevcut kenarlarından herhangi birını
     * kesip kesmediğini kontrol eder. Kesişimin, doğru parçalarının uç noktaları dışında (yani 'proper intersection')
     * olması durumunda true döner.
     * Köşegenin kendi uç noktalarına bitişik olan kenarları kesişim kontrolüne dahil etmez.
     *
     * @param segmentToTest Kontrol edilecek doğru parçası (genellikle bir köşegen adayı).
     * @param polygonPoints Çokgenin tüm noktalarını içeren liste (köşegenin ait olduğu çokgen).
     * @return Eğer segment çokgenin başka bir kenarını kesiyorsa true, aksi takdirde false.
     */
    private static boolean doesSegmentIntersectPolygonEdges(Segment segmentToTest, List<Point> polygonPoints) {
        Point dP1 = segmentToTest.getStart();
        Point dP2 = segmentToTest.getEnd();

        int idx_dP1 = -1;
        int idx_dP2 = -1;
        for (int k = 0; k < polygonPoints.size(); k++) {
            if (polygonPoints.get(k).equals(dP1)) idx_dP1 = k;
            if (polygonPoints.get(k).equals(dP2)) idx_dP2 = k;
        }

        if (idx_dP1 == -1 || idx_dP2 == -1) {
             System.out.println("    Hata: doesSegmentIntersectPolygonEdges - Köşegenin uç noktaları çokgende bulunamadı.");
             return true; 
        }
        
        boolean isAlreadyAnEdge = (Math.abs(idx_dP1 - idx_dP2) == 1) ||
                                  (idx_dP1 == 0 && idx_dP2 == polygonPoints.size() - 1) ||
                                  (idx_dP2 == 0 && idx_dP1 == polygonPoints.size() - 1);
        if (isAlreadyAnEdge) {
            return false; 
        }

        for (int i = 0; i < polygonPoints.size(); i++) {
            Point edgeP1 = polygonPoints.get(i);
            Point edgeP2 = polygonPoints.get((i + 1) % polygonPoints.size());

            boolean isAdjacentToDP1 = (edgeP1.equals(polygonPoints.get((idx_dP1 - 1 + polygonPoints.size()) % polygonPoints.size())) && edgeP2.equals(dP1)) ||
                                      (edgeP1.equals(dP1) && edgeP2.equals(polygonPoints.get((idx_dP1 + 1) % polygonPoints.size())));

            boolean isAdjacentToDP2 = (edgeP1.equals(polygonPoints.get((idx_dP2 - 1 + polygonPoints.size()) % polygonPoints.size())) && edgeP2.equals(dP2)) ||
                                      (edgeP1.equals(dP2) && edgeP2.equals(polygonPoints.get((idx_dP2 + 1) % polygonPoints.size())));
            
            if (isAdjacentToDP1 || isAdjacentToDP2) {
                continue;
            }

            if (doProperSegmentsIntersect(dP1, dP2, edgeP1, edgeP2)) {
                return true; 
            }
        }
        return false; 
    }

    // --- doProperSegmentsIntersect metodu (Ana sınıf içinde veya dışarıda tanımlı olmalı) ---
    /**
     * İki doğru parçasının **sadece iç kısımlarının** kesişip kesişmediğini kontrol eder.
     * Uç noktalarda çakışma veya doğrusal çakışma durumlarını kesişim olarak saymaz.
     *
     * @param p1 İlk doğru parçasının başlangıç noktası.
     * @param q1 İlk doğru parçasının bitiş noktası.
     * @param p2 İkinci doğru parçasının başlangıç noktası.
     * @param q2 İkinci doğru parçasının bitiş noktası.
     * @return İki doğru parçasının iç kısımları kesişiyorsa true, aksi takdirde false.
     */
    private static boolean doProperSegmentsIntersect(Point p1, Point q1, Point p2, Point q2) {
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);

        // Genel durum: Karşılıklı yönlerde ve doğrusal değillerse içten kesişirler.
        if (o1 != 0 && o2 != 0 && o3 != 0 && o4 != 0 && (o1 != o2) && (o3 != o4)) {
            return true;
        }

        // Doğrusal durumlar: Bir segmentin ucunun diğer segmentin *içinde* olup olmadığını kontrol et.
        // onSegmentInterior metodunu kullanarak ve uç noktaları hariç tutarak.
        if (o1 == 0 && onSegmentInterior(p1, p2, q1)) return true; 
        if (o2 == 0 && onSegmentInterior(p1, q2, q1)) return true; 
        if (o3 == 0 && onSegmentInterior(p2, p1, q2)) return true; 
        if (o4 == 0 && onSegmentInterior(p2, q1, q2)) return true; 

        return false;
    }

    // --- orientation metodu (Ana sınıf içinde veya dışarıda tanımlı olmalı) ---
    /**
     * Üç noktanın (p, q, r) yönünü belirler.
     * Kayan nokta hassasiyeti için daha küçük bir eşik kullanır.
     *
     * @param p İlk nokta.
     * @param q İkinci nokta.
     * @param r Üçüncü nokta.
     * @return 0: doğrusal, 1: saat yönünün tersi (counter-clockwise), -1: saat yönü (clockwise).
     */
    private static int orientation(Point p, Point q, Point r) {
        double val = (q.getY() - p.getY()) * (r.getX() - q.getX()) -
                     (q.getX() - p.getX()) * (r.getY() - q.getY());

        if (Math.abs(val) < 1e-12) return 0; 
        return (val > 0) ? 1 : -1; 
    }

    // --- onSegmentInterior metodu (Ana sınıf içinde veya dışarıda tanımlı olmalı) ---
    /**
     * Bir noktanın (q) başka bir doğru parçasının (pr) **iç kısmında** olup olmadığını kontrol eder.
     * Uç noktalar dahil değildir. Bu metot sadece noktaların doğrusal olduğu durumlarda kullanılmalıdır.
     *
     * @param p Doğru parçasının başlangıç noktası.
     * @param q Kontrol edilecek nokta.
     * @param r Doğru parçasının bitiş noktası.
     * @return q noktası pr doğru parçasının iç kısmındaysa true, aksi takdirde false.
     */
    private static boolean onSegmentInterior(Point p, Point q, Point r) {
        return q.getX() > Math.min(p.getX(), r.getX()) && q.getX() < Math.max(p.getX(), r.getX()) &&
               q.getY() > Math.min(p.getY(), r.getY()) && q.getY() < Math.max(p.getY(), r.getY());
    }

    // --- onSegment metodu (Ana sınıf içinde veya dışarıda tanımlı olmalı) ---
    /**
     * Bir noktanın (q) başka bir doğru parçasının (pr) üzerinde olup olmadığını kontrol eder.
     * Bu metot sadece noktaların doğrusal olduğu durumlarda kullanılmalıdır.
     *
     * @param p Doğru parçasının başlangıç noktası.
     * @param q Kontrol edilecek nokta.
     * @param r Doğru parçasının bitiş noktası.
     * @return q noktası pr doğru parçası üzerindeyse true, aksi takdirde false.
     */
    private static boolean onSegment(Point p, Point q, Point r) {
        return q.getX() <= Math.max(p.getX(), r.getX()) && q.getX() >= Math.min(p.getX(), r.getX()) &&
               q.getY() <= Math.max(p.getY(), r.getY()) && q.getY() >= Math.min(p.getY(), r.getY());
    }

    // --- loadAllPolygons metodu (Sizin orijinal Main sınıfınızdan) ---
    public static List<Polygon> loadAllPolygons(String inputDirectoryPath) throws Exception {
        List<Polygon> polygons = new ArrayList<>();

        List<Path> txtFiles = Files.list(Paths.get(inputDirectoryPath))
                .filter(path -> path.toString().endsWith(".txt"))
                .sorted()
                .collect(Collectors.toList());

        for (Path filePath : txtFiles) {
            List<Point> points = new ArrayList<>();
            List<String> lines = Files.readAllLines(filePath);

            for (String line : lines) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 2) {
                    double x = Double.parseDouble(parts[0].replace(",", "."));
                    double y = Double.parseDouble(parts[1].replace(",", "."));
                    points.add(new Point(x, y));
                }
            }

            if (points.size() > 1 && points.get(0).equals(points.get(points.size() - 1))) {
                points.remove(points.size() - 1);
            }

            // Polygon sınıfı tanımlı olmalı.
            Polygon polygon = new Polygon(points);
            polygons.add(polygon);
        }

        return polygons;
    }

    // --- deleteAllFiles metodu (Sizin orijinal Main sınıfınızdan) ---
    private static void deleteAllFiles(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            if (Files.exists(path) && Files.isDirectory(path)) {
                Files.list(path)
                        .filter(Files::isRegularFile)
                        .forEach(file -> {
                            try {
                                Files.delete(file);
                            } catch (IOException e) {
                                System.out.println("⚠️ Error deleting file: " + file + " - " + e.getMessage());
                            }
                        });
            } else {
                Files.createDirectories(path);
            }
            System.out.println("Klasör temizlendi/oluşturuldu: " + directoryPath);
        } catch (IOException e) {
            System.out.println("⚠️ Error managing directory: " + e.getMessage());
        }
    }
}