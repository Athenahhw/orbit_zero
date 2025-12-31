package entities;

import java.awt.Color;
import java.util.Random;

public class Star {
    public double x, y;
    public int size;
    public Color color;
    public int type; // 0=normal, 1=cross, 2=nebula
    public double twinkle;
    public double twinkleSpeed;
    public double dy;
    private Random random = new Random();

    public Star(double x, double y) {
        this.x = x;
        this.y = y;
        this.size = 1 + random.nextInt(3);
        this.type = random.nextInt(100) < 70 ? 0 : (random.nextInt(100) < 80 ? 1 : 2);
        this.twinkle = random.nextDouble();
        this.twinkleSpeed = 0.01 + random.nextDouble() * 0.02;
        this.dy = 0.1 + random.nextDouble() * 0.3;

        // 星星顏色多樣化
        int colorType = random.nextInt(100);
        if (colorType < 50) {
            color = new Color(255, 255, 255); // 白色
        } else if (colorType < 70) {
            color = new Color(200, 220, 255); // 藍白
        } else if (colorType < 85) {
            color = new Color(255, 230, 200); // 黃白
        } else if (colorType < 95) {
            color = new Color(255, 200, 200); // 紅色
        } else {
            // 星雲顏色
            int nebulaType = random.nextInt(3);
            if (nebulaType == 0) color = new Color(150, 100, 255, 100); // 紫色星雲
            else if (nebulaType == 1) color = new Color(100, 200, 255, 100); // 藍色星雲
            else color = new Color(255, 150, 200, 100); // 粉色星雲
        }
    }

    public void update(int screenHeight, int screenWidth) {
        y += dy;
        if (y > screenHeight + 10) {
            y = -10;
            x = random.nextInt(screenWidth);
        }

        twinkle += twinkleSpeed;
        if (twinkle > 1.0) {
            twinkle = 0;
            twinkleSpeed = 0.01 + random.nextDouble() * 0.02;
        }
    }
}