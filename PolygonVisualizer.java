import java.io.File;
import java.util.List;
import java.util.Locale;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class PolygonVisualizer {

    public static void drawPolygon(String filename, Polygon customPolygon, List<Triangle> triangles) {
        System.out.println("🔄 Başlıyoruz: " + filename);
        System.out.println("📌 Polygon nokta sayısı: " + customPolygon.getPoints().size());
        System.out.println("📌 Triangle sayısı: " + triangles.size());

        int width = 500;
        int height = 500;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // Arkaplan
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // Koordinatları normalize etmek için min değerleri bul
        double minX = customPolygon.getPoints().stream().mapToDouble(Point::getX).min().orElse(0);
        double minY = customPolygon.getPoints().stream().mapToDouble(Point::getY).min().orElse(0);
        double maxX = customPolygon.getPoints().stream().mapToDouble(Point::getX).max().orElse(1);
        double maxY = customPolygon.getPoints().stream().mapToDouble(Point::getY).max().orElse(1);

        double rangeX = Math.max(maxX - minX, 1e-6);
        double rangeY = Math.max(maxY - minY, 1e-6);
        double scale = Math.min(width / rangeX, height / rangeY) * 0.8;

        System.out.printf(Locale.US, "📐 Koordinatlar: X = [%.2f, %.2f], Y = [%.2f, %.2f], Ölçek: %.1f\n",
                minX, maxX, minY, maxY, scale);

        // Üçgenleri çiz
        g.setColor(Color.BLUE);
        for (int t = 0; t < triangles.size(); t++) {
            Triangle triangle = triangles.get(t);
            java.awt.Polygon awtPoly = new java.awt.Polygon();
            for (Point p : triangle.getPoints()) {
                int x = (int) ((p.getX() - minX) * scale + 20);
                int y = height - (int) ((p.getY() - minY) * scale + 20);
                awtPoly.addPoint(x, y);
            }
            g.drawPolygon(awtPoly);
            System.out.println("🔷 Triangle çizildi: " + triangle.getPoints());
        }

        // Polygon'un dış hatlarını çiz
        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(2));
        java.awt.Polygon outline = new java.awt.Polygon();
        for (Point p : customPolygon.getPoints()) {
            int x = (int) ((p.getX() - minX) * scale + 20);
            int y = height - (int) ((p.getY() - minY) * scale + 20);
            outline.addPoint(x, y);
        }
        g.drawPolygon(outline);
        System.out.println("🟥 Ana polygon dış hattı çizildi.");

        g.dispose();

        try {
            File outFile = new File(filename);
            ImageIO.write(image, "png", outFile);
            System.out.println("✅ PNG kaydedildi: " + outFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("❌ PNG kaydedilirken hata oluştu: " + e.getMessage());
        }

        System.out.println("✅ Tamamlandı: " + filename + "\n");
    }
}
