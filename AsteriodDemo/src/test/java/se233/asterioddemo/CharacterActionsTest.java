package se233.asterioddemo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        SpriteLoader spriteLoader = new SpriteLoader("/sprite/sheet.png", "/sprite/sheet.xml");
        gameEntityManager = mock(GameEntityManager.class);
        inputController = mock(InputController.class);
        playerShip = new PlayerShip(100, 100, 5, 30, mock(SpriteLoader.class));
        drone = new Drone(playerShip, spriteLoader, gameEntityManager);
    }

    @Test
    public void testPlayerShipFiresBullet() {
        when(inputController.isShootingPressed()).thenReturn(true);

        Bullet bullet = playerShip.fireBullet(inputController);

        assertNotNull(bullet, "A bullet should be created when the player shoots.");
        assertEquals("default", playerShip.getBulletMode(), "Bullet mode should be 'default' by default.");
        System.out.println("Player Shoot Bullet.");
    }

    @Test
    public void testDroneFiresWhenPlayerShoots() {
        drone.activate();
        drone.update(true);

        verify(gameEntityManager, atLeastOnce()).addBullet(any(Bullet.class));
        System.out.println("Drone Shoot Bullet Following Player.");
    }

    @Test
    public void testDroneDoesNotFireWhenPlayerIsNotShooting() {
        drone.activate();
        drone.update(false);

        verify(gameEntityManager, never()).addBullet(any(Bullet.class));
        System.out.println("Drone Not Shoot Bullet Beacuase Player Not Shoot.");
    }


    @Test
    public void testPlayerShipTakesDamage() {
        int initialHealth = playerShip.getHealth();
        int damage = 10;

        playerShip.reduceHealth(damage);

        assertEquals(initialHealth - damage, playerShip.getHealth(),
                "Player ship's health should decrease by the damage amount.");
        System.out.println("Player take damage - Current Health: " + playerShip.getHealth());
    }

    @Test
    public void testDroneOrbitsAroundPlayer() {
        playerShip.moveHorizontallyRight();
        drone.activate();

        for (int i = 0; i < 10; i++) {
            drone.update(false);
        }

        double orbitRadius = 100;
        double droneAngle = drone.getAngle();

        double expectedX = playerShip.getX() + Math.cos(droneAngle) * orbitRadius;
        double expectedY = playerShip.getY() + Math.sin(droneAngle) * orbitRadius;

        assertEquals(expectedX, drone.getX(), 0.1, "Drone X should be in orbit around the player.");
        assertEquals(expectedY, drone.getY(), 0.1, "Drone Y should be in orbit around the player.");
        System.out.println("Drone Orbit around player at position: " + drone.getX() + ", " + drone.getY());
    }

}

