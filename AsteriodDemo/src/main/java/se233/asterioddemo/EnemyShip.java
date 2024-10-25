package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.Random;

public class EnemyShip extends Character {
    private double shootInterval = 2000; // Milliseconds between shots
    private double angle;  // Angle to move and shoot
    private int health = 50;  // Enemy health
    private long lastShootTime = 0;
    private double changeDirectionTimer = 0;  // Timer to change direction periodically
    private Random random = new Random();
    private Image sprite;

    // Array of enemy sprites
    private static final String[] ENEMY_SPRITES = {
            "/sprite/big_enemy_1.png",
            "/sprite/big_enemy_2.png"
    };

    @Override
    public void move() {
        // Empty method for potential extension or subclassing
    }

    public EnemyShip(double x, double y, double speed, double size, double angle) {
        super(x, y, speed, size);
        this.angle = angle;

        // Load a random sprite for this enemy
        int spriteIndex = random.nextInt(ENEMY_SPRITES.length);
        sprite = new Image(getClass().getResourceAsStream(ENEMY_SPRITES[spriteIndex]));
    }

    // Method to shoot towards the player
    public Bullet shoot(double playerX, double playerY) {
        // Calculate angle towards player
        double directionX = playerX - this.x;
        double directionY = playerY - this.y;
        double angle = Math.atan2(directionY, directionX);

        // Create and return a bullet aimed at the player with BulletType.ENEMY
        return new Bullet(this.x, this.y, angle, Bullet.BulletType.ENEMY);
    }

    // Check if enough time has passed since the last shot to shoot again
    public boolean canShoot() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShootTime >= shootInterval) {
            lastShootTime = currentTime;
            return true;
        }
        return false;
    }

    // Move the enemy towards the player or randomly change its direction
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
    public void draw(GraphicsContext gc) {
        gc.save();
        gc.translate(x, y);
        gc.rotate(Math.toDegrees(angle));
        gc.drawImage(sprite, -sprite.getWidth() / 2, -sprite.getHeight() / 2, size, size);
        gc.restore();
    }

    // Method to take damage and reduce health
    public void takeDamage(int damage) {
        this.health -= damage;
    }

    // Getter for enemy health
    public int getHealth() {
        return health;
    }

    public Bullet shootTowards(double playerX, double playerY) {
        // Calculate the angle from the enemy's current position (x, y) to the player's position (playerX, playerY)
        double angleToPlayer = Math.atan2(playerY - y, playerX - x);

        // Create and return a bullet aimed towards the player
        return new Bullet(x, y, angleToPlayer, Bullet.BulletType.ENEMY);
    }
}
