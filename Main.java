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
        for (Polygon polygon : polygons) {
            createTriangles(polygon, index);

            if (index == 40)
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
            if (isQuadrilateral(p0, p1, p2, p3)) {
                System.out.println("Dörtlü geçerli bir dörtgen oluşturuyor. İşleniyor...");

                // Dörtgeni kaydet
                Quadrilateral currentQuad = new Quadrilateral(List.of(p0, p1, p2, p3));
                processedQuadrilaterals.add(currentQuad);

                // --- İnce üçgenlerden kaçınma ve köşegen seçimi ---
                // İki olası köşegen: (p0, p2) ve (p1, p3)
                // Her bir köşegenin oluşturacağı üçgenleri ve onların en küçük açılarını
                // hesapla.

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

    private static boolean isDiagonalInternalAndNonIntersecting(Point dP1, Point dP2, Point otherP1, Point otherP2,
                                                                List<Point> currentPolygonPoints) {
        // 1. Köşegenin dörtgenin içinde olup olmadığını kontrol et (yön testi)
        // dP1-dP2 köşegenine göre otherP1 ve otherP2 zıt yönlerde olmalı
        int orient_otherP1 = orientation(dP1, dP2, otherP1);
        int orient_otherP2 = orientation(dP1, dP2, otherP2);

        // Bir köşegenin dörtgenin içinde olması için, diğer iki noktanın köşegene göre zıt yönlerde olması gerekir.
        // Veya, içbükey bir dörtgende, bir noktanın köşegen üzerinde olması da mümkündür.
        boolean isInternalByOrientation = false;
        if ((orient_otherP1 != 0 && orient_otherP2 != 0) && (orient_otherP1 != orient_otherP2)) {
            isInternalByOrientation = true;
        } else if (orient_otherP1 == 0 && onSegment(dP1, otherP1, dP2)) { // otherP1 köşegen üzerinde
            isInternalByOrientation = true;
        } else if (orient_otherP2 == 0 && onSegment(dP1, otherP2, dP2)) { // otherP2 köşegen üzerinde
            isInternalByOrientation = true;
        }
        // Eğer her iki nokta da doğrusal ise, dejenere bir durum olabilir ve bu bir iç köşegen değildir.
        // Veya her iki nokta da aynı yönde ise, bu bir iç köşegen değildir.
        if (!isInternalByOrientation) {
            System.out.println("     Köşegen (" + dP1 + ", " + dP2 + ") dörtgenin içinde değil (yön testi başarısız).");
            return false;
        }


        // 2. Köşegenin (dP1, dP2) mevcut çokgenin (currentPolygonPoints) hiçbir kenarını kesmediğini kontrol et.
        // Kendi uç noktalarıyla çakışan kenarları atlamalıyız.
        // Yani, dP1 ve dP2'nin komşusu olan kenarları kontrol etmemeliyiz.

        for (int i = 0; i < currentPolygonPoints.size(); i++) {
            Point edgeP1 = currentPolygonPoints.get(i);
            Point edgeP2 = currentPolygonPoints.get((i + 1) % currentPolygonPoints.size());

            // Kontrol edilen kenarın, köşegenin uç noktalarıyla çakışan kenar olup olmadığını belirle.
            // Bu koşul, kontrol edilen kenarın, (dP1, otherP1), (otherP1, otherP2), (otherP2, dP2) veya (dP2, dP1)
            // kenarlarından biri olup olmadığını kontrol eder. Bu kenarlar dörtgenin kendi kenarlarıdır.
            // Ayrıca, dP1 ve dP2'nin direkt bağlantılı olduğu (çokgenin kenarı olduğu) durumlarda da atlanmalıdır.
            if ((edgeP1.equals(dP1) && edgeP2.equals(otherP1)) || (edgeP1.equals(otherP1) && edgeP2.equals(dP1)) ||
                (edgeP1.equals(otherP1) && edgeP2.equals(otherP2)) || (edgeP1.equals(otherP2) && edgeP2.equals(otherP1)) ||
                (edgeP1.equals(otherP2) && edgeP2.equals(dP2)) || (edgeP1.equals(dP2) && edgeP2.equals(otherP2)) ||
                (edgeP1.equals(dP2) && edgeP2.equals(dP1)) || (edgeP1.equals(dP1) && edgeP2.equals(dP2)) ) {
                continue; // Bu kenar dörtgenin veya köşegenin kendi kenarı, kesişim kontrolüne gerek yok.
            }

            // Eğer köşegen, çokgenin başka bir kenarını proper olarak kesiyorsa, geçersizdir.
            if (doProperSegmentsIntersect(dP1, dP2, edgeP1, edgeP2)) {
                System.out.println("     Köşegen (" + dP1 + ", " + dP2 + ") çokgen kenarını (" + edgeP1 + ", " + edgeP2 + ") KESİYOR.");
                return false; // Köşegen dışarıda kalıyor veya çokgeni kesiyor
            }
        }

        // 3. Köşegenin iç kısmında çokgenin başka bir noktası olup olmadığını kontrol et.
        // Bu, içbükeylik durumlarında köşegenin "içeride" olup olmadığını daha iyi belirler.
        for (Point p : currentPolygonPoints) {
            // Köşegenin uç noktalarını kontrol etme.
            if (p.equals(dP1) || p.equals(dP2)) {
                continue;
            }
            // Eğer bir nokta köşegenin üzerinde ve uç noktalar arasında ise, bu köşegen geçersizdir.
            // Bu, köşegenin üzerinden geçen fazladan bir nokta olduğu anlamına gelir.
            if (orientation(dP1, dP2, p) == 0 && onSegment(dP1, p, dP2)) {
                System.out.println("     Köşegen (" + dP1 + ", " + dP2 + ") üzerinde başka bir çokgen noktası (" + p + ") var.");
                return false;
            }
        }

        System.out.println("     Köşegen (" + dP1 + ", " + dP2 + ") içeride ve çokgen kenarlarını proper olarak kesmiyor.");
        return true; // Köşegen geçerli
    }

    private static boolean doProperSegmentsIntersect(Point p1, Point q1, Point p2, Point q2) {
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);

        // Genel durum: Karşılıklı yönlerde ve doğrusal değillerse içten kesişirler.
        if (o1 != 0 && o2 != 0 && o3 != 0 && o4 != 0 && (o1 != o2) && (o3 != o4)) {
            return true;
        }

        // Diğer tüm durumlar (doğrusal olma, uç noktada çakışma, kesişmeme) için false
        // döndür.
        // Çünkü biz sadece "proper" (içten) kesişimi arıyoruz.
        return false;
    }

    public static boolean isQuadrilateral(Point p0, Point p1, Point p2, Point p3) {
        // 1. Noktaların birbirinden farklı olduğunu kontrol et
        if (p0.equals(p1) || p0.equals(p2) || p0.equals(p3) ||
                p1.equals(p2) || p1.equals(p3) ||
                p2.equals(p3)) {
            System.out.println("Dörtgen oluşturulamadı: Noktalar aynı.");
            return false;
        }

        // 2. Ardışık üç noktanın doğrusal olup olmadığını kontrol et
        // Bir dörtgende doğrusal üç nokta varsa, dejenere olur.
        if (orientation(p0, p1, p2) == 0 ||
                orientation(p1, p2, p3) == 0 ||
                orientation(p2, p3, p0) == 0 ||
                orientation(p3, p0, p1) == 0) {
            System.out.println("Dörtgen oluşturulamadı: Ardışık üç nokta doğrusal.");
            return false;
        }

        // 3. Çapraz kenarların kesişip kesişmediğini kontrol et
        // Geçerli bir dörtgende sadece komşu olmayan kenarlar kesişebilir (ve
        // kesişmemelidir)
        // Kenarlar: (p0, p1), (p1, p2), (p2, p3), (p3, p0)
        // Kesişebilecek çapraz kenar çiftleri:
        // (p0, p1) ile (p2, p3)
        // (p1, p2) ile (p3, p0)

        // Birinci çapraz kenar çifti: (p0, p1) ve (p2, p3)
        if (doSegmentsIntersect(p0, p1, p2, p3)) {
            System.out.println("Dörtgen kendi kendini kesiyor: (p0, p1) ile (p2, p3) kesişiyor.");
            return false;
        }

        // İkinci çapraz kenar çifti: (p1, p2) ve (p3, p0)
        if (doSegmentsIntersect(p1, p2, p3, p0)) {
            System.out.println("Dörtgen kendi kendini kesiyor: (p1, p2) ile (p3, p0) kesişiyor.");
            return false;
        }

        // Tüm kontrollerden geçtiyse, geçerli bir dörtgen oluşturur.
        System.out.println("✅ Noktalar geçerli bir dörtgen oluşturuyor.");
        return true;
    }

    /**
     * İki doğru parçasının kesişip kesişmediğini kontrol eder.
     * Bu metot, kendi kendini kesen dörtgen kontrolünde kullanılır.
     *
     * @param p1 İlk doğru parçasının başlangıç noktası.
     * @param q1 İlk doğru parçasının bitiş noktası.
     * @param p2 İkinci doğru parçasının başlangıç noktası.
     * @param q2 İkinci doğru parçasının bitiş noktası.
     * @return İki doğru parçası kesişiyorsa true, aksi takdirde false.
     */
    private static boolean doSegmentsIntersect(Point p1, Point q1, Point p2, Point q2) {
        // Her noktanın diğer doğru parçasına göre yönünü hesapla
        double o1 = orientation(p1, q1, p2);
        double o2 = orientation(p1, q1, q2);
        double o3 = orientation(p2, q2, p1);
        double o4 = orientation(p2, q2, q1);

        // Genel Durum Kesişim Kontrolü
        // (o1, o2) farklı işaretlere sahipse VE (o3, o4) farklı işaretlere sahipse
        // kesişirler.
        if (o1 != 0 && o2 != 0 && o3 != 0 && o4 != 0 &&
                ((o1 > 0 && o2 < 0) || (o1 < 0 && o2 > 0)) &&
                ((o3 > 0 && o4 < 0) || (o3 < 0 && o4 > 0))) {
            return true;
        }

        // Özel Durumlar (Doğrusallık ve Çakışma)
        // 1. p1, q1, p2 doğrusal ve p2, p1q1 üzerinde ise
        if (o1 == 0 && onSegment(p1, p2, q1)) {
            return true;
        }

        // 2. p1, q1, q2 doğrusal ve q2, p1q1 üzerinde ise
        if (o2 == 0 && onSegment(p1, q2, q1)) {
            return true;
        }

        // 3. p2, q2, p1 doğrusal ve p1, p2q2 üzerinde ise
        if (o3 == 0 && onSegment(p2, p1, q2)) {
            return true;
        }

        // 4. p2, q2, q1 doğrusal ve q1, p2q2 üzerinde ise
        if (o4 == 0 && onSegment(p2, q1, q2)) {
            return true;
        }

        // Hiçbir kesişim durumu bulunamadı
        return false;
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

        if (Math.abs(val) < 1e-9)
            return 0; // Doğrusal (collinear)
        return (val > 0) ? 1 : -1; // Saat yönünün tersi (counter-clockwise) veya saat yönü (clockwise)
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
