package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class PlayerShip extends Character {
    private int health = 100;
    private double angle;
    private double velocityX = 0;
    private double velocityY = 0;
    final double MAX_SPEED = 5.0;
    private final double THRUST = 0.05;  // Power of thrust
    private final double DECELERATION = 0.98;  // Friction to slow down over time
    private boolean isThrusting = false;  // Track if the ship is thrusting
    private Image shipImage;
    private long lastBulletTime = 0;
    private final long bulletCooldown = 300;
    private SpriteLoader spriteLoader;
    private String bulletMode = "default";
    private static List<Bullet> bullets = new ArrayList<>();

    public PlayerShip(double x, double y, double speed, double size, SpriteLoader spriteLoader) {
        super(x, y, speed, size);
        this.angle = 0;
        this.health = 100;
        this.spriteLoader = spriteLoader;
        // Load the ship sprite from the sheet using the SpriteLoader
        this.shipImage = spriteLoader.getSprite("playerShip1_blue.png");  // Adjust based on your XML
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();

        // Center point translation
        gc.translate(x, y);

        // Convert angle to degrees and rotate
        gc.rotate(Math.toDegrees(angle));

        double imageWidth = shipImage.getWidth();
        double imageHeight = shipImage.getHeight();

        // Calculate scale factor based on desired size
        double scaleFactor = 1.0;  // Use max dimension for consistent scaling

        // Apply scale
        gc.scale(scaleFactor, scaleFactor);

        // Draw the ship image centered
        gc.drawImage(
                shipImage,
                -imageWidth / 2,  // Center horizontally
                -imageHeight / 2, // Center vertically
                imageWidth,
                imageHeight
        );

        if (isThrusting) {
            double flameSize = imageHeight * 0.2;
            for (int i = 0; i < 3; i++) {
                drawFlame(gc, flameSize, 0.7 - (i * 0.2));
            }
            gc.setGlobalAlpha(1.0);
        }

        gc.restore();
    }

    private void drawFlame(GraphicsContext gc, double size, double opacity) {
        gc.setGlobalAlpha(opacity);
        gc.setFill(getFlameColor());

        double flickerX = (Math.random() - 0.5) * size * 0.2;
        double flickerY = (Math.random() - 0.5) * size * 0.2;

        double[] xPoints = {0, -size * 0.5 + flickerX, size * 0.5 + flickerX};
        double[] yPoints = {0, size + flickerY, size + flickerY};

        gc.fillPolygon(xPoints, yPoints, 3);
    }

    private Color getFlameColor() {
        // Returns different colors for flame variation
        double random = Math.random();
        if (random < 0.3) return Color.ORANGE;
        if (random < 0.6) return Color.YELLOW;
        return Color.RED;
    }

    // Method to fire bullets from the ship
    public Bullet fireBullet(InputController inputController) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBulletTime >= bulletCooldown) {
            lastBulletTime = currentTime;
            double shipTipOffset = this.getSize() / 2;
            double bulletStartX = this.getX() + Math.cos(this.getAngle() - Math.PI / 2) * shipTipOffset;
            double bulletStartY = this.getY() + Math.sin(this.getAngle() - Math.PI / 2) * shipTipOffset;

            // Determine bullet mode based on input
            if (inputController.isShurikenMode()) {
                bulletMode = "shuriken";
            } else if (inputController.isPluseMode()) {
                bulletMode = "pluse";
            } else if (inputController.isDefaultMode()) {
                bulletMode = "default";
            }

            // Select the appropriate bullet sprite and damage based on the current mode
            String bulletSprite;
            int damage;
            switch (bulletMode) {
                case "shuriken":
                    bulletSprite = "laserBlue11.png";
                    damage = 12; // Shuriken damage
                    break;
                case "pluse":
                    bulletSprite = "laserBlue08.png";
                    damage = 15; // Pluse damage
                    break;
                default:
                    bulletSprite = "laserBlue07.png";
                    damage = 10; // Default damage
                    break;
            }

            Bullet bullet = new Bullet(bulletStartX, bulletStartY, this.getAngle() - Math.PI / 2, spriteLoader, bulletSprite, damage);
            bullets.add(bullet);  // Add the new bullet to the list
            return bullet;
        }
        return null;
    }



    public void reduceHealth(int amount) {
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }

    public void reset(double startX, double startY, double startSpeed) {
        this.x = startX;
        this.y = startY;
        this.speed = startSpeed;
        this.angle = 0;
        this.velocityX = 0;
        this.velocityY = 0;
        this.isThrusting = false;
    }

    public void handleScreenEdges(double screenWidth, double screenHeight) {
        if (x < 0) x = screenWidth;
        if (x > screenWidth) x = 0;
        if (y < 0) y = screenHeight;
        if (y > screenHeight) y = 0;
    }

    @Override
    public void move() {
        // Apply velocity to the position
        x += velocityX;
        y += velocityY;

        // Decelerate slightly to simulate inertia in space
        velocityX *= DECELERATION;
        velocityY *= DECELERATION;
    }


    public void takeDamage() {
        health -= 10;
    }

    public int getHealth() {
        return health;
    }

    public void resetHealth() {
        health = 100;
    }

    public void rotateLeft() {
        angle -= 2;
    }

    public void rotateRight() {
        angle += 2;
    }

    public void thrustForward() {
        isThrusting = true;  // Enable thrust effect
        // Increase velocity in the direction the ship is facing
        velocityX += Math.cos(angle) * THRUST;
        velocityY += Math.sin(angle) * THRUST;

        // Cap velocity at max speed
        if (Math.sqrt(velocityX * velocityX + velocityY * velocityY) > MAX_SPEED) {
            velocityX *= MAX_SPEED / Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            velocityY *= MAX_SPEED / Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        }
    }

    public void thrustBackward() {
        // Enable thrusting backwards, reducing velocity
        velocityX -= Math.cos(angle) * THRUST;
        velocityY -= Math.sin(angle) * THRUST;

        // Cap velocity at max speed
        if (Math.sqrt(velocityX * velocityX + velocityY * velocityY) > MAX_SPEED) {
            velocityX *= MAX_SPEED / Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            velocityY *= MAX_SPEED / Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        }
    }

    public void stopThrusting() {
        isThrusting = false;  // Disable thrust effect
    }

    public double getAngle() {
        return angle;
    }

    public void decelerate() {
        // Apply friction to slow down the ship gradually
        velocityX *= DECELERATION;
        velocityY *= DECELERATION;
    }

    public void rotateToMouse(double mouseX, double mouseY) {
        double deltaX = mouseX - x;
        double deltaY = mouseY - y;
        angle = Math.atan2(deltaY, deltaX);
    }

    public void moveHorizontallyLeft() {
        x -= MAX_SPEED;
    }

    public void moveHorizontallyRight() {
        x += MAX_SPEED;
    }

    public void moveVerticallyUp() {
        y -= MAX_SPEED;
    }

    public void moveVerticallyDown() {
        y += MAX_SPEED;
    }

    public String getBulletMode() {
        return bulletMode;
    }

    public static List<Bullet> getBullets() {
        return bullets;
    }
}
