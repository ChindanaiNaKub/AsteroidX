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
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import java.util.logging.LogManager;

public class AsteroidGame extends Application {

    private Canvas canvas;
    private GraphicsContext gc;

    private GameState gameState;
    private InputController inputController;
    private GameEntityManager gameEntityManager;

    private PlayerShip playerShip;
    private boolean gameOver;
    private AudioClip laserSound;
    private AudioClip hitSound;
    private AudioClip explodeSound;
    private AudioClip thrustSound;
    private AudioClip bossMusic;


    static final Logger logger = Logger.getLogger(AsteroidGame.class.getName());

    private Image backgroundImage;

    private SpriteLoader spriteLoader;

    // Define number sprites
    private static final String[] NUMBER_SPRITES = {
            "numeral0.png", "numeral1.png", "numeral2.png",
            "numeral3.png", "numeral4.png", "numeral5.png",
            "numeral6.png", "numeral7.png", "numeral8.png", "numeral9.png"
    };

    private static final int BOSS_TRIGGER_SCORE = 1000; // Score needed to trigger boss


    @Override
    public void start(Stage primaryStage) {

        try (InputStream configFile = AsteroidGame.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        spriteLoader = new SpriteLoader("/sprite/sheet.png", "/sprite/sheet.xml");

        Pane root = new Pane();
        canvas = new Canvas(800, 600);
        root.getChildren().add(canvas);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Asteroid Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        gc = canvas.getGraphicsContext2D();

        // Load the background image
        backgroundImage = new Image(getClass().getResource("/sprite/blue.png").toExternalForm());

        // Initialize the game state, input controller, and entity manager
        gameState = new GameState();
        inputController = new InputController(scene);
        gameEntityManager = new GameEntityManager(spriteLoader);

        // Create a new PlayerShip object
        playerShip = new PlayerShip(400, 300, 5, 30, spriteLoader);

        // Load sounds
        laserSound = new AudioClip(getClass().getResource("/sounds/laser.m4a").toExternalForm());
        hitSound = new AudioClip(getClass().getResource("/sounds/hit.m4a").toExternalForm());
        explodeSound = new AudioClip(getClass().getResource("/sounds/explode.m4a").toExternalForm());
        thrustSound = new AudioClip(getClass().getResource("/sounds/thrust.m4a").toExternalForm());
        bossMusic = new AudioClip(getClass().getResource("/sounds/boss.mp3").toExternalForm());

        // Set up continuous spawning of asteroids and enemies
        startAsteroidAndEnemySpawning();

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

            if (!gameEntityManager.isBossActive()) {
                // Normal game updates
                gameEntityManager.updateAndDrawBullets(gc, canvas.getWidth(), canvas.getHeight());
                gameEntityManager.updateAndDrawEnemyShips(gc, playerShip.getX(), playerShip.getY());
                gameEntityManager.updateAndDrawEnemyBullets(gc, canvas.getWidth(), canvas.getHeight());
                gameEntityManager.updateAndDrawAsteroids(gc);
                checkBossStage(); // Check if it's time for boss
                checkCheatMode();
            } else {
                // Boss stage updates
                gameEntityManager.updateAndDrawBoss(gc, playerShip, gameState, hitSound, logger);
            }

            drawUI(gc, spriteLoader, gameState);
            checkCollisions();

            if (gameState.isGameOver()) {
                triggerGameOver();
            }

            if (inputController.isShootingPressed()) {
                fireBullet(inputController);  // Pass InputController to fireBullet to determine bullet mode
            }
        } else {
            drawGameOver();
        }
    }

    private void checkBossStage() {
        if (!gameEntityManager.isBossActive() && gameState.getScore() >= BOSS_TRIGGER_SCORE) {
            gameEntityManager.startBossStage(bossMusic);
        }
    }

    private void checkCheatMode() {
        if (inputController.isCheatModeEnabled() && !gameEntityManager.isBossActive()) {
            // Activate boss stage instantly when cheat mode is enabled
            gameEntityManager.startBossStage(bossMusic);
            logger.info("Cheat mode activated: Boss stage started.");
        }
    }

    private void startAsteroidAndEnemySpawning() {
        Timer spawnTimer = new Timer(true);

        // Spawn asteroids every 4 seconds
        spawnTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gameEntityManager.continuousSpawnAsteroids(gc);  // Updated to use continuous spawning
            }
        }, 0, 4000);

        // Spawn enemies every 5 seconds
        spawnTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gameEntityManager.continuousSpawnEnemyShips();  // Updated to use continuous spawning
            }
        }, 0, 5000);
    }

    private void fireBullet(InputController inputController) {
        Bullet bullet = playerShip.fireBullet(inputController);  // Pass InputController to determine bullet mode
        if (bullet != null) {
            gameEntityManager.addBullet(bullet);
            laserSound.play();
        }
    }

    // Handle collision detection
    private void checkCollisions() {
        gameEntityManager.checkCollisions(gameState, playerShip, hitSound, explodeSound, logger);
    }

    private void clearScreen() {
        gc.drawImage(backgroundImage, 0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void updatePlayerShip() {
        if (inputController.isLeftPressed()) playerShip.moveHorizontallyLeft();
        if (inputController.isRightPressed()) playerShip.moveHorizontallyRight();
        if (inputController.isUpPressed()) playerShip.moveVerticallyUp();
        if (inputController.isDownPressed()) playerShip.moveVerticallyDown();

        playerShip.rotateToMouse(inputController.getMouseX(), inputController.getMouseY());

        playerShip.move();
        playerShip.draw(gc);
        playerShip.handleScreenEdges(canvas.getWidth(), canvas.getHeight());
    }

    public void drawNumber(GraphicsContext gc, int number, double x, double y, SpriteLoader spriteLoader) {
        String numberString = Integer.toString(number);
        double spacing = 20; // Adjust spacing between digits

        for (int i = 0; i < numberString.length(); i++) {
            int digit = Integer.parseInt(String.valueOf(numberString.charAt(i))); // Convert char to int
            Image sprite = spriteLoader.getSprite(NUMBER_SPRITES[digit]);
            gc.drawImage(sprite, x + i * spacing, y);
        }
    }


    private void drawUI(GraphicsContext gc, SpriteLoader spriteLoader, GameState gameState) {
        gc.setFill(Color.WHITE);
        gc.setFont(new Font(20));

        // Draw lives in the top left corner
        gc.drawImage(spriteLoader.getSprite("playerLife1_blue.png"), 20, 20);
        drawNumber(gc, gameState.getLives(), 60, 20, spriteLoader);

        // Draw score in the top right corner
        double screenWidth = gc.getCanvas().getWidth();
        drawNumber(gc, gameState.getScore(), screenWidth - 100, 20, spriteLoader);

        // Draw bullet mode
        String bulletMode = playerShip.getBulletMode();
        gc.fillText("Bullet Mode: " + bulletMode, screenWidth / 2 - 60, canvas.getHeight() - 30);

        // If the boss is active, draw its health bar
        if (gameEntityManager.getBoss() != null) {
            double bossHealthWidth = 400;
            double bossHealthHeight = 20;
            double bossHealthX = (screenWidth - bossHealthWidth) / 2;

            // Background of health bar
            gc.setFill(Color.DARKGRAY);
            gc.fillRect(bossHealthX, 10, bossHealthWidth, bossHealthHeight);

            // Actual health
            gc.setFill(Color.RED);
            gc.fillRect(bossHealthX, 10,
                    bossHealthWidth * (gameEntityManager.getBoss().getHealth() / 100.0), bossHealthHeight);

            // Boss health text
            gc.setFill(Color.WHITE);
            gc.setFont(new Font(15));
            gc.fillText("BOSS HP: " + gameEntityManager.getBoss().getHealth() + "/100",
                    bossHealthX + bossHealthWidth / 2 - 50, 45);
        }
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
        gc.fillText("Click to Retry", canvas.getWidth() / 2 - 100, canvas.getHeight() / 2 + 50);
    }

    private void restartGame() {
        gameOver = false;
        gameState.reset();
        playerShip.reset(400, 300, 5);
        playerShip.resetHealth();
        gameEntityManager.clearAll();
        logger.info("Game restarted.");
    }
}
