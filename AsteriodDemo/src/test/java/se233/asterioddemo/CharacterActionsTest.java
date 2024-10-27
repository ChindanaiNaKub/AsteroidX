package se233.asterioddemo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se233.asterioddemo.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CharacterActionsTest {

    private PlayerShip playerShip;
    private Drone drone;
    private GameEntityManager gameEntityManager;
    private SpriteLoader spriteLoader;
    private InputController inputController;

    @BeforeEach
    public void setUp() {
        spriteLoader = mock(SpriteLoader.class);
        gameEntityManager = mock(GameEntityManager.class);
        inputController = mock(InputController.class);
        playerShip = new PlayerShip(100, 100, 5, 30, spriteLoader);
        drone = new Drone(playerShip, spriteLoader, gameEntityManager);
    }

    @Test
    public void testPlayerShipFiresBullet() {
        // Simulate the input for firing a bullet.
        when(inputController.isShootingPressed()).thenReturn(true);

        // Fire a bullet and verify that it is added to the game entity manager.
        Bullet bullet = playerShip.fireBullet(inputController);

        // Verify that the bullet is created correctly.
        assertNotNull(bullet, "A bullet should be created when the player shoots.");
        assertEquals("default", playerShip.getBulletMode(), "Bullet mode should be 'default' by default.");
    }

    @Test
    public void testDroneFiresWhenPlayerShoots() {
        // Activate the drone and simulate player shooting.
        drone.activate();
        drone.update(true); // Passing 'true' to indicate that the player is shooting.

        // Verify that the drone attempts to fire bullets when the player is shooting.
        verify(gameEntityManager, atLeastOnce()).addBullet(any(Bullet.class));
    }

    @Test
    public void testDroneDoesNotFireWhenPlayerIsNotShooting() {
        // Activate the drone and simulate player not shooting.
        drone.activate();
        drone.update(false); // Passing 'false' to indicate that the player is not shooting.

        // Verify that the drone does not fire bullets when the player is not shooting.
        verify(gameEntityManager, never()).addBullet(any(Bullet.class));
    }


    @Test
    public void testPlayerShipTakesDamage() {
        int initialHealth = playerShip.getHealth();
        int damage = 10;

        playerShip.reduceHealth(damage);

        assertEquals(initialHealth - damage, playerShip.getHealth(),
                "Player ship's health should decrease by the damage amount.");
    }

    @Test
    public void testDroneMovesRelativeToPlayer() {
        playerShip.moveHorizontallyRight();
        drone.activate();
        drone.update(false); // Updating without shooting

        // Check if the drone's position is updated relative to the player's position.
        double expectedX = playerShip.getX() + Math.cos(playerShip.getAngle()) * 100;
        double expectedY = playerShip.getY() + Math.sin(playerShip.getAngle()) * 100;

        assertEquals(expectedX, drone.getX(), 0.1, "Drone X should be relative to player position.");
        assertEquals(expectedY, drone.getY(), 0.1, "Drone Y should be relative to player position.");
    }
}

