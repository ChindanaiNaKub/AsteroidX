package se233.asterioddemo;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

public class InputController {
    private boolean left, right, up, down, shooting;
    private boolean cheatMode;
    private boolean AIModeActive; // Changed to track if F1 was pressed
    private boolean AIModeDeactivate; // Track if F2 was pressed
    private boolean shurikenMode, pluseMode, defaultMode;
    private boolean summonDrone; // New variable to track drone summoning
    private double mouseX, mouseY;

    public InputController(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) left = true;
            if (event.getCode() == KeyCode.RIGHT) right = true;
            if (event.getCode() == KeyCode.UP) up = true;
            if (event.getCode() == KeyCode.DOWN) down = true;
            if (event.getCode() == KeyCode.A) left = true;
            if (event.getCode() == KeyCode.D) right = true;
            if (event.getCode() == KeyCode.W) up = true;
            if (event.getCode() == KeyCode.S) down = true;

            // Activate cheat mode when 'C' is pressed
            if (event.getCode() == KeyCode.C) cheatMode = true;

            if (event.getCode() == KeyCode.F1) AIModeActive = true;

            // Deactivate AI mode when 'F2' is pressed
            if (event.getCode() == KeyCode.F2) AIModeDeactivate = true;


            // Bullet mode switching
            if (event.getCode() == KeyCode.Z) {
                shurikenMode = true;
                pluseMode = false;
                defaultMode = false;
            } else if (event.getCode() == KeyCode.X) {
                pluseMode = true;
                shurikenMode = false;
                defaultMode = false;
            } else if (event.getCode() == KeyCode.F) {
                defaultMode = true;
                shurikenMode = false;
                pluseMode = false;
            }

            // Summon a drone when 'Q' is pressed
            if (event.getCode() == KeyCode.Q) {
                summonDrone = true;
            }
        });

        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.LEFT) left = false;
            if (event.getCode() == KeyCode.RIGHT) right = false;
            if (event.getCode() == KeyCode.UP) up = false;
            if (event.getCode() == KeyCode.DOWN) down = false;
            if (event.getCode() == KeyCode.A) left = false;
            if (event.getCode() == KeyCode.D) right = false;
            if (event.getCode() == KeyCode.W) up = false;
            if (event.getCode() == KeyCode.S) down = false;

            // Deactivate cheat mode when 'C' is released
            if (event.getCode() == KeyCode.C) cheatMode = false;

            if (event.getCode() == KeyCode.F1) AIModeActive = false;
            if (event.getCode() == KeyCode.F2) AIModeDeactivate = false;

            // Reset drone summon state when 'Q' is released
            if (event.getCode() == KeyCode.Q) {
                summonDrone = false;
            }
        });

        scene.setOnMousePressed(event -> shooting = true);
        scene.setOnMouseReleased(event -> shooting = false);
        scene.setOnMouseMoved(this::handleMouseMoved);
        scene.setOnMouseDragged(this::handleMouseMoved);
    }

    private void handleMouseMoved(MouseEvent event) {
        mouseX = event.getX();
        mouseY = event.getY();
    }

    // Getter methods for movement, actions, and state
    public boolean isLeftPressed() {
        return left;
    }

    public boolean isRightPressed() {
        return right;
    }

    public boolean isUpPressed() {
        return up;
    }

    public boolean isDownPressed() {
        return down;
    }

    public boolean isShootingPressed() {
        return shooting;
    }

    public boolean isCheatModeEnabled() {
        return cheatMode;
    }

    public boolean isAIModeActive() {
        return AIModeActive;
    }

    public boolean isAIModeDeactivate() {
        return AIModeDeactivate;
    }

    public boolean isShurikenMode() {
        return shurikenMode;
    }

    public boolean isPluseMode() {
        return pluseMode;
    }

    public boolean isDefaultMode() {
        return defaultMode;
    }

    // Check if the 'Q' key was pressed to summon a drone
    public boolean isSummonDrone() {
        return summonDrone;
    }

    // Get position of mouse movement
    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }
}
