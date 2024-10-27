package se233.asterioddemo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se233.asterioddemo.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class GameScoringTest {
    private GameEntityManager gameEntityManager;
    private GameState gameState;
    private PlayerShip playerShip;
    private SpriteLoader spriteLoader;
    private Logger logger;

    @BeforeEach
    public void setUp() {
        spriteLoader = mock(SpriteLoader.class);
        gameEntityManager = new GameEntityManager(spriteLoader);
        gameState = new GameState();
        playerShip = new PlayerShip(100, 100, 5, 30, spriteLoader);
        logger = Logger.getLogger("TestLogger");
    }

    @Test
    public void testScoreIncreasesWhenAsteroidIsDestroyed() {
        // Create a large asteroid and simulate its destruction by a bullet.
        Asteroid largeAsteroid = new Asteroid(100, 100, 2, 60, 3, false, spriteLoader);
        gameEntityManager.asteroids.add(largeAsteroid);
        Bullet bullet = new Bullet(100, 100, 0, spriteLoader, "laserBlue01.png", 10);
        gameEntityManager.addBullet(bullet);

        // Simulate collision detection between the bullet and the large asteroid.
        gameEntityManager.checkPlayerBulletAsteroidCollisions(gameState, logger);

        // Check that the score increased by 3 points for destroying a large asteroid.
        assertEquals(3, gameState.getScore(), "Score should increase by 3 for a large asteroid.");
    }

    @Test
    public void testScoreIncreasesWhenMediumAsteroidIsDestroyed() {
        // Create a medium asteroid and simulate its destruction by a bullet.
        Asteroid mediumAsteroid = new Asteroid(100, 100, 2, 40, 2, false, spriteLoader);
        gameEntityManager.asteroids.add(mediumAsteroid);
        Bullet bullet = new Bullet(100, 100, 0, spriteLoader, "laserBlue01.png", 10);
        gameEntityManager.addBullet(bullet);

        // Simulate collision detection between the bullet and the medium asteroid.
        gameEntityManager.checkPlayerBulletAsteroidCollisions(gameState, logger);

        // Check that the score increased by 2 points for destroying a medium asteroid.
        assertEquals(2, gameState.getScore(), "Score should increase by 2 for a medium asteroid.");
    }

    @Test
    public void testScoreIncreasesWhenSmallAsteroidIsDestroyed() {
        // Create a small asteroid and simulate its destruction by a bullet.
        Asteroid smallAsteroid = new Asteroid(100, 100, 2, 20, 1, false, spriteLoader);
        gameEntityManager.asteroids.add(smallAsteroid);
        Bullet bullet = new Bullet(100, 100, 0, spriteLoader, "laserBlue01.png", 10);
        gameEntityManager.addBullet(bullet);

        // Simulate collision detection between the bullet and the small asteroid.
        gameEntityManager.checkPlayerBulletAsteroidCollisions(gameState, logger);

        // Check that the score increased by 1 point for destroying a small asteroid.
        assertEquals(1, gameState.getScore(), "Score should increase by 1 for a small asteroid.");
    }

    @Test
    public void testScoreIncreasesWhenEnemyShipIsDestroyed() {
        // Arrange: Create an enemy ship with default health.
        // Assume that the default health of the enemy is 10, matching the bullet's damage.
        EnemyShip enemyShip = new EnemyShip(200, 200, 1, 75, Math.PI / 2, spriteLoader);
        gameEntityManager.enemyShips.add(enemyShip);

        // Act: Reduce the enemy's health to 0 to simulate its destruction.
        while (enemyShip.getHealth() > 0) {
            enemyShip.takeDamage(10); // Simulate the enemy taking damage.
        }

        // Call the method to handle the enemy's defeat and increment the score.
        gameEntityManager.defeatEnemyShip(enemyShip, gameState, logger);

        // Assert: Verify that the score increased by 2 points after the enemy ship is destroyed.
        assertEquals(2, gameState.getScore(), "Score should increase by 2 when an enemy ship is destroyed.");
    }

    @Test
    public void testScoreIncreasesWhenBossIsDefeated() {
        // Create a boss and reduce its health to simulate near defeat.
        Boss boss = new Boss(300, 300, 2, 100, spriteLoader);
        gameEntityManager.startBossStage(null);
        gameEntityManager.boss = boss; // Directly set the boss.
        gameEntityManager.setBossActive(true);

        // Reduce boss health to 0 to simulate defeat.
        while (boss.getHealth() > 0) {
            boss.takeDamage(); // Reduce health by 10 each time.
        }

        // Simulate boss defeat and score increment.
        gameEntityManager.defeatBoss(gameState, logger);

        // Check that the score increased by 5 points for defeating the boss.
        assertEquals(5, gameState.getScore(), "Score should increase by 5 when the boss is defeated.");
    }
}
