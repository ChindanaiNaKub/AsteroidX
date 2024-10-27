package se233.asterioddemo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CharacterMovementTest {

    private PlayerShip playerShip;

    @BeforeEach
    public void setUp() {
        SpriteLoader spriteLoader = new SpriteLoader("/sprite/sheet.png", "/sprite/sheet.xml");
        playerShip = new PlayerShip(100, 100, 5, 30, spriteLoader);
    }

    @Test
    public void testMoveHorizontallyLeft() {
        double initialX = playerShip.getX();
        playerShip.moveHorizontallyLeft();
        assertTrue(playerShip.getX() < initialX, "Player should move left.");
    }

    @Test
    public void testMoveHorizontallyRight() {
        double initialX = playerShip.getX();
        playerShip.moveHorizontallyRight();
        assertTrue(playerShip.getX() > initialX, "Player should move right.");
    }

    @Test
    public void testMoveVerticallyUp() {
        double initialY = playerShip.getY();
        playerShip.moveVerticallyUp();
        assertTrue(playerShip.getY() < initialY, "Player should move up.");
    }

    @Test
    public void testMoveVerticallyDown() {
        double initialY = playerShip.getY();
        playerShip.moveVerticallyDown();
        assertTrue(playerShip.getY() > initialY, "Player should move down.");
    }


    @Test
    public void testRotateToMouse() {
        double initialAngle = playerShip.getAngle();
        playerShip.rotateToMouse(200, 200);
        assertNotEquals(initialAngle, playerShip.getAngle(), "Player's angle should change when rotating to mouse.");
    }
}
