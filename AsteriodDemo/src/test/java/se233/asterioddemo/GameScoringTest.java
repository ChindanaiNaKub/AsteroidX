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
        playerShip = new PlayerShip(100, 100, 5, 30, mock(SpriteLoader.class));
        logger = Logger.getLogger("TestLogger");
    }

    @Test
    public void testScoreIncreasesWhenAsteroidIsDestroyed() {
        Asteroid largeAsteroid = new Asteroid(100, 100, 2, 60, 3, false, mock(SpriteLoader.class));
        gameEntityManager.asteroids.add(largeAsteroid);
        Bullet bullet = new Bullet(100, 100, 0, mock(SpriteLoader.class), mock(SpriteLoader.class).toString(), 10);
        gameEntityManager.addBullet(bullet);

        gameEntityManager.checkPlayerBulletAsteroidCollisions(gameState, logger);

        assertEquals(3, gameState.getScore(), "Score should increase by 3 for a large asteroid.");
    }

    @Test
    public void testScoreIncreasesWhenMediumAsteroidIsDestroyed() {
        Asteroid mediumAsteroid = new Asteroid(100, 100, 2, 40, 2, false, mock(SpriteLoader.class));
        gameEntityManager.asteroids.add(mediumAsteroid);
        Bullet bullet = new Bullet(100, 100, 0, mock(SpriteLoader.class), mock(SpriteLoader.class).toString(), 10);
        gameEntityManager.addBullet(bullet);

        gameEntityManager.checkPlayerBulletAsteroidCollisions(gameState, logger);

        assertEquals(2, gameState.getScore(), "Score should increase by 2 for a medium asteroid.");
    }

    @Test
    public void testScoreIncreasesWhenSmallAsteroidIsDestroyed() {
        Asteroid smallAsteroid = new Asteroid(100, 100, 2, 20, 1, false, mock(SpriteLoader.class));
        gameEntityManager.asteroids.add(smallAsteroid);
        Bullet bullet = new Bullet(100, 100, 0, mock(SpriteLoader.class), mock(SpriteLoader.class).toString(), 10);
        gameEntityManager.addBullet(bullet);

        gameEntityManager.checkPlayerBulletAsteroidCollisions(gameState, logger);

        assertEquals(1, gameState.getScore(), "Score should increase by 1 for a small asteroid.");
    }

    @Test
    public void testScoreIncreasesWhenEnemyShipIsDestroyed() {
        EnemyShip enemyShip = new EnemyShip(200, 200, 1, 75, Math.PI / 2,  mock(SpriteLoader.class));
        gameEntityManager.enemyShips.add(enemyShip);
        while (enemyShip.getHealth() > 0) {
            enemyShip.takeDamage(10);
        }

        gameEntityManager.defeatEnemyShip(enemyShip, gameState, logger);

        assertEquals(2, gameState.getScore(), "Score should increase by 2 when an enemy ship is destroyed.");
    }

    @Test
    public void testScoreIncreasesWhenBossIsDefeated() {
        Boss boss = new Boss(300, 300, 2, 100,  mock(SpriteLoader.class));
        gameEntityManager.startBossStage(null);
        gameEntityManager.boss = boss;
        gameEntityManager.setBossActive(true);

        while (boss.getHealth() > 0) {
            boss.takeDamage();
        }

        gameEntityManager.defeatBoss(gameState, logger);

        assertEquals(5, gameState.getScore(), "Score should increase by 5 when the boss is defeated.");
    }
}
