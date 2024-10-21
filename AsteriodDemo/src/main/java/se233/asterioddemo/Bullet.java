package se233.asterioddemo;

class Bullet {
    private double x, y;
    private double speed = 10;

    public Bullet(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        y -= speed;
    }

    public boolean isOffScreen(double width, double height) {
        return y < 0 || y > height;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}

