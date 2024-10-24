package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Bullet {
    private double x, y;
    private double angle;
    private double speed = 3;
    private Image bulletImage;  // The bullet image

    public Bullet(double startX, double startY, double angle) {
        this.x = startX;
        this.y = startY;
        this.angle = angle;
        // Load the bullet image
        this.bulletImage = new Image(getClass().getResourceAsStream("/sprite/bullet.png"));
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

    // Draw the bullet on the screen using the image
    public void draw(GraphicsContext gc) {
        gc.drawImage(bulletImage, x - bulletImage.getWidth() / 2, y - bulletImage.getHeight() / 2);
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
