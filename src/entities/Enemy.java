package entities;

public class Enemy {
    public double x, y, angle;

    public Enemy(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void update(double targetX, double targetY, double difficultyMultiplier) {
        angle = Math.atan2(targetY - y, targetX - x);
        x += Math.cos(angle) * 2 * difficultyMultiplier;
        y += Math.sin(angle) * 2 * difficultyMultiplier;
    }

    public boolean isOutOfBounds(int screenWidth, int screenHeight) {
        return x < -50 || x > screenWidth + 50 || y < -50 || y > screenHeight + 50;
    }
}