import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Triangle extends Geometry {

    public Triangle(Point... points) {
        super(points);
    }

    public Triangle(List<Point> points) {
        super(points);
    }

    @Override
    public double getArea() {
        if (points.size() != 3) {
            throw new IllegalArgumentException("A triangle must have exactly 3 points.");
        }

        Point p1 = points.get(0);
        Point p2 = points.get(1);
        Point p3 = points.get(2);

        double area = Math.abs((p1.getX() * (p2.getY() - p3.getY()) +
                p2.getX() * (p3.getY() - p1.getY()) +
                p3.getX() * (p1.getY() - p2.getY())) / 2.0);
        return area;
    }

    public double getSmallestAngleInRadians() {
        if (points.size() != 3) {
            return 0.0; // Geçersiz üçgen için 0 döndür (veya NaN, tùy theo yêu cầu)
        }
        List<Double> lengths = getEdgeLengths();
        double a = lengths.get(0); // p1-p2
        double b = lengths.get(1); // p2-p3
        double c = lengths.get(2); // p3-p1

        // Eğer herhangi bir kenar sıfır veya çok küçükse (dejenere üçgen), açıları hesaplamak sorunlu olabilir.
        // Bu durumda en küçük açıyı sıfır olarak kabul edebiliriz.
        if (a < 1e-9 || b < 1e-9 || c < 1e-9) {
            return 0.0;
        }

        // Kosinüs teoremi: cos(A) = (b^2 + c^2 - a^2) / (2bc)
        // Açı A (p1'deki açı)
        double cosA = (b * b + c * c - a * a) / (2 * b * c);
        // Açı B (p2'deki açı)
        double cosB = (a * a + c * c - b * b) / (2 * a * c);
        // Açı C (p3'teki açı)
        double cosC = (a * a + b * b - c * c) / (2 * a * b);

        // Kayan nokta hassasiyeti nedeniyle cos değeri -1.0 ile 1.0 aralığının dışına çıkabilir.
        // Math.acos çağrısı yapmadan önce bu aralığa kısıtla.
        cosA = Math.max(-1.0, Math.min(1.0, cosA));
        cosB = Math.max(-1.0, Math.min(1.0, cosB));
        cosC = Math.max(-1.0, Math.min(1.0, cosC));


        // Açıları radyan cinsinden bul
        double angleA = Math.acos(cosA);
        double angleB = Math.acos(cosB);
        double angleC = Math.acos(cosC);

        return Math.min(angleA, Math.min(angleB, angleC));
    }

    public List<Double> getEdgeLengths() {
        if (points.size() != 3) {
            return Collections.emptyList(); // Geçersiz üçgen için boş liste döndür
        }
        Point p1 = points.get(0);
        Point p2 = points.get(1);
        Point p3 = points.get(2);
        List<Double> lengths = new ArrayList<>();
        lengths.add(p1.distanceTo(p2));
        lengths.add(p2.distanceTo(p3));
        lengths.add(p3.distanceTo(p1));
        return lengths;
    }
}
