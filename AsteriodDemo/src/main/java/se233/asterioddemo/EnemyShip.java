package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.Random;

public class EnemyShip extends Character {
    private double shootInterval = 2000; // Milliseconds between shots
    private double angle;  // Angle to move and shoot
    private int health = 30;  // Enemy health
    private long lastShootTime = 0;
    private double changeDirectionTimer = 0;  // Timer to change direction periodically
    private Random random = new Random();
    private Image sprite;
    private final SpriteLoader spriteLoader;

    // Array of enemy sprites from the texture atlas
    private static final String[] ENEMY_SPRITES = {
            "enemyBlack1.png",
            "enemyBlack2.png",
            "enemyBlack3.png",
            "enemyBlack4.png",
            "enemyBlack5.png",
            "enemyBlue1.png",
            "enemyBlue2.png",
            "enemyBlue3.png",
            "enemyBlue4.png",
            "enemyBlue5.png",
            "enemyGreen1.png",
            "enemyGreen2.png",
            "enemyGreen3.png",
            "enemyGreen4.png",
            "enemyGreen5.png",
            "enemyRed1.png",
            "enemyRed2.png",
            "enemyRed3.png",
            "enemyRed4.png",
            "enemyRed5.png"
    };

    // Constructor
    public EnemyShip(double x, double y, double speed, double size, double angle, SpriteLoader spriteLoader) {
        super(x, y, speed, size);
        this.angle = angle;
        this.spriteLoader = spriteLoader;

        // Load a random sprite for this enemy from the texture atlas
        Random random = new Random();
        String spriteKey = ENEMY_SPRITES[random.nextInt(ENEMY_SPRITES.length)];
        sprite = spriteLoader.getSprite(spriteKey);  // Fetch the sprite from the atlas
    }

    // Shooting logic towards the player
    public Bullet shootTowards(double playerX, double playerY) {
        double angleToPlayer = Math.atan2(playerY - y, playerX - x);
        return new Bullet(x, y, angleToPlayer, spriteLoader,"laserRed01.png",10);  // Shoot bullet towards player
    }

    public boolean canShoot() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShootTime >= shootInterval) {
            lastShootTime = currentTime;
            return true;
        }
        return false;
    }

    // Move the enemy towards the player or in random movement
    public void move(double targetX, double targetY) {
        // Randomly change direction after a certain period
        if (changeDirectionTimer <= 0) {
            if (random.nextDouble() < 0.5) {
                // Move randomly
                angle = random.nextDouble() * Math.PI * 2;
            } else {
                // Move towards the player
                double directionX = targetX - x;
                double directionY = targetY - y;
                angle = Math.atan2(directionY, directionX);
            }
            changeDirectionTimer = random.nextInt(100) + 50;  // Reset timer for direction change
        } else {
            changeDirectionTimer--;
        }

        // Apply movement based on the calculated angle
        x += Math.cos(angle) * speed;
        y += Math.sin(angle) * speed;
    }

    @Override
    public void move() {

    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();
        gc.translate(x, y);
        gc.rotate(Math.toDegrees(angle));

        double imageWidth = sprite.getWidth();
        double imageHeight = sprite.getHeight();

        // Calculate scale factor based on desired size
        double scaleFactor = this.getSize() / Math.max(imageWidth, imageHeight);  // Use max dimension for consistent scaling

        // Apply scaling
        gc.scale(scaleFactor, scaleFactor);

        // Draw the enemy sprite centered
        gc.drawImage(sprite, -imageWidth / 2, -imageHeight / 2, imageWidth, imageHeight);
        gc.restore();
    }

    public int getHealth() {
        return health;
    }

    public void takeDamage(int damage) {
        this.health -= damage;
    }
}
