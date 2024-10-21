package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.AudioClip;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class LevelManager {
    private List<Asteroid> asteroids;
    private List<Bullet> bullets;  // List to store bullets
    private Boss boss;
    private boolean bossActive;
    private Random random;

    public LevelManager() {
        this.asteroids = new ArrayList<>();
        this.bullets = new ArrayList<>();  // Initialize bullets list
        this.random = new Random();
        this.bossActive = false;
    }

    public void clearAsteroids() {
        asteroids.clear();
    }


    public void checkCollisions(GameState gameState, PlayerShip playerShip, AudioClip hitSound, AudioClip explodeSound, Logger logger) {
        List<Bullet> bulletsToRemove = new ArrayList<>();
        List<Asteroid> asteroidsToRemove = new ArrayList<>();
        List<Bullet> bossBulletsToRemove = new ArrayList<>();

        // Check for collisions between player bullets and the boss
        if (boss != null) {
            for (Bullet bullet : bullets) {
                if (Math.hypot(bullet.getX() - boss.getX(), bullet.getY() - boss.getY()) < boss.getSize() / 2) {
                    // Bullet hit the boss
                    boss.takeDamage();
                    bulletsToRemove.add(bullet); // Remove the bullet
                    logger.info("Boss hit! Boss health: " + boss.getHealth());

                    if (boss.getHealth() <= 0) {
                        // Boss defeated
                        logger.info("Boss defeated!");
                        boss = null;  // Remove the boss
                        setBossActive(false);  // Mark the boss as inactive
                        gameState.addScore(100);  // Add score for defeating the boss
                    }
                }
            }

            // Check for collisions between boss bullets and player ship
            if (boss != null){
                for (Bullet bossBullet : boss.getBossBullets()) {
                    if (Math.hypot(bossBullet.getX() - playerShip.getX(), bossBullet.getY() - playerShip.getY()) < playerShip.getSize() / 2) {
                        // Player hit by boss bullet
                        playerShip.reduceHealth(20); // Reduce player health
                        bossBulletsToRemove.add(bossBullet);  // Remove boss bullet
                        logger.info("Player hit by boss bullet! Player health: " + playerShip.getHealth());

                        if (playerShip.getHealth() <= 0) {
                            // Trigger game over if player health is 0
                            explodeSound.play();  // Play explosion sound
                        }
                    }
                }
            }
        }

        // Check for collisions between playerShip and asteroids
        for (Asteroid asteroid : asteroids) {
            double distanceToAsteroid = Math.hypot(playerShip.getX() - asteroid.getX(), playerShip.getY() - asteroid.getY());
            if (distanceToAsteroid < (asteroid.getSize() / 2 + 15)) { // Approximate radius of the spaceship is 15
                // Collision detected
                gameState.loseLife();  // Lose a life
                asteroidsToRemove.add(asteroid);  // Mark asteroid for removal
                hitSound.play();  // Play hit sound

                if (gameState.isGameOver()) {
                    explodeSound.play();  // Play explosion sound when game is over
                }
                break;
            }
        }

        // Check for collisions between bullets and asteroids
        for (Bullet bullet : bullets) {
            for (Asteroid asteroid : asteroids) {
                double distanceToBullet = Math.hypot(bullet.getX() - asteroid.getX(), bullet.getY() - asteroid.getY());
                if (distanceToBullet < asteroid.getSize() / 2) {
                    // Bullet hit an asteroid
                    gameState.addScore(asteroid.getPoints());  // Add points based on asteroid size
                    logger.info("Asteroid destroyed! Score: " + gameState.getScore());

                    // Split the asteroid if possible
                    List<Asteroid> newAsteroids = asteroid.split();
                    asteroids.addAll(newAsteroids);

                    bulletsToRemove.add(bullet);  // Mark bullet for removal
                    asteroidsToRemove.add(asteroid);  // Mark asteroid for removal
                    break;
                }
            }
        }

        // Remove the bullets and asteroids after iteration to avoid ConcurrentModificationException
        bullets.removeAll(bulletsToRemove);
        asteroids.removeAll(asteroidsToRemove);
        if (boss != null) {
            boss.getBossBullets().removeAll(bossBulletsToRemove);
        }
    }


    // Spawn asteroids for the level
    public void spawnAsteroidsForLevel(int level, GraphicsContext gc) {
        int numAsteroids = level * 5;  // Increase number of asteroids with each level
        for (int i = 0; i < numAsteroids; i++) {
            double asteroidSize;
            int asteroidPoints;
            double speed = random.nextDouble() * 2 + 1;

            // Randomize asteroid size
            double sizeType = random.nextDouble();
            if (sizeType < 0.33) {
                asteroidSize = 20;
                asteroidPoints = 1;
            } else if (sizeType < 0.66) {
                asteroidSize = 40;
                asteroidPoints = 2;
            } else {
                asteroidSize = 60;
                asteroidPoints = 3;
            }

            asteroids.add(new Asteroid(random.nextInt((int) gc.getCanvas().getWidth()), random.nextInt((int) gc.getCanvas().getHeight()), speed, asteroidSize, asteroidPoints, false));
        }
    }

    // Update and draw asteroids
    public void updateAndDrawAsteroids(GraphicsContext gc) {
        Iterator<Asteroid> asteroidIterator = asteroids.iterator();
        while (asteroidIterator.hasNext()) {
            Asteroid asteroid = asteroidIterator.next();
            asteroid.move();
            if (asteroid.isOffScreen(gc.getCanvas().getWidth(), gc.getCanvas().getHeight())) {
                asteroidIterator.remove();
            } else {
                asteroid.draw(gc);
            }
        }
    }

    // Update and draw bullets
    public void updateAndDrawBullets(GraphicsContext gc) {
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.update();
            if (bullet.isOffScreen(gc.getCanvas().getWidth(), gc.getCanvas().getHeight())) {
                bulletIterator.remove();
            } else {
                bullet.draw(gc);
            }
        }
    }
    public void clearBossBullets() {
        if (boss != null) {
            boss.getBossBullets().clear();
        }
    }

    // Add a bullet to the list
    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }

    // Return the list of bullets
    public List<Bullet> getBullets() {
        return bullets;
    }

    public boolean areAsteroidsCleared() {
        return asteroids.isEmpty();
    }

    public Boss spawnBoss() {
        if (!bossActive) {
            if (boss == null) {
                boss = new Boss(400, 100, 1.5, 80);
                Logger.getLogger(Boss.class.getName()).info("Boss initialized at position: (" + boss.getX() + ", " + boss.getY() + ")");
            }
            bossActive = true;
        }
        return boss;
    }
    public void setBossActive(boolean isActive) {
        this.bossActive = isActive;

    }


    public boolean isBossActive() {
        return bossActive;
    }

    public List<Asteroid> getAsteroids() {
        return asteroids;
    }

    public void setBoss(Boss boss) {
        this.boss = boss;
    }

}
