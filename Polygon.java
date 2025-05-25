import java.util.Collections;
import java.util.List;

public class Polygon extends Geometry {
    public Polygon() {
        super();
    }

    public Polygon(List<Point> points) {
        super(points);

        if (points.size() < 3) {
            throw new IllegalArgumentException("A polygon must have at least 3 points.");
        }

        if (!isClockwise(points)) {
            // saat yönünde değilse ters çevir
            Collections.reverse(points);
        }
    }

    @Override
    public double getArea() {
        double area = 0.0;
        int n = points.size();
        for (int i = 0; i < n; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get((i + 1) % n);
            area += p1.getX() * p2.getY();
            area -= p2.getX() * p1.getY();
        }
        return Math.abs(area) / 2.0;
    }

    public void addPoint(double x, double y) {
        points.add(new Point(x, y));
    }

    private boolean isClockwise(List<Point> pts) {
        double sum = 0.0;
        for (int i = 0; i < pts.size(); i++) {
            Point p1 = pts.get(i);
            Point p2 = pts.get((i + 1) % pts.size());
            sum += (p2.getX() - p1.getX()) * (p2.getY() + p1.getY());
        }
        return sum > 0; // saat yönüyse pozitif olur
    }
}
