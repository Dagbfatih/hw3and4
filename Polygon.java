import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Polygon extends Geometry {
    private List<Segment> edges;

    public Polygon() {
        super();
    }

    public Polygon(List<Point> points) {
        super(points);

        if (this.points.size() < 3) {
            throw new IllegalArgumentException("A polygon must have at least 3 points.");
        }

        if (!isClockwise(this.points)) {
            Collections.reverse(this.points);
        }

        // Yeni: Kenarları oluştur ve kaydet
        this.edges = new ArrayList<>();
        for (int i = 0; i < this.points.size(); i++) {
            Point p1 = this.points.get(i);
            Point p2 = this.points.get((i + 1) % this.points.size());
            this.edges.add(new Segment(p1, p2));
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

    public List<Segment> getEdges() {
        return Collections.unmodifiableList(edges); // Kenarların dışarıdan değiştirilmesini engelle
    }
}
