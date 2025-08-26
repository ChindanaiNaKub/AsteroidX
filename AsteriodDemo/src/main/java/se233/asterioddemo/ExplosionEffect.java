package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.effect.BlendMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ExplosionEffect {
    // Caps to prevent excessive particle counts on large explosions
    private static final int MAX_DEBRIS = 28;
    private static final int MAX_SPARKS = 36;
    private static final int MAX_SMOKE = 24;
    // Margin for culling off-screen particles during drawing
    private static final double OFFSCREEN_MARGIN = 16.0;
    public enum ParticleType {
        DEBRIS, SPARK, SMOKE, CORE
    }

    private static class ExplosionParticle {
        double x, y;
        double velocityX, velocityY;
        double rotationAngle;
        double rotationSpeed;
        double size;
        double life;
        double maxLife;
        Color color;
        ParticleType type;
        double gravity;
        double drag;

        ExplosionParticle(double x, double y, ParticleType type, ExplosionConfig config) {
            this.x = x;
            this.y = y;
            this.type = type;

            double angle = config.random.nextDouble() * 2 * Math.PI;
            double speed = switch (type) {
                case DEBRIS -> 2 + config.random.nextDouble() * 5;
                case SPARK -> 3 + config.random.nextDouble() * 7;
                case SMOKE -> 0.5 + config.random.nextDouble() * 2;
                case CORE -> 0.2 + config.random.nextDouble();
            };

            this.velocityX = Math.cos(angle) * speed;
            this.velocityY = Math.sin(angle) * speed;

            this.rotationAngle = config.random.nextDouble() * 360;
            this.rotationSpeed = config.random.nextDouble() * 10 - 5;

            this.size = switch (type) {
                case DEBRIS -> 3 + config.random.nextDouble() * (config.baseSize / 3);
                case SPARK -> 1 + config.random.nextDouble() * (config.baseSize / 4);
                case SMOKE -> 5 + config.random.nextDouble() * (config.baseSize / 2);
                case CORE -> config.baseSize / 2;
            };

            this.maxLife = switch (type) {
                case DEBRIS -> 12 + config.random.nextDouble() * 8;
                case SPARK -> 6 + config.random.nextDouble() * 6;
                case SMOKE -> 18 + config.random.nextDouble() * 10;
                case CORE -> 4 + config.random.nextDouble() * 4;
            };

            this.life = maxLife;
            this.color = config.getColorForType(type);

            this.gravity = switch (type) {
                case DEBRIS -> 0.2;
                case SPARK -> 0.1;
                case SMOKE -> -0.05; // Smoke rises
                case CORE -> 0;
            };

            this.drag = switch (type) {
                case DEBRIS -> 0.98;
                case SPARK -> 0.95;
                case SMOKE -> 0.99;
                case CORE -> 0.9;
            };
        }

        void update() {
            velocityX *= drag;
            velocityY *= drag;
            velocityY += gravity;

            x += velocityX;
            y += velocityY;

            rotationAngle += rotationSpeed;
            life--;

            if (type == ParticleType.SMOKE) {
                size *= 1.02; // Smoke expands
            } else {
                size *= 0.97; // Other particles shrink
            }
        }

        boolean isDead() {
            return life <= 0 || size < 0.5;
        }

        void draw(GraphicsContext gc) {
            double opacity = life / maxLife;
            if (type == ParticleType.SMOKE) {
                opacity *= 0.3; // Make smoke more transparent
            }

            gc.save();
            gc.setGlobalBlendMode(type == ParticleType.SPARK ? BlendMode.ADD : BlendMode.SRC_OVER);
            gc.setGlobalAlpha(opacity);
            gc.setFill(color);

            gc.translate(x, y);
            gc.rotate(rotationAngle);

            switch (type) {
                case DEBRIS -> {
                    // Simplified debris to avoid array allocations each frame
                    gc.fillRect(-size / 2, -size / 4, size, size / 2);
                }
                case SPARK -> {
                    // Draw glowing spark
                    gc.setFill(Color.WHITE);
                    gc.fillOval(-size/4, -size/4, size/2, size/2);
                    gc.setFill(color);
                    gc.fillOval(-size/2, -size/2, size, size);
                }
                case SMOKE -> {
                    // Draw smoke cloud
                    gc.fillOval(-size/2, -size/2, size, size);
                }
                case CORE -> {
                    // Draw explosion core
                    gc.setFill(Color.WHITE);
                    gc.fillOval(-size/2, -size/2, size, size);
                }
            }

            gc.restore();
        }
    }

    public static class ExplosionConfig {
        private final Random random = new Random();
        private final double baseSize;
        private Color[] debrisColors = {Color.BROWN, Color.GRAY, Color.DARKGRAY};
        private Color[] sparkColors = {Color.ORANGE, Color.YELLOW, Color.WHITE};
        private Color[] smokeColors = {Color.DARKGRAY, Color.GRAY, Color.LIGHTGRAY};
        private Color[] coreColors = {Color.WHITE, Color.YELLOW, Color.ORANGE};

        public ExplosionConfig(double baseSize) {
            this.baseSize = baseSize;
        }

        public void setDebrisColors(Color... colors) {
            this.debrisColors = colors;
        }

        public void setSparkColors(Color... colors) {
            this.sparkColors = colors;
        }

        public void setSmokeColors(Color... colors) {
            this.smokeColors = colors;
        }

        public void setCoreColors(Color... colors) {
            this.coreColors = colors;
        }

        private Color getColorForType(ParticleType type) {
            Color[] colors = switch (type) {
                case DEBRIS -> debrisColors;
                case SPARK -> sparkColors;
                case SMOKE -> smokeColors;
                case CORE -> coreColors;
            };
            return colors[random.nextInt(colors.length)];
        }
    }

    private final List<ExplosionParticle> particles = new ArrayList<>();
    private final ExplosionConfig config;

    public ExplosionEffect(double baseSize) {
        this.config = new ExplosionConfig(baseSize);
    }

    public void createExplosion(double x, double y, double size) {
        // Core flash
        particles.add(new ExplosionParticle(x, y, ParticleType.CORE, config));

        // Debris particles (clamped)
        int debrisCount = Math.min((int) (size * 1.0), MAX_DEBRIS);
        for (int i = 0; i < debrisCount; i++) {
            particles.add(new ExplosionParticle(x, y, ParticleType.DEBRIS, config));
        }

        // Spark particles (clamped)
        int sparkCount = Math.min((int) (size * 1.2), MAX_SPARKS);
        for (int i = 0; i < sparkCount; i++) {
            particles.add(new ExplosionParticle(x, y, ParticleType.SPARK, config));
        }

        // Smoke particles (clamped)
        int smokeCount = Math.min((int) (size * 0.8), MAX_SMOKE);
        for (int i = 0; i < smokeCount; i++) {
            particles.add(new ExplosionParticle(x, y, ParticleType.SMOKE, config));
        }
    }

    public void update() {
        Iterator<ExplosionParticle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            ExplosionParticle particle = iterator.next();
            particle.update();
            if (particle.isDead()) {
                iterator.remove();
            }
        }
    }

    public void draw(GraphicsContext gc) {
        // Draw particles in layers for better visual effect using for-loops (no stream overhead)
        double canvasW = gc.getCanvas().getWidth();
        double canvasH = gc.getCanvas().getHeight();

        // First smoke
        for (ExplosionParticle p : particles) {
            if (p.type == ParticleType.SMOKE) {
                // Off-screen culling
                if (p.x + p.size < -OFFSCREEN_MARGIN || p.x - p.size > canvasW + OFFSCREEN_MARGIN ||
                        p.y + p.size < -OFFSCREEN_MARGIN || p.y - p.size > canvasH + OFFSCREEN_MARGIN) {
                    continue;
                }
                p.draw(gc);
            }
        }

        // Then debris
        for (ExplosionParticle p : particles) {
            if (p.type == ParticleType.DEBRIS) {
                if (p.x + p.size < -OFFSCREEN_MARGIN || p.x - p.size > canvasW + OFFSCREEN_MARGIN ||
                        p.y + p.size < -OFFSCREEN_MARGIN || p.y - p.size > canvasH + OFFSCREEN_MARGIN) {
                    continue;
                }
                p.draw(gc);
            }
        }

        // Finally sparks and core for the glow effect
        for (ExplosionParticle p : particles) {
            if (p.type == ParticleType.SPARK || p.type == ParticleType.CORE) {
                if (p.x + p.size < -OFFSCREEN_MARGIN || p.x - p.size > canvasW + OFFSCREEN_MARGIN ||
                        p.y + p.size < -OFFSCREEN_MARGIN || p.y - p.size > canvasH + OFFSCREEN_MARGIN) {
                    continue;
                }
                p.draw(gc);
            }
        }
    }

    public ExplosionConfig getConfig() {
        return config;
    }

    public boolean isActive() {
        return !particles.isEmpty();
    }
}