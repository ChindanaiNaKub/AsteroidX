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
    public List<Asteroid> asteroids;
    private List<ExplosionEffect> explosions;
    public List<Bullet> bullets;
    public List<EnemyShip> enemyShips;
    private List<Bullet> enemyBullets;
    public Boss boss;
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

    private List<ShipExplosionEffect> shipExplosions = new ArrayList<>();


    public GameEntityManager(SpriteLoader spriteLoader) {
        this.asteroids = new ArrayList<>();
        this.explosions = new ArrayList<>();
        this.bullets = new ArrayList<>();
        this.enemyShips = new ArrayList<>();
        this.enemyBullets = new ArrayList<>();
        this.random = new Random();
        this.bossActive = false;
        this.spriteLoader = spriteLoader;
    }

    public void updateAndDrawEnemyShipExplosions(GraphicsContext gc) {
        Iterator<ShipExplosionEffect> iterator = shipExplosions.iterator();
        while (iterator.hasNext()) {
            ShipExplosionEffect explosion = iterator.next();
            explosion.update();
            explosion.draw(gc);
            if (!explosion.isActive()) {
                iterator.remove();
            }
        }
    }

    public void updateAndDrawExplosions(GraphicsContext gc) {
        Iterator<ExplosionEffect> explosionIter = explosions.iterator();
        while (explosionIter.hasNext()) {
            ExplosionEffect explosion = explosionIter.next();
            explosion.update();
            explosion.draw(gc);

            // Remove the explosion if it is no longer active
            if (!explosion.isActive()) {
                explosionIter.remove();
            }
        }
    }


    public void startBossStage(AudioClip bossMusic) {
        if (!bossActive) {
            logger.info("Attempting to start boss stage...");
            bossActive = true;
            boss = new Boss(400, 100, 2.0, 91, spriteLoader);
            clearAll();  // Clear other entities but don't deactivate the boss
            if (bossMusic != null) {
                bossMusic.play();
            }
            logger.info("Boss created successfully");
        } else {
            logger.info("Boss is already active, skipping creation.");
        }
    }

    public void continuousSpawnAsteroids(GraphicsContext gc) {
        if (!bossActive) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAsteroidSpawnTime >= ASTEROID_SPAWN_COOLDOWN) {
                for (int i = 0; i < ASTEROIDS_PER_SPAWN; i++) {
                    spawnSingleAsteroid(gc);
                }
                lastAsteroidSpawnTime = currentTime;
            }
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
                spriteLoader
        );

        asteroids.add(asteroid);
    }

    public boolean isBossActive() {
        return bossActive;
    }

    public Boss getBoss() {
        return boss;
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
        if (!bossActive) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastEnemySpawnTime >= ENEMY_SPAWN_COOLDOWN) {
                spawnEnemyShip();
                lastEnemySpawnTime = currentTime;
            }
        }
    }

    private void spawnEnemyShip() {
        double x = random.nextInt(800);  // Screen width assumption
        double y = random.nextInt(600);  // Screen height assumption
        double speed = 1.0 + random.nextDouble() * 2.0;
        double size = 75;  // Define the enemy size
        double angle = Math.PI / 2;  // Define or randomize the angle

        enemyShips.add(new EnemyShip(x, y, speed, size, angle, spriteLoader));
    }

    public void updateAndDrawBoss(GraphicsContext gc, PlayerShip playerShip, GameState gameState, AudioClip hitSound, Logger logger) {
        if (bossActive && boss != null) {
            boss.move();
            boss.attack(spriteLoader);
            boss.draw(gc);

            // Update boss bullets
            Iterator<Bullet> bulletIterator = boss.getBossBullets().iterator();
            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                bullet.move();

                if (bullet.isOffScreen(gc.getCanvas().getWidth(), gc.getCanvas().getHeight())) {
                    bulletIterator.remove();
                } else {
                    bullet.draw(gc);
                }
            }
        } else {
            logger.severe("Boss object is null in updateAndDrawBoss!");
        }
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
        if (!bossActive) {
            checkPlayerEnemyBulletCollisions(playerShip, gameState);
            checkPlayerEnemyShipCollisions(playerShip, gameState, hitSound, explodeSound);
            checkPlayerBulletEnemyCollisions(gameState);
            checkPlayerBulletAsteroidCollisions(gameState, logger);
            checkPlayerAsteroidCollisions(playerShip, gameState, hitSound, explodeSound);
        }

        if (bossActive && boss != null) {
            checkBossCollisions(playerShip, gameState, logger, hitSound);
        }
    }

    private void checkPlayerBulletBossCollisions(PlayerShip playerShip, GameState gameState, Logger logger, AudioClip hitSound) {
        if (boss != null && bossActive) {
            List<Bullet> bulletsToRemove = new ArrayList<>();
            for (Bullet bullet : playerShip.getBullets()) {
                if (isColliding(bullet, boss)) {
                    boss.takeDamage();
                    bulletsToRemove.add(bullet);
                    hitSound.play();
                    logger.info("Boss hit! Boss health: " + boss.getHealth());

                    if (boss.getHealth() <= 0) {
                        defeatBoss(gameState, logger);
                    }
                }
            }
            playerShip.getBullets().removeAll(bulletsToRemove);
        }
    }

    public void defeatBoss(GameState gameState, Logger logger) {
        bossActive = false;
        boss = null;
        gameState.addScore(5); // Example score for defeating the boss
        logger.info("Boss defeated! Bonus score added.");
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
                playerShip.reduceHealth(10);
                enemyIter.remove();
                hitSound.play();
                logger.info("Player hit by EnemyShip! Remaining Health: " + playerShip.getHealth());

                if (playerShip.getHealth() <= 0) {
                    gameState.setGameOver(true);
                    logger.warning("Player killed by EnemyShip. Game Over!");
                    explodeSound.play();
                }
            }
        }
    }

    public void checkPlayerBulletEnemyCollisions(GameState gameState) {
        // Use Iterator approach for better performance in game loop
        Iterator<Bullet> bulletIterator = bullets.iterator();

        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            Iterator<EnemyShip> enemyIterator = enemyShips.iterator();
            boolean bulletHit = false;

            while (enemyIterator.hasNext() && !bulletHit) {
                EnemyShip enemy = enemyIterator.next();

                if (isColliding(bullet, enemy)) {
                    // Deal damage to enemy
                    enemy.takeDamage(10);

                    // Remove the bullet that hit
                    bulletIterator.remove();
                    bulletHit = true;  // Mark that this bullet hit something

                    // Check if enemy is destroyed
                    if (enemy.getHealth() <= 0) {
                        // Remove the enemy
                        enemyIterator.remove();

                        // Handle scoring and game state
                        defeatEnemyShip(enemy, gameState, logger);

                        // Create explosion effect
                        ShipExplosionEffect explosion = new ShipExplosionEffect(20);
                        explosion.createExplosion(
                                enemy.getX(),
                                enemy.getY(),
                                enemy.getSize(),
                                "standard"
                        );
                        shipExplosions.add(explosion);
                    }
                }
            }
        }
    }

    public void checkPlayerBulletAsteroidCollisions(GameState gameState, Logger logger) {
        // Lists to track what needs to be removed and added
        List<Bullet> bulletsToRemove = new ArrayList<>();
        List<Asteroid> asteroidsToRemove = new ArrayList<>();
        List<Asteroid> newAsteroidsToAdd = new ArrayList<>();
        List<ExplosionEffect> newExplosions = new ArrayList<>();

        // First pass: Check collisions and collect changes
        for (Bullet bullet : bullets) {
            for (Asteroid asteroid : asteroids) {
                if (!bulletsToRemove.contains(bullet) && !asteroidsToRemove.contains(asteroid) && isColliding(bullet, asteroid)) {
                    // Mark entities for removal
                    bulletsToRemove.add(bullet);
                    asteroidsToRemove.add(asteroid);

                    // Update score
                    gameState.addScore(asteroid.getPoints());
                    logger.info("Asteroid destroyed! Score: " + gameState.getScore());

                    // Create explosion effect
                    ExplosionEffect explosion = new ExplosionEffect(asteroid.getSize());
                    explosion.createExplosion(asteroid.getX(), asteroid.getY(), asteroid.getSize());
                    newExplosions.add(explosion);

                    // Get new asteroids from splitting
                    List<Asteroid> splitAsteroids = asteroid.split();
                    if (!splitAsteroids.isEmpty()) {
                        newAsteroidsToAdd.addAll(splitAsteroids);
                    }

                    break; // Move to next bullet since this one hit something
                }
            }
        }

        // Second pass: Safely apply all changes
        synchronized (this) {
            bullets.removeAll(bulletsToRemove);
            asteroids.removeAll(asteroidsToRemove);
            asteroids.addAll(newAsteroidsToAdd);
            explosions.addAll(newExplosions);
        }
    }

    private void checkPlayerAsteroidCollisions(PlayerShip playerShip, GameState gameState, AudioClip hitSound, AudioClip explodeSound) {
        Iterator<Asteroid> asteroidIter = asteroids.iterator();
        while (asteroidIter.hasNext()) {
            Asteroid asteroid = asteroidIter.next();
            if (isColliding(playerShip, asteroid)) {
                gameState.loseLife();
                logger.warning("Player hit by Asteroid! Lives remaining: " + gameState.getLives());
                asteroidIter.remove();
                hitSound.play();

                // Automatically activate shield when a life is lost
                playerShip.reset(playerShip.getX(), playerShip.getY(), playerShip.getSpeed());
                playerShip.activateShield();

                if (playerShip.getHealth() <= 0) {
                    gameState.setGameOver(true);
                    logger.warning("Player killed by Asteroid. Game Over!");
                    explodeSound.play();
                }
                break;
            }
        }
    }

    private void checkBossCollisions(PlayerShip playerShip, GameState gameState, Logger logger, AudioClip hitSound) {
        if (boss != null) {
            // Check for boss bullets hitting player
            List<Bullet> bossBulletsToRemove = new ArrayList<>();
            for (Bullet bossBullet : boss.getBossBullets()) {
                if (isColliding(bossBullet, playerShip)) {
                    playerShip.reduceHealth(20);  // Reduce player health on hit
                    bossBulletsToRemove.add(bossBullet);
                    hitSound.play();
                    logger.warning("Player hit by boss bullet! Player health: " + playerShip.getHealth());

                    if (playerShip.getHealth() <= 0) {
                        // If the player's health drops to 0, they lose a life
                        gameState.loseLife();
                        logger.warning("Player lost a life! Lives remaining: " + gameState.getLives());

                        if (!gameState.isGameOver()) {
                            // If the game is not over, respawn the player with full health
                            playerShip.resetHealth();
                            playerShip.reset(640, 360, playerShip.getSpeed()); // Respawn at a default position
                            playerShip.activateShield(); // Optionally activate a temporary shield
                            logger.warning("Player respawned with " + gameState.getLives() + " lives.");
                        } else {
                            logger.warning("Player has no remaining lives. Game Over!");
                        }
                    }
                }
            }
            boss.getBossBullets().removeAll(bossBulletsToRemove);

            // Check for player bullets hitting the boss
            List<Bullet> bulletsToRemove = new ArrayList<>();
            for (Bullet bullet : playerShip.getBullets()) {
                if (isColliding(bullet, boss)) {
                    boss.takeDamage();
                    bulletsToRemove.add(bullet);
                    logger.info("Boss hit! Boss health: " + boss.getHealth());

                    if (boss.getHealth() <= 0) {
                        defeatBoss(gameState, logger);  // Call defeat method when boss health reaches zero
                    }
                }
            }
            playerShip.getBullets().removeAll(bulletsToRemove);
        } else {
            logger.warning("checkBossCollisions called but boss is null.");
        }
    }

    // Method for Bullet vs Boss collision
    private boolean isColliding(Bullet bullet, Boss boss) {
        if (bullet == null || boss == null) {
            return false;
        } // Ensure boss is not null before checking collision
        double distance = Math.hypot(bullet.getX() - boss.getX(), bullet.getY() - boss.getY());
        return distance < (bullet.getSize() / 2 + boss.getSize() / 2);
    }

    private boolean isColliding(Character entityA, Character entityB) {
        if (entityA == null || entityB == null) {
            return false;
        }
        double distance = Math.hypot(entityA.getX() - entityB.getX(), entityA.getY() - entityB.getY());
        return distance < (entityA.getSize() / 2 + entityB.getSize() / 2);
    }

    private boolean isColliding(Bullet bullet, PlayerShip player) {
        if (bullet == null || player == null) {
            return false;
        }
        double distance = Math.hypot(bullet.getX() - player.getX(), bullet.getY() - player.getY());
        return distance < (bullet.getRadius() + player.getSize() / 2);
    }

    private boolean isColliding(Bullet bullet, EnemyShip enemy) {
        if (bullet == null || enemy == null) {
            return false;
        }
        double distance = Math.hypot(bullet.getX() - enemy.getX(), bullet.getY() - enemy.getY());
        return distance < (bullet.getRadius() + enemy.getSize() / 2);
    }

    private boolean isColliding(Bullet bullet, Asteroid asteroid) {
        if (bullet == null || asteroid == null) {
            return false;
        }
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
        PlayerShip.getBullets().clear();
        if (boss != null) {
            boss.getBossBullets().clear();
        }
    }

    public void setBossActive(boolean active) {
        this.bossActive = active;
        if (!active) {
            this.boss = null; // Ensure that the boss object is set to null when deactivating
        }
    }


//    public void defeatEnemyShip(EnemyShip enemy, GameState gameState, Logger logger) {
//        // Remove the enemy from the game
//        enemyShips.remove(enemy);
//
//        // Add score for defeating the enemy
//        gameState.addScore(2); // Example score for defeating an enemy ship.
//
//        // Log the defeat of the enemy
//        logger.info("Enemy ship defeated! Score increased by 2.");
//
//        // Create an explosion effect at the enemy's location
//        ShipExplosionEffect explosion = new ShipExplosionEffect(20);
//        explosion.createExplosion(enemy.getX(), enemy.getY(), enemy.getSize(), "standard");
//        shipExplosions.add(explosion);
//    }


    public List<Asteroid> getAsteroids() {
        return asteroids;
    }

    public List<EnemyShip> getEnemyShips() {
        return enemyShips;
    }


    public void defeatEnemyShip(EnemyShip enemy, GameState gameState, Logger logger) {
        // Remove the enemy from the game
        enemyShips.remove(enemy);

        // Add score for defeating the enemy
        gameState.addScore(2); // Example score for defeating an enemy ship.

        // Log the defeat of the enemy
        logger.info("Enemy ship defeated! "+ gameState.getScore());

        // Create an explosion effect at the enemy's location
        ShipExplosionEffect explosion = new ShipExplosionEffect(20);
        explosion.createExplosion(enemy.getX(), enemy.getY(), enemy.getSize(), "standard");
        shipExplosions.add(explosion);
    }

}
