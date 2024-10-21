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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class AsteroidGame extends Application {

    private double spaceshipX = 400, spaceshipY = 300;
    private double spaceshipAngle = 0;
    private double spaceshipSpeed = 5;
    private boolean left, right, up, down, shooting, gameOver;
    private List<Bullet> bullets = new ArrayList<>();
    private List<Asteroid> asteroids = new ArrayList<>();
    private Random random = new Random();
    private int lives = 3;
    private int score = 0;

    // Sound effects
    private AudioClip laserSound;
    private AudioClip hitSound;
    private AudioClip explodeSound;

    @Override
    public void start(Stage primaryStage) {
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
            }
        });

        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.LEFT) left = false;
            if (event.getCode() == KeyCode.RIGHT) right = false;
            if (event.getCode() == KeyCode.UP) up = false;
            if (event.getCode() == KeyCode.DOWN) down = false;
            if (event.getCode() == KeyCode.SPACE) shooting = false;
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

                    // Draw spaceship as a triangle
                    drawSpaceship(gc);

                    // Shooting bullets
                    if (shooting) {
                        double tipX = spaceshipX + Math.cos(spaceshipAngle) * 15;  // Position at the tip of the spaceship
                        double tipY = spaceshipY + Math.sin(spaceshipAngle) * 15;

                        // Add the bullet from the tip of the spaceship
                        bullets.add(new Bullet(tipX, tipY, spaceshipAngle));
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

                        asteroids.add(new Asteroid(random.nextInt((int) canvas.getWidth()), 0, speed, asteroidSize, asteroidPoints));
                    }

                    // Update and draw asteroids
                    Iterator<Asteroid> asteroidIterator = asteroids.iterator();
                    while (asteroidIterator.hasNext()) {
                        Asteroid asteroid = asteroidIterator.next();
                        asteroid.update();
                        if (asteroid.isOffScreen(canvas.getWidth(), canvas.getHeight())) {
                            asteroidIterator.remove();
                        } else {
                            gc.setFill(Color.GRAY);
                            gc.fillOval(asteroid.getX(), asteroid.getY(), asteroid.getSize(), asteroid.getSize());
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
                    explodeSound.play();  // Play explosion sound when game is over
                }
            }
        }.start();
    }

    private void drawSpaceship(GraphicsContext gc) {
        gc.save();
        gc.translate(spaceshipX + 10, spaceshipY + 10);  // Center the rotation
        gc.rotate(Math.toDegrees(spaceshipAngle));

        // Draw the spaceship as a yellow triangle with a white outline
        gc.setFill(Color.YELLOW);
        double[] xPoints = {0, -10, 10};  // Triangle X coordinates
        double[] yPoints = {-15, 10, 10};  // Triangle Y coordinates
        gc.fillPolygon(xPoints, yPoints, 3);

        // Draw the outline of the spaceship
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokePolygon(xPoints, yPoints, 3);

        gc.restore();
    }

    private void moveSpaceship() {
        if (left) spaceshipAngle -= 0.05;
        if (right) spaceshipAngle += 0.05;

        if (up) {
            spaceshipX += Math.cos(spaceshipAngle) * spaceshipSpeed;
            spaceshipY += Math.sin(spaceshipAngle) * spaceshipSpeed;
        }
        if (down) {
            spaceshipX -= Math.cos(spaceshipAngle) * spaceshipSpeed / 2;
            spaceshipY -= Math.sin(spaceshipAngle) * spaceshipSpeed / 2;
        }
    }

    private void checkCollisions() {
        // Check collision between spaceship and asteroids
        for (Asteroid asteroid : asteroids) {
            if (Math.hypot(spaceshipX - asteroid.getX(), spaceshipY - asteroid.getY()) < asteroid.getSize()) {
                lives--;  // Lose a life
                hitSound.play();  // Play hit sound when colliding with asteroid
                asteroids.remove(asteroid);
                break;
            }
        }

        // Check collision between bullets and asteroids
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            for (Asteroid asteroid : asteroids) {
                if (Math.hypot(bullet.getX() - asteroid.getX(), bullet.getY() - asteroid.getY()) < asteroid.getSize()) {
                    score += asteroid.getPoints();  // Increase score based on asteroid size
                    bulletIterator.remove();
                    asteroids.remove(asteroid);
                    break;
                }
            }
        }
    }

    private void restartGame() {
        // Reset game variables
        spaceshipX = 400;
        spaceshipY = 300;
        spaceshipAngle = 0;
        lives = 3;
        score = 0;
        bullets.clear();
        asteroids.clear();
        gameOver = false;
    }
}
