package se233.asterioddemo;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

public class InputController {
    private boolean left, right, up, down, shooting;
    private boolean cheatMode;
    private boolean shurikenMode, pluseMode, defaultMode;
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
        });

        scene.setOnMousePressed(event -> shooting = true);
        scene.setOnMouseReleased(event -> shooting = false);
        scene.setOnMouseMoved(this::handleMouseMoved);
    }

    private void handleMouseMoved(MouseEvent event) {
        mouseX = event.getX();
        mouseY = event.getY();
    }

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

    public boolean isShurikenMode() {
        return shurikenMode;
    }

    public boolean isPluseMode() {
        return pluseMode;
    }

    public boolean isDefaultMode() {
        return defaultMode;
    }

    // Get position of mouse movement
    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }
}
