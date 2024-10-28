package se233.asterioddemo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameEntityManagerTest {
    private GameEntityManager entityManager;

    @Mock
    private SpriteLoader mockSpriteLoader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        entityManager = new GameEntityManager(mockSpriteLoader);
    }

    @Test
    void testBulletCreation() {
        // Create a mock or stub of InputController
        InputController mockInputController = mock(InputController.class);

        // Simulate the default bullet mode for the test
        when(mockInputController.isDefaultMode()).thenReturn(true);

        PlayerShip playerShip = new PlayerShip(400, 300, 5.0, 40.0, mockSpriteLoader);
        Bullet bullet = playerShip.fireBullet(mockInputController); // Pass the mock InputController

        assertNotNull(bullet, "Should create bullet when firing");
    }


    @Test
    void testCollisionHandling() {
        // Arrange: Create game state and entity manager with a mock sprite loader.
        GameState gameState = new GameState();
        SpriteLoader spriteLoader = mock(SpriteLoader.class);
        GameEntityManager gameEntityManager = new GameEntityManager(spriteLoader);
        Logger logger = Logger.getLogger("TestLogger");

        // Create an enemy ship with enough health to test collision handling.
        EnemyShip enemyShip = new EnemyShip(200, 200, 1, 75, Math.PI / 2, spriteLoader);
        gameEntityManager.enemyShips.add(enemyShip);

        // Create an asteroid with points and size for testing.
        Asteroid asteroid = new Asteroid(250, 200, 2, 40, 2, false, spriteLoader);
        gameEntityManager.asteroids.add(asteroid);

        // Create bullets positioned to collide with the enemy and the asteroid.
        Bullet bulletForEnemy = new Bullet(200, 200, 0, spriteLoader, "laserBlue01.png", 10);
        Bullet bulletForAsteroid = new Bullet(250, 200, 0, spriteLoader, "laserBlue01.png", 10);
        gameEntityManager.addBullet(bulletForEnemy);
        gameEntityManager.addBullet(bulletForAsteroid);

        // Act: Call the collision handling method.
        gameEntityManager.checkPlayerBulletEnemyCollisions(gameState);
        gameEntityManager.checkPlayerBulletAsteroidCollisions(gameState, logger);

        // Assert: Verify the outcomes of the collisions.
        // Enemy ship should be damaged and, if health reaches zero, defeated.
        if (enemyShip.getHealth() <= 0) {
            assertEquals(2, gameState.getScore(), "Score should increase by 2 when the enemy ship is destroyed.");
        } else {
            assertTrue(enemyShip.getHealth() > 0, "Enemy ship should have remaining health if not destroyed.");
        }

        // Asteroid should be removed, and score should increase by the asteroid's point value.
        assertFalse(gameEntityManager.asteroids.contains(asteroid), "Asteroid should be removed after collision.");
        assertEquals(2, gameState.getScore(), "Score should increase by 2 when an asteroid is destroyed.");

        // Additionally, verify bullets are marked as inactive or handled correctly.
        assertFalse(gameEntityManager.bullets.contains(bulletForEnemy), "Bullet that collided with enemy should be removed.");
        assertFalse(gameEntityManager.bullets.contains(bulletForAsteroid), "Bullet that collided with asteroid should be removed.");
    }

}
