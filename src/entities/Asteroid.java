package entities;

import java.awt.geom.Path2D;
import java.util.Random;

public class Asteroid {
    public double x, y, dx, dy, size;
    public int level;
    public Path2D shape;
    public double rotation = 0;
    public double rotationSpeed;
    private Random random = new Random();

    public Asteroid(double x, double y) {
        this(x, y, 15 + new Random().nextInt(20), 1);
    }

    public Asteroid(double x, double y, double size, int level) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.level = level;
        double speed = 0.5 + random.nextDouble();
        double angle = random.nextDouble() * Math.PI * 2;
        this.dx = Math.cos(angle) * speed;
        this.dy = Math.sin(angle) * speed;
        this.rotationSpeed = (random.nextDouble() - 0.5) * 0.05;

        // 生成不規則隕石形狀
        shape = new Path2D.Double();
        int points = 6 + random.nextInt(4);
        double angleStep = 2 * Math.PI / points;
        for (int i = 0; i < points; i++) {
            double ang = i * angleStep;
            double radius = size * (0.5 + random.nextDouble() * 0.5);
            double px = Math.cos(ang) * radius;
            double py = Math.sin(ang) * radius;
            if (i == 0) shape.moveTo(px, py);
            else shape.lineTo(px, py);
        }
        shape.closePath();
    }

    public void update(int screenWidth, int screenHeight) {
        x += dx;
        y += dy;
        rotation += rotationSpeed;

        if (x < -size) x = screenWidth + size;
        if (x > screenWidth + size) x = -size;
        if (y < -size) y = screenHeight + size;
        if (y > screenHeight + size) y = -size;
    }
}