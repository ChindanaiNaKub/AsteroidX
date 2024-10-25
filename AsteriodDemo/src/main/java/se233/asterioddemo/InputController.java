package se233.asterioddemo;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

public class InputController {
    private boolean left, right, up, down, shooting;
    private boolean cheatMode;

    public InputController(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) left = true;
            if (event.getCode() == KeyCode.RIGHT) right = true;
            if (event.getCode() == KeyCode.UP) up = true;
            if (event.getCode() == KeyCode.DOWN) down = true;
            if (event.getCode() == KeyCode.SPACE) shooting = true;
            // Activate cheat mode when 'C' is pressed
            if (event.getCode() == KeyCode.C) cheatMode = true;
        });

        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.LEFT) left = false;
            if (event.getCode() == KeyCode.RIGHT) right = false;
            if (event.getCode() == KeyCode.UP) up = false;
            if (event.getCode() == KeyCode.DOWN) down = false;
            if (event.getCode() == KeyCode.SPACE) shooting = false;
            // Deactivate cheat mode when 'C' is released
            if (event.getCode() == KeyCode.C) cheatMode = false;
        });
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

}
