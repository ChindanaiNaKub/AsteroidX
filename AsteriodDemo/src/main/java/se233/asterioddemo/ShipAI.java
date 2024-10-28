package se233.asterioddemo;

import javafx.scene.media.AudioClip;

import java.util.List;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class ShipAI {
    private final PlayerShip playerShip;
    private final GameEntityManager entityManager;
    private final double screenWidth;
    private final double screenHeight;
    private static final double SAFE_DISTANCE = 150.0;
    private static final double SHOOTING_ACCURACY = 0.95; // AI accuracy factor
    private final AtomicLong lastShootTime = new AtomicLong(0);
    private static final long SHOOT_COOLDOWN = 300; // Milliseconds between shots
    private final AudioClip laserSound;


    public ShipAI(PlayerShip playerShip, GameEntityManager entityManager, double screenWidth, double screenHeight,AudioClip laserSound) {
        this.playerShip = playerShip;
        this.entityManager = entityManager;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.laserSound = laserSound;
    }

    public void update() {
        synchronized (entityManager) {
            if (entityManager.isBossActive()) {
                handleBossFight();
            } else {
                handleNormalGameplay();
            }
        }
    }

    private void handleNormalGameplay() {
        // Find the closest threat (asteroid or enemy ship)
        Optional<GameObject> closestThreat = findClosestThreat();

        if (closestThreat.isPresent()) {
            GameObject threat = closestThreat.get();
            double distanceToThreat = calculateDistance(playerShip.getX(), playerShip.getY(),
                    threat.getX(), threat.getY());

            // Dodge if too close
            if (distanceToThreat < SAFE_DISTANCE) {
                dodge(threat);
            }

            // Aim and shoot at the threat
            aimAndShoot(threat.getX(), threat.getY());
        } else {
            // Move to the center if no threats are present
            moveToCenter();
        }
    }

    private void handleBossFight() {
        Boss boss = entityManager.getBoss();
        if (boss != null) {
            double distanceToBoss = calculateDistance(playerShip.getX(), playerShip.getY(), boss.getX(), boss.getY());

            // Find and dodge the closest boss bullet
            Optional<Bullet> closestBossBullet = findClosestBossBullet();
            closestBossBullet.ifPresent(this::dodge);

            // Adjust position based on distance to the boss
            if (distanceToBoss < SAFE_DISTANCE * 1.5) {
                moveAwayFrom(boss.getX(), boss.getY());
            } else if (distanceToBoss > SAFE_DISTANCE * 2.5) {
                moveTowards(boss.getX(), boss.getY());
            }

            // Aim and shoot at the boss
            aimAndShoot(boss.getX(), boss.getY());
        }
    }

    private Optional<GameObject> findClosestThreat() {
        double closestDistance = Double.MAX_VALUE;
        GameObject closestThreat = null;

        // Check for closest asteroid
        synchronized (entityManager) {
            for (Asteroid asteroid : entityManager.getAsteroids()) {
                double distance = calculateDistance(playerShip.getX(), playerShip.getY(), asteroid.getX(), asteroid.getY());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestThreat = asteroid;
                }
            }

            // Check for closest enemy ship
            for (EnemyShip enemy : entityManager.getEnemyShips()) {
                double distance = calculateDistance(playerShip.getX(), playerShip.getY(), enemy.getX(), enemy.getY());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestThreat = enemy;
                }
            }
        }

        return Optional.ofNullable(closestThreat);
    }

    private Optional<Bullet> findClosestBossBullet() {
        synchronized (entityManager) {
            if (!entityManager.isBossActive() || entityManager.getBoss() == null) {
                return Optional.empty();
            }

            return entityManager.getBoss().getBossBullets().stream()
                    .min(Comparator.comparingDouble(bullet ->
                            calculateDistance(playerShip.getX(), playerShip.getY(), bullet.getX(), bullet.getY())));
        }
    }

    private void dodge(GameObject threat) {
        synchronized (playerShip) {
            double angleToThreat = Math.atan2(threat.getY() - playerShip.getY(), threat.getX() - playerShip.getX());
            double dodgeAngle = angleToThreat + Math.PI / 2; // Move perpendicular to the threat
            double moveX = Math.cos(dodgeAngle) * playerShip.getSpeed();
            double moveY = Math.sin(dodgeAngle) * playerShip.getSpeed();
            double newX = playerShip.getX() + moveX;
            double newY = playerShip.getY() + moveY;

            // Keep the player within the screen boundaries
            if (newX > 0 && newX < screenWidth) playerShip.setX(newX);
            if (newY > 0 && newY < screenHeight) playerShip.setY(newY);
        }
    }

    private void aimAndShoot(double targetX, double targetY) {
        synchronized (playerShip) {
            double angleToTarget = Math.atan2(targetY - playerShip.getY(), targetX - playerShip.getX());
            angleToTarget += (Math.random() - 0.5) * (1 - SHOOTING_ACCURACY); // Add some randomness for realism
            playerShip.setAngle(angleToTarget);

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastShootTime.get() >= SHOOT_COOLDOWN) {
                Bullet bullet = playerShip.fireBullet(angleToTarget);
                if (bullet != null) {
                    synchronized (entityManager) {
                        entityManager.addBullet(bullet);
                    }
                    lastShootTime.set(currentTime);
                    laserSound.play();
                }
            }
        }
    }

    private void moveToCenter() {
        moveTowards(screenWidth / 2, screenHeight / 2);
    }

    private void moveTowards(double targetX, double targetY) {
        synchronized (playerShip) {
            double dx = targetX - playerShip.getX();
            double dy = targetY - playerShip.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance > 0) {
                double moveX = (dx / distance) * playerShip.getSpeed();
                double moveY = (dy / distance) * playerShip.getSpeed();
                playerShip.setX(playerShip.getX() + moveX);
                playerShip.setY(playerShip.getY() + moveY);
            }
        }
    }

    private void moveAwayFrom(double targetX, double targetY) {
        synchronized (playerShip) {
            double dx = playerShip.getX() - targetX;
            double dy = playerShip.getY() - targetY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance > 0) {
                double moveX = (dx / distance) * playerShip.getSpeed();
                double moveY = (dy / distance) * playerShip.getSpeed();
                playerShip.setX(playerShip.getX() + moveX);
                playerShip.setY(playerShip.getY() + moveY);
            }
        }
    }

    private double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}
