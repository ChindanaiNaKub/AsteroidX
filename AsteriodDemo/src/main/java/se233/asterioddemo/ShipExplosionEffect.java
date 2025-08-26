package se233.asterioddemo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.effect.BlendMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ShipExplosionEffect {
    // Caps to prevent excessive particle counts on large explosions
    private static final int MAX_SHIP_DEBRIS = 28;
    private static final int MAX_ENERGY_SPARKS = 36;
    private static final int MAX_PLASMA_CLOUDS = 24;
    private static final int MAX_ELECTRIC_ARCS = 12;
    // Margin for culling off-screen particles during drawing
    private static final double OFFSCREEN_MARGIN = 16.0;
    public enum ParticleType {
        SHIP_DEBRIS, ENERGY_SPARK, PLASMA_CLOUD, CORE_BURST, ELECTRIC_ARC
    }

    private static class ShipExplosionParticle {
        double x, y;
        double velocityX, velocityY;
        double rotationAngle;
        double rotationSpeed;
        double size;
        double life;
        double maxLife;
        double opacity = life / maxLife;
        Color primaryColor;
        Color secondaryColor;
        ParticleType type;
        double flickerRate;
        List<Point> arcPoints; // For electric arcs

        private static class Point {
            double x, y;
            Point(double x, double y) {
                this.x = x;
                this.y = y;
            }
        }

        ShipExplosionParticle(double x, double y, ParticleType type, ShipExplosionConfig config) {
            this.x = x;
            this.y = y;
            this.type = type;

            double angle = config.random.nextDouble() * 2 * Math.PI;
            double speed = switch (type) {
                case SHIP_DEBRIS -> 2 + config.random.nextDouble() * 6;
                case ENERGY_SPARK -> 4 + config.random.nextDouble() * 8;
                case PLASMA_CLOUD -> 1 + config.random.nextDouble() * 3;
                case CORE_BURST -> 0.5 + config.random.nextDouble() * 2;
                case ELECTRIC_ARC -> 0.2 + config.random.nextDouble();
            };

            this.velocityX = Math.cos(angle) * speed;
            this.velocityY = Math.sin(angle) * speed;

            this.rotationAngle = config.random.nextDouble() * 360;
            this.rotationSpeed = config.random.nextDouble() * 15 - 7.5;

            this.size = switch (type) {
                case SHIP_DEBRIS -> 4 + config.random.nextDouble() * (config.baseSize / 2.5);
                case ENERGY_SPARK -> 2 + config.random.nextDouble() * (config.baseSize / 3);
                case PLASMA_CLOUD -> 6 + config.random.nextDouble() * (config.baseSize / 2);
                case CORE_BURST -> config.baseSize / 1.5;
                case ELECTRIC_ARC -> 3 + config.random.nextDouble() * (config.baseSize / 4);
            };

            this.maxLife = switch (type) {
                // Shorter lifetimes to reduce total per-frame workload
                case SHIP_DEBRIS -> 12 + config.random.nextDouble() * 8;
                case ENERGY_SPARK -> 6 + config.random.nextDouble() * 6;
                case PLASMA_CLOUD -> 18 + config.random.nextDouble() * 10;
                case CORE_BURST -> 6 + config.random.nextDouble() * 6;
                case ELECTRIC_ARC -> 6 + config.random.nextDouble() * 4;
            };

            this.life = maxLife;
            this.flickerRate = config.random.nextDouble() * 0.3 + 0.7;

            // Set colors based on type
            Color[] colors = config.getColorsForType(type);
            this.primaryColor = colors[0];
            this.secondaryColor = colors.length > 1 ? colors[1] : colors[0];

            // Initialize arc points for ELECTRIC_ARC type
            if (type == ParticleType.ELECTRIC_ARC) {
                initializeArcPoints(config);
            }
        }

        private void initializeArcPoints(ShipExplosionConfig config) {
            arcPoints = new ArrayList<>();
            int segments = 3 + config.random.nextInt(3); // fewer segments for performance
            double segmentLength = size / segments;
            double currentX = 0;
            double currentY = 0;
            arcPoints.add(new Point(currentX, currentY));

            for (int i = 1; i < segments; i++) {
                currentX += segmentLength;
                currentY += (config.random.nextDouble() - 0.5) * segmentLength;
                arcPoints.add(new Point(currentX, currentY));
            }
        }

        void update() {
            velocityX *= 0.98;
            velocityY *= 0.98;

            x += velocityX;
            y += velocityY;

            rotationAngle += rotationSpeed;
            life--;

            if (type == ParticleType.PLASMA_CLOUD) {
                size *= 1.03; // Plasma expands
            } else if (type != ParticleType.CORE_BURST) {
                size *= 0.97; // Other particles shrink
            }

            // Update arc points for electric arcs
            if (type == ParticleType.ELECTRIC_ARC && arcPoints != null) {
                for (Point p : arcPoints) {
                    p.y += (Math.random() - 0.5) * 2; // Makes the arc crackle
                }
            }
        }

        boolean isDead() {
            return life <= 0 || size < 0.5;
        }

        void draw(GraphicsContext gc) {
            double opacity = life / maxLife;

            // Flicker effect for energy-based particles
            if (type == ParticleType.ENERGY_SPARK || type == ParticleType.ELECTRIC_ARC) {
                opacity *= Math.random() * 0.3 + flickerRate;
            }

            gc.save();
            gc.setGlobalBlendMode(type == ParticleType.ENERGY_SPARK ? BlendMode.ADD : BlendMode.SRC_OVER);
            gc.setGlobalAlpha(opacity);

            gc.translate(x, y);
            gc.rotate(rotationAngle);

            switch (type) {
                case SHIP_DEBRIS -> drawShipDebris(gc);
                case ENERGY_SPARK -> drawEnergySpark(gc);
                case PLASMA_CLOUD -> drawPlasmaCloud(gc);
                case CORE_BURST -> drawCoreBurst(gc);
                case ELECTRIC_ARC -> drawElectricArc(gc);
            }

            gc.restore();
        }

        private void drawShipDebris(GraphicsContext gc) {
            // Simplified debris to avoid array allocations each frame
            gc.setFill(primaryColor);
            gc.fillRect(-size / 2, -size / 4, size, size / 2);
        }

        private void drawEnergySpark(GraphicsContext gc) {
            // Inner glow
            gc.setFill(secondaryColor);
            gc.fillOval(-size/2, -size/2, size, size);

            // Outer glow
            gc.setFill(primaryColor);
            gc.fillOval(-size/3, -size/3, size/1.5, size/1.5);
        }

        private void drawPlasmaCloud(GraphicsContext gc) {
            // Simplified plasma: two overlapping ovals to simulate glow without gradient allocation
            gc.setFill(primaryColor.deriveColor(0, 1, 1, 0.35));
            gc.fillOval(-size/2, -size/2, size, size);
            gc.setFill(secondaryColor.deriveColor(0, 1, 1, 0.5));
            gc.fillOval(-size/3, -size/3, (2*size)/3, (2*size)/3);
        }

        private void drawCoreBurst(GraphicsContext gc) {
            // Bright core
            gc.setFill(Color.WHITE);
            gc.fillOval(-size/3, -size/3, size/1.5, size/1.5);

            // Outer ring
            gc.setStroke(primaryColor);
            gc.setLineWidth(size/4);
            gc.strokeOval(-size/2, -size/2, size, size);
        }

        private void drawElectricArc(GraphicsContext gc) {
            if (arcPoints == null || arcPoints.isEmpty()) return;

            gc.setStroke(primaryColor);
            gc.setLineWidth(2);

            // Draw main arc
            for (int i = 0; i < arcPoints.size() - 1; i++) {
                Point p1 = arcPoints.get(i);
                Point p2 = arcPoints.get(i + 1);
                gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
            }

            // Draw glow
            gc.setStroke(secondaryColor);
            gc.setLineWidth(1);
            gc.setGlobalAlpha(opacity * 0.5);
            for (int i = 0; i < arcPoints.size() - 1; i++) {
                Point p1 = arcPoints.get(i);
                Point p2 = arcPoints.get(i + 1);
                gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }

    public static class ShipExplosionConfig {
        private final Random random = new Random();
        private final double baseSize;

        // Default color schemes for different ship types
        private Color[][] debrisColors = {
                {Color.GRAY, Color.LIGHTGRAY}, // Standard
                {Color.DARKBLUE, Color.BLUE},  // Blue ships
                {Color.DARKGREEN, Color.GREEN}, // Green ships
                {Color.DARKRED, Color.RED}      // Red ships
        };

        private Color[][] energyColors = {
                {Color.YELLOW, Color.WHITE},
                {Color.CYAN, Color.WHITE},
                {Color.MAGENTA, Color.WHITE}
        };

        private int colorSchemeIndex = 0;

        public ShipExplosionConfig(double baseSize) {
            this.baseSize = baseSize;
        }

        public void setColorScheme(String shipType) {
            switch (shipType.toLowerCase()) {
                case "blue" -> colorSchemeIndex = 1;
                case "green" -> colorSchemeIndex = 2;
                case "red" -> colorSchemeIndex = 3;
                default -> colorSchemeIndex = 0;
            }
        }

        Color[] getColorsForType(ParticleType type) {
            return switch (type) {
                case SHIP_DEBRIS -> debrisColors[colorSchemeIndex];
                case ENERGY_SPARK, ELECTRIC_ARC ->
                        energyColors[random.nextInt(energyColors.length)];
                case PLASMA_CLOUD -> new Color[]{
                        debrisColors[colorSchemeIndex][0].brighter(),
                        debrisColors[colorSchemeIndex][1]
                };
                case CORE_BURST -> new Color[]{Color.WHITE, Color.YELLOW};
            };
        }
    }

    private final List<ShipExplosionParticle> particles = new ArrayList<>();
    private final ShipExplosionConfig config;

    public ShipExplosionEffect(double baseSize) {
        this.config = new ShipExplosionConfig(baseSize);
    }

    public void createExplosion(double x, double y, double size, String shipType) {
        config.setColorScheme(shipType);

        // Core burst
        particles.add(new ShipExplosionParticle(x, y, ParticleType.CORE_BURST, config));

        // Ship debris
        int debrisCount = Math.min((int) (size * 1.0), MAX_SHIP_DEBRIS);
        for (int i = 0; i < debrisCount; i++) {
            particles.add(new ShipExplosionParticle(x, y, ParticleType.SHIP_DEBRIS, config));
        }

        // Energy sparks
        int sparkCount = Math.min((int) (size * 1.2), MAX_ENERGY_SPARKS);
        for (int i = 0; i < sparkCount; i++) {
            particles.add(new ShipExplosionParticle(x, y, ParticleType.ENERGY_SPARK, config));
        }

        // Plasma clouds
        int cloudCount = Math.min((int) (size * 0.8), MAX_PLASMA_CLOUDS);
        for (int i = 0; i < cloudCount; i++) {
            particles.add(new ShipExplosionParticle(x, y, ParticleType.PLASMA_CLOUD, config));
        }

        // Electric arcs
        int arcCount = Math.min((int) (size * 0.6), MAX_ELECTRIC_ARCS);
        for (int i = 0; i < arcCount; i++) {
            particles.add(new ShipExplosionParticle(x, y, ParticleType.ELECTRIC_ARC, config));
        }
    }

    public void update() {
        Iterator<ShipExplosionParticle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            ShipExplosionParticle particle = iterator.next();
            particle.update();
            if (particle.isDead()) {
                iterator.remove();
            }
        }
    }

    public void draw(GraphicsContext gc) {
        // Draw in layers for better visual effect using for-loops (no stream overhead)
        double canvasW = gc.getCanvas().getWidth();
        double canvasH = gc.getCanvas().getHeight();

        // First plasma clouds
        for (ShipExplosionParticle p : particles) {
            if (p.type == ParticleType.PLASMA_CLOUD) {
                if (p.x + p.size < -OFFSCREEN_MARGIN || p.x - p.size > canvasW + OFFSCREEN_MARGIN ||
                        p.y + p.size < -OFFSCREEN_MARGIN || p.y - p.size > canvasH + OFFSCREEN_MARGIN) {
                    continue;
                }
                p.draw(gc);
            }
        }

        // Then debris
        for (ShipExplosionParticle p : particles) {
            if (p.type == ParticleType.SHIP_DEBRIS) {
                if (p.x + p.size < -OFFSCREEN_MARGIN || p.x - p.size > canvasW + OFFSCREEN_MARGIN ||
                        p.y + p.size < -OFFSCREEN_MARGIN || p.y - p.size > canvasH + OFFSCREEN_MARGIN) {
                    continue;
                }
                p.draw(gc);
            }
        }

        // Finally energy effects
        for (ShipExplosionParticle p : particles) {
            if (p.type == ParticleType.ENERGY_SPARK || p.type == ParticleType.CORE_BURST || p.type == ParticleType.ELECTRIC_ARC) {
                if (p.x + p.size < -OFFSCREEN_MARGIN || p.x - p.size > canvasW + OFFSCREEN_MARGIN ||
                        p.y + p.size < -OFFSCREEN_MARGIN || p.y - p.size > canvasH + OFFSCREEN_MARGIN) {
                    continue;
                }
                p.draw(gc);
            }
        }
    }

    public ShipExplosionConfig getConfig() {
        return config;
    }

    public boolean isActive() {
        return !particles.isEmpty();
    }
}