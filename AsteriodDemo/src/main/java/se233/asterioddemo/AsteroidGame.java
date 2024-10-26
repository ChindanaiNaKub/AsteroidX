package se233.asterioddemo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
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
    private boolean bossDefeated; // Flag to control boss reappearance
    private AudioClip laserSound;
    private AudioClip hitSound;
    private AudioClip explodeSound;
    private AudioClip thrustSound;
    private AudioClip bossMusic;

    static final Logger logger = Logger.getLogger(AsteroidGame.class.getName());

    private Image backgroundImage;

    private SpriteLoader spriteLoader;
    private Scene menuScene, gameScene;
    private AnimationTimer gameLoop;

    // Define number sprites
    private static final String[] NUMBER_SPRITES = {
            "numeral0.png", "numeral1.png", "numeral2.png",
            "numeral3.png", "numeral4.png", "numeral5.png",
            "numeral6.png", "numeral7.png", "numeral8.png", "numeral9.png"
    };

    private static final int BOSS_TRIGGER_SCORE = 17; // Score needed to trigger boss

    @Override
    public void start(Stage primaryStage) {
        // Initialize logging
        try (InputStream configFile = AsteroidGame.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        spriteLoader = new SpriteLoader("/sprite/sheet.png", "/sprite/sheet.xml");

        // Set up main menu
        Pane menuLayout = createMainMenu(primaryStage);
        menuScene = new Scene(menuLayout, 800, 600);

        primaryStage.setTitle("Asteroid Game");
        primaryStage.setScene(menuScene);
        primaryStage.show();

        // Prepare game scene but do not set it yet
        setupGameScene(primaryStage);
    }

    private Pane createMainMenu(Stage primaryStage) {
        VBox menuLayout = new VBox(30);
        menuLayout.setStyle("-fx-alignment: center;");

        // Use the background image with adjusted size for 1680x900
        Image menuBackgroundImage = new Image(getClass().getResource("/sprite/background.png").toExternalForm());
        BackgroundImage backgroundImage = new BackgroundImage(
                menuBackgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(1680, 900, false, false, false, false) // Adjusted size for the menu
        );
        menuLayout.setBackground(new Background(backgroundImage));

        // Create buttons with larger size and style
        Button startButton = createStyledButton("Start", primaryStage);
        Button selectAlbumButton = createStyledButton("Select Album", primaryStage);
        Button exitButton = createStyledButton("Exit", primaryStage);

        startButton.setOnAction(e -> startGame(primaryStage));
        selectAlbumButton.setOnAction(e -> selectAlbum());
        exitButton.setOnAction(e -> primaryStage.close());

        menuLayout.getChildren().addAll(startButton, selectAlbumButton, exitButton);
        return menuLayout;
    }

    private Button createStyledButton(String text, Stage primaryStage) {
        Button button = new Button(text);
        button.setPrefSize(300, 60); // Increase button size for better balance
        button.setStyle("-fx-font-size: 20px; -fx-text-fill: #FFFFFF; -fx-background-color: #333333;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-font-size: 20px; -fx-text-fill: #FFFFFF; -fx-background-color: #555555;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-font-size: 20px; -fx-text-fill: #FFFFFF; -fx-background-color: #333333;"));
        return button;
    }

    private void setupGameScene(Stage primaryStage) {
        Pane gameRoot = new Pane();
        canvas = new Canvas(1280, 720); // Updated to 1680x900 for 720p resolution
        gameRoot.getChildren().add(canvas);
        gameScene = new Scene(gameRoot, 1280, 720); // Updated to match the new resolution

        gc = canvas.getGraphicsContext2D();
        backgroundImage = new Image(getClass().getResource("/sprite/background.png").toExternalForm());

        gameState = new GameState();
        inputController = new InputController(gameScene);
        gameEntityManager = new GameEntityManager(spriteLoader);
        playerShip = new PlayerShip(840, 450, 5, 30, spriteLoader); // Centered for the new resolution

        // Load sounds
        laserSound = new AudioClip(getClass().getResource("/sounds/laser.m4a").toExternalForm());
        hitSound = new AudioClip(getClass().getResource("/sounds/hit.m4a").toExternalForm());
        explodeSound = new AudioClip(getClass().getResource("/sounds/explode.m4a").toExternalForm());
        thrustSound = new AudioClip(getClass().getResource("/sounds/thrust.m4a").toExternalForm());
        bossMusic = new AudioClip(getClass().getResource("/sounds/boss.mp3").toExternalForm());

        startAsteroidAndEnemySpawning();

        // Define the game loop but do not start it yet
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGame();
            }
        };
    }

    private void startGame(Stage primaryStage) {
        // Ensure the game scene is clean
        Pane rootPane = (Pane) gameScene.getRoot();
        rootPane.getChildren().clear();
        rootPane.getChildren().add(canvas); // Add the canvas back for rendering the game

        // Properly reset game variables
        gameOver = false;
        bossDefeated = false;
        gameState.reset();
        playerShip.reset(840, 450, 5); // Center for 1680x900 resolution
        playerShip.resetHealth();
        gameEntityManager.clearAll();
        gameEntityManager.setBossActive(false);

        // Set the scene to the game and start the game loop
        primaryStage.setScene(gameScene);
        gameLoop.start();
        logger.info("Game started.");
    }


    private void selectAlbum() {
        // Handle album selection logic here (e.g., open a file chooser)
        logger.info("Select Album button clicked.");
    }

    // Other game methods remain unchanged...

    private void updateGame() {
        clearScreen();

        if (!gameOver) {
            updatePlayerShip();

            if (!gameEntityManager.isBossActive()) {
                gameEntityManager.updateAndDrawBullets(gc, canvas.getWidth(), canvas.getHeight());
                gameEntityManager.updateAndDrawEnemyShips(gc, playerShip.getX(), playerShip.getY());
                gameEntityManager.updateAndDrawEnemyBullets(gc, canvas.getWidth(), canvas.getHeight());
                gameEntityManager.updateAndDrawAsteroids(gc);
                checkBossStage();
                checkCheatMode();
            } else {
                gameEntityManager.updateAndDrawBoss(gc, playerShip, gameState, hitSound, logger);
                gameEntityManager.updateAndDrawBullets(gc, canvas.getWidth(), canvas.getHeight());
            }

            gameEntityManager.updateAndDrawExplosions(gc);

            drawUI(gc, spriteLoader, gameState);
            checkCollisions();

            if (gameState.isGameOver()) {
                triggerGameOver();
            }

            if (inputController.isShootingPressed()) {
                fireBullet(inputController);
            }
        } else {
            drawGameOver();
        }
    }

    private void checkBossStage() {
        // Check if the boss is not active, not already defeated, and the score is a multiple of 17 (17, 34, 51, ...)
        if (!gameEntityManager.isBossActive() && !bossDefeated &&
                gameState.getScore() >= BOSS_TRIGGER_SCORE &&
                gameState.getScore() % BOSS_TRIGGER_SCORE == 0) {
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

        // Update the shield status
        playerShip.updateShield();

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

        // Draw PlayerShip health bar
        double playerHealthWidth = 200;
        double playerHealthHeight = 15;
        double playerHealthX = 20; // Adjust the X position as needed
        double playerHealthY = 60; // Adjust the Y position as needed

        gc.setFill(Color.DARKGRAY);
        gc.fillRect(playerHealthX, playerHealthY, playerHealthWidth, playerHealthHeight);

        gc.setFill(Color.GREEN);
        gc.fillRect(playerHealthX, playerHealthY, playerHealthWidth * (playerShip.getHealth() / 100.0), playerHealthHeight);

        gc.setFill(Color.WHITE);
        gc.setFont(new Font(15));
        gc.fillText("PLAYER HP: " + playerShip.getHealth() + "/100", playerHealthX + 50, playerHealthY + 12);

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
        gameLoop.stop(); // Stop the game loop when the game is over
        explodeSound.play();
        logger.warning("Game Over! Final Score: " + gameState.getScore());
        drawGameOver(); // Call the method to display the game over screen
    }


    private void drawGameOver() {
        gc.setFill(Color.RED);
        gc.setFont(new Font(40));
        gc.fillText("Game Over", canvas.getWidth() / 2 - 100, canvas.getHeight() / 2 - 150);

        // Create the "Restart" button
        Button restartButton = new Button("Restart");
        restartButton.setPrefSize(200, 50);
        restartButton.setLayoutX(canvas.getWidth() / 2 - 100);
        restartButton.setLayoutY(canvas.getHeight() / 2 - 50);
        restartButton.setOnAction(e -> restartGame());

        // Create the "Main Menu" button
        Button mainMenuButton = new Button("Main Menu");
        mainMenuButton.setPrefSize(200, 50);
        mainMenuButton.setLayoutX(canvas.getWidth() / 2 - 100);
        mainMenuButton.setLayoutY(canvas.getHeight() / 2 + 50);
        mainMenuButton.setOnAction(e -> returnToMainMenu());

        // Clear previous children and add buttons to the game scene root
        Pane rootPane = (Pane) gameScene.getRoot();
        rootPane.getChildren().clear();
        rootPane.getChildren().addAll(canvas, restartButton, mainMenuButton);

        logger.info("Game Over screen displayed.");
    }

    private void returnToMainMenu() {
        // Clear all elements from the game scene to ensure a clean state
        Pane rootPane = (Pane) gameScene.getRoot();
        rootPane.getChildren().clear(); // Clear game elements and UI elements
        gameLoop.stop(); // Stop the game loop
        playerShip.resetHealth(); // Reset player's health
        playerShip.reset(840, 450, 5); // Reset player position for 1680x900

        // Switch back to the main menu scene
        Stage primaryStage = (Stage) gameScene.getWindow();
        primaryStage.setScene(menuScene);
        logger.info("Returned to main menu.");
    }

    private void restartGame() {
        // Clear all children except the canvas to remove "Restart" and "Main Menu" buttons
        Pane rootPane = (Pane) gameScene.getRoot();
        rootPane.getChildren().clear();
        rootPane.getChildren().add(canvas); // Add the canvas back for rendering the game

        gameOver = false;
        gameState.reset();
        playerShip.reset(840, 450, 5); // Centered for 1680x900 resolution
        playerShip.resetHealth();
        gameEntityManager.clearAll();
        gameEntityManager.setBossActive(false);
        bossDefeated = false;

        // Start the game loop when the user restarts the game
        gameLoop.start();
        logger.info("Game restarted.");
    }

}
