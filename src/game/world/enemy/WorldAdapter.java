package game.world.enemy;

import game.ai.WorldAPI;
import game.config.GameConfig;
import game.map.MapData;
import game.world.Player;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class WorldAdapter implements WorldAPI {

    private final MapData map;
    private final Player player;

    private float timeSeconds = 0f;

    private Vector2i lastNoiseCell = null;

    private List<Vector2i> patrol = List.of();

    public WorldAdapter(MapData map, Player player) {
        this.map = Objects.requireNonNull(map);
        this.player = Objects.requireNonNull(player);
    }

    @Override
    public boolean isWalkable(Vector2i c) {
        return inBounds(c.x, c.y);
    }

    @Override
    public boolean hasLineOfSight(Vector2i from, Vector2i to) {
        int x0 = from.x, y0 = from.y;
        int x1 = to.x,   y1 = to.y;

        int dx = Math.abs(x1 - x0);
        int sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0);
        int sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;

        int x = x0, y = y0;
        while (true) {
            if (!inBounds(x, y)) return false;
            if (x == x1 && y == y1) break;
            int e2 = 2 * err;
            if (e2 >= dy) { err += dy; x += sx; }
            if (e2 <= dx) { err += dx; y += sy; }
        }
        return true;
    }

    @Override
    public List<Vector2i> neighbors4(Vector2i c) {
        int x = c.x, y = c.y;
        List<Vector2i> out = new ArrayList<>(4);
        tryAdd(out, x + 1, y);
        tryAdd(out, x - 1, y);
        tryAdd(out, x, y + 1);
        tryAdd(out, x, y - 1);
        return out;
    }

    @Override
    public float heuristicCost(Vector2i a, Vector2i b) {
        // Manhattan works well on 4-connected grids
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    @Override
    public float seconds() {
        return timeSeconds;
    }

    @Override
    public Vector2i playerCell() {
        return worldToCell(player.getX(), player.getZ());
    }

    @Override
    public Vector2i lastNoiseCell() {
        return lastNoiseCell; // may be null
    }
    public void accumulateTime(float dt) { timeSeconds += dt; }
    public void onNoise(Vector2i cell) { this.lastNoiseCell = new Vector2i(cell); }
    public void setPatrol(List<Vector2i> patrol) { this.patrol = List.copyOf(patrol); }
    public List<Vector2i> getPatrol() { return patrol; }

    private void tryAdd(List<Vector2i> out, int tx, int ty) {
        if (inBounds(tx, ty)) out.add(new Vector2i(tx, ty));
    }

    private boolean inBounds(int tx, int ty) {
        int w = map.getWidth();
        int h = map.getHeight();
        return tx >= 0 && ty >= 0 && tx < w && ty < h;
    }

    private static Vector2i worldToCell(float worldX, float worldZ) {
        final float ts = GameConfig.TILE_SIZE;
        return new Vector2i((int)(worldX / ts), (int)(worldZ / ts));
    }
}
