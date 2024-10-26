package se233.asterioddemo;

public class GameState {
    private int score;
    private int lives;
    private int level;
    private boolean gameOver; // New field for game over state

    public GameState() {
        this.score = 0;
        this.lives = 3;
        this.level = 1;
        this.gameOver = false; // Initialize game over to false
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        if (score <= Integer.MAX_VALUE - points) {
            score += points;
        } else {
            score = Integer.MAX_VALUE;
        }
    }

    public int getLives() {
        return lives;
    }

    public void loseLife() {
        if (lives > 0) {
            lives--;
        }
        if (lives == 0) {
            setGameOver(true); // Set game over if no lives left
        }
    }

    public boolean isGameOver() {
        return gameOver || lives <= 0; // Check explicit game over or lives
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
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
        this.gameOver = false; // Reset game over state
    }
}
