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

    private static final int DRONE_ATTACK_PATTERN = 2; // 0 = single, 1 = spread, 2 = spiral

    public Drone(PlayerShip player, SpriteLoader spriteLoader, GameEntityManager gameEntityManager) {
        super(player.getX(), player.getY(), player.getSpeed(), 20);
        this.player = player;
        this.spriteLoader = spriteLoader;
        this.gameEntityManager = gameEntityManager;
        this.droneImage = spriteLoader.getSprite("playerLife1_orange.png");
    }

    public void activate() {
        isActive = true;
        activationTime = System.currentTimeMillis();
    }

    @Override
    public void move() {
        if (!isActive) return;

        // Increase the angle for smooth movement (rotate around the player).
        angle += 0.05; // Adjust this value to control the speed of the orbit.

        // Calculate new x and y positions to maintain circular movement.
        x = player.getX() + Math.cos(angle) * distanceFromPlayer;
        y = player.getY() + Math.sin(angle) * distanceFromPlayer;
    }

    public void update(boolean playerShooting) {
        if (!isActive) return;

        move();

        // Fire bullets based on the drone's attack pattern
        handleAttack(playerShooting);

        if (System.currentTimeMillis() - activationTime >= DRONE_DURATION) {
            deactivate();
        }
    }

    private void handleAttack(boolean playerShooting) {
        if (!playerShooting) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBulletTime >= BULLET_COOLDOWN) {
            fireBulletPattern();
            lastBulletTime = currentTime;
        }
    }

    private void fireBulletPattern() {
        switch (DRONE_ATTACK_PATTERN) {
            case 0: // Single shot
                fireBullet();
                break;
            case 1: // Spread shot
                for (int i = -2; i <= 2; i++) {
                    double angle = this.angle + (i * Math.PI / 8);
                    fireBulletAt(angle);
                }
                break;
            case 2: // Spiral shot
                double baseAngle = System.currentTimeMillis() / 1000.0;
                for (int i = 0; i < 8; i++) {
                    double angle = baseAngle + (i * Math.PI / 4);
                    fireBulletAt(angle);
                }
                break;
        }
    }
    private void fireBulletAt(double angle) {
        double bulletX = x + Math.cos(angle) * 20;
        double bulletY = y + Math.sin(angle) * 20;

        Bullet bullet = new Bullet(
                bulletX,
                bulletY,
                angle,
                spriteLoader,
                "laserGreen07.png",
                7
        );

        gameEntityManager.addBullet(bullet);
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
