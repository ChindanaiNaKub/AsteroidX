package se233.asterioddemo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
        PlayerShip playerShip = new PlayerShip(400, 300, 5.0, 40.0, mockSpriteLoader);
        Bullet bullet = playerShip.fireBullet();
        assertNotNull(bullet, "Should create bullet when firing");
    }


    @Test
    void testCollisionHandling() {
        // Set up collision scenario and verify it handles properly
    }
}
