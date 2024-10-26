package se233.asterioddemo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {
    private GameState gameState;

    @BeforeEach
    void setUp() {
        gameState = new GameState();
    }

    @Test
    void testGameScoring() {
        assertEquals(0, gameState.getScore(), "Initial score should be 0");
        gameState.addScore(100);
        assertEquals(100, gameState.getScore(), "Score should increase by 100");
    }

    @Test
    void testPlayerLives() {
        int initialLives = gameState.getLives();

        gameState.loseLife();
        assertEquals(initialLives - 1, gameState.getLives(), "Should lose one life");

        for (int i = 0; i < initialLives; i++) {
            gameState.loseLife();
        }
        assertTrue(gameState.isGameOver(), "Game should be over when all lives are lost");
    }

    @Test
    void testGameStateEdgeCases() {
        // Test maximum score accumulation
        gameState.addScore(Integer.MAX_VALUE);
        gameState.addScore(1); // Overflow check
        assertEquals(Integer.MAX_VALUE, gameState.getScore(), "Score should not exceed Integer.MAX_VALUE");

        // Test lives decrement to game over
        int initialLives = gameState.getLives();
        for (int i = 0; i < initialLives; i++) {
            gameState.loseLife();
        }
        assertTrue(gameState.isGameOver(), "Game should be over when all lives are lost");

        // Reset game and verify lives reset
        gameState.reset();
        assertEquals(initialLives, gameState.getLives(), "Lives should reset to initial value after game reset");
        assertEquals(0, gameState.getScore(), "Score should reset to 0 after game reset");
    }

}
