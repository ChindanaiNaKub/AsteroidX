package se233.asterioddemo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.media.AudioClip;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.Level;


public class AsteroidGame extends Application {

    private double spaceshipX = 400, spaceshipY = 300;
    private double spaceshipAngle = 0;
    private double spaceshipSpeed = 5;
    private boolean left, right, up, down, shooting, gameOver;
    private boolean cheatMode = false;  // Cheat mode flag
    private List<Bullet> bullets = new ArrayList<>();
    private List<Asteroid> asteroids = new ArrayList<>();
    private Random random = new Random();
    private int lives = 3;
    private int score = 0;
    private int level = 1;  // Track the current level

    // Sound effects
    private AudioClip laserSound;
    private AudioClip hitSound;
    private AudioClip explodeSound;

    private boolean bossActive = false;  // To track whether the boss is in play
    private Boss boss;

    private static final Logger logger = Logger.getLogger(AsteroidGame.class.getName());


    @Override
    public void start(Stage primaryStage) {
        try (InputStream configFile = AsteroidGame.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.setLevel(Level.FINE);

        Pane root = new Pane();
        Canvas canvas = new Canvas(800, 600);
        root.getChildren().add(canvas);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Asteroid Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Load sounds
        laserSound = new AudioClip(getClass().getResource("/sounds/laser.m4a").toExternalForm());
        hitSound = new AudioClip(getClass().getResource("/sounds/hit.m4a").toExternalForm());
        explodeSound = new AudioClip(getClass().getResource("/sounds/explode.m4a").toExternalForm());

        // Handle mouse click for restarting the game
        scene.setOnMouseClicked(event -> {
            if (gameOver && event.getButton() == MouseButton.PRIMARY) {
                restartGame();
            }
        });

        // Handle key events
        scene.setOnKeyPressed(event -> {
            if (!gameOver) {
                if (event.getCode() == KeyCode.LEFT) left = true;
                if (event.getCode() == KeyCode.RIGHT) right = true;
                if (event.getCode() == KeyCode.UP) up = true;
                if (event.getCode() == KeyCode.DOWN) down = true;
                if (event.getCode() == KeyCode.SPACE) shooting = true;
                // Cheat mode activation
                if (event.getCode() == KeyCode.C) {
                    cheatMode();
                }            }
        });

        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.LEFT) left = false;
            if (event.getCode() == KeyCode.RIGHT) right = false;
            if (event.getCode() == KeyCode.UP) up = false;
            if (event.getCode() == KeyCode.DOWN) down = false;
            if (event.getCode() == KeyCode.SPACE) shooting = false;
            if (event.getCode() == KeyCode.C) cheatMode = false;  // Disable cheat mode on release
        });

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Clear screen and draw dark background
                gc.setFill(Color.BLACK);  // Set background color to black
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

                if (!gameOver) {
                    // Move spaceship
                    moveSpaceship();
                    handleScreenEdges(canvas);
                    drawSpaceship(gc);

                    // Shooting bullets
                    if (shooting) {
                        // Calculate the tip of the spaceship to spawn the bullet
                        double tipOffset = 15;  // Distance from the center to the tip of the spaceship
                        double bulletStartX = spaceshipX + Math.cos(spaceshipAngle) * tipOffset;  // X position at the tip
                        double bulletStartY = spaceshipY + Math.sin(spaceshipAngle) * tipOffset;  // Y position at the tip

                        // Add the bullet from the tip of the spaceship with its angle
                        bullets.add(new Bullet(bulletStartX, bulletStartY, spaceshipAngle));
                        logger.info("Bullet fired from position: (" + bulletStartX + ", " + bulletStartY + ")");
                        laserSound.play();  // Play laser sound when shooting
                        shooting = false;  // Fire once per space press
                    }

                    // Update and draw bullets
                    Iterator<Bullet> bulletIterator = bullets.iterator();
                    while (bulletIterator.hasNext()) {
                        Bullet bullet = bulletIterator.next();
                        bullet.update();
                        if (bullet.isOffScreen(canvas.getWidth(), canvas.getHeight())) {
                            bulletIterator.remove();
                        } else {
                            gc.setFill(Color.RED);
                            gc.fillRect(bullet.getX(), bullet.getY(), 5, 5);
                        }
                    }

                    // Handle regular gameplay if boss is not active
                    if (!bossActive) {

                        // Regular asteroid gameplay
                        if (asteroids.isEmpty() && level == 1) {
                            spawnAsteroidsForLevel();  // Ensure asteroids are spawned for the level
                        }

                        // Update and draw asteroids
                        spawnAndDrawAsteroids(gc);

                        // Check if all asteroids are cleared and spawn the boss
                        if (asteroids.isEmpty() && level == 1) {
                            level++;
                            boss = new Boss(400, 100, 1.5, 80);  // Create an easier boss
                            bossActive = true;  // Set boss as active
                        }
                    } else {
                        // Handle boss movement and attacks
                        boss.move();
                        boss.attack();
                        boss.draw(gc);

                        // Draw boss bullets
                        List<Bullet> bossBullets = boss.getBossBullets();
                        for (Bullet bullet : bossBullets) {
                            bullet.update();
                            gc.setFill(Color.YELLOW);  // Boss bullets are yellow
                            gc.fillRect(bullet.getX(), bullet.getY(), 5, 5);
                        }

                        // Check collisions with the boss
                        Iterator<Bullet> playerBulletIterator = bullets.iterator();
                        while (playerBulletIterator.hasNext()) {
                            Bullet bullet = playerBulletIterator.next();
                            if (Math.hypot(bullet.getX() - boss.getX(), bullet.getY() - boss.getY()) < boss.getSize() / 2) {
                                playerBulletIterator.remove();
                                boss.takeDamage();
                                if (boss.getHealth() <= 0) {
                                    bossActive = false;  // Boss defeated
                                    // Handle victory or transition to the next level here
                                }
                                break;
                            }
                        }
                    }

                    // Collision detection
                    checkCollisions();
                }

                // Display score and lives with smaller font size and better alignment
                gc.setFill(Color.WHITE);
                gc.setFont(new Font(20));  // Smaller font for score and lives
                gc.fillText("Score: " + score, 20, 30);  // Adjusted position
                gc.fillText("Lives: " + lives, 20, 60);  // Adjusted position

                // Draw "Game Over" text if gameOver is true
                if (gameOver) {
                    gc.setFill(Color.RED);

                    // Set larger font size for game over text
                    gc.setFont(new Font(40));
                    gc.fillText("Game Over", (canvas.getWidth() / 2) - 120, canvas.getHeight() / 2 - 50);

                    // Set slightly smaller font size for retry text
                    gc.setFont(new Font(30));
                    gc.fillText("Click to Retry", (canvas.getWidth() / 2) - 100, canvas.getHeight() / 2 + 30);
                }

                // Check game over
                if (lives <= 0 && !gameOver) {
                    // Set gameOver to true after lives reach 0
                    gameOver = true;
                    logger.warning("Game Over! Final Score: " + score);
                    explodeSound.play();  // Play explosion sound when game is over
                }
            }

            // Add this method to ensure asteroids are spawned at the start of the level
            private void spawnAsteroidsForLevel() {
                for (int i = 0; i < 5; i++) {  // Spawn only 5 asteroids initially
                    double asteroidSize;
                    int asteroidPoints;
                    double speed = random.nextDouble() * 1.5 + 0.5;  // Reduced speed

                    // Randomize asteroid size
                    double sizeType = random.nextDouble();
                    if (sizeType < 0.33) {
                        asteroidSize = 20;
                        asteroidPoints = 1;
                    } else if (sizeType < 0.66) {
                        asteroidSize = 40;
                        asteroidPoints = 2;
                    } else {
                        asteroidSize = 60;
                        asteroidPoints = 3;
                    }

                    asteroids.add(new Asteroid(random.nextInt((int) gc.getCanvas().getWidth()), random.nextInt((int) gc.getCanvas().getHeight()), speed, asteroidSize, asteroidPoints, false));
                }
            }

            // Add this method to spawn and draw asteroids
            private void spawnAndDrawAsteroids(GraphicsContext gc) {
                Iterator<Asteroid> asteroidIterator = asteroids.iterator();
                while (asteroidIterator.hasNext()) {
                    Asteroid asteroid = asteroidIterator.next();
                    asteroid.update();
                    if (asteroid.isOffScreen(gc.getCanvas().getWidth(), gc.getCanvas().getHeight())) {
                        asteroidIterator.remove();
                    } else {
                        asteroid.draw(gc); // Use the draw method to display the polygon asteroid
                    }
                }
            }

        }.start();



    }

    // Cheat mode function
    private void cheatMode() {
        // Clear all asteroids
        asteroids.clear();

        // Automatically transition to the boss stage
        if (!bossActive) {
            boss = new Boss(400, 100, 1.5, 80);  // Customize boss position, speed, and size as needed
            bossActive = true;
            System.out.println("Cheat mode activated: Transitioned to boss stage.");
        }
    }

    private void spawnAndDrawAsteroids(GraphicsContext gc) {
        // Spawn asteroids randomly with different sizes and points
        if (random.nextDouble() < 0.02) {
            double asteroidSize;
            int asteroidPoints;
            double speed = random.nextDouble() * 2 + 1;  // Random speed between 1 and 3

            // Randomize asteroid size (small, medium, large)
            double sizeType = random.nextDouble();
            if (sizeType < 0.33) {
                asteroidSize = 20;   // Small
                asteroidPoints = 1;  // 1 point for small asteroid
            } else if (sizeType < 0.66) {
                asteroidSize = 40;   // Medium
                asteroidPoints = 2;  // 2 points for medium asteroid
            } else {
                asteroidSize = 60;   // Large
                asteroidPoints = 3;  // 3 points for large asteroid
            }

            asteroids.add(new Asteroid(random.nextInt((int) gc.getCanvas().getWidth()), 0, speed, asteroidSize, asteroidPoints, false));
        }

        // Update and draw asteroids
        Iterator<Asteroid> asteroidIterator = asteroids.iterator();
        while (asteroidIterator.hasNext()) {
            Asteroid asteroid = asteroidIterator.next();
            asteroid.update();
            if (asteroid.isOffScreen(gc.getCanvas().getWidth(), gc.getCanvas().getHeight())) {
                asteroidIterator.remove();
            } else {
                asteroid.draw(gc); // Use the draw method to display the polygon asteroid
            }
        }
    }


    // Method to spawn asteroids based on the level
    private void spawnAsteroidsForLevel() {
        int numAsteroids = level * 5;  // Increase number of asteroids with each level
        for (int i = 0; i < numAsteroids; i++) {
            double asteroidSize;
            int asteroidPoints;
            double speed = random.nextDouble() * 2 + 1;

            // Randomize asteroid size
            double sizeType = random.nextDouble();
            if (sizeType < 0.33) {
                asteroidSize = 20;
                asteroidPoints = 1;
            } else if (sizeType < 0.66) {
                asteroidSize = 40;
                asteroidPoints = 2;
            } else {
                asteroidSize = 60;
                asteroidPoints = 3;
            }

            asteroids.add(new Asteroid(random.nextInt(800), random.nextInt(600), speed, asteroidSize, asteroidPoints, false));
        }
    }


    private void drawSpaceship(GraphicsContext gc) {
        gc.save();

        // Translate to the spaceship's current position
        gc.translate(spaceshipX, spaceshipY);

        // Rotate the spaceship based on its current angle
        gc.rotate(Math.toDegrees(spaceshipAngle));

        // Draw the spaceship triangle with its center at (0, 0) since we are already translated
        gc.setFill(Color.YELLOW);
        double[] xPoints = {0, -10, 10};  // X points relative to the spaceship's center
        double[] yPoints = {-15, 10, 10}; // Y points relative to the spaceship's center
        gc.fillPolygon(xPoints, yPoints, 3);

        // Draw the outline of the spaceship
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokePolygon(xPoints, yPoints, 3);

        gc.restore();
    }



    private void moveSpaceship() {
        if (left) {
            spaceshipAngle -= 0.05;
            logger.fine("Spaceship turned left. Current angle: " + spaceshipAngle);
        }
        if (right) {
            spaceshipAngle += 0.05;
            logger.fine("Spaceship turned right. Current angle: " + spaceshipAngle);
        }

        if (up) {
            spaceshipX += Math.cos(spaceshipAngle) * spaceshipSpeed;
            spaceshipY += Math.sin(spaceshipAngle) * spaceshipSpeed;
            logger.info("Spaceship moved forward. Position: (" + spaceshipX + ", " + spaceshipY + ")");
        }
        if (down) {
            spaceshipX -= Math.cos(spaceshipAngle) * spaceshipSpeed / 2;
            spaceshipY -= Math.sin(spaceshipAngle) * spaceshipSpeed / 2;
            logger.info("Spaceship moved backward. Position: (" + spaceshipX + ", " + spaceshipY + ")");
        }
    }


    private void handleScreenEdges(Canvas canvas) {
        // Screen wrapping for spaceship
        if (spaceshipX < 0) spaceshipX = canvas.getWidth();
        if (spaceshipX > canvas.getWidth()) spaceshipX = 0;
        if (spaceshipY < 0) spaceshipY = canvas.getHeight();
        if (spaceshipY > canvas.getHeight()) spaceshipY = 0;

        // Screen wrapping for asteroids
        for (Asteroid asteroid : asteroids) {
            if (asteroid.getX() < 0) asteroid.setX(canvas.getWidth());
            if (asteroid.getX() > canvas.getWidth()) asteroid.setX(0);
            if (asteroid.getY() < 0) asteroid.setY(canvas.getHeight());
            if (asteroid.getY() > canvas.getHeight()) asteroid.setY(0);
        }
    }



    private void checkCollisions() {
        List<Bullet> bulletsToRemove = new ArrayList<>();
        List<Asteroid> asteroidsToRemove = new ArrayList<>();

        // Check for collisions between spaceship and asteroids
        for (Asteroid asteroid : asteroids) {
            double distanceToAsteroid = Math.hypot(spaceshipX - asteroid.getX(), spaceshipY - asteroid.getY());
            if (distanceToAsteroid < (asteroid.getSize() / 2 + 15)) { // 15 is an approximate radius of the spaceship
                // Collision detected
                lives--;  // Lose a life
                asteroidsToRemove.add(asteroid);  // Mark asteroid for removal
                hitSound.play();  // Play hit sound
                if (lives <= 0) {
                    gameOver = true;
                    explodeSound.play();  // Play explosion sound when game is over
                }
                break;
            }
        }

        // Check for collisions between bullets and asteroids
        for (Bullet bullet : bullets) {
            for (Asteroid asteroid : asteroids) {
                double distanceToBullet = Math.hypot(bullet.getX() - asteroid.getX(), bullet.getY() - asteroid.getY());
                if (distanceToBullet < asteroid.getSize() / 2) {
                    // Add points for asteroid size
                    score += asteroid.getPoints();
                    logger.info("Asteroid destroyed! Score: " + score);

                    // Split the asteroid if possible
                    List<Asteroid> newAsteroids = asteroid.split();
                    asteroids.addAll(newAsteroids);

                    // Mark the bullet and asteroid for removal
                    bulletsToRemove.add(bullet);
                    asteroidsToRemove.add(asteroid);
                    break;
                }
            }
        }

        // Remove the bullets and asteroids after iteration to avoid ConcurrentModificationException
        bullets.removeAll(bulletsToRemove);
        asteroids.removeAll(asteroidsToRemove);
    }




    private void restartGame() {
        // Reset game variables
        spaceshipX = 400;
        spaceshipY = 300;
        spaceshipAngle = 0;
        lives = 3;
        score = 0;
        level = 1;  // Reset to level 1
        bullets.clear();
        asteroids.clear();
        gameOver = false;
        bossActive = false;  // Reset boss state
        spawnAsteroidsForLevel();  // Start the first level
    }
}
