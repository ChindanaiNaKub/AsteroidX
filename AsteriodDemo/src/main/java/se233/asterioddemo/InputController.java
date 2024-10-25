package se233.asterioddemo;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

public class InputController {
    private boolean left, right, up, down, shooting;
    private boolean cheatMode;
    private double mouseX, mouseY;

    public InputController(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) left = true;
            if (event.getCode() == KeyCode.RIGHT) right = true;
            if (event.getCode() == KeyCode.UP) up = true;
            if (event.getCode() == KeyCode.DOWN) down = true;
            // Activate cheat mode when 'C' is pressed
            if (event.getCode() == KeyCode.C) cheatMode = true;
        });

        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.LEFT) left = false;
            if (event.getCode() == KeyCode.RIGHT) right = false;
            if (event.getCode() == KeyCode.UP) up = false;
            if (event.getCode() == KeyCode.DOWN) down = false;
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

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }
}