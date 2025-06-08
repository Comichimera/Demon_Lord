package game.world;

import game.map.MapData;
import game.physics.CollisionHandler;

public class Player {
    private float x, y, z;
    private float yaw;

    public Player() {
        this.x = 0.0f;
        this.y = 1.5f;
        this.z = 0.0f;
        this.yaw = 0.0f;
    }

    /**
     * Try to move by (dx, dz); only actually updates x/z
     * if CollisionHandler says that cell is walkable.
     */
    public void move(float dx, float dz, MapData mapData) {
        float newX = x + dx;
        float newZ = z + dz;

        if (!CollisionHandler.isColliding(newX, newZ, mapData)) {
            x = newX;
            z = newZ;
        }
    }

    public void rotate(float angle) {
        yaw += angle;
    }

    public void applyCameraTransform() {
        // your existing GL camera code here...
    }

    public float getYaw() { return yaw; }
    public float getX()   { return x; }
    public float getY()   { return y; }
    public float getZ()   { return z; }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPosition(float x, float z) {
        this.x = x;
        this.z = z;
    }
}
