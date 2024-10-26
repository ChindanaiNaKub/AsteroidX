package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import se233.asterioddemo.exception.DrawingException;
import se233.asterioddemo.exception.SpriteNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static final Logger logger = Logger.getLogger(Asteroid.class.getName());

    public Asteroid(double x, double y, double speed, double size, int points,
                    boolean isSplit, SpriteLoader spriteLoader) {
        super(x, y, speed, size);
        this.points = points;
        this.isSplit = isSplit;
        this.spriteLoader = spriteLoader;
        this.direction = Math.random() * 2 * Math.PI;

        // Random rotation speed between -2 and 2 degrees per frame
        this.rotationSpeed = (random.nextDouble() * 4 - 2);
        this.rotationAngle = random.nextDouble() * 360;

        // Load the sprite with error handling
        try {
            String spriteKey = ASTEROID_SPRITES[random.nextInt(isSplit ?
                    ASTEROID_SPRITES.length : 4)]; // Use smaller sprites for split asteroids
            this.asteroidImage = spriteLoader.getSprite(spriteKey);

            if (this.asteroidImage == null) {
                throw new SpriteNotFoundException("Sprite not found for key: " + spriteKey);
            }
        } catch (SpriteNotFoundException e) {
            logger.log(Level.SEVERE, "Failed to load asteroid sprite: " + e.getMessage());
            this.asteroidImage = null; // Assign a fallback or null to avoid crashes
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error while loading asteroid sprite: ", e);
        }
    }

    @Override
    public void move() {
        x += Math.cos(direction) * speed;
        y += Math.sin(direction) * speed;
        rotationAngle += rotationSpeed;
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
        try {
            if (asteroidImage != null) {
                gc.save();

                gc.translate(x, y);
                gc.rotate(rotationAngle);

                double scaleFactor = this.getSize() /
                        Math.max(asteroidImage.getWidth(), asteroidImage.getHeight());
                gc.scale(scaleFactor, scaleFactor);

                gc.drawImage(
                        asteroidImage,
                        -asteroidImage.getWidth() / 2,
                        -asteroidImage.getHeight() / 2,
                        asteroidImage.getWidth(),
                        asteroidImage.getHeight()
                );

                gc.restore();
            } else {
                throw new DrawingException("Asteroid image is null, cannot draw.");
            }
        } catch (DrawingException e) {
            logger.log(Level.WARNING, e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error while drawing asteroid: ", e);
        }
    }

    public List<Asteroid> split() {
        List<Asteroid> smallerAsteroids = new ArrayList<>();
        try {
            if (size > 20) {
                double newSize = size / 1.5;
                int newPoints = points / 2;

                double splitAngle1 = direction + Math.PI / 4;
                double splitAngle2 = direction - Math.PI / 4;

                Asteroid asteroid1 = new Asteroid(x, y, speed * 1.2, newSize,
                        newPoints, true, spriteLoader);
                asteroid1.direction = splitAngle1;

                Asteroid asteroid2 = new Asteroid(x, y, speed * 1.2, newSize,
                        newPoints, true, spriteLoader);
                asteroid2.direction = splitAngle2;

                smallerAsteroids.add(asteroid1);
                smallerAsteroids.add(asteroid2);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during asteroid split: ", e);
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
