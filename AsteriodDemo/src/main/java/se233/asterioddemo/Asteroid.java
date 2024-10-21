package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Asteroid extends Character {

    private int points;  // Points for destroying the asteroid
    private double[] xPoints;
    private double[] yPoints;
    private int numVertices;

    private double direction;
    private boolean isSplit;  // Track whether this asteroid is a result of a split

    public Asteroid(double x, double y, double speed, double size, int points, boolean isSplit) {
        // Call the Character constructor to initialize x, y, speed, and size
        super(x, y, speed, size);
        this.points = points;
        this.isSplit = isSplit;

        // Randomize the direction of movement
        this.direction = Math.random() * 2 * Math.PI;

        // Generate random polygon shape for the asteroid
        generateRandomPolygon();
    }

    // Generate a random polygon for the asteroid
    private void generateRandomPolygon() {
        Random random = new Random();
        numVertices = random.nextInt(3) + 5; // Random number of vertices (between 5 and 7)

        xPoints = new double[numVertices];
        yPoints = new double[numVertices];

        for (int i = 0; i < numVertices; i++) {
            double angle = 2 * Math.PI / numVertices * i;
            double distance = size / 2 + random.nextDouble() * size / 2; // Randomize the distance of each point

            xPoints[i] = Math.cos(angle) * distance;
            yPoints[i] = Math.sin(angle) * distance;
        }
    }

    @Override
    public void move() {
        // Move the asteroid based on its direction and speed
        x += Math.cos(direction) * speed;
        y += Math.sin(direction) * speed;
    }

    public boolean isOffScreen(double screenWidth, double screenHeight) {
        return (x < -size || x > screenWidth + size || y < -size || y > screenHeight + size);
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Draw the asteroid as a polygon
        gc.save();
        gc.translate(x, y);
        gc.setFill(Color.GRAY);
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(2);
        gc.strokePolygon(xPoints, yPoints, numVertices);
        gc.restore();
    }

    // Method to split an asteroid into two smaller pieces
    public List<Asteroid> split() {
        List<Asteroid> smallerAsteroids = new ArrayList<>();
        if (size > 20) {  // Define a threshold for splitting
            double newSize = size / 2;
            int newPoints = points / 2;

            // Create two smaller asteroids
            smallerAsteroids.add(new Asteroid(x, y, speed * 1.2, newSize, newPoints, true));
            smallerAsteroids.add(new Asteroid(x, y, speed * 1.2, newSize, newPoints, true));
        }
        return smallerAsteroids;
    }

    public int getPoints() {
        return points;
    }

    public boolean isSplit() {
        return isSplit;
    }
}
