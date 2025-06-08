package game.input;

import game.map.MapData;
import game.world.Player;
import static org.lwjgl.glfw.GLFW.*;

public class KeyboardInput {
    public void processInput(Player player, float deltaTime, MapData mapData) {
        float moveSpeed = 5.0f;
        float rotateSpeed = 90.0f;
        long context = glfwGetCurrentContext();

        if (glfwGetKey(context, GLFW_KEY_W) == GLFW_PRESS) {
            player.move((float)Math.sin(Math.toRadians(player.getYaw())) * moveSpeed * deltaTime,
                    -(float)Math.cos(Math.toRadians(player.getYaw())) * moveSpeed * deltaTime,
                    mapData);
        }
        if (glfwGetKey(context, GLFW_KEY_S) == GLFW_PRESS) {
            player.move(-(float)Math.sin(Math.toRadians(player.getYaw())) * moveSpeed * deltaTime,
                    (float)Math.cos(Math.toRadians(player.getYaw())) * moveSpeed * deltaTime,
                    mapData);
        }
        if (glfwGetKey(context, GLFW_KEY_D) == GLFW_PRESS) {
            player.rotate(rotateSpeed * deltaTime);
        }
        if (glfwGetKey(context, GLFW_KEY_A) == GLFW_PRESS) {
            player.rotate(-rotateSpeed * deltaTime);
        }
    }
}
