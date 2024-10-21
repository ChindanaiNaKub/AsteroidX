package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PlayerShip extends Character {

    private double angle;

    public PlayerShip(double x, double y, double speed, double size) {
        super(x, y, speed, size);
        this.angle = 0;
    }

    @Override
    public void move() {
        // Movement logic for the spaceship
        x += Math.cos(angle) * speed;
        y += Math.sin(angle) * speed;
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
        gc.restore();
    }

    public void rotateLeft() {
        angle -= 0.05;
    }

    public void rotateRight() {
        angle += 0.05;
    }

    public void thrustForward() {
        x += Math.cos(angle) * speed;
        y += Math.sin(angle) * speed;
    }
}
