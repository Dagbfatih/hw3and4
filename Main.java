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
        }
    }

    private static void mainProgram() throws Exception {
        deleteAllFiles("outputImages");

        List<Polygon> polygons = loadAllPolygons("inputData");

        int index = 1;
        for (Polygon polygon : polygons.subList(0, polygons.size())) {
            createTriangles(polygon, index);

            if (index == 20)
                break; // sadece ilk 100 polygon için işlem
            index++;
        }
    }

    private static void createTriangles(Polygon polygon, int index) {
        List<Point> points = polygon.getPoints();
        List<Triangle> triangles = triangulate(points, TriangulationAlgorithmType.NAIVE_2);

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
        List<Triangle> triangles = new ArrayList<>();

        if (points.size() < 3)
            return triangles;

        // 1. nokta sabit, sıradaki her iki nokta ile üçgen oluştur
        Point anchor = points.get(0);

        for (int i = 1; i < points.size() - 1; i++) {
            Point b = points.get(i);
            Point c = points.get(i + 1);

            // Aynı nokta ile üçgen oluşturulmasın
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

    private static List<Triangle> naiveTriangulate2(List<Point> points) {
        List<Triangle> triangles = new ArrayList<>();
        List<Quadrilateral> processedQuadrilaterals = new ArrayList<>(); // İşlenen dörtgenleri tutmak için

        // Orijinal listeyi doğrudan değiştirmemek için bir kopya oluştur.
        List<Point> currentPoints = new ArrayList<>(points);

        System.out.println("Naive Triangulate 2 (kademeli dörtgen işleme) algoritması çalıştırılıyor.");
        System.out.println("Başlangıç noktası sayısı: " + currentPoints.size());

        int rotationCount = 0; // Sonsuz döngüyü engellemek için döndürme sayacı

        // Çokgende 4 veya daha fazla nokta olduğu sürece döngü devam eder.
        while (currentPoints.size() >= 4) {
            // Mevcut çokgenin ilk dört noktasını al.
            Point p0 = currentPoints.get(0);
            Point p1 = currentPoints.get(1);
            Point p2 = currentPoints.get(2);
            Point p3 = currentPoints.get(3);

            System.out.println("Kontrol edilen dörtlü: " + p0 + ", " + p1 + ", " + p2 + ", " + p3);

            // Bu dört noktanın bir dörtgen oluşturup oluşturmadığını kontrol et.
            if (isQuadrilateral(p0, p1, p2, p3)) { // isQuadrilateral şimdi isConvex kontrolünü içeriyor
                System.out.println("Dörtlü geçerli bir dörtgen oluşturuyor. İşleniyor...");

                // Dörtgeni kaydet
                Quadrilateral currentQuad = new Quadrilateral(List.of(p0, p1, p2, p3));
                processedQuadrilaterals.add(currentQuad);

                // --- İnce üçgenlerden kaçınma ve köşegen seçimi ---
                // İki olası köşegen: (p0, p2) ve (p1, p3)
                // Her bir köşegenin oluşturacağı üçgenleri ve onların en küçük açılarını
                // hesapla.

                // isDiagonalInternalAndNonIntersecting metoduna currentPoints listesini
                // geçiyoruz.
                // Bu metot, köşegenin çokgen kenarlarını kesip kesmediğini de kontrol eder.
                boolean diagonal1_valid = isDiagonalInternalAndNonIntersecting(p0, p2, p1, p3, currentPoints); // (q0,
                                                                                                               // q2)
                boolean diagonal2_valid = isDiagonalInternalAndNonIntersecting(p1, p3, p0, p2, currentPoints); // (q1,
                                                                                                               // q3)

                List<Triangle> tempTriangles1 = new ArrayList<>(); // (p0,p1,p2) ve (p0,p2,p3)
                List<Triangle> tempTriangles2 = new ArrayList<>(); // (p0,p1,p3) ve (p1,p2,p3)

                // Sadece geçerli olan köşegenler için üçgenleri oluştur
                if (diagonal1_valid) {
                    tempTriangles1.add(new Triangle(List.of(p0, p1, p2)));
                    tempTriangles1.add(new Triangle(List.of(p0, p2, p3)));
                }
                if (diagonal2_valid) {
                    tempTriangles2.add(new Triangle(List.of(p0, p1, p3)));
                    tempTriangles2.add(new Triangle(List.of(p1, p2, p3)));
                }

                if (diagonal1_valid && diagonal2_valid) {
                    // Her iki köşegen de geçerli (dışbükey dörtgen). İnce üçgenlerden kaçınmak için
                    // seçim yap.
                    double minAngle1 = Math.min(tempTriangles1.get(0).getSmallestAngleInRadians(),
                            tempTriangles1.get(1).getSmallestAngleInRadians());
                    double minAngle2 = Math.min(tempTriangles2.get(0).getSmallestAngleInRadians(),
                            tempTriangles2.get(1).getSmallestAngleInRadians());

                    if (minAngle1 >= minAngle2) { // İlk köşegen (p0,p2) daha iyi veya eşit
                        triangles.addAll(tempTriangles1);
                        System.out.println(
                                "    Her iki köşegen de geçerli. İlk köşegen (p0, p2) seçildi (daha az ince üçgen).");
                    } else { // İkinci köşegen (p1,p3) daha iyi
                        triangles.addAll(tempTriangles2);
                        System.out.println(
                                "    Her iki köşegen de geçerli. İkinci köşegen (p1, p3) seçildi (daha az ince üçgen).");
                    }
                } else if (diagonal1_valid) {
                    // Sadece ilk köşegen (p0,p2) geçerli (içbükey dörtgen)
                    triangles.addAll(tempTriangles1);
                    System.out.println("    Sadece köşegen (p0, p2) geçerli. Bu köşegen kullanılıyor.");
                } else if (diagonal2_valid) {
                    // Sadece ikinci köşegen (p1,p3) geçerli (içbükey dörtgen)
                    triangles.addAll(tempTriangles2);
                    System.out.println("    Sadece köşegen (p1, p3) geçerli. Bu köşegen kullanılıyor.");
                } else {
                    // isQuadrilateral metodu geçerli bir dörtgen garanti ettiği için bu durum
                    // teorik olarak oluşmamalıdır.
                    // Geçerli bir basit dörtgende en az bir iç köşegen olmalıdır.
                    System.out.println(
                            "    ⚠️ Hata: Geçerli bir dörtgen olmasına rağmen geçerli iç köşegen bulunamadı. Üçgenlenemedi.");
                    // Bu durumda, bu dörtgeni üçgenleyemediğimiz için döngüyü sonlandırabiliriz
                    // veya farklı bir strateji deneyebiliriz.
                    // Şimdilik, ilerleyemediğimiz için döngüden çıkalım.
                    break;
                }

                // Dörtgen işlendikten ve üçgenler eklendikten sonra, aradaki noktaları
                // çokgenden çıkar.
                // Talimat: "dışarda kalan 2 noktayı listeden çıkararak yapabilirsin"
                // Bu, P1 ve P2 noktalarını listeden çıkarmak anlamına gelir.
                currentPoints.remove(2); // p2'yi çıkar
                currentPoints.remove(1); // p1'i çıkar

                System.out.println("Noktalar çıkarıldı. Güncel nokta sayısı: " + currentPoints.size());
                System.out.println("Kalan noktalar: " + currentPoints);

                rotationCount = 0; // Başarılı bir işlem sonrası sayacı sıfırla
            } else {
                // Eğer ilk dörtlü bir dörtgen oluşturmuyorsa (kendi kendini kesiyor veya
                // dejenere),
                // bu dörtlüden bir dörtgen koparamıyoruz.
                // Bu durumda, bir sonraki dörtlü setini denemek için listenin başındaki noktayı
                // listenin sonuna taşıyarak noktaları döndürüyoruz.
                if (currentPoints.size() > 0) {
                    Point firstPoint = currentPoints.remove(0);
                    currentPoints.add(firstPoint);
                    rotationCount++;
                    System.out.println("Dörtlü geçerli bir dörtgen oluşturmuyor. Noktalar döndürüldü. Döndürme Sayısı: "
                            + rotationCount);
                    System.out.println("Yeni başlangıç: " + currentPoints.get(0) + ". Kalan nokta sayısı: "
                            + currentPoints.size());

                    if (rotationCount >= currentPoints.size()) {
                        System.out.println(
                                "⚠️ Tüm noktalar döndürüldü ancak geçerli bir dörtgen bulunamadı. Döngü sonlandırılıyor.");
                        break;
                    }
                } else {
                    System.out.println("currentPoints boşaldı, döngü sonlanıyor.");
                    break;
                }
            }
        }

        // Döngü bittiğinde, `currentPoints` listesinde 3 veya daha az nokta kalmış
        // demektir.
        // Eğer tam olarak 3 nokta kaldıysa, bu son çokgeni bir üçgen olarak ekle.
        if (currentPoints.size() == 3) {
            Triangle finalTriangle = new Triangle(currentPoints);
            triangles.add(finalTriangle);
            System.out.println("✅ Son üçgen oluşturuldu: " + finalTriangle.getPoints());
        } else if (currentPoints.size() < 3) {
            System.out.println(
                    "⚠️ Kalan nokta sayısı üçgen oluşturmak için yeterli değil (" + currentPoints.size() + " nokta).");
        }

        System.out.println(
                "naiveTriangulate2 algoritması tamamlandı. Toplam bulunan dörtgen: " + processedQuadrilaterals.size());
        System.out.println("Toplam üçgen: " + triangles.size());
        return triangles;
    }

    /**
     * Bir köşegenin dörtgenin içinde olup olmadığını ve çokgenin kenarlarını kesip
     * kesmediğini kontrol eder.
     * Bu, bir köşegenin geçerli olup olmadığını belirlemek için kritik bir
     * fonksiyondur.
     *
     * @param dP1                  Köşegenin birinci noktası.
     * @param dP2                  Köşegenin ikinci noktası.
     * @param otherP1              Köşegenin ait olmadığı dörtgenin diğer
     *                             noktalarından ilki.
     * @param otherP2              Köşegenin ait olmadığı dörtgenin diğer
     *                             noktalarından ikincisi.
     * @param currentPolygonPoints Köşegenin çokgenin kenarlarını kesip kesmediğini
     *                             kontrol etmek için mevcut çokgenin tüm noktaları.
     * @return Köşegen dörtgenin içindeyse ve çokgenin kenarlarını kesmiyorsa true,
     *         aksi takdirde false.
     */
    private static boolean isDiagonalInternalAndNonIntersecting(Point dP1, Point dP2, Point otherP1, Point otherP2,
            List<Point> currentPolygonPoints) {
        // 1. Köşegenin dörtgenin içinde olup olmadığını kontrol et (yön testi)
        // dP1-dP2 köşegenine göre otherP1 ve otherP2 zıt yönlerde olmalı
        int orient_otherP1 = orientation(dP1, dP2, otherP1);
        int orient_otherP2 = orientation(dP1, dP2, otherP2);

        boolean isInternalByOrientation = false;
        if ((orient_otherP1 != 0 && orient_otherP2 != 0) && (orient_otherP1 != orient_otherP2)) {
            isInternalByOrientation = true;
        }
        // Özel durumlar: Eğer otherP1 veya otherP2, köşegen üzerinde ise ve segment
        // üzerinde ise.
        else if (orient_otherP1 == 0 && onSegment(dP1, otherP1, dP2)) {
            isInternalByOrientation = true;
        } else if (orient_otherP2 == 0 && onSegment(dP1, otherP2, dP2)) {
            isInternalByOrientation = true;
        }

        if (!isInternalByOrientation) {
            System.out.println("    Köşegen (" + dP1 + ", " + dP2 + ") dörtgenin içinde değil (yön testi başarısız).");
            return false;
        }

        // 2. Köşegenin (dP1, dP2) mevcut çokgenin (currentPolygonPoints) hiçbir
        // kenarını kesmediğini kontrol et.
        // Bu kontrolü doesSegmentIntersectPolygonEdges metoduna devrediyoruz.
        // Bu metod, köşegenin kendi uç noktalarına bitişik olan kenarları atlar.
        if (doesSegmentIntersectPolygonEdges(new Segment(dP1, dP2), currentPolygonPoints)) {
            System.out.println("    Köşegen (" + dP1 + ", " + dP2 + ") çokgenin kenarlarını kesiyor.");
            return false;
        }

        // Köşegenin iç kısmında çokgenin başka bir noktası olup olmadığını kontrol et.
        // Bu, içbükeylik durumlarında köşegenin "içeride" olup olmadığını daha iyi
        // belirler.
        for (Point p : currentPolygonPoints) {
            // Köşegenin uç noktalarını kontrol etme.
            if (p.equals(dP1) || p.equals(dP2)) {
                continue;
            }
            // Eğer bir nokta köşegenin üzerinde ve uç noktalar arasında ise, bu köşegen
            // geçersizdir.
            if (orientation(dP1, dP2, p) == 0 && onSegment(dP1, p, dP2)) {
                System.out.println(
                        "    Köşegen (" + dP1 + ", " + dP2 + ") üzerinde başka bir çokgen noktası (" + p + ") var.");
                return false;
            }
        }

        System.out.println("    Köşegen (" + dP1 + ", " + dP2 + ") içeride ve çokgen kenarlarını kesmiyor.");
        return true; // Köşegen geçerli
    }

    /**
     * İki doğru parçasının **sadece iç kısımlarının** kesişip kesişmediğini kontrol
     * eder.
     * Uç noktalarda çakışma veya doğrusal çakışma durumlarını kesişim olarak
     * saymaz.
     *
     * @param p1 İlk doğru parçasının başlangıç noktası.
     * @param q1 İlk doğru parçasının bitiş noktası.
     * @param p2 İkinci doğru parçasının başlangıç noktası.
     * @param q2 İkinci doğru parçasının bitiş noktası.
     * @return İki doğru parçasının iç kısımları kesişiyorsa true, aksi takdirde
     *         false.
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
        // Uç noktaların çakışması proper kesişim değildir.
        if (o1 == 0 && onSegmentInterior(p1, p2, q1)) return true; // p2, p1-q1'in içinde mi?
        if (o2 == 0 && onSegmentInterior(p1, q2, q1)) return true; // q2, p1-q1'in içinde mi?
        if (o3 == 0 && onSegmentInterior(p2, p1, q2)) return true; // p1, p2-q2'in içinde mi?
        if (o4 == 0 && onSegmentInterior(p2, q1, q2)) return true; // q1, p2-q2'in içinde mi?

        // Diğer tüm durumlar (doğrusal olma, uç noktada çakışma, kesişmeme) için false döndür.
        return false;
    }

    public static boolean isQuadrilateral(Point p0, Point p1, Point p2, Point p3) {
        // 1. Noktaların birbirinden farklı olduğunu kontrol et
        if (p0.equals(p1) || p0.equals(p2) || p0.equals(p3) ||
                p1.equals(p2) || p1.equals(p3) ||
                p2.equals(p3)) {
            System.out.println("   [isQuadrilateral] Dörtgen oluşturulamadı: Noktalar aynı.");
            return false;
        }

        // 2. Ardışık üç noktanın doğrusal olup olmadığını kontrol et
        // Bir dörtgende doğrusal üç nokta varsa, dejenere olur.
        if (orientation(p0, p1, p2) == 0 ||
                orientation(p1, p2, p3) == 0 ||
                orientation(p2, p3, p0) == 0 ||
                orientation(p3, p0, p1) == 0) {
            System.out.println("   [isQuadrilateral] Dörtgen oluşturulamadı: Ardışık üç nokta doğrusal.");
            return false;
        }

        // 3. Çapraz kenarların kesişip kesişmediğini kontrol et (Kendi kendini kesen
        // dörtgenler için)
        // Burada doProperSegmentsIntersect kullanılıyor, yani sadece iç kesişimler
        // kontrol edilir.
        // Kenarlar: (p0, p1), (p1, p2), (p2, p3), (p3, p0)
        // Kesişebilecek çapraz kenar çiftleri:
        // (p0, p1) ile (p2, p3)
        // (p1, p2) ile (p3, p0)

        // Birinci çapraz kenar çifti: (p0, p1) ve (p2, p3)
        if (doProperSegmentsIntersect(p0, p1, p2, p3)) {
            System.out.println("   [isQuadrilateral] Dörtgen kendi kendini kesiyor: (p0, p1) ile (p2, p3) kesişiyor.");
            return false;
        }

        // İkinci çapraz kenar çifti: (p1, p2) ve (p3, p0)
        if (doProperSegmentsIntersect(p1, p2, p3, p0)) {
            System.out.println("   [isQuadrilateral] Dörtgen kendi kendini kesiyor: (p1, p2) ile (p3, p0) kesişiyor.");
            return false;
        }

        // --- Dörtgenin Dışbükeyliğini Kontrol Etme ---
        // Kendi kendini kesmeyen bir dörtgenin geçerli olabilmesi için dışbükey olması
        // gerekir.
        // İçbükey dörtgenler bu algoritmada dörtgen olarak kabul edilmez.
        if (!isConvex(p0, p1, p2, p3)) {
            System.out
                    .println("   [isQuadrilateral] Dörtgen içbükeydir ve bu algoritmada dörtgen olarak kabul edilmez.");
            return false;
        }

        System.out.println("   [isQuadrilateral] Noktalar geçerli bir dörtgen oluşturuyor.");
        return true;
    }

    /**
     * Verilen bir doğru parçasının, belirtilen çokgenin mevcut kenarlarından
     * herhangi birini kesip kesmediğini kontrol eder.
     * Kesişimin, doğru parçalarının uç noktaları dışında (yani 'proper
     * intersection') olması durumunda true döner.
     * Köşegenin kendi uç noktalarına bitişik olan kenarları kesişim kontrolüne
     * dahil etmez.
     *
     * @param segmentToTest   Kontrol edilecek doğru parçası (genellikle p3-p0 gibi
     *                        bir kapanış kenarı veya bir köşegen).
     * @param polygonEdges    Çokgenin tüm kenarlarını içeren liste (Segment
     *                        nesneleri).
     * @param p0SegmentToTest SegmentToTest'in ilk noktası.
     * @param p3SegmentToTest SegmentToTest'in son noktası.
     * @return Eğer segment çokgenin başka bir kenarını kesiyorsa true, aksi
     *         takdirde false.
     */
    private static boolean doesSegmentIntersectPolygonEdges(Segment segmentToTest, List<Point> polygonPoints) {
        Point dP1 = segmentToTest.getStart();
        Point dP2 = segmentToTest.getEnd();

        // polygonPoints listesindeki dP1 ve dP2'nin indekslerini bulalım.
        int idx_dP1 = -1;
        int idx_dP2 = -1;
        for (int k = 0; k < polygonPoints.size(); k++) {
            if (polygonPoints.get(k).equals(dP1))
                idx_dP1 = k;
            if (polygonPoints.get(k).equals(dP2))
                idx_dP2 = k;
        }

        // Eğer dP1 veya dP2 mevcut çokgende bulunamazsa (ki olmamalı), hata.
        if (idx_dP1 == -1 || idx_dP2 == -1) {
            System.out.println(
                    "    Hata: doesSegmentIntersectPolygonEdges - Köşegenin uç noktaları çokgende bulunamadı.");
            return true; // Hata durumunda kesişim var gibi davran
        }

        // Köşegenin zaten çokgenin bir kenarı olup olmadığını kontrol et.
        // Eğer öyleyse, bu bir köşegen değil, mevcut bir kenardır ve kesişim olarak
        // sayılmaz.
        boolean isAlreadyAnEdge = (Math.abs(idx_dP1 - idx_dP2) == 1) ||
                (idx_dP1 == 0 && idx_dP2 == polygonPoints.size() - 1) ||
                (idx_dP2 == 0 && idx_dP1 == polygonPoints.size() - 1);
        if (isAlreadyAnEdge) {
            return false;
        }

        // Mevcut çokgenin tüm kenarları üzerinde döngü yap
        for (int i = 0; i < polygonPoints.size(); i++) {
            Point edgeP1 = polygonPoints.get(i);
            Point edgeP2 = polygonPoints.get((i + 1) % polygonPoints.size());

            // Köşegenin uç noktalarına bitişik olan kenarları atla.
            // Bu, kontrol edilen kenar (edgeP1, edgeP2), köşegenin dP1 veya dP2 noktalarına
            // doğrudan bağlıysa atla.
            boolean isAdjacentToDP1 = (edgeP1
                    .equals(polygonPoints.get((idx_dP1 - 1 + polygonPoints.size()) % polygonPoints.size()))
                    && edgeP2.equals(dP1)) ||
                    (edgeP1.equals(dP1) && edgeP2.equals(polygonPoints.get((idx_dP1 + 1) % polygonPoints.size())));

            boolean isAdjacentToDP2 = (edgeP1
                    .equals(polygonPoints.get((idx_dP2 - 1 + polygonPoints.size()) % polygonPoints.size()))
                    && edgeP2.equals(dP2)) ||
                    (edgeP1.equals(dP2) && edgeP2.equals(polygonPoints.get((idx_dP2 + 1) % polygonPoints.size())));

            // Eğer kenar köşegenin direkt komşusu ise, kesişim kontrolünü atla.
            if (isAdjacentToDP1 || isAdjacentToDP2) {
                continue;
            }

            // Eğer köşegen, çokgenin başka bir kenarını kesiyorsa, geçersizdir.
            // Burada doProperSegmentsIntersect kullanılıyor, yani sadece iç kesişimler
            // sayılır.
            if (doProperSegmentsIntersect(dP1, dP2, edgeP1, edgeP2)) {
                return true; // Kesişim bulundu
            }
        }
        return false; // Hiçbir kesişim bulunamadı
    }

    /**
     * Verilen dört sıralı noktanın dışbükey bir dörtgen oluşturup oluşturmadığını
     * kontrol eder.
     * Tüm iç açılar 180 dereceden küçükse dışbükeydir.
     * Kendi kendini kesmeyen ve doğrusal olmayan dörtgenler için kullanılır.
     *
     * @param p0 İlk nokta.
     * @param p1 İkinci nokta.
     * @param p2 Üçüncü nokta.
     * @param p3 Dördüncü nokta.
     * @return Dörtgen dışbükey ise true, aksi takdirde false.
     */
    public static boolean isConvex(Point p0, Point p1, Point p2, Point p3) {
        // İlk yönü al ve tüm diğer yönlerin onunla aynı olup olmadığını kontrol et.
        // orientation 0 döndürmediği varsayılır, çünkü isQuadrilateral zaten doğrusal
        // noktaları ele almıştır.
        int firstOrientation = orientation(p0, p1, p2);
        // Eğer ilk üç nokta doğrusal ise, dışbükey değildir.
        if (firstOrientation == 0) {
            System.out.println("   [isConvex] İlk üç nokta doğrusal, bu nedenle dışbükey değil.");
            return false;
        }

        if (orientation(p1, p2, p3) != firstOrientation) {
            System.out.println("   [isConvex] Yön P1-P2-P3 farklı.");
            return false;
        }
        if (orientation(p2, p3, p0) != firstOrientation) {
            System.out.println("   [isConvex] Yön P2-P3-P0 farklı.");
            return false;
        }
        if (orientation(p3, p0, p1) != firstOrientation) {
            System.out.println("   [isConvex] Yön P3-P0-P1 farklı.");
            return false;
        }

        System.out.println("   [isConvex] Dörtgen dışbükeydir.");
        return true; // Tüm yönler aynıysa dışbükeydir.
    }

    /**
     * Üç noktanın (p, q, r) yönünü belirler.
     *
     * @param p İlk nokta.
     * @param q İkinci nokta.
     * @param r Üçüncü nokta.
     * @return 0: doğrusal, >0: saat yönünün tersi, <0: saat yönü.
     */
    private static int orientation(Point p, Point q, Point r) {
        // (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y)
        double val = (q.getY() - p.getY()) * (r.getX() - q.getX()) -
                     (q.getX() - p.getX()) * (r.getY() - q.getY());

        // Kayan nokta hassasiyeti için eşik değeri daha küçük (1e-12)
        if (Math.abs(val) < 1e-12) return 0; // Doğrusal (collinear)
        return (val > 0) ? 1 : -1; // Saat yönünün tersi (counter-clockwise) veya saat yönü (clockwise)
    }

    private static boolean onSegmentInterior(Point p, Point q, Point r) {
        // q'nun x koordinatı, p ve r'nin x koordinatları arasındaysa (uç noktalar hariç)
        // VE q'nun y koordinatı, p ve r'nin y koordinatları arasındaysa (uç noktalar hariç)
        // VE q, p veya r'ye eşit değilse
        return q.getX() > Math.min(p.getX(), r.getX()) && q.getX() < Math.max(p.getX(), r.getX()) &&
               q.getY() > Math.min(p.getY(), r.getY()) && q.getY() < Math.max(p.getY(), r.getY());
    }

    /**
     * Bir noktanın (q) başka bir doğru parçası (pr) üzerinde olup olmadığını
     * kontrol eder.
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

            Polygon polygon = new Polygon(points);
            polygons.add(polygon);
        }

        return polygons;
    }

    private static void deleteAllFiles(String directoryPath) {
        try {
            Files.list(Paths.get(directoryPath))
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            Files.delete(file);
                        } catch (Exception e) {
                            System.out.println("⚠️ Error deleting file: " + file + " - " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            System.out.println("⚠️ Error deleting files: " + e.getMessage());
        }
    }
}
