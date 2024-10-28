package se233.asterioddemo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;

class EnemyShipTest {
    private EnemyShip enemyShip;

    @Mock
    private SpriteLoader mockSpriteLoader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        enemyShip = new EnemyShip(100, 100, 3.0, 30.0, Math.PI / 2, mockSpriteLoader);
    }

    @Test
    void testEnemyShipMovement() {
        double initialX = enemyShip.getX();
        double initialY = enemyShip.getY();

        enemyShip.move(500, 500);
        assertNotEquals(initialX, enemyShip.getX(), "Enemy X position should change");
        assertNotEquals(initialY, enemyShip.getY(), "Enemy Y position should change");
    }

    @Test
    void testEnemyShipHealth() {
        assertEquals(30, enemyShip.getHealth(), "Initial health should be 30");
    }
}
