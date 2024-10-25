package se233.asterioddemo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.media.AudioClip;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.LogManager;

public class AsteroidGame extends Application {

    private Canvas canvas;
    private GraphicsContext gc;

    private GameState gameState;
    private InputController inputController;
    private GameEntityManager gameEntityManager;

    private PlayerShip playerShip;
    private Boss boss;
    private boolean gameOver;
    private AudioClip laserSound;
    private AudioClip hitSound;
    private AudioClip explodeSound;
    private AudioClip thrustSound;

    private boolean asteroidsSpawned = false;
    private boolean cheatModeActivated = false;

    static final Logger logger = Logger.getLogger(AsteroidGame.class.getName());

    private LevelProgressManager levelProgressManager;
    private Image backgroundImage;

    @Override
    public void start(Stage primaryStage) {

        try (InputStream configFile = AsteroidGame.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        levelProgressManager = new LevelProgressManager(8000, logger);

        Pane root = new Pane();
        canvas = new Canvas(800, 600);
        root.getChildren().add(canvas);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Asteroid Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        gc = canvas.getGraphicsContext2D();

        // Load the background image
        backgroundImage = new Image(getClass().getResource("/sprite/background.png").toExternalForm());

        // Initialize the game state, input controller, and level manager
        gameState = new GameState();
        inputController = new InputController(scene);
        gameEntityManager = new GameEntityManager();

        // Create a new PlayerShip object
        playerShip = new PlayerShip(400, 300, 5, 30); // x, y, speed, size

        // Load sounds
        laserSound = new AudioClip(getClass().getResource("/sounds/laser.m4a").toExternalForm());
        hitSound = new AudioClip(getClass().getResource("/sounds/hit.m4a").toExternalForm());
        explodeSound = new AudioClip(getClass().getResource("/sounds/explode.m4a").toExternalForm());
        thrustSound = new AudioClip(getClass().getResource("/sounds/thrust.m4a").toExternalForm());

        // Spawn asteroids for the first level
        gameEntityManager.spawnAsteroidsForLevel(gameState.getLevel(), gc);

        // Main game loop
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGame();
            }
        }.start();

        // Handle mouse click to restart game or shoot
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

            // Spawn asteroids and enemies only once per level
            if (!asteroidsSpawned) {
                gameEntityManager.spawnAsteroidsForLevel(gameState.getLevel(), gc);
                gameEntityManager.spawnEnemyShipsForLevel(gameState.getLevel());  // Spawn enemies here
                asteroidsSpawned = true;
            }

            // Update and draw bullets (player, boss, and enemy)
            gameEntityManager.updateAndDrawBullets(gc, canvas.getWidth(), canvas.getHeight());
            gameEntityManager.updateAndDrawEnemyShips(gc, playerShip.getX(), playerShip.getY());  // Update enemies and handle movement
            gameEntityManager.updateAndDrawEnemyBullets(gc, canvas.getWidth(), canvas.getHeight()); // Update enemy bullets
            drawBossBullets();  // Boss bullet logic
            gameEntityManager.updateAndDrawAsteroids(gc);  // Asteroid logic
            drawUI();
            checkCollisions();  // Check collisions
            levelProgressManager.handleLevelTimeout(gameState, gameEntityManager);  // Check for level time limit

            if (gameState.isGameOver()) {
                triggerGameOver();
            }

            if (inputController.isShootingPressed()) {
                fireBullet();
            }

            // Spawn boss or move to the next level if all enemies and asteroids are cleared
            if (gameEntityManager.areAsteroidsCleared() && gameEntityManager.areEnemiesCleared()) {
                if (!gameEntityManager.isBossActive()) {
                    if (gameState.getLevel() >= 10 || cheatModeActivated) {
                        boss = gameEntityManager.spawnBoss();
                        logger.info("Boss spawned at level: " + gameState.getLevel());
                    } else {
                        gameState.nextLevel();
                        asteroidsSpawned = false;
                        logger.info("Level up! Now at level: " + gameState.getLevel());
                    }
                }
            }

            // Update and draw boss logic
            if (gameEntityManager.isBossActive() && boss != null) {
                boss.move();
                boss.attack();
                boss.draw(gc);
                drawBossBullets();

                // Check for collisions with boss bullets
                List<Bullet> bossBullets = boss.getBossBullets();
                for (Bullet bullet : bossBullets) {
                    bullet.update(canvas.getWidth(), canvas.getHeight());
                    gc.setFill(Color.YELLOW);
                    gc.fillRect(bullet.getX(), bullet.getY(), 5, 5);

                    if (Math.hypot(bullet.getX() - playerShip.getX(), bullet.getY() - playerShip.getY()) < playerShip.getSize() / 2) {
                        gameState.loseLife();
                        logger.info("Player hit by boss bullet!");
                        if (gameState.isGameOver()) {
                            triggerGameOver();
                        }
                    }
                }
            }

            // Handle boss defeat logic
            if (boss != null && boss.getHealth() <= 0) {
                gameEntityManager.setBossActive(false);
                logger.info("Boss defeated!");
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
                bullet.update(canvas.getWidth(), canvas.getHeight());  // Update bullet's position
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
        Bullet bullet = playerShip.fireBullet();
        if (bullet != null) {
            gameEntityManager.addBullet(bullet);  // Add bullet to level manager for global updates
            laserSound.play();
            logger.info("Bullet fired from position: (" + bullet.getX() + ", " + bullet.getY() + ")");
        }
    }

    // Handle collision detection
    private void checkCollisions() {
        gameEntityManager.checkCollisions(gameState, playerShip, hitSound, explodeSound, logger);
    }

    private void clearScreen() {
        // Draw the background image
        gc.drawImage(backgroundImage, 0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void updatePlayerShip() {
        if (inputController.isLeftPressed()) playerShip.moveHorizontallyLeft();
        if (inputController.isRightPressed()) playerShip.moveHorizontallyRight();
        if (inputController.isUpPressed()) playerShip.moveVerticallyUp();
        if (inputController.isDownPressed()) playerShip.moveVerticallyDown();

        playerShip.rotateToMouse(inputController.getMouseX(), inputController.getMouseY());

        playerShip.move();  // Apply movement changes
        playerShip.draw(gc);  // Draw the ship and the flames (if thrusting)
        playerShip.handleScreenEdges(canvas.getWidth(), canvas.getHeight());
    }

    private void drawUI() {
        gc.setFill(Color.WHITE);
        gc.setFont(new Font(20));
        gc.fillText("Score: " + gameState.getScore(), 20, 30);
        gc.fillText("Lives: " + gameState.getLives(), 20, 60);
        gc.fillText("Level: " + gameState.getLevel(), 20, 90);
        gc.fillText("Player Health: " + playerShip.getHealth(), 20, 120);
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
            gameEntityManager.clearAsteroids();
            logger.info("Cheat mode activated! Skipping to boss stage.");

            if (!gameEntityManager.isBossActive()) {
                boss = gameEntityManager.spawnBoss();  // Ensure the correct boss instance is used
                logger.info("Cheat mode: Boss spawned directly at position: (" + boss.getX() + ", " + boss.getY() + ")");
            }
            cheatModeActivated = true;
        }
    }

    private void restartGame() {
        gameOver = false;  // Reset this early
        gameState.reset();  // Reset score, level, lives
        playerShip.reset(400, 300, 5); // Reset spaceship position and health
        playerShip.resetHealth();  // Reset player health to 100
        gameEntityManager.clearAsteroids();  // Clear asteroids
        gameEntityManager.clearBullets();  // Clear bullets
        gameEntityManager.clearEnemyBullets();
        gameEntityManager.setBossActive(false);  // Reset boss flag
        boss = null;  // Reset boss instance
        gameEntityManager.clearEnemyShips();  // Clear enemy ships
        cheatModeActivated = false;  // Reset cheat mode
        gameEntityManager.spawnAsteroidsForLevel(gameState.getLevel(), gc);  // Spawn new asteroids
        levelProgressManager.resetLevelTimer();  // Reset timer for the new level
        logger.info("Game restarted.");
    }
}
