package se233.asterioddemo;

public class GameState {
    private int score;
    private int lives;
    private int level;

    public GameState() {
        this.score = 0;
        this.lives = 3;
        this.level = 1;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public int getLives() {
        return lives;
    }

    public void loseLife() {
        if (lives > 0) {
            lives--;
        }
    }

    public boolean isGameOver() {
        return lives <= 0;
    }

    public int getLevel() {
        return level;
    }

    public void nextLevel() {
        level++;
    }

    public void reset() {
        this.score = 0;
        this.lives = 3;
        this.level = 1;
    }
}
