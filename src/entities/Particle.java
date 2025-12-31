package entities;

import java.awt.Color;

public class Particle {
    public double x, y, dx, dy, life;
    public Color color;

    public Particle(double x, double y, double angle, double speed, Color color) {
        this.x = x;
        this.y = y;
        this.dx = Math.cos(angle) * speed;
        this.dy = Math.sin(angle) * speed;
        this.life = 1.0;
        this.color = color;
    }

    public void update() {
        x += dx;
        y += dy;
        dx *= 0.95;
        dy *= 0.95;
        life -= 0.02;
    }

    public boolean isDead() {
        return life <= 0;
    }
}