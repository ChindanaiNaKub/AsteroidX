package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class BulletTrail {
    private double x, y;
    private double alpha;
    private final double size;

    public BulletTrail(double x, double y, double size) {
        this.x = x;
        this.y = y;
        this.alpha = 1.0;
        this.size = size;
    }

    public boolean update() {
        alpha -= 0.05; // Fade out speed
        return alpha > 0;
    }

    public void draw(GraphicsContext gc) {
        gc.setGlobalAlpha(alpha);
        gc.setFill(Color.CYAN);
        gc.fillOval(x - size/2, y - size/2, size, size);
        gc.setGlobalAlpha(1.0);
    }
}