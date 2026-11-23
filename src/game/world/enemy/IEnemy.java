package game.world.enemy;

import game.logic.event.EventBus;
import game.map.MapData;
import game.world.Player;

public interface IEnemy {
    String getType();

    float getX();
    float getY();
    float getZ();
    float getYaw();

    void setPosition(float x, float z);
    void setYaw(float yawDegrees);

    void update(float dt, MapData map, Player player, EventBus events);

    boolean canSeePlayer(MapData map, Player player);
}
