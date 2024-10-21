package se233.asterioddemo;

public class Bullet {
    private double x, y;
    private double angle;
    private double speed = 10;

    public Bullet(double startX, double startY, double angle) {
        this.x = startX;
        this.y = startY;
        this.angle = angle;
    }

    public void update() {
        x += Math.cos(angle) * speed;
        y += Math.sin(angle) * speed;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isOffScreen(double screenWidth, double screenHeight) {
        return (x < 0 || x > screenWidth || y < 0 || y > screenHeight);
    }
}

