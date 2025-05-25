import java.util.ArrayList;
import java.util.List;

public abstract class Geometry {
    protected List<Point> points;

    public Geometry() {
        this.points = new ArrayList<>();
    }

    public Geometry(Point... points) {
        List<Point> pointList = List.of(points);
        this.points = pointList;
    }

    public Geometry(List<Point> points) {
        this.points = points;
    }

    public List<Point> getPoints() {
        return points;
    }

    public abstract double getArea();
}
