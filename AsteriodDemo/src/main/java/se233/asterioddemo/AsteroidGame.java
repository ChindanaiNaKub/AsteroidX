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

    static final Logger logger = Logger.getLogger(AsteroidGame.class.getName());

    private Image backgroundImage;

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

        // Load the background image
        backgroundImage = new Image(getClass().getResource("/sprite/background.png").toExternalForm());

        // Initialize the game state, input controller, and entity manager
        gameState = new GameState();
        inputController = new InputController(scene);
        gameEntityManager = new GameEntityManager();

        // Create a new PlayerShip object
        playerShip = new PlayerShip(400, 300, 5, 30);

        // Load sounds
        laserSound = new AudioClip(getClass().getResource("/sounds/laser.m4a").toExternalForm());
        hitSound = new AudioClip(getClass().getResource("/sounds/hit.m4a").toExternalForm());
        explodeSound = new AudioClip(getClass().getResource("/sounds/explode.m4a").toExternalForm());
        thrustSound = new AudioClip(getClass().getResource("/sounds/thrust.m4a").toExternalForm());

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

            // Update and draw bullets (player and enemy)
            gameEntityManager.updateAndDrawBullets(gc, canvas.getWidth(), canvas.getHeight());
            gameEntityManager.updateAndDrawEnemyShips(gc, playerShip.getX(), playerShip.getY());
            gameEntityManager.updateAndDrawEnemyBullets(gc, canvas.getWidth(), canvas.getHeight());
            gameEntityManager.updateAndDrawAsteroids(gc);

            drawUI();
            checkCollisions();

            if (gameState.isGameOver()) {
                triggerGameOver();
            }

            if (inputController.isShootingPressed()) {
                fireBullet();
            }
        } else {
            drawGameOver();
        }
    }

    private void startAsteroidAndEnemySpawning() {
        Timer spawnTimer = new Timer(true);

        // Spawn asteroids every 2 seconds
        spawnTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gameEntityManager.continuousSpawnAsteroids(gc);  // Updated to use continuous spawning
            }
        }, 0, 2000);

        // Spawn enemies every 5 seconds
        spawnTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gameEntityManager.continuousSpawnEnemyShips();  // Updated to use continuous spawning
            }
        }, 0, 5000);
    }


    private void fireBullet() {
        Bullet bullet = playerShip.fireBullet();
        if (bullet != null) {
            gameEntityManager.addBullet(bullet);
            laserSound.play();
            logger.info("Bullet fired from position: (" + bullet.getX() + ", " + bullet.getY() + ")");
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
        if (inputController.isLeftPressed()) playerShip.rotateLeft();
        if (inputController.isRightPressed()) playerShip.rotateRight();
        if (inputController.isUpPressed()) {
            playerShip.thrustForward();
            if (!thrustSound.isPlaying()) {
                thrustSound.play();
            }
        } else {
            playerShip.stopThrusting();
            thrustSound.stop();
            playerShip.decelerate();
        }
        if (inputController.isDownPressed()) {
            playerShip.thrustBackward();
        }

        playerShip.move();
        playerShip.draw(gc);
        playerShip.handleScreenEdges(canvas.getWidth(), canvas.getHeight());
    }

    private void drawUI() {
        gc.setFill(Color.WHITE);
        gc.setFont(new Font(20));
        gc.fillText("Score: " + gameState.getScore(), 20, 30);
        gc.fillText("Lives: " + gameState.getLives(), 20, 60);
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
        gameEntityManager.clearAsteroids();
        gameEntityManager.clearBullets();
        gameEntityManager.clearEnemyShips();
        logger.info("Game restarted.");
    }
}
