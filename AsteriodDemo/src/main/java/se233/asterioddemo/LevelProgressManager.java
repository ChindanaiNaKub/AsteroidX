package se233.asterioddemo;

import java.util.logging.Logger;

public class LevelProgressManager {
    private long levelStartTime;
    private final long levelTimeLimit;
    private Logger logger;

    public LevelProgressManager(long levelTimeLimit, Logger logger) {
        this.levelTimeLimit = levelTimeLimit;
        this.logger = logger;
        resetLevelTimer();
    }

    public void resetLevelTimer() {
        levelStartTime = System.currentTimeMillis();
    }

    public boolean isTimeLimitReached() {
        return System.currentTimeMillis() - levelStartTime > levelTimeLimit;
    }

    public void handleLevelTimeout(GameState gameState, LevelManager levelManager) {
        if (isTimeLimitReached()) {
            gameState.nextLevel();
            levelManager.clearEnemyShips();
            logger.info("Time limit reached, moving to the next level.");
            resetLevelTimer();
        }
    }
}
