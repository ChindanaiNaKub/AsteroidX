package se233.asterioddemo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    private boolean bossDefeated;
    private AudioClip laserSound;
    private AudioClip hitSound;
    private AudioClip explodeSound;
    private AudioClip thrustSound;
    private AudioClip bossMusic;
    private AudioClip bossStageMusic;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;
    private boolean isMovingUp = false;
    private boolean isMovingDown = false;


    static final Logger logger = Logger.getLogger(AsteroidGame.class.getName());

    private Image backgroundImage;
    private Image backgroundImageBoss;

    private SpriteLoader spriteLoader;
    private Scene menuScene, gameScene;
    private AnimationTimer gameLoop;

    private Drone drone; // To track the drone
    private boolean canSummonDrone = true; // Track if the drone can be summoned
    private long lastDroneTime = 0;
    private final long DRONE_COOLDOWN = 10000; // Cooldown time in milliseconds (10 seconds)

    private static final String[] NUMBER_SPRITES = {
            "numeral0.png", "numeral1.png", "numeral2.png",
            "numeral3.png", "numeral4.png", "numeral5.png",
            "numeral6.png", "numeral7.png", "numeral8.png", "numeral9.png"
    };

    private static final int BOSS_TRIGGER_SCORE = 17;

    private ShipAI shipAI;
    private boolean aiMode = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try (InputStream configFile = AsteroidGame.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            spriteLoader = new SpriteLoader("/sprite/sheet.png", "/sprite/sheet.xml");
            backgroundImage = new Image(getClass().getResource("/sprite/background.png").toExternalForm());
            backgroundImageBoss = new Image(getClass().getResource("/sprite/background_1.png").toExternalForm());
        } catch (Exception e) {
            logger.severe("Error loading resources: " + e.getMessage());
        }

        Pane menuLayout = createMainMenu(primaryStage);
        menuScene = new Scene(menuLayout, 1280, 720);

        primaryStage.setTitle("Asteroid Game");
        primaryStage.setScene(menuScene);
        primaryStage.show();

        setupGameScene(primaryStage);
    }

    private Pane createMainMenu(Stage primaryStage) {
        VBox menuLayout = new VBox(30);
        menuLayout.setStyle("-fx-alignment: center;");

        try {
            Image menuBackgroundImage = new Image(getClass().getResource("/sprite/background.png").toExternalForm());
            BackgroundImage backgroundImage = new BackgroundImage(
                    menuBackgroundImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(1680, 900, false, false, false, false)
            );
            menuLayout.setBackground(new Background(backgroundImage));
        } catch (Exception e) {
            logger.severe("Error setting menu background: " + e.getMessage());
        }

        // Create the label with "AsteroidX"
        Label titleLabel = new Label("AsteroidX");
        titleLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 5, 0.5, 0, 0);");
        // Style explanation:
        // - #FFD700 is a gold/yellow color for high contrast.
        // - dropshadow adds a subtle shadow to improve readability.

        // Create buttons
        Button startButton = createStyledButton("Start", primaryStage);
        Button exitButton = createStyledButton("Exit", primaryStage);

        startButton.setOnAction(e -> startGame(primaryStage));
        exitButton.setOnAction(e -> primaryStage.close());

        // Add label and buttons to the layout
        menuLayout.getChildren().addAll(titleLabel, startButton, exitButton);
        return menuLayout;
    }

    private Button createStyledButton(String text, Stage primaryStage) {
        Button button = new Button(text);
        button.setPrefSize(300, 60);
        button.setStyle("-fx-font-size: 20px; -fx-text-fill: #FFFFFF; -fx-background-color: #333333;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-font-size: 20px; -fx-text-fill: #FFFFFF; -fx-background-color: #555555;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-font-size: 20px; -fx-text-fill: #FFFFFF; -fx-background-color: #333333;"));
        return button;
    }

    private void setupGameScene(Stage primaryStage) {
        try {
            Pane gameRoot = new Pane();
            canvas = new Canvas(1280, 720);
            gameRoot.getChildren().add(canvas);
            gameScene = new Scene(gameRoot, 1280, 720);

            gc = canvas.getGraphicsContext2D();
            gameState = new GameState();
            inputController = new InputController(gameScene);
            gameEntityManager = new GameEntityManager(spriteLoader);
            playerShip = new PlayerShip(640, 360, 5, 30, spriteLoader);

            laserSound = new AudioClip(getClass().getResource("/sounds/laser.m4a").toExternalForm());
            hitSound = new AudioClip(getClass().getResource("/sounds/hit.m4a").toExternalForm());
            explodeSound = new AudioClip(getClass().getResource("/sounds/explode.m4a").toExternalForm());
            thrustSound = new AudioClip(getClass().getResource("/sounds/thrust.m4a").toExternalForm());
            bossMusic = new AudioClip(getClass().getResource("/sounds/boss.mp3").toExternalForm());
            bossStageMusic = new AudioClip(getClass().getResource("/sounds/FEIN.wav").toExternalForm());

            setSoundVolumes();
            startAsteroidAndEnemySpawning();

            gameLoop = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    try {
                        updateGame();
                    } catch (Exception e) {
                        logger.severe("Error in game loop: " + e.getMessage());
                    }
                }
            };
        } catch (Exception e) {
            logger.severe("Error setting up game scene: " + e.getMessage());
        }
    }

    private void setSoundVolumes() {
        laserSound.setVolume(0.2);
        hitSound.setVolume(0.2);
        explodeSound.setVolume(0.3);
        thrustSound.setVolume(0.2);
        bossMusic.setVolume(0.6);
        bossStageMusic.setVolume(0.6);
    }

    private void startGame(Stage primaryStage) {
        try {
            Pane rootPane = (Pane) gameScene.getRoot();
            rootPane.getChildren().clear();
            rootPane.getChildren().add(canvas);

            gameOver = false;
            bossDefeated = false;
            gameState.reset();
            playerShip.reset(840, 450, 5);
            playerShip.resetHealth();
            gameEntityManager.clearAll();
            gameEntityManager.setBossActive(false);

            canSummonDrone = true;
            lastDroneTime = 0;
            drone = null;

            shipAI = new ShipAI(playerShip, gameEntityManager, canvas.getWidth(), canvas.getHeight(), laserSound);

            primaryStage.setScene(gameScene);
            gameLoop.start();
            logger.info("Game started.");
        } catch (Exception e) {
            logger.severe("Error starting game: " + e.getMessage());
        }
    }

    private void updateGame() {
        try {
            clearScreen();

            if (!gameOver) {
                checkShipAIMode();

                if (aiMode && shipAI != null) {
                    shipAI.update();
                    playerShip.draw(gc);
                } else {
                    updatePlayerShip();
                    handleDroneSummon();
                }

                if (!gameEntityManager.isBossActive()) {
                    gameEntityManager.updateAndDrawBullets(gc, canvas.getWidth(), canvas.getHeight());
                    gameEntityManager.updateAndDrawEnemyShips(gc, playerShip.getX(), playerShip.getY());
                    gameEntityManager.updateAndDrawEnemyBullets(gc, canvas.getWidth(), canvas.getHeight());
                    gameEntityManager.updateAndDrawAsteroids(gc);
                    gameEntityManager.updateAndDrawEnemyShipExplosions(gc);
                    checkBossStage();
                    checkCheatMode();
                } else {
                    gameEntityManager.updateAndDrawBoss(gc, playerShip, gameState, hitSound, logger);
                    gameEntityManager.updateAndDrawBullets(gc, canvas.getWidth(), canvas.getHeight());

                    if (gameEntityManager.getBoss() != null && gameEntityManager.getBoss().getHealth() <= 0) {
                        gameEntityManager.setBossActive(false);
                        bossDefeated = true;
                        bossStageMusic.stop();
                        logger.info("Boss defeated, returning to normal stage.");
                    }
                }

                gameEntityManager.updateAndDrawExplosions(gc);
                drawUI(gc, spriteLoader, gameState);
                checkCollisions();

                if (gameState.isGameOver()) {
                    triggerGameOver();
                }

                if (!aiMode && inputController.isShootingPressed()) {
                    fireBullet(inputController);
                }
            } else {
                drawGameOver();
            }
        } catch (Exception e) {
            logger.severe("Error updating game state: " + e.getMessage());
        }
    }


    private void checkBossStage() {
        if (!gameEntityManager.isBossActive() && !bossDefeated &&
                gameState.getScore() >= BOSS_TRIGGER_SCORE &&
                gameState.getScore() % BOSS_TRIGGER_SCORE == 0) {
            gameEntityManager.startBossStage(bossMusic);
            bossStageMusic.play(); // Play the boss stage background music when the boss appears
            logger.info("Boss stage started, playing boss stage music.");
        }
    }

    private void checkShipAIMode() {
        if (inputController.isAIModeActive()) {
            if (!aiMode) {
                aiMode = true; // Activate AI mode
                shipAI = new ShipAI(playerShip, gameEntityManager, canvas.getWidth(), canvas.getHeight(),laserSound);
                logger.info("AI Mode activated");
            }
        }

        if (inputController.isAIModeDeactivate()) {
            if (aiMode) {
                aiMode = false; // Deactivate AI mode
                shipAI = null;
                logger.info("AI Mode deactivated");
            }
        }
    }

    private void checkCheatMode() {
        if (inputController.isCheatModeEnabled() && !gameEntityManager.isBossActive()) {
            gameEntityManager.startBossStage(bossMusic);
            bossStageMusic.play(); // Play the boss stage background music when cheat mode activates the boss
            logger.info("Cheat mode activated: Boss stage started.");
        }
    }

    private void startAsteroidAndEnemySpawning() {
        try {
            Timer spawnTimer = new Timer(true);

            spawnTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        gameEntityManager.continuousSpawnAsteroids(gc);
                    } catch (Exception e) {
                        logger.severe("Error spawning asteroids: " + e.getMessage());
                    }
                }
            }, 0, 4000);

            spawnTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        gameEntityManager.continuousSpawnEnemyShips();
                    } catch (Exception e) {
                        logger.severe("Error spawning enemy ships: " + e.getMessage());
                    }
                }
            }, 0, 5000);
        } catch (Exception e) {
            logger.severe("Error setting up spawning: " + e.getMessage());
        }
    }

    private void fireBullet(InputController inputController) {
        // Player's bullet
        Bullet bullet = playerShip.fireBullet(inputController);
        if (bullet != null) {
            gameEntityManager.addBullet(bullet);
            laserSound.play();
        }

        // If the drone is active, make it fire as well
        if (drone != null && drone.isActive()) {
            drone.handleShooting(true); // Trigger drone shooting when the player shoots
        }
    }


    private void checkCollisions() {
        gameEntityManager.checkCollisions(gameState, playerShip, hitSound, explodeSound, logger);
    }

    private void clearScreen() {
        if (gameEntityManager.isBossActive()) {
            gc.drawImage(backgroundImageBoss, 0, 0, canvas.getWidth(), canvas.getHeight());
        } else {
            gc.drawImage(backgroundImage, 0, 0, canvas.getWidth(), canvas.getHeight());
        }

        // Draw grid overlay
        drawGrid();
    }

    private void drawGrid() {
        gc.setStroke(Color.rgb(255, 255, 255, 0.1)); // White with 10% opacity
        gc.setLineWidth(0.5);

        // Draw vertical lines
        double gridSize = 50; // Adjust this value to change grid size
        for (double x = 0; x <= canvas.getWidth(); x += gridSize) {
            gc.strokeLine(x, 0, x, canvas.getHeight());
        }

        // Draw horizontal lines
        for (double y = 0; y <= canvas.getHeight(); y += gridSize) {
            gc.strokeLine(0, y, canvas.getWidth(), y);
        }
    }

    private void updatePlayerShip() {
        if (inputController.isLeftPressed()) {
            if (!isMovingLeft) {
                logger.info("PlayerShip moved left");
                isMovingLeft = true;
            }
            playerShip.moveHorizontallyLeft();
        } else {
            isMovingLeft = false;
        }

        // Check for right movement input
        if (inputController.isRightPressed()) {
            if (!isMovingRight) {
                logger.info("PlayerShip moved right");
                isMovingRight = true;
            }
            playerShip.moveHorizontallyRight();
        } else {
            isMovingRight = false;
        }

        // Check for up movement input
        if (inputController.isUpPressed()) {
            if (!isMovingUp) {
                logger.info("PlayerShip moved up");
                isMovingUp = true;
            }
            playerShip.moveVerticallyUp();
        } else {
            isMovingUp = false;
        }

        if (inputController.isDownPressed()) {
            if (!isMovingDown) {
                logger.info("PlayerShip moved down");
                isMovingDown = true;
            }
            playerShip.moveVerticallyDown();
        } else {
            isMovingDown = false;
        }

        playerShip.rotateToMouse(inputController.getMouseX(), inputController.getMouseY());

        playerShip.updateShield();
        playerShip.move();
        playerShip.draw(gc);
        playerShip.handleScreenEdges(canvas.getWidth(), canvas.getHeight());
    }

    public void drawNumber(GraphicsContext gc, int number, double x, double y, SpriteLoader spriteLoader) {
        String numberString = Integer.toString(number);
        double spacing = 20;

        for (int i = 0; i < numberString.length(); i++) {
            int digit = Integer.parseInt(String.valueOf(numberString.charAt(i)));
            Image sprite = spriteLoader.getSprite(NUMBER_SPRITES[digit]);
            gc.drawImage(sprite, x + i * spacing, y);
        }
    }

    private void drawUI(GraphicsContext gc, SpriteLoader spriteLoader, GameState gameState) {
        gc.setFill(Color.WHITE);
        gc.setFont(new Font(20));

        gc.drawImage(spriteLoader.getSprite("playerLife1_blue.png"), 20, 20);
        drawNumber(gc, gameState.getLives(), 60, 20, spriteLoader);

        double screenWidth = gc.getCanvas().getWidth();
        drawNumber(gc, gameState.getScore(), screenWidth - 100, 20, spriteLoader);

        String bulletMode = playerShip.getBulletMode();
        gc.fillText("Bullet Mode: " + bulletMode, screenWidth / 2 - 60, canvas.getHeight() - 30);

        // Display drone status
        String droneStatusText;
        Color droneStatusColor;
        if (drone != null && drone.isActive()) {
            droneStatusText = "Drone: Active";
            droneStatusColor = Color.GREEN;
        } else {
            long currentTime = System.currentTimeMillis();
            long cooldownRemaining = (lastDroneTime + DRONE_COOLDOWN - currentTime) / 1000; // Convert to seconds
            if (cooldownRemaining > 0) {
                droneStatusText = "Drone: Cooldown " + cooldownRemaining + "s";
                droneStatusColor = Color.RED;
            } else {
                droneStatusText = "Drone: Ready";
                droneStatusColor = Color.GREEN;
            }
        }

        gc.setFill(droneStatusColor);
        gc.fillText(droneStatusText, screenWidth / 2 - 60, canvas.getHeight() - 10); // Display below bullet mode

        double playerHealthWidth = 200;
        double playerHealthHeight = 15;
        double playerHealthX = 20;
        double playerHealthY = 60;

        gc.setFill(Color.DARKGRAY);
        gc.fillRect(playerHealthX, playerHealthY, playerHealthWidth, playerHealthHeight);

        gc.setFill(Color.GREEN);
        gc.fillRect(playerHealthX, playerHealthY, playerHealthWidth * (playerShip.getHealth() / 100.0), playerHealthHeight);

        gc.setFill(Color.WHITE);
        gc.setFont(new Font(15));
        gc.fillText("PLAYER HP: " + playerShip.getHealth() + "/100", playerHealthX + 50, playerHealthY + 12);

        if (gameEntityManager.getBoss() != null) {
            double bossHealthWidth = 400;
            double bossHealthHeight = 20;
            double bossHealthX = (screenWidth - bossHealthWidth) / 2;

            gc.setFill(Color.DARKGRAY);
            gc.fillRect(bossHealthX, 10, bossHealthWidth, bossHealthHeight);

            gc.setFill(Color.RED);
            gc.fillRect(bossHealthX, 10, bossHealthWidth * (gameEntityManager.getBoss().getHealth() / 200.0), bossHealthHeight);
            gc.setFill(Color.WHITE);
            gc.setFont(new Font(15));
            gc.fillText("BOSS HP: " + gameEntityManager.getBoss().getHealth() + "/200", bossHealthX + bossHealthWidth / 2 - 50, 45);
        }
    }

    private void triggerGameOver() {
        gameOver = true;
        gameLoop.stop();
        bossStageMusic.stop();
        explodeSound.play();
        logger.warning("Game Over! Final Score: " + gameState.getScore());
        drawGameOver();
    }

    private void drawGameOver() {
        // Create a VBox layout for the Game Over menu
        VBox gameOverLayout = new VBox(20); // 20 is the spacing between elements
        gameOverLayout.setStyle("-fx-alignment: center; -fx-padding: 50px;");
        gameOverLayout.setPrefSize(1280, 720); // Set the size to match the other scenes

        // Create the Game Over label with styling
        Label gameOverLabel = new Label("Game Over!");
        gameOverLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #FF0000; -fx-font-weight: bold;");

        // Create the final score label with styling
        Label scoreLabel = new Label("Final Score: " + gameState.getScore());
        scoreLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #FFFFFF;");

        // Use the existing createStyledButton method for styling the buttons
        Button restartButton = createStyledButton("Restart", (Stage) gameScene.getWindow());
        restartButton.setOnAction(e -> restartGame());

        Button mainMenuButton = createStyledButton("Main Menu", (Stage) gameScene.getWindow());
        mainMenuButton.setOnAction(e -> returnToMainMenu());

        // Add all elements to the VBox
        gameOverLayout.getChildren().addAll(gameOverLabel, scoreLabel, restartButton, mainMenuButton);

        // Set a background image similar to the main menu for consistency
        Image gameOverBackgroundImage = new Image(getClass().getResource("/sprite/background.png").toExternalForm());
        BackgroundImage backgroundImage = new BackgroundImage(
                gameOverBackgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(1280, 720, false, false, false, false)
        );
        gameOverLayout.setBackground(new Background(backgroundImage));

        // Clear the root pane and add the game-over layout
        Pane rootPane = (Pane) gameScene.getRoot();
        rootPane.getChildren().clear();
        rootPane.getChildren().add(gameOverLayout);

        logger.info("Game Over screen displayed.");
    }

    private void returnToMainMenu() {
        Pane rootPane = (Pane) gameScene.getRoot();
        rootPane.getChildren().clear();
        gameLoop.stop();
        bossStageMusic.stop();
        playerShip.resetHealth();
        playerShip.reset(840, 450, 5);

        Stage primaryStage = (Stage) gameScene.getWindow();
        primaryStage.setScene(menuScene);
        logger.info("Returned to main menu.");
    }

    private void restartGame() {
        Pane rootPane = (Pane) gameScene.getRoot();
        rootPane.getChildren().clear();
        rootPane.getChildren().add(canvas);

        gameOver = false;
        gameState.reset();
        playerShip.reset(840, 450, 5);
        playerShip.resetHealth();
        gameEntityManager.clearAll();
        gameEntityManager.setBossActive(false);
        bossDefeated = false;

        bossStageMusic.stop();
        canSummonDrone = true; // Allow the player to summon the drone again after restart
        lastDroneTime = 0; // Reset the last drone time for cooldown
        drone = null; // Clear any existing drone

        gameLoop.start();
        logger.info("Game restarted.");
    }

    private void handleDroneSummon() {
        try {
            long currentTime = System.currentTimeMillis();

            if (inputController.isSummonDrone() && canSummonDrone) {
                drone = new Drone(playerShip, spriteLoader, gameEntityManager);
                drone.activate();
                canSummonDrone = false;
                lastDroneTime = currentTime;
                logger.info("Drone summoned!");
            }

            if (drone != null && drone.isActive()) {
                boolean playerShooting = inputController.isShootingPressed();
                drone.update(playerShooting);
                drone.draw(gc);
            }

            if (!canSummonDrone && currentTime - lastDroneTime >= DRONE_COOLDOWN) {
                canSummonDrone = true;
                logger.info("Drone is ready to be summoned again.");
            }
        } catch (Exception e) {
            logger.severe("Error handling drone summon: " + e.getMessage());
        }
    }
}
