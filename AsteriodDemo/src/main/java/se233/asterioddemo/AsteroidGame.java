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
    private boolean bossDefeated;
    private AudioClip laserSound;
    private AudioClip hitSound;
    private AudioClip explodeSound;
    private AudioClip thrustSound;
    private AudioClip bossMusic;
    private AudioClip bossStageMusic;

    static final Logger logger = Logger.getLogger(AsteroidGame.class.getName());

    private Image backgroundImage;
    private Image backgroundImageBoss;

    private SpriteLoader spriteLoader;
    private Scene menuScene, gameScene;
    private AnimationTimer gameLoop;

    private static final String[] NUMBER_SPRITES = {
            "numeral0.png", "numeral1.png", "numeral2.png",
            "numeral3.png", "numeral4.png", "numeral5.png",
            "numeral6.png", "numeral7.png", "numeral8.png", "numeral9.png"
    };

    private static final int BOSS_TRIGGER_SCORE = 17;

    @Override
    public void start(Stage primaryStage) {
        try (InputStream configFile = AsteroidGame.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        spriteLoader = new SpriteLoader("/sprite/sheet.png", "/sprite/sheet.xml");
        backgroundImage = new Image(getClass().getResource("/sprite/background.png").toExternalForm());
        backgroundImageBoss = new Image(getClass().getResource("/sprite/background_1.png").toExternalForm());

        Pane menuLayout = createMainMenu(primaryStage);
        menuScene = new Scene(menuLayout, 800, 600);

        primaryStage.setTitle("Asteroid Game");
        primaryStage.setScene(menuScene);
        primaryStage.show();

        setupGameScene(primaryStage);
    }

    private Pane createMainMenu(Stage primaryStage) {
        VBox menuLayout = new VBox(30);
        menuLayout.setStyle("-fx-alignment: center;");

        Image menuBackgroundImage = new Image(getClass().getResource("/sprite/background.png").toExternalForm());
        BackgroundImage backgroundImage = new BackgroundImage(
                menuBackgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(1680, 900, false, false, false, false)
        );
        menuLayout.setBackground(new Background(backgroundImage));

        Button startButton = createStyledButton("Start", primaryStage);
        Button exitButton = createStyledButton("Exit", primaryStage);

        startButton.setOnAction(e -> startGame(primaryStage));
        exitButton.setOnAction(e -> primaryStage.close());

        menuLayout.getChildren().addAll(startButton, exitButton);
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
                updateGame();
            }
        };
    }

    private void setSoundVolumes() {
        laserSound.setVolume(0.1);
        hitSound.setVolume(0.1);
        explodeSound.setVolume(0.3);
        thrustSound.setVolume(0.2);
        bossMusic.setVolume(0.6);
        bossStageMusic.setVolume(0.7);
    }

    private void startGame(Stage primaryStage) {
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

        primaryStage.setScene(gameScene);
        gameLoop.start();
        logger.info("Game started.");
    }

    private void updateGame() {
        clearScreen();

        if (!gameOver) {
            updatePlayerShip();

            if (!gameEntityManager.isBossActive()) {
                // Normal stage behavior
                gameEntityManager.updateAndDrawBullets(gc, canvas.getWidth(), canvas.getHeight());
                gameEntityManager.updateAndDrawEnemyShips(gc, playerShip.getX(), playerShip.getY());
                gameEntityManager.updateAndDrawEnemyBullets(gc, canvas.getWidth(), canvas.getHeight());
                gameEntityManager.updateAndDrawAsteroids(gc);
                gameEntityManager.updateAndDrawEnemyShipExplosions(gc);
                checkBossStage();
                checkCheatMode();
            } else {
                // Boss stage behavior
                gameEntityManager.updateAndDrawBoss(gc, playerShip, gameState, hitSound, logger);
                gameEntityManager.updateAndDrawBullets(gc, canvas.getWidth(), canvas.getHeight());

                // Check if the boss is defeated
                if (gameEntityManager.getBoss() != null && gameEntityManager.getBoss().getHealth() <= 0) {
                    gameEntityManager.setBossActive(false);
                    bossDefeated = true;
                    bossStageMusic.stop(); // Ensure the music stops immediately
                    logger.info("Boss defeated, returning to normal stage.");
                }
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
        if (!gameEntityManager.isBossActive() && !bossDefeated &&
                gameState.getScore() >= BOSS_TRIGGER_SCORE &&
                gameState.getScore() % BOSS_TRIGGER_SCORE == 0) {
            gameEntityManager.startBossStage(bossMusic);
            bossStageMusic.play(); // Play the boss stage background music when the boss appears
            logger.info("Boss stage started, playing boss stage music.");
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
        Timer spawnTimer = new Timer(true);

        spawnTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gameEntityManager.continuousSpawnAsteroids(gc);
            }
        }, 0, 4000);

        spawnTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gameEntityManager.continuousSpawnEnemyShips();
            }
        }, 0, 5000);
    }

    private void fireBullet(InputController inputController) {
        Bullet bullet = playerShip.fireBullet(inputController);
        if (bullet != null) {
            gameEntityManager.addBullet(bullet);
            laserSound.play();
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
    }

    private void updatePlayerShip() {
        if (inputController.isLeftPressed()) playerShip.moveHorizontallyLeft();
        if (inputController.isRightPressed()) playerShip.moveHorizontallyRight();
        if (inputController.isUpPressed()) playerShip.moveVerticallyUp();
        if (inputController.isDownPressed()) playerShip.moveVerticallyDown();

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
        gc.setFill(Color.RED);
        gc.setFont(new Font(40));
        gc.fillText("Game Over", canvas.getWidth() / 2 - 100, canvas.getHeight() / 2 - 150);

        Button restartButton = new Button("Restart");
        restartButton.setPrefSize(200, 50);
        restartButton.setLayoutX(canvas.getWidth() / 2 - 100);
        restartButton.setLayoutY(canvas.getHeight() / 2 - 50);
        restartButton.setOnAction(e -> restartGame());

        Button mainMenuButton = new Button("Main Menu");
        mainMenuButton.setPrefSize(200, 50);
        mainMenuButton.setLayoutX(canvas.getWidth() / 2 - 100);
        mainMenuButton.setLayoutY(canvas.getHeight() / 2 + 50);
        mainMenuButton.setOnAction(e -> returnToMainMenu());

        Pane rootPane = (Pane) gameScene.getRoot();
        rootPane.getChildren().clear();
        rootPane.getChildren().addAll(canvas, restartButton, mainMenuButton);

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
        gameLoop.start();
        logger.info("Game restarted.");
    }
}
