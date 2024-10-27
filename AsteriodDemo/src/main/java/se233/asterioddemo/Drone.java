package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Drone extends Character {
    private PlayerShip player;
    private GameEntityManager gameEntityManager;
    private double angle;
    private double distanceFromPlayer = 100;
    private Image droneImage;
    private boolean isActive = false;
    private long activationTime;
    private final long DRONE_DURATION = 5000;
    private final long BULLET_COOLDOWN = 500;
    private long lastBulletTime = 0;
    private SpriteLoader spriteLoader;
    private final long DRONE_COOLDOWN = 10_000;

    public Drone(PlayerShip player, SpriteLoader spriteLoader, GameEntityManager gameEntityManager) {
        super(player.getX(), player.getY(), player.getSpeed(), 20);
        this.player = player;
        this.spriteLoader = spriteLoader;
        this.gameEntityManager = gameEntityManager;
        this.droneImage = spriteLoader.getSprite("playerShip3_orange.png");
    }

    public void activate() {
        isActive = true;
        activationTime = System.currentTimeMillis();
    }

    @Override
    public void move() {
        if (!isActive) return;

        angle = player.getAngle();
        x = player.getX() + Math.cos(angle) * distanceFromPlayer;
        y = player.getY() + Math.sin(angle) * distanceFromPlayer;
    }

    public void update(boolean playerShooting) {
        if (!isActive) return;

        move();

        // Fire bullets if the player is shooting.
        handleShooting(playerShooting);

        if (System.currentTimeMillis() - activationTime >= DRONE_DURATION) {
            deactivate();
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (!isActive) return;

        gc.save();
        gc.translate(x, y);
        gc.rotate(Math.toDegrees(angle));

        if (droneImage != null) {
            double imageWidth = droneImage.getWidth();
            double imageHeight = droneImage.getHeight();
            gc.drawImage(droneImage, -imageWidth / 2, -imageHeight / 2, imageWidth, imageHeight);
        } else {
            gc.setFill(Color.GRAY);
            gc.fillOval(-10, -10, 20, 20);
        }

        gc.restore();
    }

    void handleShooting(boolean playerShooting) {
        if (!playerShooting) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBulletTime >= BULLET_COOLDOWN) {
            fireBullet();
            lastBulletTime = currentTime;
        }
    }

    private void fireBullet() {
        double bulletX = x + Math.cos(angle - Math.PI / 2) * 20;
        double bulletY = y + Math.sin(angle - Math.PI / 2) * 20;

        Bullet bullet = new Bullet(
                bulletX,
                bulletY,
                angle - Math.PI / 2,
                spriteLoader,
                "laserGreen07.png",
                7
        );

        gameEntityManager.addBullet(bullet);
        System.out.println("Drone fired a bullet!");
    }

    public void deactivate() {
        isActive = false;
    }

    public boolean isActive() {
        return isActive;
    }
}
