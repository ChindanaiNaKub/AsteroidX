package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import se233.asterioddemo.exception.SpriteNotFoundException;

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
    private boolean isShieldActive = false;
    private double shieldAlpha = 0.0;
    private boolean isHit = false;
    private long hitAnimationStart = 0;
    private final long HIT_ANIMATION_DURATION = 500; // milliseconds
    private long shieldActivationTime = 0;
    private final long SHIELD_DURATION = 3000; // Shield lasts for 3 seconds
    private AnimatedSprite shipSprite;

    public PlayerShip(double x, double y, double speed, double size, SpriteLoader spriteLoader) {
        super(x, y, speed, size);
        this.angle = 0;
        this.health = 100;
        this.spriteLoader = spriteLoader; // Assign the spriteLoader instance

        try {
            // Load newship.png sprite sheet using the spriteLoader
            Image newShipImage = new Image(getClass().getResource("/sprite/newship.png").toExternalForm());
            if (newShipImage.isError()) {
                throw new SpriteNotFoundException("Failed to load ship sprite sheet");
            }

            int frameCount = 4; // Assuming 4 frames in a row
            int frameWidth = 117;
            int frameHeight = 117;
            int columns = 4;
            int rows = 1;

            // Initialize AnimatedSprite with the new sprite sheet
            this.shipSprite = new AnimatedSprite(newShipImage, frameCount, columns, rows, 0, 0, frameWidth, frameHeight);
        } catch (Exception e) {
            System.err.println("Error loading ship sprite: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();
        gc.translate(x, y);
        gc.rotate(Math.toDegrees(angle));

        // Hit animation effect
        if (isHit) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - hitAnimationStart < HIT_ANIMATION_DURATION) {
                if ((currentTime / 100) % 2 == 0) {
                    gc.setGlobalAlpha(0.5);
                }
            } else {
                isHit = false;
            }
        }

        // Update and draw the animated sprite for the ship
        shipSprite.tick();
        shipSprite.render(gc, -shipSprite.getWidth() / 2, -shipSprite.getHeight() / 2);

        // Draw shield if active
        if (isShieldActive) {
            drawShield(gc, shipSprite.getWidth() * 1.2);
        }

        // Draw thrust effect
        if (isThrusting) {
            drawThrustEffect(gc);
        }

        gc.restore();
    }


    private void drawShield(GraphicsContext gc, double size) {
        gc.setStroke(Color.rgb(100, 200, 255, shieldAlpha));
        gc.setLineWidth(3);
        gc.strokeOval(-size/2, -size/2, size, size);

        // Add shield wave effect
        double waveSize = size * (1 + Math.sin(System.currentTimeMillis() * 0.005) * 0.05);
        gc.setStroke(Color.rgb(100, 200, 255, shieldAlpha * 0.5));
        gc.strokeOval(-waveSize/2, -waveSize/2, waveSize, waveSize);
    }

    public void activateShield() {
        isShieldActive = true;
        shieldAlpha = Math.min(shieldAlpha + 0.1, 0.7);
        shieldActivationTime = System.currentTimeMillis();
    }

    public void updateShield() {
        long currentTime = System.currentTimeMillis();
        if (isShieldActive && currentTime - shieldActivationTime >= SHIELD_DURATION) {
            deactivateShield();
        }
    }

    public void deactivateShield() {
        isShieldActive = false;
        shieldAlpha = Math.max(shieldAlpha - 0.1, 0);
    }

    private void drawThrustEffect(GraphicsContext gc) {
        double baseSize = shipImage.getHeight() * 0.2;
        double time = System.currentTimeMillis() * 0.001;

        // Main thrust
        for (int i = 0; i < 3; i++) {
            double flickerSize = baseSize * (1 + Math.sin(time * 10 + i) * 0.2);
            double opacity = 0.7 - (i * 0.2);

            gc.setGlobalAlpha(opacity);
            gc.setFill(getFlameColor());

            double flickerX = Math.sin(time * 20 + i) * baseSize * 0.1;
            double[] xPoints = {
                    flickerX,
                    -flickerSize + flickerX,
                    flickerSize + flickerX
            };
            double[] yPoints = {
                    0,
                    flickerSize * 1.5,
                    flickerSize * 1.5
            };

            gc.fillPolygon(xPoints, yPoints, 3);
        }

        // Add particle effects
        for (int i = 0; i < 2; i++) {
            double particleSize = baseSize * 0.3;
            double particleX = (Math.random() - 0.5) * baseSize;
            double particleY = baseSize * 1.2 + Math.random() * baseSize * 0.5;

            gc.setGlobalAlpha(0.3);
            gc.setFill(Color.WHITE);
            gc.fillOval(particleX - particleSize/2, particleY - particleSize/2,
                    particleSize, particleSize);
        }

        gc.setGlobalAlpha(1.0);
    }

    private Color getFlameColor() {
        // Returns different colors for flame variation
        double random = Math.random();
        if (random < 0.3) return Color.ORANGE;
        if (random < 0.6) return Color.YELLOW;
        return Color.RED;
    }

    // Add this overloaded method in PlayerShip class to handle AI shooting directly with an angle
    public Bullet fireBullet(double angle) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBulletTime >= bulletCooldown) {
            lastBulletTime = currentTime;
            double shipTipOffset = this.getSize() / 2;
            double bulletStartX = this.getX() + Math.cos(angle) * shipTipOffset;
            double bulletStartY = this.getY() + Math.sin(angle) * shipTipOffset;

            // Select the appropriate bullet sprite and damage based on the current bullet mode
            String bulletSprite;
            int damage;
            switch (bulletMode) {
                case "shuriken":
                    bulletSprite = "laserBlue11.png";
                    damage = 12;
                    break;
                case "pluse":
                    bulletSprite = "laserBlue08.png";
                    damage = 15;
                    break;
                default:
                    bulletSprite = "laserBlue07.png";
                    damage = 10;
                    break;
            }

            Bullet bullet = new Bullet(bulletStartX, bulletStartY, angle, spriteLoader, bulletSprite, damage);
            bullets.add(bullet);
            return bullet;
        }
        return null;
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
        activateShield(); // Activate the shield when the ship resets.
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

    public double getSpeed() {
        return speed;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

}
