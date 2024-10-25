package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Asteroid extends Character {
    private int points;
    private double rotationAngle;
    private double rotationSpeed;
    private boolean isSplit;
    private double direction;
    private Image asteroidImage;
    private static final String[] ASTEROID_SPRITES = {
            "meteorBrown_big1.png",
            "meteorBrown_big2.png",
            "meteorBrown_big3.png",
            "meteorBrown_big4.png",
            "meteorBrown_med1.png",
            "meteorBrown_med3.png",
            "meteorBrown_small1.png",
            "meteorBrown_small2.png",
            "meteorBrown_tiny1.png",
            "meteorBrown_tiny2.png",
            "meteorGrey_big1.png",
            "meteorGrey_big2.png",
            "meteorGrey_big3.png",
            "meteorGrey_big4.png",
            "meteorGrey_med1.png",
            "meteorGrey_med2.png",
            "meteorGrey_small1.png",
            "meteorGrey_small2.png",
            "meteorGrey_tiny1.png",
            "meteorGrey_tiny2.png"
    };

    private static final Random random = new Random();
    private final SpriteLoader spriteLoader;

    public Asteroid(double x, double y, double speed, double size, int points,
                    boolean isSplit, SpriteLoader spriteLoader) {
        super(x, y, speed, size);
        this.points = points;
        this.isSplit = isSplit;
        this.spriteLoader = spriteLoader;
        this.direction = Math.random() * 2 * Math.PI;

        // Random rotation speed between -2 and 2 degrees per frame
        this.rotationSpeed = (random.nextDouble() * 4 - 2);
        this.rotationAngle = random.nextDouble() * 360; // Random initial rotation

        // Select random asteroid sprite
        String spriteKey = ASTEROID_SPRITES[random.nextInt(isSplit ?
                ASTEROID_SPRITES.length : 4)]; // Use smaller sprites for split asteroids
        this.asteroidImage = spriteLoader.getSprite(spriteKey);
    }

    @Override
    public void move() {
        x += Math.cos(direction) * speed;
        y += Math.sin(direction) * speed;
        rotationAngle += rotationSpeed; // Update rotation
    }

    public boolean isOffScreen(double screenWidth, double screenHeight) {
        return (x < -size || x > screenWidth + size ||
                y < -size || y > screenHeight + size);
    }

    public void handleScreenEdges(double screenWidth, double screenHeight) {
        // Wrap around horizontally
        if (x < -size) {
            x = screenWidth + size;
        } else if (x > screenWidth + size) {
            x = -size;
        }

        // Wrap around vertically
        if (y < -size) {
            y = screenHeight + size;
        } else if (y > screenHeight + size) {
            y = -size;
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (asteroidImage != null) {
            gc.save();

            // Move to asteroid position
            gc.translate(x, y);

            // Rotate
            gc.rotate(rotationAngle);

            // Calculate scale factor based on desired size
            double scaleFactor = this.getSize() /
                    Math.max(asteroidImage.getWidth(), asteroidImage.getHeight());
            gc.scale(scaleFactor, scaleFactor);

            // Draw the asteroid image centered
            gc.drawImage(
                    asteroidImage,
                    -asteroidImage.getWidth() / 2,
                    -asteroidImage.getHeight() / 2,
                    asteroidImage.getWidth(),
                    asteroidImage.getHeight()
            );

            gc.restore();
        }
    }

    public List<Asteroid> split() {
        List<Asteroid> smallerAsteroids = new ArrayList<>();
        if (size > 20) {  // Minimum size threshold for splitting
            double newSize = size / 1.5; // Less dramatic size reduction
            int newPoints = points / 2;

            // Create two smaller asteroids with slightly different directions
            double splitAngle1 = direction + Math.PI/4; // 45 degrees one way
            double splitAngle2 = direction - Math.PI/4; // 45 degrees other way

            Asteroid asteroid1 = new Asteroid(x, y, speed * 1.2, newSize,
                    newPoints, true, spriteLoader);
            asteroid1.direction = splitAngle1;

            Asteroid asteroid2 = new Asteroid(x, y, speed * 1.2, newSize,
                    newPoints, true, spriteLoader);
            asteroid2.direction = splitAngle2;

            smallerAsteroids.add(asteroid1);
            smallerAsteroids.add(asteroid2);
        }
        return smallerAsteroids;
    }

    public int getPoints() {
        return points;
    }

    public boolean isSplit() {
        return isSplit;
    }
}