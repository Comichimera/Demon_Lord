package game.input;

import game.config.Settings;
import game.map.MapData;
import game.world.Player;

public class KeyboardInput {
    public void processInput(Player player, float deltaTime, MapData mapData) {
        float moveSpeed = 5.0f * player.getSpeedMultiplier();
        float rotateSpeed = Settings.turnSpeed;

        if (Input.isDown(EAction.MOVE_FORWARD)) {
            player.move((float)Math.sin(Math.toRadians(player.getYaw())) * moveSpeed * deltaTime,
                    -(float)Math.cos(Math.toRadians(player.getYaw())) * moveSpeed * deltaTime,
                    mapData);
        }
        if (Input.isDown(EAction.MOVE_BACK)) {
            player.move(-(float)Math.sin(Math.toRadians(player.getYaw())) * moveSpeed * deltaTime,
                    (float)Math.cos(Math.toRadians(player.getYaw())) * moveSpeed * deltaTime,
                    mapData);
        }
        if (Input.isDown(EAction.TURN_RIGHT)) {
            player.rotate(rotateSpeed * deltaTime);
        }
        if (Input.isDown(EAction.TURN_LEFT)) {
            player.rotate(-rotateSpeed * deltaTime);
        }
    }
}
