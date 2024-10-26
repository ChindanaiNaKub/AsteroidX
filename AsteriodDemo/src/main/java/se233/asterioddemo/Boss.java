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

    // Sprite animation properties
    private Image[] ufoSprites;
    private int currentSpriteIndex;
    private long lastSpriteChange;
    private static final long SPRITE_CHANGE_INTERVAL = 200; // Change sprite every 200ms
    private static final String[] BOSS_SPRITES = {
            "ufoBlue.png", "ufoGreen.png", "ufoRed.png", "ufoYellow.png"
    };

    public Boss(double x, double y, double speed, double size, SpriteLoader spriteLoader) {
        super(x, y, speed, size);
        this.x = x;
        this.y = y;
        this.size = size;
        this.speed = speed;
        this.health = 100;
        this.bossBullets = new ArrayList<>();

        // Initialize sprites with error checking
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
        x += speed;
        if (x > 800 - size || x < 0) {
            speed = -speed; // Switch direction when hitting edges
        }
        updateSprite(); // Update sprite animation
    }

    public void attack(SpriteLoader spriteLoader) {
        if (Math.random() < 0.02) {  // Randomly fire a bullet
            double bulletSpeed = 3;
            bossBullets.add(new Bullet(x, y + size / 2, Math.PI / 2, spriteLoader));
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
            // Draw the sprite centered on the boss position
            gc.drawImage(currentSprite,
                    x - size / 2,
                    y - size / 2,
                    size,
                    size);
        }

        // Draw health bar
        double healthBarWidth = size * (health / 100.0);
        gc.setFill(javafx.scene.paint.Color.RED);
        gc.fillRect(x - size / 2, y - size - 10, healthBarWidth, 5);
    }

    public void takeDamage() {
        health -= 10;
        Logger.getLogger(Boss.class.getName()).info("Boss took damage. Current health: " + health);
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