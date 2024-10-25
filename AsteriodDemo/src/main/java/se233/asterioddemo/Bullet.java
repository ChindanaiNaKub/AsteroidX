package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Bullet {
    public enum BulletType {
        PLAYER,
        ENEMY,
        BOSS
    }

    private double x, y;
    private double angle;
    private double speed = 3;
    private double size;  // Size of the bullet (can vary for enemy and boss)
    private Image bulletImage;

    // Constructor that takes a bullet type and sets the size based on the type
    public Bullet(double startX, double startY, double angle, BulletType type) {
        this.x = startX;
        this.y = startY;
        this.angle = angle;

        // Load image and set size based on bullet type.
        switch (type) {
            case PLAYER:
                this.bulletImage = new Image(getClass().getResourceAsStream("/sprite/bullet.png"));
                this.size = 64;  // Default size for player bullets
                break;
            case ENEMY:
                this.bulletImage = new Image(getClass().getResourceAsStream("/sprite/enemy_shot_0.png"));
                this.size = 20;  // Example: smaller size for enemy bullets
                break;
            case BOSS:
                this.bulletImage = new Image(getClass().getResourceAsStream("/sprite/enemy_shot_0.png"));
                this.size = 20;  // Example: larger size for boss bullets
                break;
        }
    }

    // Update method with screen wrapping logic
    public void update(double screenWidth, double screenHeight) {
        x += Math.cos(angle) * speed;
        y += Math.sin(angle) * speed;

        // Screen wrapping logic
        if (x < 0) x = screenWidth;
        if (x > screenWidth) x = 0;
        if (y < 0) y = screenHeight;
        if (y > screenHeight) y = 0;
    }

    // Draw the bullet on the screen using the image, scaled to the specified size
    public void draw(GraphicsContext gc) {
        gc.drawImage(bulletImage, x - size / 2, y - size / 2, size, size);
    }

    // Getters for bullet position
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    // Check if the bullet is off the screen
    public boolean isOffScreen(double screenWidth, double screenHeight) {
        return (x < 0 || x > screenWidth || y < 0 || y > screenHeight);
    }
}
