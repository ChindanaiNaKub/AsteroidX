package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelReader;

public class Bullet {
    private double x, y;
    private double angle;
    private double speed = 3;
    private Image bulletImage;  // The bullet image
    private final double size = 10.0;  // Define bullet size
    private SpriteLoader spriteLoader;

    // Define bullet sprite names from your sprite sheet
    private static final String[] BULLET_SPRITES = {
            "laserBlue01.png",
            "laserBlue02.png",
            "laserBlue03.png",
            "laserBlue04.png",
            "laserBlue05.png",
            "laserBlue06.png",
            "laserBlue07.png",
            "laserBlue08.png",
            "laserBlue09.png",
            "laserBlue10.png",
            "laserBlue11.png",
            "laserBlue12.png",
            "laserBlue13.png",
            "laserBlue14.png",
            "laserBlue15.png",
            "laserBlue16.png",
            "laserGreen01.png",
            "laserGreen02.png",
            "laserGreen03.png",
            "laserGreen04.png",
            "laserGreen05.png",
            "laserGreen06.png",
            "laserGreen07.png",
            "laserGreen08.png",
            "laserGreen09.png",
            "laserGreen10.png",
            "laserGreen11.png",
            "laserGreen12.png",
            "laserGreen13.png",
            "laserGreen14.png",
            "laserGreen15.png",
            "laserGreen16.png",
            "laserRed01.png",
            "laserRed02.png",
            "laserRed03.png",
            "laserRed04.png",
            "laserRed05.png",
            "laserRed06.png",
            "laserRed07.png",
            "laserRed08.png",
            "laserRed09.png",
            "laserRed10.png",
            "laserRed11.png",
            "laserRed12.png",
            "laserRed13.png",
            "laserRed14.png",
            "laserRed15.png",
            "laserRed16.png"
    };


    // Constructor accepting SpriteLoader
    public Bullet(double startX, double startY, double angle, SpriteLoader spriteLoader) {
        this.x = startX;
        this.y = startY;
        this.angle = angle;
        this.spriteLoader = spriteLoader;

        // Randomly select a bullet sprite from the available ones
        String selectedBulletSprite = BULLET_SPRITES[(int) (Math.random() * BULLET_SPRITES.length)];
        this.bulletImage = spriteLoader.getSprite(selectedBulletSprite);
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

    // Draw the bullet on the screen using the selected sprite
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

    // Method to return bullet size for collision purposes
    public double getSize() {
        return size;
    }

    // Optional method to get bullet's radius if you want to calculate based on radius
    public double getRadius() {
        return size / 2;
    }
}
