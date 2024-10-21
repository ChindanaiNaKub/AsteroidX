package se233.asterioddemo;

public class Asteroid {
    private double x, y;
    private double speed;
    private double size;
    private int points;  // Points the asteroid awards when destroyed

    // Constructor that defines the size (small, medium, large)
    public Asteroid(double x, double y, double speed, double size, int points) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.size = size;
        this.points = points;
    }

    public void update() {
        y += speed;
    }

    public boolean isOffScreen(double width, double height) {
        return y > height;
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

    public int getPoints() {
        return points;
    }
}
