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
    public void testDroneOrbitsAroundPlayer() {
        // Move player horizontally and activate drone.
        playerShip.moveHorizontallyRight();
        drone.activate();

        // Simulate multiple updates to allow the drone to orbit around the player.
        for (int i = 0; i < 10; i++) {
            drone.update(false); // Update without shooting
        }

        // Calculate expected position of the drone considering the rotation angle around the player.
        double orbitRadius = 100; // Distance of drone from the player
        double droneAngle = drone.getAngle(); // Retrieve the current angle of the drone

        double expectedX = playerShip.getX() + Math.cos(droneAngle) * orbitRadius;
        double expectedY = playerShip.getY() + Math.sin(droneAngle) * orbitRadius;

        // Check that the drone is in the expected orbit position with a tolerance.
        assertEquals(expectedX, drone.getX(), 0.1, "Drone X should be in orbit around the player.");
        assertEquals(expectedY, drone.getY(), 0.1, "Drone Y should be in orbit around the player.");
    }

}

