package se233.asterioddemo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.media.AudioClip;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.LogManager;


public class AsteroidGame extends Application {

    private Canvas canvas;
    private GraphicsContext gc;

    private GameState gameState;
    private InputController inputController;
    private LevelManager levelManager;

    private PlayerShip playerShip;
    private Boss boss;
    private boolean gameOver;
    private AudioClip laserSound;
    private AudioClip hitSound;
    private AudioClip explodeSound;

    private boolean asteroidsSpawned = false;
    private boolean cheatModeActivated = false;

    static final Logger logger = Logger.getLogger(AsteroidGame.class.getName());

    @Override
    public void start(Stage primaryStage) {

        try (InputStream configFile = AsteroidGame.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Pane root = new Pane();
        canvas = new Canvas(800, 600);
        root.getChildren().add(canvas);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Asteroid Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        gc = canvas.getGraphicsContext2D();

        // Initialize the game state, input controller, and level manager
        gameState = new GameState();
        inputController = new InputController(scene);
        levelManager = new LevelManager();

        // Create a new PlayerShip object
        playerShip = new PlayerShip(400, 300, 5, 30); // x, y, speed, size

        // Load sounds
        laserSound = new AudioClip(getClass().getResource("/sounds/laser.m4a").toExternalForm());
        hitSound = new AudioClip(getClass().getResource("/sounds/hit.m4a").toExternalForm());
        explodeSound = new AudioClip(getClass().getResource("/sounds/explode.m4a").toExternalForm());

        // Spawn asteroids for the first level
        levelManager.spawnAsteroidsForLevel(gameState.getLevel(), gc);

        // Main game loop
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGame();
            }
        }.start();

        // Handle mouse click to restart game
        scene.setOnMouseClicked(event -> {
            if (gameOver) {
                restartGame();
            }
        });
    }

    private void updateGame() {
        clearScreen();

        if (!gameOver) {
            updatePlayerShip();

            // Only spawn asteroids once per level
            if (!asteroidsSpawned) {
                levelManager.spawnAsteroidsForLevel(gameState.getLevel(), gc);
                asteroidsSpawned = true;
            }

            // Update and draw bullets (both player and boss bullets)
            levelManager.updateAndDrawBullets(gc);
            drawBossBullets();
            levelManager.updateAndDrawAsteroids(gc);
            drawUI();
            checkCollisions();

            if (gameState.isGameOver()) {
                triggerGameOver();
            }

            if (inputController.isShootingPressed()) {
                fireBullet();
            }

            // If all asteroids are cleared, spawn the boss or move to the next level
            if (levelManager.areAsteroidsCleared()) {
                if (!levelManager.isBossActive()) {
                    if (gameState.getLevel() >= 10 || cheatModeActivated) {  // Show boss if at level 10 or in cheat mode
                        boss = levelManager.spawnBoss();
                        logger.info("Boss spawned at level: " + gameState.getLevel());
                    } else {
                        gameState.nextLevel();
                        asteroidsSpawned = false;
                        logger.info("Level up! Now at level: " + gameState.getLevel());
                    }
                }
            }

            // Update and draw the boss if active
            if (levelManager.isBossActive() && boss != null) {
                boss.move();
                boss.attack();
                boss.draw(gc);
                drawBossBullets();

                // New: Draw boss bullets and check collisions with player
                List<Bullet> bossBullets = boss.getBossBullets();
                for (Bullet bullet : bossBullets) {
                    bullet.update();
                    gc.setFill(Color.YELLOW);
                    gc.fillRect(bullet.getX(), bullet.getY(), 5, 5);

                    // Check if bullet hits the player
                    if (Math.hypot(bullet.getX() - playerShip.getX(), bullet.getY() - playerShip.getY()) < playerShip.getSize() / 2) {
                        gameState.loseLife();  // Player hit, lose a life
                        logger.info("Player hit by boss bullet!");
                        // Handle game over condition
                        if (gameState.isGameOver()) {
                            triggerGameOver();
                        }
                    }
                }
            }

            // New: Check if boss is defeated
            if (boss != null && boss.getHealth() <= 0) {
                levelManager.setBossActive(false);
                logger.info("Boss defeated!");
                // Proceed to next stage or victory screen
                handleVictory();
            }

            if (inputController.isCheatModeEnabled()) {
                activateCheatMode();
            }
        } else {
            drawGameOver();
        }
    }

    private void drawBossBullets() {
        if (boss != null) {  // Check if boss is not null
            List<Bullet> bossBullets = boss.getBossBullets();
            for (Bullet bullet : bossBullets) {
                bullet.update();  // Update bullet's position
                gc.setFill(Color.YELLOW);  // Boss bullets color
                gc.fillRect(bullet.getX(), bullet.getY(), 5, 5);  // Draw boss bullet
            }
        }
    }



    // New method to handle victory or next level transition
    private void handleVictory() {
        // Move to next level or trigger victory condition
        gameState.nextLevel();
        logger.info("Victory! Moving to next level.");
    }



    private void fireBullet() {
        double tipOffset = 15;
        double bulletStartX = playerShip.getX() + Math.cos(playerShip.getAngle()) * tipOffset;
        double bulletStartY = playerShip.getY() + Math.sin(playerShip.getAngle()) * tipOffset;

        // Add bullet to LevelManager
        levelManager.addBullet(new Bullet(bulletStartX, bulletStartY, playerShip.getAngle()));

        // Play laser sound
        laserSound.play();

        logger.info("Bullet fired from position: (" + bulletStartX + ", " + bulletStartY + ")");
    }


    // Handle collision detection
    private void checkCollisions() {
        levelManager.checkCollisions(gameState, playerShip, hitSound, explodeSound, logger);
    }

    private void clearScreen() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void updatePlayerShip() {
        if (inputController.isLeftPressed()) playerShip.rotateLeft();
        if (inputController.isRightPressed()) playerShip.rotateRight();
        if (inputController.isUpPressed()) playerShip.thrustForward();
        playerShip.draw(gc);
        playerShip.handleScreenEdges(canvas.getWidth(), canvas.getHeight());
    }

    private void drawUI() {
        gc.setFill(Color.WHITE);
        gc.setFont(new Font(20));
        gc.fillText("Score: " + gameState.getScore(), 20, 30);
        gc.fillText("Lives: " + gameState.getLives(), 20, 60);
        gc.fillText("Level: " + gameState.getLevel(), 20, 90);
    }

    private void triggerGameOver() {
        gameOver = true;
        explodeSound.play();
        logger.warning("Game Over! Final Score: " + gameState.getScore());
    }

    private void drawGameOver() {
        gc.setFill(Color.RED);
        gc.setFont(new Font(40));
        gc.fillText("Game Over", canvas.getWidth() / 2 - 120, canvas.getHeight() / 2 - 50);

        gc.setFont(new Font(30));
        gc.fillText("Final Level: " + gameState.getLevel(), canvas.getWidth() / 2 - 100, canvas.getHeight() / 2 + 10);
        gc.fillText("Click to Retry", canvas.getWidth() / 2 - 100, canvas.getHeight() / 2 + 50);
    }

    private void activateCheatMode() {
        if (!cheatModeActivated) {
            levelManager.clearAsteroids();
            logger.info("Cheat mode activated! Skipping to boss stage.");

            if (!levelManager.isBossActive()) {
                boss = levelManager.spawnBoss();  // Ensure the correct boss instance is used
                logger.info("Cheat mode: Boss spawned directly at position: (" + boss.getX() + ", " + boss.getY() + ")");
            }
            cheatModeActivated = true;
        }
    }

    private void restartGame() {
        gameState.reset();
        levelManager.clearAsteroids();
        levelManager.spawnAsteroidsForLevel(gameState.getLevel(), gc);
        playerShip.reset(400, 300, 5); // Reset spaceship position
        gameOver = false;
        logger.info("Game restarted.");
    }
}
