package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.AudioClip;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import static se233.asterioddemo.AsteroidGame.logger;

public class GameEntityManager {
    private List<Asteroid> asteroids;
    private List<Bullet> bullets;
    private List<EnemyShip> enemyShips;
    private List<Bullet> enemyBullets;
    private Boss boss;
    private boolean bossActive;
    private Random random;

    private long lastBulletTime = 0;
    private long lastAsteroidSpawnTime = 0;
    private long lastEnemySpawnTime = 0;
    private static final long BULLET_COOLDOWN = 300;
    private static final long ASTEROID_SPAWN_COOLDOWN = 1000;
    private static final long ENEMY_SPAWN_COOLDOWN = 5000;
    private static final int ASTEROIDS_PER_SPAWN = 3;

    private static final double SMALL_ASTEROID_SIZE = 20;
    private static final double MEDIUM_ASTEROID_SIZE = 40;
    private static final double LARGE_ASTEROID_SIZE = 60;

    private final SpriteLoader spriteLoader;

    public GameEntityManager(SpriteLoader spriteLoader) {
        this.asteroids = new ArrayList<>();
        this.bullets = new ArrayList<>();
        this.enemyShips = new ArrayList<>();
        this.enemyBullets = new ArrayList<>();
        this.random = new Random();
        this.bossActive = false;
        this.spriteLoader = spriteLoader; // Pass the SpriteLoader instance here
    }

    public void continuousSpawnAsteroids(GraphicsContext gc) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAsteroidSpawnTime >= ASTEROID_SPAWN_COOLDOWN) {
            for (int i = 0; i < ASTEROIDS_PER_SPAWN; i++) {
                spawnSingleAsteroid(gc);
            }
            lastAsteroidSpawnTime = currentTime;
        }
    }

    private void spawnSingleAsteroid(GraphicsContext gc) {
        double speed = 1.0 + random.nextDouble() * 2.0; // Speed between 1.0 and 3.0
        AsteroidSize size = getRandomAsteroidSize();

        Asteroid asteroid = new Asteroid(
                random.nextInt((int) gc.getCanvas().getWidth()),
                random.nextInt((int) gc.getCanvas().getHeight()),
                speed,
                size.size,
                size.points,
                false,  // Not a split asteroid
                spriteLoader // Pass the spriteLoader here
        );

        asteroids.add(asteroid);
    }

    private static class AsteroidSize {
        final double size;
        final int points;

        AsteroidSize(double size, int points) {
            this.size = size;
            this.points = points;
        }
    }

    private AsteroidSize getRandomAsteroidSize() {
        double roll = random.nextDouble();
        if (roll < 0.33) {
            return new AsteroidSize(SMALL_ASTEROID_SIZE, 3);
        } else if (roll < 0.66) {
            return new AsteroidSize(MEDIUM_ASTEROID_SIZE, 2);
        } else {
            return new AsteroidSize(LARGE_ASTEROID_SIZE, 1);
        }
    }

    public void continuousSpawnEnemyShips() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastEnemySpawnTime >= ENEMY_SPAWN_COOLDOWN) {
            spawnEnemyShip();
            lastEnemySpawnTime = currentTime;
        }
    }

    private void spawnEnemyShip() {
        double x = random.nextInt(800);  // Screen width assumption
        double y = random.nextInt(600);  // Screen height assumption
        double speed = 1.0 + random.nextDouble() * 2.0;
        double size = 30;  // Define the enemy size
        double angle = Math.PI / 2;  // Define or randomize the angle

        enemyShips.add(new EnemyShip(x, y, speed, size, angle, spriteLoader));
    }

    public void updateAndDrawBullets(GraphicsContext gc, double screenWidth, double screenHeight) {
        Iterator<Bullet> bulletIter = bullets.iterator();
        while (bulletIter.hasNext()) {
            Bullet bullet = bulletIter.next();
            bullet.update(screenWidth, screenHeight);
            if (bullet.isOffScreen(screenWidth, screenHeight)) {
                bulletIter.remove();
            } else {
                bullet.draw(gc);
            }
        }
    }

    public void updateAndDrawAsteroids(GraphicsContext gc) {
        Iterator<Asteroid> asteroidIter = asteroids.iterator();
        while (asteroidIter.hasNext()) {
            Asteroid asteroid = asteroidIter.next();
            asteroid.move();
            asteroid.handleScreenEdges(gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
            asteroid.draw(gc);
        }
    }

    public void updateAndDrawEnemyBullets(GraphicsContext gc, double screenWidth, double screenHeight) {
        Iterator<Bullet> bulletIter = enemyBullets.iterator();
        while (bulletIter.hasNext()) {
            Bullet bullet = bulletIter.next();
            bullet.update(screenWidth, screenHeight);
            if (bullet.isOffScreen(screenWidth, screenHeight)) {
                bulletIter.remove();
            } else {
                bullet.draw(gc);
            }
        }
    }

    public void updateAndDrawEnemyShips(GraphicsContext gc, double playerX, double playerY) {
        Iterator<EnemyShip> enemyIter = enemyShips.iterator();
        while (enemyIter.hasNext()) {
            EnemyShip enemy = enemyIter.next();
            enemy.move(playerX, playerY);

            if (enemy.canShoot()) {
                Bullet bullet = enemy.shootTowards(playerX, playerY);
                enemyBullets.add(bullet);
            }

            if (enemy.getHealth() <= 0) {
                enemyIter.remove();
            } else {
                enemy.draw(gc);
            }
        }
    }

    public void checkCollisions(GameState gameState, PlayerShip playerShip, AudioClip hitSound, AudioClip explodeSound, Logger logger) {
        checkPlayerEnemyBulletCollisions(playerShip, gameState);
        checkPlayerEnemyShipCollisions(playerShip, gameState, hitSound, explodeSound);
        checkPlayerBulletEnemyCollisions(gameState);
        checkPlayerBulletAsteroidCollisions(gameState, logger);
        checkPlayerAsteroidCollisions(playerShip, gameState, hitSound, explodeSound);

        if (boss != null) {
            checkBossCollisions(playerShip, gameState, logger);
        }
    }

    private void checkPlayerEnemyBulletCollisions(PlayerShip playerShip, GameState gameState) {
        Iterator<Bullet> bulletIter = enemyBullets.iterator();
        while (bulletIter.hasNext()) {
            Bullet bullet = bulletIter.next();
            if (isColliding(bullet, playerShip)) {
                playerShip.reduceHealth(5);
                bulletIter.remove();

                if (playerShip.getHealth() <= 0) {
                    gameState.setGameOver(true);
                }
            }
        }
    }

    private void checkPlayerEnemyShipCollisions(PlayerShip playerShip, GameState gameState, AudioClip hitSound, AudioClip explodeSound) {
        Iterator<EnemyShip> enemyIter = enemyShips.iterator();
        while (enemyIter.hasNext()) {
            EnemyShip enemy = enemyIter.next();
            if (isColliding(playerShip, enemy)) {
                playerShip.reduceHealth(10); // Reduce player health on collision
                enemyIter.remove();  // Remove enemy ship after collision
                hitSound.play();  // Play hit sound
                logger.info("Player hit by EnemyShip! Remaining Health: " + playerShip.getHealth());

                if (playerShip.getHealth() <= 0) {
                    gameState.setGameOver(true);  // Set game over if health is 0\
                    logger.warning("Player killed by EnemyShip. Game Over!");
                    explodeSound.play();  // Play explosion sound
                }
            }
        }
    }


    private void checkPlayerBulletEnemyCollisions(GameState gameState) {
        List<Bullet> bulletsToRemove = new ArrayList<>();
        List<EnemyShip> enemiesToRemove = new ArrayList<>();

        for (Bullet bullet : bullets) {
            for (EnemyShip enemy : enemyShips) {
                if (isColliding(bullet, enemy)) {
                    enemy.takeDamage(10);  // Reduce enemy health
                    bulletsToRemove.add(bullet);  // Mark bullet for removal

                    if (enemy.getHealth() <= 0) {
                        enemiesToRemove.add(enemy);  // Mark enemy for removal
                        gameState.addScore(50);  // Add points for destroying an enemy
                    }
                }
            }
        }

        bullets.removeAll(bulletsToRemove);
        enemyShips.removeAll(enemiesToRemove);
    }


    private void checkPlayerBulletAsteroidCollisions(GameState gameState, Logger logger) {
        List<Bullet> bulletsToRemove = new ArrayList<>();
        List<Asteroid> asteroidsToRemove = new ArrayList<>();

        for (Bullet bullet : bullets) {
            for (Asteroid asteroid : asteroids) {
                if (isColliding(bullet, asteroid)) {
                    gameState.addScore(asteroid.getPoints());  // Add points
                    logger.info("Asteroid destroyed! Score: " + gameState.getScore());

                    List<Asteroid> newAsteroids = asteroid.split();  // Split asteroid
                    asteroids.addAll(newAsteroids);  // Add smaller asteroids

                    bulletsToRemove.add(bullet);  // Mark bullet for removal
                    asteroidsToRemove.add(asteroid);  // Mark asteroid for removal
                    break;
                }
            }
        }

        bullets.removeAll(bulletsToRemove);
        asteroids.removeAll(asteroidsToRemove);
    }


    private void checkPlayerAsteroidCollisions(PlayerShip playerShip, GameState gameState, AudioClip hitSound, AudioClip explodeSound) {
        Iterator<Asteroid> asteroidIter = asteroids.iterator();
        while (asteroidIter.hasNext()) {
            Asteroid asteroid = asteroidIter.next();
            if (isColliding(playerShip, asteroid)) {
                gameState.loseLife();  // Reduce player lives
                logger.warning("Player hit by Asteroid! Lives remaining: " + gameState.getLives());
                asteroidIter.remove();  // Remove asteroid after collision
                hitSound.play();  // Play hit sound

                if (playerShip.getHealth() <= 0) {
                    gameState.setGameOver(true);// Set game over if player health is 0
                    logger.warning("Player killed by Asteroid. Game Over!");
                    explodeSound.play();  // Play explosion sound
                }
                break;
            }
        }
    }


    private void checkBossCollisions(PlayerShip playerShip, GameState gameState, Logger logger) {
        if (boss != null) {
            // Check for boss bullets hitting player
            List<Bullet> bossBulletsToRemove = new ArrayList<>();
            for (Bullet bossBullet : boss.getBossBullets()) {
                if (isColliding(bossBullet, playerShip)) {
                    playerShip.reduceHealth(20);  // Reduce player health on hit
                    bossBulletsToRemove.add(bossBullet);  // Remove bullet
                    logger.info("Player hit by boss bullet! Player health: " + playerShip.getHealth());

                    if (playerShip.getHealth() <= 0) {
                        gameState.setGameOver(true);  // Set game over if player health is 0
                    }
                }
            }
            boss.getBossBullets().removeAll(bossBulletsToRemove);

            // Check for player bullets hitting the boss
            List<Bullet> bulletsToRemove = new ArrayList<>();
            for (Bullet bullet : bullets) {
                if (isColliding(bullet, boss)) {
                    boss.takeDamage();  // Reduce boss health
                    bulletsToRemove.add(bullet);  // Remove bullet
                    logger.info("Boss hit! Boss health: " + boss.getHealth());

                    if (boss.getHealth() <= 0) {
                        boss = null;  // Remove boss
                        gameState.addScore(100);  // Add points for defeating boss
                        logger.info("Boss defeated!");
                    }
                }
            }
            bullets.removeAll(bulletsToRemove);
        }
    }

    // Method for Bullet vs Boss collision
    private boolean isColliding(Bullet bullet, Boss boss) {
        double distance = Math.hypot(bullet.getX() - boss.getX(), bullet.getY() - boss.getY());
        return distance < (bullet.getSize() / 2 + boss.getSize() / 2);
    }

    // Generic method for collision detection between two characters (PlayerShip, EnemyShip, Asteroid, etc.)
    private boolean isColliding(Character entityA, Character entityB) {
        double distance = Math.hypot(entityA.getX() - entityB.getX(), entityA.getY() - entityB.getY());
        return distance < (entityA.getSize() / 2 + entityB.getSize() / 2);
    }

    // Overloaded method for Bullet vs PlayerShip collision
    private boolean isColliding(Bullet bullet, PlayerShip player) {
        double distance = Math.hypot(bullet.getX() - player.getX(), bullet.getY() - player.getY());
        return distance < (bullet.getRadius() + player.getSize() / 2);  // Adjust based on Bullet's collision size
    }

    // Overloaded method for Bullet vs EnemyShip collision
    private boolean isColliding(Bullet bullet, EnemyShip enemy) {
        double distance = Math.hypot(bullet.getX() - enemy.getX(), bullet.getY() - enemy.getY());
        return distance < (bullet.getRadius() + enemy.getSize() / 2);
    }

    // Overloaded method for Bullet vs Asteroid collision
    private boolean isColliding(Bullet bullet, Asteroid asteroid) {
        double distance = Math.hypot(bullet.getX() - asteroid.getX(), bullet.getY() - asteroid.getY());
        return distance < (bullet.getRadius() + asteroid.getSize() / 2);
    }

    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }

    public void clearAsteroids() {
        asteroids.clear();
    }

    public void clearBullets() {
        bullets.clear();
        enemyBullets.clear();
    }

    public void clearEnemyShips() {
        enemyShips.clear();
    }

    public void clearAll() {
        asteroids.clear();
        bullets.clear();
        enemyShips.clear();
        enemyBullets.clear();
        if (boss != null) {
            boss.getBossBullets().clear();
        }
        boss = null;
        bossActive = false;
    }
}
