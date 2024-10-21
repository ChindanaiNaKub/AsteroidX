package se233.asterioddemo;

class Bullet {
    private double x, y;
    private double velocityX, velocityY;
    private double angle;  // New: the angle at which the bullet is fired
    private double speed = 10;

    public Bullet(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;  // Store the spaceship angle
    }

    public void update() {
        // Move the bullet in the direction of the angle
        x += Math.cos(angle) * speed;
        y += Math.sin(angle) * speed;
    }

//    public void update() {
//        // Move bullet in the direction of the angle
//        x += Math.cos(angle) * speed;
//        y += Math.sin(angle) * speed;
//    }

    public boolean isOffScreen(double width, double height) {
        return x < 0 || x > width || y < 0 || y > height;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
