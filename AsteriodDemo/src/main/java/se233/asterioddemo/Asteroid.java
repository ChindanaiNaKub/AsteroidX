package se233.asterioddemo;

class Asteroid {
    private double x, y;
    private double speed = 2;
    private double size = 30;

    public Asteroid(double x, double y) {
        this.x = x;
        this.y = y;
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
}

