import java.util.List;

public class Quadrilateral extends Geometry {

    public Quadrilateral(Point... points) {
        super(points);
    }

    public Quadrilateral(List<Point> points) {
        super(points);
    }

    @Override
    public double getArea() {
        if (points.size() != 3) {
            throw new IllegalArgumentException("A quadrilateral must have exactly 3 points.");
        }

        Point p1 = points.get(0);
        Point p2 = points.get(1);
        Point p3 = points.get(2);
        Point p4 = points.get(3);

        double area = Math.abs(
                (p1.getX() * p2.getY() - p1.getY() * p2.getX()) +
                        (p2.getX() * p3.getY() - p2.getY() * p3.getX()) +
                        (p3.getX() * p4.getY() - p3.getY() * p4.getX()) +
                        (p4.getX() * p1.getY() - p4.getY() * p1.getX()))
                / 2.0;
        return area;
    }
}
