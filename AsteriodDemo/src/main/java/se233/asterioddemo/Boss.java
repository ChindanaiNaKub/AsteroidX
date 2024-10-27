package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static se233.asterioddemo.AsteroidGame.logger;

public class Boss extends Character {
    private double x, y;
    private double speed;
    private double size;
    private int health;
    private List<Bullet> bossBullets;
    private boolean isVisible = true;
    private int phase = 1;
    private AttackPattern currentPattern = AttackPattern.SINGLE;
    private double originalY;
    private double verticalMovement = 0;
    private long lastPatternChange;
    private static final long PATTERN_CHANGE_INTERVAL = 5000; // 5 seconds

    // Sprite animation properties
    private Image[] ufoSprites;
    private int currentSpriteIndex;
    private long lastSpriteChange;
    private static final long SPRITE_CHANGE_INTERVAL = 200; // Change sprite every 200ms
    private static final String[] BOSS_SPRITES = {
            "ufoBlue.png", "ufoGreen.png", "ufoRed.png", "ufoYellow.png"
    };

    private enum AttackPattern {
        SINGLE,     // Single bullet
        SPREAD,     // Multiple bullets in a spread pattern
        SPIRAL,     // Rotating spiral of bullets
        WAVE        // Sinusoidal wave pattern
    }

    public Boss(double x, double y, double speed, double size, SpriteLoader spriteLoader) {
        super(x, y, speed, size);
        this.x = x;
        this.y = y;
        this.originalY = y;
        this.size = size;
        this.speed = speed;
        this.health = 200;
        this.bossBullets = new ArrayList<>();
        this.lastPatternChange = System.currentTimeMillis();

        initializeSprites(spriteLoader);
        this.currentSpriteIndex = 0;
        this.lastSpriteChange = System.currentTimeMillis();
    }

    private void initializeSprites(SpriteLoader spriteLoader) {
        ufoSprites = new Image[4];
        String[] spriteNames = {"ufoBlue.png", "ufoGreen.png", "ufoRed.png", "ufoYellow.png"};

        boolean loadError = false;
        for (int i = 0; i < spriteNames.length; i++) {
            ufoSprites[i] = spriteLoader.getSprite(spriteNames[i]);
            if (ufoSprites[i] == null) {
                logger.severe("Failed to load sprite: " + spriteNames[i]);
                loadError = true;
            }
        }

        if (loadError) {
            logger.severe("Some boss sprites failed to load!");
        } else {
            logger.info("All boss sprites loaded successfully");
        }
    }

    private void updateSprite() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpriteChange > SPRITE_CHANGE_INTERVAL) {
            currentSpriteIndex = (currentSpriteIndex + 1) % ufoSprites.length;
            lastSpriteChange = currentTime;
        }
    }

    public void move() {
        // Horizontal movement with edge bouncing
        x += speed;
        if (x > 800 - size || x < 0) {
            speed = -speed; // Switch direction when hitting edges
        }
        // Vertical movement based on phase
        if (phase >= 2) {
            verticalMovement += 0.05;
            y = originalY + Math.sin(verticalMovement) * 50; // Sinusoidal movement
        }

        // Change attack pattern periodically
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPatternChange > PATTERN_CHANGE_INTERVAL) {
            switchAttackPattern();
            lastPatternChange = currentTime;
        }
        updateSprite(); // Update sprite animation
    }

    private void switchAttackPattern() {
        AttackPattern[] patterns = AttackPattern.values();
        int nextPattern = (currentPattern.ordinal() + 1) % patterns.length;
        currentPattern = patterns[nextPattern];
        logger.info("Boss switched to attack pattern: " + currentPattern);
    }

    public void attack(SpriteLoader spriteLoader) {
        switch (currentPattern) {
            case SINGLE:
                if (Math.random() < 0.02) {
                    bossBullets.add(new Bullet(x, y + size / 2, Math.PI / 2, spriteLoader, "laserGreen01.png", 20));
                }
                break;

            case SPREAD:
                if (Math.random() < 0.01) {
                    for (int i = -2; i <= 2; i++) {
                        double angle = Math.PI / 2 + (i * Math.PI / 8);
                        bossBullets.add(new Bullet(x, y + size / 2, angle, spriteLoader, "laserRed01.png", 15));
                    }
                }
                break;

            case SPIRAL:
                if (Math.random() < 0.03) {
                    double baseAngle = System.currentTimeMillis() / 1000.0;
                    for (int i = 0; i < 8; i++) {
                        double angle = baseAngle + (i * Math.PI / 4);
                        bossBullets.add(new Bullet(x, y, angle, spriteLoader, "laserBlue01.png", 12));
                    }
                }
                break;

            case WAVE:
                if (Math.random() < 0.015) {
                    double baseSpeed = 4;
                    for (int i = 0; i < 3; i++) {
                        bossBullets.add(new Bullet(x + (i * 30), y + size / 2, Math.PI / 2, spriteLoader, "laserYellow01.png", (int) (baseSpeed + i)));
                    }
                }
                break;
        }
    }

    public void draw(GraphicsContext gc) {
        if (!isVisible) {
            logger.warning("Boss is not visible!");
            return;
        }

        // Draw the current UFO sprite
        Image currentSprite = ufoSprites[currentSpriteIndex];
        if (currentSprite != null) {
            gc.drawImage(currentSprite,
                    x - size / 2,
                    y - size / 2,
                    size,
                    size);
        }

        // Draw health bar with phase indicator
        double healthBarWidth = size * (health / 200.0);
        gc.setFill(getPhaseColor());
        gc.fillRect(x - size / 2, y - size - 10, healthBarWidth, 5);

        // Draw attack pattern indicator
        gc.setFill(getPatternColor());
        gc.fillOval(x - 5, y - size - 20, 10, 10);
    }

    private javafx.scene.paint.Color getPhaseColor() {
        return switch (phase) {
            case 1 -> javafx.scene.paint.Color.RED;
            case 2 -> javafx.scene.paint.Color.ORANGE;
            case 3 -> javafx.scene.paint.Color.PURPLE;
            default -> javafx.scene.paint.Color.RED;
        };
    }

    private javafx.scene.paint.Color getPatternColor() {
        return switch (currentPattern) {
            case SINGLE -> javafx.scene.paint.Color.GREEN;
            case SPREAD -> javafx.scene.paint.Color.RED;
            case SPIRAL -> javafx.scene.paint.Color.BLUE;
            case WAVE -> javafx.scene.paint.Color.YELLOW;
        };
    }



    public void takeDamage() {
        health -= 10;
        Logger.getLogger(Boss.class.getName()).info("Boss took damage. Current health: " + health);

        // Phase transitions
        if (health <= 60 && phase == 1) {
            phase = 2;
            speed *= 1.5;
            logger.info("Boss entered phase 2!");
        } else if (health <= 30 && phase == 2) {
            phase = 3;
            speed *= 1.5;
            logger.info("Boss entered phase 3!");
        }
    }

    public int getHealth() {
        return health;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getSize() {
        return size;
    }

    public List<Bullet> getBossBullets() {
        return bossBullets;
    }
}
