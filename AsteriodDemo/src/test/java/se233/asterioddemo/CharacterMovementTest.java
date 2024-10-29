package se233.asterioddemo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class CharacterMovementTest {

    private PlayerShip playerShip;

    @BeforeEach
    public void setUp() {
        SpriteLoader spriteLoader = new SpriteLoader("/sprite/sheet.png", "/sprite/sheet.xml");
        playerShip = new PlayerShip(100, 100, 5, 30, mock(SpriteLoader.class));
    }

    @Test
    public void testMoveHorizontallyLeft() {
        double initialX = playerShip.getX();
        playerShip.moveHorizontallyLeft();
        assertTrue(playerShip.getX() < initialX, "Player should move left.");
        System.out.println("Player move left.");
    }

    @Test
    public void testMoveHorizontallyRight() {
        double initialX = playerShip.getX();
        playerShip.moveHorizontallyRight();
        assertTrue(playerShip.getX() > initialX, "Player should move right.");
        System.out.println("Player move right.");
    }

    @Test
    public void testMoveVerticallyUp() {
        double initialY = playerShip.getY();
        playerShip.moveVerticallyUp();
        assertTrue(playerShip.getY() < initialY, "Player should move up.");
        System.out.println("Player move up.");
    }

    @Test
    public void testMoveVerticallyDown() {
        double initialY = playerShip.getY();
        playerShip.moveVerticallyDown();
        assertTrue(playerShip.getY() > initialY, "Player should move down.");
        System.out.println("Player move down.");
    }


    @Test
    public void testRotateToMouse() {
        double initialAngle = playerShip.getAngle();
        playerShip.rotateToMouse(200, 200);
        assertNotEquals(initialAngle, playerShip.getAngle(), "Player's angle should change when rotating to mouse.");
        System.out.println("Player's angle change when rotating to mouse.");
    }
}
