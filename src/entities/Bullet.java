package entities;

public class Bullet {
    public double x, y, dx, dy;
    public boolean isEnemy;

    public Bullet(double x, double y, double angle) {
        this(x, y, angle, false);
    }

    public Bullet(double x, double y, double angle, boolean isEnemy) {
        this.x = x;
        this.y = y;
        this.isEnemy = isEnemy;
        double speed = 8;
        this.dx = Math.cos(angle) * speed;
        this.dy = Math.sin(angle) * speed;
    }

    public void update() {
        x += dx;
        y += dy;
    }

    public boolean isOutOfBounds(int screenWidth, int screenHeight) {
        return x < -10 || x > screenWidth + 10 || y < -10 || y > screenHeight + 10;
    }
}