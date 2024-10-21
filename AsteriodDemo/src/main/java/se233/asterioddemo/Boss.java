package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class Boss extends Character{
    private double x, y;
    private double speed;
    private double size;
    private int health;
    private List<Bullet> bossBullets;

    public Boss(double x, double y, double speed, double size) {
        super(x, y, speed, size);
        this.health = 100;  // Set the boss health
        this.bossBullets = new ArrayList<>();
    }

    public void move() {
        // Simple back-and-forth horizontal movement for the boss
        x += speed;
        if (x > 800 - size || x < size) {
            speed = -speed;  // Change direction when hitting the screen edge
        }
    }

    public void attack() {
        // Boss attacks by shooting bullets
        if (Math.random() < 0.05) {  // Randomly fire a bullet
            double bulletSpeed = 3;
            bossBullets.add(new Bullet(x, y + size / 2, Math.PI / 2));  // Shoot downward
        }
    }

    public void draw(GraphicsContext gc) {
        // Draw the boss as a large rectangle
        gc.setFill(Color.PURPLE);
        gc.fillRect(x - size / 2, y - size / 2, size, size);

        // Draw the boss's health bar
        gc.setFill(Color.RED);
        gc.fillRect(x - size / 2, y - size - 10, size * (health / 100.0), 5);  // Health bar
    }

    public void takeDamage() {
        health -= 10;  // Reduce health by 10 for each hit
    }

    public int getHealth() {
        return health;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getSize() {
        return size;
    }

    public List<Bullet> getBossBullets() {
        return bossBullets;
    }
}
