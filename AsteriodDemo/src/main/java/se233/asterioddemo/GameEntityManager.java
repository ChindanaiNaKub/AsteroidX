package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.AudioClip;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class GameEntityManager {
    private List<Asteroid> asteroids;
    private List<Bullet> bullets;  // List to store bullets
    private List<EnemyShip> enemyShips;  // Enemy ships list
    private List<Bullet> enemyBullets = new ArrayList<>(); // Enemy bullets
    private Boss boss;
    private boolean bossActive;
    private Random random;
    private long lastBulletTime = 0;
    private final long bulletCooldown = 300;  // Time between bullets in milliseconds

    public GameEntityManager() {
        this.asteroids = new ArrayList<>();
        this.bullets = new ArrayList<>();  // Initialize bullets list
        this.enemyShips = new ArrayList<>();  // Initialize enemy ship list
        this.random = new Random();
        this.bossActive = false;
    }
    // Clear enemy bullets
    public void clearEnemyBullets() {
        enemyBullets.clear();
    }

    // Method to clear enemy ships
    public void clearEnemyShips() {
        enemyShips.clear();
    }

    public void clearAsteroids() {
        asteroids.clear();
    }

    public void spawnEnemiesForLevel(int level, double screenWidth, double screenHeight) {
        int numEnemies = level;  // More enemies each level
        for (int i = 0; i < numEnemies; i++) {
            // Spawn enemies randomly
            double x = random.nextDouble() * screenWidth;
            double y = random.nextDouble() * screenHeight;
            double speed = 1 + random.nextDouble() * 2;  // Vary speed
            enemyShips.add(new EnemyShip(x, y, speed, 30, Math.toRadians(random.nextInt(360))));
        }
    }

    public void spawnEnemyShipsForLevel(int level) {
        int numEnemies = Math.min(level, 5);  // Max 5 enemy ships
        for (int i = 0; i < numEnemies; i++) {
            enemyShips.add(new EnemyShip(random.nextInt(800), random.nextInt(600), 2, 30, Math.PI / 2));
        }
    }

    public void updateAndDrawEnemyBullets(GraphicsContext gc, double screenWidth, double screenHeight) {
        Iterator<Bullet> bulletIterator = enemyBullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.update(screenWidth, screenHeight);  // Update bullet with screen wrapping
            if (bullet.isOffScreen(screenWidth, screenHeight)) {
                bulletIterator.remove();  // Remove if offscreen
            } else {
                bullet.draw(gc);  // Draw bullet
            }
        }
    }

    public void updateAndDrawEnemyShips(GraphicsContext gc, double playerX, double playerY) {
        Iterator<EnemyShip> enemyIterator = enemyShips.iterator();
        while (enemyIterator.hasNext()) {
            EnemyShip enemy = enemyIterator.next();
            enemy.move(playerX, playerY);  // Move toward the player

            // Logic to shoot a bullet towards the player at intervals
            if (enemy.canShoot()) {
                Bullet enemyBullet = enemy.shootTowards(playerX, playerY);
                addEnemyBullet(enemyBullet);  // Add the bullet to the enemy bullets list
            }

            if (enemy.getHealth() <= 0) {
                enemyIterator.remove();  // Remove enemy if destroyed
            } else {
                enemy.draw(gc);
            }
        }
    }



    public boolean areEnemiesCleared() {
        return enemyShips.isEmpty();
    }


    public void checkCollisions(GameState gameState, PlayerShip playerShip, AudioClip hitSound, AudioClip explodeSound, Logger logger) {
        List<Bullet> bulletsToRemove = new ArrayList<>();
        List<Asteroid> asteroidsToRemove = new ArrayList<>();
        List<Bullet> bossBulletsToRemove = new ArrayList<>();
        List<EnemyShip> enemiesToRemove = new ArrayList<>();
        List<Bullet> enemyBulletsToRemove = new ArrayList<>(); // For enemy bullets

        // Check for collisions between enemy bullets and the player ship
        for (Bullet bullet : enemyBullets) {
            if (Math.hypot(bullet.getX() - playerShip.getX(), bullet.getY() - playerShip.getY()) < playerShip.getSize() / 2) {
                playerShip.reduceHealth(5);  // Reduce health per bullet hit
                enemyBulletsToRemove.add(bullet);

                if (playerShip.getHealth() <= 0) {
                    gameState.setGameOver(true);
                }
            }
        }


        // Check for collisions between player bullets and enemy ships
        for (EnemyShip enemy : enemyShips) {
            for (Bullet bullet : bullets) {
                if (Math.hypot(bullet.getX() - enemy.getX(), bullet.getY() - enemy.getY()) < enemy.getSize() / 2) {
                    // Bullet hit an enemy ship
                    enemy.takeDamage(10);  // Adjust damage value as needed
                    bulletsToRemove.add(bullet); // Remove the bullet

                    if (enemy.getHealth() <= 0) {
                        enemiesToRemove.add(enemy);  // Mark the enemy for removal if destroyed
                        gameState.addScore(50);  // Add points for destroying an enemy
                    }
                }
            }
        }

        // Check for collisions between playerShip and enemy ships
        for (EnemyShip enemy : enemyShips) {
            double distanceToEnemy = Math.hypot(playerShip.getX() - enemy.getX(), playerShip.getY() - enemy.getY());
            if (distanceToEnemy < (enemy.getSize() / 2 + 15)) {  // Approximate radius of the player ship is 15
                // Collision detected with an enemy ship
                // gameState.loseLife();  // Lose a life
                enemiesToRemove.add(enemy);  // Remove the enemy after collision
                hitSound.play();  // Play hit sound

                if (gameState.isGameOver()) {
                    explodeSound.play();  // Play explosion sound when game is over
                }
                break;
            }
        }

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

        bullets.removeAll(bulletsToRemove);
        enemyBullets.removeAll(enemyBulletsToRemove);
        enemyShips.removeAll(enemiesToRemove);
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
    public void updateAndDrawBullets(GraphicsContext gc, double screenWidth, double screenHeight) {
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.update(screenWidth, screenHeight);  // Pass the screen dimensions to handle wrapping
            bullet.draw(gc);
        }
    }

    public void addEnemyBullet(Bullet bullet) {
        enemyBullets.add(bullet);
    }

    public List<EnemyShip> getEnemyShips() {
        return enemyShips;
    }

    public List<Bullet> getEnemyBullets() {
        return enemyBullets;
    }

    public void clearBullets() {
        bullets.clear();  // Clear all bullets when the game is restarted
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
