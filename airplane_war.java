import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;

public class airplane_war extends JPanel implements ActionListener, KeyListener {
    enum GameState { MENU, PLAYING, GAME_OVER, LEVEL_COMPLETE, WIN, CHOOSING_PATH }
    private GameState state = GameState.MENU;
    
    private int screenWidth = 800;
    private int screenHeight = 600;
    
    private double playerX = 400;
    private double playerY = 300;
    private double playerAngle = -Math.PI / 2;
    private double playerSpeed = 0;
    private final double MAX_SPEED = 5;
    private final double ACCELERATION = 0.2;
    private final double ROTATION_SPEED = 0.1;
    
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private ArrayList<Asteroid> asteroids = new ArrayList<>();
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Particle> particles = new ArrayList<>();
    private ArrayList<Star> stars = new ArrayList<>();
    
    private boolean leftPressed, rightPressed, upPressed, downPressed, spacePressed;
    private boolean spacePreviouslyPressed = false;
    
    private Timer gameTimer;
    private long gameStartTime;
    private Random random = new Random();
    
    private int asteroidsDestroyed = 0;
    private int enemiesDestroyed = 0;
    private int totalShots = 0;
    private int hits = 0;
    
    private double difficultyMultiplier = 1.0;
    
    private int level = 1;
    private String[] planetNames = {"Neptune", "Uranus", "Saturn", "Jupiter", "Mars", "Earth Choice", "Moon", "Earth", "Venus", "Mercury", "Sun"};
    private int[] planetSizes = {200, 200, 475, 560, 25, 50, 30, 50, 47, 19, 500};
    private boolean targetVisible = false;
    private long levelStartTime;
    private ArrayList<String> collectedItems = new ArrayList<>();
    private ArrayList<SmallAsteroid> smallAsteroids = new ArrayList<>();
    private boolean visitedMoon = false;
    
    public airplane_war() {
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        gameTimer = new Timer(16, this);
        gameTimer.start();
    }
    
    private void updateScreenSize() {
        screenWidth = getWidth();
        screenHeight = getHeight();
    }
    
    private void startGame() {
        updateScreenSize();
        state = GameState.PLAYING;
        playerX = screenWidth / 2.0;
        playerY = screenHeight / 2.0;
        playerAngle = -Math.PI / 2;
        playerSpeed = 0;
        bullets.clear();
        asteroids.clear();
        enemies.clear();
        particles.clear();
        stars.clear();
        smallAsteroids.clear();
        
        if (level == 1) {
            asteroidsDestroyed = 0;
            enemiesDestroyed = 0;
            totalShots = 0;
            hits = 0;
            collectedItems.clear();
            visitedMoon = false;
        }
        
        difficultyMultiplier = 1.0 + (level - 1) * 0.1;
        levelStartTime = System.currentTimeMillis();
        targetVisible = false;
        gameStartTime = System.currentTimeMillis();
        
        // 初始化星星背景
        for (int i = 0; i < 300; i++) {
            stars.add(new Star(random.nextInt(screenWidth), random.nextInt(screenHeight)));
        }
        
        // 生成隕石
        int asteroidCount = 8 + (level - 1) * 1;
        for (int i = 0; i < asteroidCount; i++) {
            spawnAsteroid();
        }
        
        // 土星特殊效果 - 環帶
        if (level == 3) {
            for (int i = 0; i < 80; i++) {
                smallAsteroids.add(new SmallAsteroid(random.nextInt(screenWidth), random.nextInt(screenHeight)));
            }
        }
    }
    
    private void spawnAsteroid() {
        double x = random.nextDouble() * screenWidth;
        double y = random.nextDouble() * screenHeight;
        while (Math.hypot(x - playerX, y - playerY) < 100) {
            x = random.nextDouble() * screenWidth;
            y = random.nextDouble() * screenHeight;
        }
        asteroids.add(new Asteroid(x, y));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (state == GameState.PLAYING) {
            updateScreenSize();
            updateGame();
        }
        repaint();
    }
    
    private void updateGame() {
        if (leftPressed) playerAngle -= ROTATION_SPEED;
        if (rightPressed) playerAngle += ROTATION_SPEED;
        
        if (upPressed) {
            playerSpeed = Math.min(playerSpeed + ACCELERATION, MAX_SPEED);
            // 添加尾焰效果
            if (random.nextDouble() > 0.3) {
                double exhaustAngle = playerAngle + Math.PI + (random.nextDouble() - 0.5) * 0.5;
                particles.add(new Particle(
                    playerX - Math.cos(playerAngle) * 10,
                    playerY - Math.sin(playerAngle) * 10,
                    exhaustAngle,
                    random.nextDouble() * 2 + 1,
                    new Color(255, 150 + random.nextInt(50), random.nextInt(50))
                ));
            }
        } else if (downPressed) {
            playerSpeed = Math.max(playerSpeed - ACCELERATION, -MAX_SPEED / 2);
        } else {
            playerSpeed *= 0.98;
        }
        
        playerX += Math.cos(playerAngle) * playerSpeed;
        playerY += Math.sin(playerAngle) * playerSpeed;
        
        if (playerX < 0) playerX = screenWidth;
        if (playerX > screenWidth) playerX = 0;
        if (playerY < 0) playerY = screenHeight;
        if (playerY > screenHeight) playerY = 0;
        
        if (spacePressed && !spacePreviouslyPressed) {
            bullets.add(new Bullet(playerX, playerY, playerAngle));
            totalShots++;
            playShootSound();
        }
        spacePreviouslyPressed = spacePressed;
        
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update();
            if (b.isOutOfBounds()) {
                bullets.remove(i);
            }
        }
        
        for (Asteroid a : asteroids) {
            a.update();
        }
        
        for (SmallAsteroid sa : smallAsteroids) {
            sa.update();
        }
        
        for (Star s : stars) {
            s.update();
        }
        
        // 碰撞檢測 - 子彈與隕石
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            if (b.isEnemy) continue;
            
            for (int j = asteroids.size() - 1; j >= 0; j--) {
                Asteroid a = asteroids.get(j);
                if (Math.hypot(b.x - a.x, b.y - a.y) < a.size) {
                    bullets.remove(i);
                    asteroids.remove(j);
                    asteroidsDestroyed++;
                    hits++;
                    createExplosion(a.x, a.y, new Color(150, 150, 150));
                    playHitSound();
                    if (a.level < 3) {
                        for (int k = 0; k < 2; k++) {
                            Asteroid small = new Asteroid(a.x + random.nextInt(20) - 10, a.y + random.nextInt(20) - 10, a.size * 0.6, a.level + 1);
                            small.dx = a.dx + random.nextDouble() * 2 - 1;
                            small.dy = a.dy + random.nextDouble() * 2 - 1;
                            asteroids.add(small);
                        }
                    } else {
                        spawnAsteroid();
                    }
                    break;
                }
            }
        }
        
        // 碰撞檢測 - 玩家與隕石
        for (Asteroid a : asteroids) {
            if (Math.hypot(playerX - a.x, playerY - a.y) < a.size + 10) {
                createExplosion(playerX, playerY, new Color(255, 100, 0));
                gameOver();
                return;
            }
        }
        
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update();
            if (p.isDead()) {
                particles.remove(i);
            }
        }
        
        // 檢查是否到達目標
        if (level == 6) { // 地球選擇關
            // 檢查是否靠近地球（上方）
            if (playerY < 100 && playerX > screenWidth / 2 - 100 && playerX < screenWidth / 2 + 100) {
                levelComplete();
            }
            // 檢查是否靠近月球（右側）
            if (playerX > screenWidth - 100 && playerY > screenHeight / 2 - 100 && playerY < screenHeight / 2 + 100) {
                visitedMoon = true;
                levelComplete();
            }
        } else if (targetVisible && playerY < 80) {
            levelComplete();
        }
        
        // 15秒後顯示終點
        if (!targetVisible && System.currentTimeMillis() - levelStartTime > 15000) {
            targetVisible = true;
        }
    }
    
    private void gameOver() {
        state = GameState.GAME_OVER;
    }
    
    private void levelComplete() {
        // 添加特產
        String item = getSouvenir(level);
        if (!collectedItems.contains(item)) {
            collectedItems.add(item);
        }
        
        if (level == 6) { // 地球選擇
            if (visitedMoon) {
                level = 7; // 前往月球
            } else {
                level = 8; // 直接前往地球
            }
        } else if (level >= 11) { // 到達太陽
            state = GameState.WIN;
        } else {
            level++;
        }
        
        if (state != GameState.WIN) {
            state = GameState.LEVEL_COMPLETE;
        }
    }
    
    private void createExplosion(double x, double y, Color color) {
        for (int i = 0; i < 30; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = random.nextDouble() * 4 + 2;
            Color particleColor = new Color(
                Math.min(255, color.getRed() + random.nextInt(50)),
                Math.min(255, color.getGreen() + random.nextInt(50)),
                Math.min(255, color.getBlue() + random.nextInt(50))
            );
            particles.add(new Particle(x, y, angle, speed, particleColor));
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        if (state == GameState.MENU) {
            drawMenu(g2d);
        } else if (state == GameState.PLAYING) {
            drawGame(g2d);
        } else if (state == GameState.GAME_OVER) {
            drawGameOver(g2d);
        } else if (state == GameState.LEVEL_COMPLETE) {
            drawLevelComplete(g2d);
        } else if (state == GameState.WIN) {
            drawWin(g2d);
        }
    }
    
    private void drawStars(Graphics2D g2d) {
        for (Star s : stars) {
            g2d.setColor(s.color);
            if (s.type == 0) {
                g2d.fillOval((int)s.x, (int)s.y, s.size, s.size);
            } else if (s.type == 1) {
                // 十字星
                g2d.fillOval((int)s.x, (int)s.y, s.size, s.size);
                g2d.drawLine((int)s.x - s.size, (int)s.y + s.size/2, (int)s.x + s.size*2, (int)s.y + s.size/2);
                g2d.drawLine((int)s.x + s.size/2, (int)s.y - s.size, (int)s.x + s.size/2, (int)s.y + s.size*2);
            } else {
                // 星雲效果
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2d.fillOval((int)s.x - s.size, (int)s.y - s.size, s.size * 3, s.size * 3);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        }
    }
    // 繪製菜單
    private void drawMenu(Graphics2D g2d) {
        // 繪製星空背景
        drawStars(g2d);
        // 標題光暈效果
        g2d.setFont(new Font("Arial", Font.BOLD, Math.max(50, screenHeight / 10)));
        String title = "ORBIT ZERO";
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        int titleX = (screenWidth - titleWidth) / 2;
        int titleY = screenHeight / 4;
        // 光暈
        for (int i = 5; i > 0; i--) {
            g2d.setColor(new Color(100, 150, 255, 30));
            g2d.drawString(title, titleX + i, titleY + i);
        }
        // 漸變標題
        GradientPaint gradient = new GradientPaint(
            titleX, titleY, new Color(100, 200, 255),
            titleX + titleWidth, titleY, new Color(200, 100, 255)
        );
        g2d.setPaint(gradient);
        g2d.drawString(title, titleX, titleY);
        // 副標題
        g2d.setFont(new Font("Arial", Font.ITALIC, 20));
        g2d.setColor(new Color(150, 200, 255));
        String subtitle = "Journey Through the Solar System";
        int subWidth = g2d.getFontMetrics().stringWidth(subtitle);
        g2d.drawString(subtitle, (screenWidth - subWidth) / 2, titleY + 50);
        // 按鈕效果
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        String start = "Press ENTER to Begin";
        int startWidth = g2d.getFontMetrics().stringWidth(start);
        int startX = (screenWidth - startWidth) / 2;
        int startY = screenHeight / 2;
        // 按鈕背景
        g2d.setColor(new Color(50, 100, 150, 100));
        g2d.fillRoundRect(startX - 20, startY - 30, startWidth + 40, 50, 25, 25);
        g2d.setColor(new Color(100, 200, 255));
        g2d.drawRoundRect(startX - 20, startY - 30, startWidth + 40, 50, 25, 25);
        g2d.drawString(start, startX, startY);
        // 控制說明
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        String[] controls = {
            "◆ Controls ◆",
            "Arrow Keys - Navigate",
            "SPACE - Fire Weapons"
        };
        int controlY = screenHeight * 3 / 4;
        for (int i = 0; i < controls.length; i++) {
            g2d.setColor(new Color(200, 200, 255));
            int width = g2d.getFontMetrics().stringWidth(controls[i]);
            g2d.drawString(controls[i], (screenWidth - width) / 2, controlY + i * 30);
        }
    }
    
    private void drawGame(Graphics2D g2d) {
        // 繪製星空
        drawStars(g2d);
        
        // 繪製粒子效果
        for (Particle p : particles) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)p.life));
            g2d.setColor(p.color);
            int size = (int)(6 * p.life) + 2;
            g2d.fillOval((int)p.x - size/2, (int)p.y - size/2, size, size);
        }
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        
        // 繪製隕石
        for (Asteroid a : asteroids) {
            AffineTransform old = g2d.getTransform();
            g2d.translate(a.x, a.y);
            g2d.setColor(new Color(120, 100, 80));
            g2d.fill(a.shape);
            g2d.setColor(new Color(80, 60, 40));
            g2d.setStroke(new BasicStroke(2));
            g2d.draw(a.shape);
            g2d.setTransform(old);
        }
        
        // 繪製小隕石環（土星）
        if (level == 3) {
            for (SmallAsteroid sa : smallAsteroids) {
                g2d.setColor(new Color(200, 180, 140, 180));
                g2d.fillOval((int)sa.x - 3, (int)sa.y - 3, 6, 6);
            }
        }
        
        // 繪製子彈
        for (Bullet b : bullets) {
            g2d.setColor(new Color(0, 255, 255));
            // 子彈尾跡
            g2d.setStroke(new BasicStroke(3));
            double tailX = b.x - b.dx * 0.5;
            double tailY = b.y - b.dy * 0.5;
            g2d.drawLine((int)tailX, (int)tailY, (int)b.x, (int)b.y);
            // 子彈本體
            g2d.fillOval((int)b.x - 4, (int)b.y - 4, 8, 8);
            g2d.setColor(new Color(255, 255, 255));
            g2d.fillOval((int)b.x - 2, (int)b.y - 2, 4, 4);
        }
        
        // 繪製玩家飛船
        AffineTransform old = g2d.getTransform();
        g2d.translate(playerX, playerY);
        g2d.rotate(playerAngle);
        
        // 機身
        GradientPaint shipGradient = new GradientPaint(-10, 0, new Color(100, 150, 255), 15, 0, new Color(50, 100, 200));
        g2d.setPaint(shipGradient);
        int[] xPoints = {20, -12, -12, -8, -8};
        int[] yPoints = {0, -12, -8, -4, 4};
        g2d.fillPolygon(xPoints, yPoints, 5);
        
        int[] xPoints2 = {20, -12, -8, -8, -12};
        int[] yPoints2 = {0, 8, 4, 12, 12};
        g2d.fillPolygon(xPoints2, yPoints2, 5);
        
        // 駕駛艙
        g2d.setColor(new Color(150, 200, 255));
        g2d.fillOval(0, -5, 10, 10);
        
        // 引擎光
        if (upPressed) {
            g2d.setColor(new Color(255, 150, 50));
            int[] engineX = {-12, -20, -12};
            int[] engineY = {-6, 0, 6};
            g2d.fillPolygon(engineX, engineY, 3);
        }
        // 輪廓
        g2d.setColor(new Color(200, 220, 255));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawPolygon(xPoints, yPoints, 5);
        g2d.drawPolygon(xPoints2, yPoints2, 5);
        
        g2d.setTransform(old);
        
        // 繪製目標星球
        if (level == 6) { // 地球選擇關
            // 繪製地球（上方）
            int earthSize = 60;
            int earthX = screenWidth / 2;
            int earthY = 30;
            g2d.setColor(new Color(100, 150, 255));
            g2d.fillArc(earthX - earthSize/2, earthY - earthSize/2, earthSize, earthSize, 180, 180);
            g2d.setColor(new Color(50, 200, 100));
            g2d.fillArc(earthX - earthSize/2 + 10, earthY - earthSize/2, earthSize/3, earthSize, 180, 180);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            String earthLabel = "Earth";
            int labelWidth = g2d.getFontMetrics().stringWidth(earthLabel);
            g2d.drawString(earthLabel, earthX - labelWidth/2, earthY + 40);
            
            // 繪製月球（右側）
            int moonSize = 45;
            int moonX = screenWidth - 40;
            int moonY = screenHeight / 2;
            g2d.setColor(new Color(200, 200, 200));
            g2d.fillOval(moonX - moonSize/2, moonY - moonSize/2, moonSize, moonSize);
            // 月球隕石坑
            g2d.setColor(new Color(150, 150, 150));
            g2d.fillOval(moonX - 10, moonY - 5, 8, 8);
            g2d.fillOval(moonX + 5, moonY + 5, 6, 6);
            g2d.setColor(Color.WHITE);
            String moonLabel = "Moon";
            labelWidth = g2d.getFontMetrics().stringWidth(moonLabel);
            g2d.drawString(moonLabel, moonX - labelWidth/2, moonY + 35);
            
        } else if (targetVisible && level < planetNames.length) {
            int size = planetSizes[level];
            Color planetColor = getPlanetColor(level);
            
            // 行星漸變效果
            RadialGradientPaint planetGradient = new RadialGradientPaint(
                screenWidth / 2f, 0f, size / 2f,
                new float[]{0f, 0.7f, 1f},
                new Color[]{
                    new Color(planetColor.getRed(), planetColor.getGreen(), planetColor.getBlue(), 255),
                    planetColor,
                    new Color(planetColor.getRed() / 2, planetColor.getGreen() / 2, planetColor.getBlue() / 2, 200)
                }
            );
            g2d.setPaint(planetGradient);
            g2d.fillArc(screenWidth / 2 - size / 2, -size / 2, size, size, 180, 180);
            
            // 行星名稱
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            String targetName = planetNames[level];
            int nameWidth = g2d.getFontMetrics().stringWidth(targetName);
            g2d.drawString(targetName, screenWidth / 2 - nameWidth / 2, 25);
        }
        
        // UI面板 - 半透明背景
        g2d.setColor(new Color(0, 0, 50, 150));
        g2d.fillRoundRect(10, 10, 250, 200, 15, 15);
        g2d.setColor(new Color(100, 150, 255));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(10, 10, 250, 200, 15, 15);
        
        g2d.setColor(new Color(200, 220, 255));
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("╔═══ STATUS ═══╗", 15, 30);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString("Asteroids: " + asteroidsDestroyed, 20, 55);
        g2d.drawString("Accuracy: " + (totalShots > 0 ? (hits * 100 / totalShots) : 0) + "%", 20, 75);
        g2d.drawString("Level: " + getDisplayLevel(), 20, 95);
        g2d.drawString("Target: " + (level < planetNames.length ? planetNames[level] : "Victory"), 20, 115);
        
        // 進度條
        g2d.setColor(new Color(50, 50, 100));
        g2d.fillRoundRect(20, 130, 220, 20, 10, 10);
        long elapsed = System.currentTimeMillis() - levelStartTime;
        float progress = Math.min(1.0f, elapsed / 15000f);
        g2d.setColor(targetVisible ? new Color(100, 255, 100) : new Color(255, 200, 50));
        g2d.fillRoundRect(20, 130, (int)(220 * progress), 20, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.drawString(targetVisible ? "Target Visible!" : "Clearing Area...", 25, 145);
        
        // 收集品
        if (!collectedItems.isEmpty()) {
            g2d.setColor(new Color(255, 200, 100));
            g2d.drawString("Items: " + collectedItems.size(), 20, 170);
        }
    }
    
    private int getDisplayLevel() {
        if (level == 7 && visitedMoon) return 6; // 月球視為6.5關
        if (level >= 8 && visitedMoon) return level - 1;
        return level;
    }
    
    private Color getPlanetColor(int level) {
        switch (level) {
            case 1: return new Color(80, 120, 255); // 海王星 - 深藍
            case 2: return new Color(120, 220, 255); // 天王星 - 青藍
            case 3: return new Color(230, 200, 130); // 土星 - 金黃
            case 4: return new Color(200, 150, 100); // 木星 - 棕橙
            case 5: return new Color(220, 100, 80); // 火星 - 紅棕
            case 6: return new Color(100, 150, 255); // 地球選擇
            case 7: return new Color(200, 200, 200); // 月球 - 灰白
            case 8: return new Color(80, 150, 220); // 地球 - 藍綠
            case 9: return new Color(255, 200, 120); // 金星 - 黃白
            case 10: return new Color(150, 150, 150); // 水星 - 灰色
            case 11: return new Color(255, 220, 100); // 太陽 - 金黃
            default: return Color.WHITE;
        }
    }
    
    private void drawGameOver(Graphics2D g2d) {
        drawStars(g2d);
        
        // 標題
        g2d.setFont(new Font("Arial", Font.BOLD, 50));
        String gameOver = "MISSION FAILED";
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(gameOver);
        g2d.setColor(new Color(255, 50, 50));
        g2d.drawString(gameOver, (screenWidth - width) / 2, screenHeight / 4);
        
        // 統計面板
        g2d.setColor(new Color(0, 0, 50, 180));
        g2d.fillRoundRect(screenWidth / 2 - 200, screenHeight / 2 - 150, 400, 300, 20, 20);
        g2d.setColor(new Color(100, 150, 255));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(screenWidth / 2 - 200, screenHeight / 2 - 150, 400, 300, 20, 20);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        String[] stats = {
            "═══ STATISTICS ═══",
            "",
            "Asteroids Destroyed: " + asteroidsDestroyed,
            "Total Shots: " + totalShots,
            "Accuracy: " + (totalShots > 0 ? (hits * 100 / totalShots) : 0) + "%",
            "Reached Level: " + getDisplayLevel(),
            "",
            "Press R - Restart Mission",
            "Press C - Continue"
        };
        
        int startY = screenHeight / 2 - 100;
        for (int i = 0; i < stats.length; i++) {
            if (i == 0) g2d.setColor(new Color(255, 200, 100));
            else g2d.setColor(Color.WHITE);
            
            int statWidth = g2d.getFontMetrics().stringWidth(stats[i]);
            g2d.drawString(stats[i], screenWidth / 2 - statWidth / 2, startY + i * 30);
        }
    }
    
    private void drawLevelComplete(Graphics2D g2d) {
        drawStars(g2d);
        
        // 標題
        g2d.setFont(new Font("Arial", Font.BOLD, 45));
        String complete = "LEVEL COMPLETE!";
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(complete);
        
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(100, 255, 150),
            screenWidth, 0, new Color(100, 200, 255)
        );
        g2d.setPaint(gradient);
        g2d.drawString(complete, (screenWidth - width) / 2, screenHeight / 3);
        
        // 獲得物品
        if (!collectedItems.isEmpty()) {
            g2d.setColor(new Color(255, 220, 100));
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            String newItem = collectedItems.get(collectedItems.size() - 1);
            String itemText = "✦ Acquired: " + newItem + " ✦";
            int itemWidth = g2d.getFontMetrics().stringWidth(itemText);
            g2d.drawString(itemText, (screenWidth - itemWidth) / 2, screenHeight / 2 - 40);
        }
        
        // 下一關提示
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        String next = "Press ENTER for Next Level";
        int nextWidth = g2d.getFontMetrics().stringWidth(next);
        
        // 按鈕背景
        g2d.setColor(new Color(50, 100, 150, 150));
        g2d.fillRoundRect(screenWidth / 2 - nextWidth / 2 - 20, screenHeight / 2 + 20, nextWidth + 40, 50, 25, 25);
        g2d.setColor(new Color(100, 200, 255));
        g2d.drawRoundRect(screenWidth / 2 - nextWidth / 2 - 20, screenHeight / 2 + 20, nextWidth + 40, 50, 25, 25);
        g2d.drawString(next, screenWidth / 2 - nextWidth / 2, screenHeight / 2 + 55);
    }
    
    private void drawWin(Graphics2D g2d) {
        drawStars(g2d);
        
        // 勝利標題
        g2d.setFont(new Font("Arial", Font.BOLD, 60));
        String win = "MISSION SUCCESS!";
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(win);
        
        // 彩虹漸變效果
        for (int i = 0; i < 5; i++) {
            g2d.setColor(new Color(255 - i * 30, 215 - i * 20, 100 + i * 30, 150 - i * 25));
            g2d.drawString(win, (screenWidth - width) / 2 + i * 2, screenHeight / 4 + i * 2);
        }
        
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(255, 220, 100),
            screenWidth, 0, new Color(255, 150, 100)
        );
        g2d.setPaint(gradient);
        g2d.drawString(win, (screenWidth - width) / 2, screenHeight / 4);
        
        // 收集品展示
        g2d.setColor(new Color(0, 0, 50, 180));
        int panelHeight = Math.min(400, 150 + collectedItems.size() * 25);
        g2d.fillRoundRect(screenWidth / 2 - 250, screenHeight / 2 - 100, 500, panelHeight, 20, 20);
        g2d.setColor(new Color(100, 200, 255));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(screenWidth / 2 - 250, screenHeight / 2 - 100, 500, panelHeight, 20, 20);
        
        g2d.setColor(new Color(255, 220, 150));
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        String collectionTitle = "✦ Journey Souvenirs ✦";
        int titleWidth = g2d.getFontMetrics().stringWidth(collectionTitle);
        g2d.drawString(collectionTitle, screenWidth / 2 - titleWidth / 2, screenHeight / 2 - 60);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        for (int i = 0; i < collectedItems.size(); i++) {
            String item = "★ " + collectedItems.get(i);
            int itemWidth = g2d.getFontMetrics().stringWidth(item);
            g2d.drawString(item, screenWidth / 2 - itemWidth / 2, screenHeight / 2 - 20 + i * 25);
        }
        
        // 重玩按鈕
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        String restart = "Press ENTER to Restart";
        int restartWidth = g2d.getFontMetrics().stringWidth(restart);
        g2d.setColor(new Color(50, 100, 150, 150));
        g2d.fillRoundRect(screenWidth / 2 - restartWidth / 2 - 20, screenHeight - 120, restartWidth + 40, 50, 25, 25);
        g2d.setColor(new Color(100, 255, 150));
        g2d.drawRoundRect(screenWidth / 2 - restartWidth / 2 - 20, screenHeight - 120, restartWidth + 40, 50, 25, 25);
        g2d.drawString(restart, screenWidth / 2 - restartWidth / 2, screenHeight - 85);
    }
    
    private void playShootSound() {
        playSound(800, 50);
    }
    
    private void playHitSound() {
        playSound(400, 100);
    }
    
    private void playSound(final int frequency, final int duration) {
        new Thread(() -> {
            try {
                byte[] buf = new byte[1];
                AudioFormat af = new AudioFormat(8000f, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();
                for (int i = 0; i < duration * 8; i++) {
                    double angle = i / (8000f / frequency) * 2.0 * Math.PI;
                    buf[0] = (byte)(Math.sin(angle) * 80.0);
                    sdl.write(buf, 0, 1);
                }
                sdl.drain();
                sdl.stop();
                sdl.close();
            } catch (Exception e) {}
        }).start();
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (state == GameState.MENU || state == GameState.LEVEL_COMPLETE) {
                startGame();
            } else if (state == GameState.WIN) {
                level = 1;
                startGame();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_R) {
            if (state == GameState.GAME_OVER) {
                level = 1;
                startGame();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_C) {
            if (state == GameState.GAME_OVER) {
                startGame();
            }
        }
        
        if (e.getKeyCode() == KeyEvent.VK_LEFT) leftPressed = true;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = true;
        if (e.getKeyCode() == KeyEvent.VK_UP) upPressed = true;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) downPressed = true;
        if (e.getKeyCode() == KeyEvent.VK_SPACE) spacePressed = true;
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) leftPressed = false;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = false;
        if (e.getKeyCode() == KeyEvent.VK_UP) upPressed = false;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) downPressed = false;
        if (e.getKeyCode() == KeyEvent.VK_SPACE) spacePressed = false;
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    private String getSouvenir(int level) {
        switch (level) {
            case 1: return "Neptune's Trident";
            case 2: return "Uranus Ice Crystal";
            case 3: return "Saturn's Ring Fragment";
            case 4: return "Jupiter's Storm Eye";
            case 5: return "Mars Rover Model";
            case 6: return "Earth Compass";
            case 7: return "Moon Cake from Chang'e";
            case 8: return "Earth's Blue Marble";
            case 9: return "Venus Rose Quartz";
            case 10: return "Mercury Speed Boots";
            case 11: return "Solar Core Fragment";
            default: return "Cosmic Dust";
        }
    }
    
    // 內部類別
    class Bullet {
        double x, y, dx, dy;
        boolean isEnemy;
        
        Bullet(double x, double y, double angle) {
            this(x, y, angle, false);
        }
        
        Bullet(double x, double y, double angle, boolean isEnemy) {
            this.x = x;
            this.y = y;
            this.isEnemy = isEnemy;
            double speed = 8;
            this.dx = Math.cos(angle) * speed;
            this.dy = Math.sin(angle) * speed;
        }
        
        void update() {
            x += dx;
            y += dy;
        }
        
        boolean isOutOfBounds() {
            return x < -10 || x > screenWidth + 10 || y < -10 || y > screenHeight + 10;
        }
    }
    
    class Asteroid {
        double x, y, dx, dy, size;
        int level;
        Path2D shape;
        double rotation = 0;
        double rotationSpeed;
        
        Asteroid(double x, double y) {
            this(x, y, 15 + random.nextInt(20), 1);
        }
        
        Asteroid(double x, double y, double size, int level) {
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
        
        void update() {
            x += dx;
            y += dy;
            rotation += rotationSpeed;
            
            if (x < -size) x = screenWidth + size;
            if (x > screenWidth + size) x = -size;
            if (y < -size) y = screenHeight + size;
            if (y > screenHeight + size) y = -size;
        }
    }
    
    class Enemy {
        double x, y, angle;
        
        Enemy(double x, double y) {
            this.x = x;
            this.y = y;
        }
        
        void update(double targetX, double targetY) {
            angle = Math.atan2(targetY - y, targetX - x);
            x += Math.cos(angle) * 2 * difficultyMultiplier;
            y += Math.sin(angle) * 2 * difficultyMultiplier;
        }
        
        boolean isOutOfBounds() {
            return x < -50 || x > screenWidth + 50 || y < -50 || y > screenHeight + 50;
        }
    }
    
    class SmallAsteroid {
        double x, y, dx, dy;
        int size;
        
        SmallAsteroid(double x, double y) {
            this.x = x;
            this.y = y;
            this.size = 3 + random.nextInt(3);
            double speed = 0.2 + random.nextDouble() * 0.3;
            double angle = random.nextDouble() * Math.PI * 2;
            this.dx = Math.cos(angle) * speed;
            this.dy = Math.sin(angle) * speed;
        }
        
        void update() {
            x += dx;
            y += dy;
            
            if (x < -5) x = screenWidth + 5;
            if (x > screenWidth + 5) x = -5;
            if (y < -5) y = screenHeight + 5;
            if (y > screenHeight + 5) y = -5;
        }
    }
    
    class Particle {
        double x, y, dx, dy, life;
        Color color;
        
        Particle(double x, double y, double angle, double speed, Color color) {
            this.x = x;
            this.y = y;
            this.dx = Math.cos(angle) * speed;
            this.dy = Math.sin(angle) * speed;
            this.life = 1.0;
            this.color = color;
        }
        
        void update() {
            x += dx;
            y += dy;
            dx *= 0.95;
            dy *= 0.95;
            life -= 0.02;
        }
        
        boolean isDead() {
            return life <= 0;
        }
    }
    
    class Star {
        double x, y;
        int size;
        Color color;
        int type; // 0=normal, 1=cross, 2=nebula
        double twinkle;
        double twinkleSpeed;
        double dy;
        
        Star(double x, double y) {
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
        
        void update() {
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
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Space Odyssey - Solar System Journey");
            airplane_war game = new airplane_war();
            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);
            game.requestFocusInWindow();
        });
    }
}