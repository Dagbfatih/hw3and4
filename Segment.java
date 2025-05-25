public class Segment {
    private final Point start;
    private final Point end;

    public Segment(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public Point getStart() {
        return start;
    }

    public Point getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "[" + start + " -> " + end + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Segment segment = (Segment) o;
        // Segmentler aynı uç noktalara sahipse ve aynı yöndeyse eşit kabul edilir.
        // Veya uç noktaları ters yönde ise de eşit kabul edilebilir (P-Q == Q-P)
        return (start.equals(segment.start) && end.equals(segment.end)) ||
               (start.equals(segment.end) && end.equals(segment.start));
    }
}