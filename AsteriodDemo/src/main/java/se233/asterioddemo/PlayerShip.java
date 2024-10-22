package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PlayerShip extends Character {
    private int health = 100;
    private double angle;
    private double velocityX = 0;
    private double velocityY = 0;
    private final double MAX_SPEED = 5.0;
    private final double THRUST = 0.05;  // Power of thrust
    private final double DECELERATION = 1.0;  // Friction to slow down over time
    private boolean isThrusting = false;  // Track if the ship is thrusting

    public PlayerShip(double x, double y, double speed, double size) {
        super(x, y, speed, size);
        this.angle = 0;
        this.health = 100;
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

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();
        gc.translate(x, y);
        gc.rotate(Math.toDegrees(angle));
        gc.setFill(Color.YELLOW);
        double[] xPoints = {0, -10, 10};
        double[] yPoints = {-15, 10, 10};
        gc.fillPolygon(xPoints, yPoints, 3);

        // Draw the thrust flames if thrusting
        if (isThrusting) {
            gc.setFill(Color.RED);
            double[] flameXPoints = {0, -7, 7};
            double[] flameYPoints = {12, 25, 25};
            gc.fillPolygon(flameXPoints, flameYPoints, 3);
        }

        gc.restore();
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
        angle -= 0.05;
    }

    public void rotateRight() {
        angle += 0.05;
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

    public void stopThrusting() {
        isThrusting = false;  // Disable thrust effect
    }

    public double getAngle() {
        return angle;
    }
}
