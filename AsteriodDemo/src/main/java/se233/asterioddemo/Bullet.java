package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class Bullet {
    private double x, y;
    private double angle;
    private double speed = 3;
    private Image bulletImage;
    private double size = 10.0;
    private SpriteLoader spriteLoader;
    private int damage; // Add damage field
    private List<BulletTrail> trails;
    private String bulletMode;
    private double rotation; // Add this field to store the bullet's rotation

    // Define bullet sprite names from your sprite sheet
    private static final String[] BULLET_SPRITES = {
            "laserBlue01.png", "laserBlue02.png", "laserBlue03.png", "laserBlue04.png",
            "laserBlue05.png", "laserBlue06.png", "laserBlue07.png", "laserBlue08.png",
            "laserBlue09.png", "laserBlue10.png", "laserBlue11.png", "laserBlue12.png",
            "laserBlue13.png", "laserBlue14.png", "laserBlue15.png", "laserBlue16.png",
            "laserGreen01.png", "laserGreen02.png", "laserGreen03.png", "laserGreen04.png",
            "laserGreen05.png", "laserGreen06.png", "laserGreen07.png", "laserGreen08.png",
            "laserGreen09.png", "laserGreen10.png", "laserGreen11.png", "laserGreen12.png",
            "laserGreen13.png", "laserGreen14.png", "laserGreen15.png", "laserGreen16.png",
            "laserRed01.png", "laserRed02.png", "laserRed03.png", "laserRed04.png",
            "laserRed05.png", "laserRed06.png", "laserRed07.png", "laserRed08.png",
            "laserRed09.png", "laserRed10.png", "laserRed11.png", "laserRed12.png",
            "laserRed13.png", "laserRed14.png", "laserRed15.png", "laserRed16.png"
    };

    // Overloaded constructor to specify a particular bullet sprite
    public Bullet(double startX, double startY, double angle, SpriteLoader spriteLoader, String spriteName, int damage) {        this.x = startX;
        this.y = startY;
        this.angle = angle;
        this.rotation = angle + Math.PI/2; // Add 90 degrees to align with ship's direction
        this.spriteLoader = spriteLoader;
        this.bulletImage = spriteLoader.getSprite(spriteName);  // Use specified sprite
        this.damage = damage;
        this.trails = new ArrayList<>();
        this.bulletMode = spriteName.contains("Blue11") ? "shuriken" :
                spriteName.contains("Blue08") ? "pulse" : "default";
    }

    public int getDamage() {
        return damage;
    }

    // Basic movement method
    public void move() {
        x += Math.cos(angle) * speed;
        y += Math.sin(angle) * speed;
    }

    // Main update method that handles movement and screen wrapping
    public void update(double screenWidth, double screenHeight) {
        // Add trail effect
        trails.add(new BulletTrail(x, y, size * 0.5));

        // Update existing trails
        trails.removeIf(trail -> !trail.update());

        move();

        // Special effects based on bullet mode
        if (bulletMode.equals("shuriken")) {
            angle += 0.2; // Rotate shuriken bullet
        } else if (bulletMode.equals("pulse")) {
            // Pulse effect - vary the size
            size = 10.0 + Math.sin(System.currentTimeMillis() * 0.01) * 2;
        }

        // Wrap around screen edges
        if (x < 0) x = screenWidth;
        if (x > screenWidth) x = 0;
        if (y < 0) y = screenHeight;
        if (y > screenHeight) y = 0;
    }

    public void draw(GraphicsContext gc) {
        // Draw trails first
        for (BulletTrail trail : trails) {
            trail.draw(gc);
        }

        gc.save();
        gc.translate(x, y);

        // Rotate based on the bullet's direction
        // Convert angle to degrees and add 90 to account for vertical sprite
        gc.rotate(Math.toDegrees(rotation));

        if (bulletMode.equals("shuriken")) {
            gc.rotate(Math.toDegrees(angle));
        }

        gc.drawImage(bulletImage,
                -bulletImage.getWidth() / 2,
                -bulletImage.getHeight() / 2);
        gc.restore();
    }

    public double getX() { return x; }

    public double getY() { return y; }

    // This method is now only used for temporary off-screen checking if needed
    public boolean isOffScreen(double screenWidth, double screenHeight) {
        return (x < 0 || x > screenWidth || y < 0 || y > screenHeight);
    }

    public double getSize() { return size; }

    public double getRadius() { return size / 2; }
}