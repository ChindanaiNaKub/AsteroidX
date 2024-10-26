package se233.asterioddemo;

import javafx.scene.media.AudioClip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerShipTest {

    private PlayerShip playerShip;

    @Mock
    private SpriteLoader mockSpriteLoader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        playerShip = new PlayerShip(400, 300, 5.0, 40.0, mockSpriteLoader);
    }

    @Test
    void testPlayerShipMovement() {
        double initialX = playerShip.getX();
        playerShip.moveHorizontallyRight();
        assertEquals(initialX + playerShip.MAX_SPEED, playerShip.getX(), "Player should move right by MAX_SPEED");
    }

    @Test
    void testPlayerShipRotation() {
        double initialAngle = playerShip.getAngle();
        playerShip.rotateLeft();
        assertTrue(playerShip.getAngle() < initialAngle, "Player should rotate left");
    }

    @Test
    void testPlayerShipHealth() {
        assertEquals(100, playerShip.getHealth(), "Initial health should be 100");
        playerShip.takeDamage();
        assertEquals(90, playerShip.getHealth(), "Health should decrease by 10 after damage");
    }

    @Test
    void testPlayerShipEdgeWrap() {
        double screenWidth = 800;
        double screenHeight = 600;

        // Place PlayerShip slightly beyond the right edge
        playerShip.setX(screenWidth + 5);
        playerShip.handleScreenEdges(screenWidth, screenHeight);
        assertEquals(0, playerShip.getX(), "Ship should wrap to the left edge");

        // Place PlayerShip slightly beyond the bottom edge
        playerShip.setY(screenHeight + 5);
        playerShip.handleScreenEdges(screenWidth, screenHeight);
        assertEquals(0, playerShip.getY(), "Ship should wrap to the top edge");

        // Test near the left edge
        playerShip.setX(-5);
        playerShip.handleScreenEdges(screenWidth, screenHeight);
        assertEquals(screenWidth, playerShip.getX(), "Ship should wrap to the right edge");

        // Test near the top edge
        playerShip.setY(-5);
        playerShip.handleScreenEdges(screenWidth, screenHeight);
        assertEquals(screenHeight, playerShip.getY(), "Ship should wrap to the bottom edge");
    }


    // Add more PlayerShip-specific tests here
}
