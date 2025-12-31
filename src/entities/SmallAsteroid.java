package entities;

import java.util.Random;

public class SmallAsteroid {
    public double x, y, dx, dy;
    public int size;
    private Random random = new Random();

    public SmallAsteroid(double x, double y) {
        this.x = x;
        this.y = y;
        this.size = 3 + random.nextInt(3);
        double speed = 0.2 + random.nextDouble() * 0.3;
        double angle = random.nextDouble() * Math.PI * 2;
        this.dx = Math.cos(angle) * speed;
        this.dy = Math.sin(angle) * speed;
    }

    public void update(int screenWidth, int screenHeight) {
        x += dx;
        y += dy;

        if (x < -5) x = screenWidth + 5;
        if (x > screenWidth + 5) x = -5;
        if (y < -5) y = screenHeight + 5;
        if (y > screenHeight + 5) y = -5;
    }
}