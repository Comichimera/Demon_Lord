package game.world.enemy;

import game.ai.IWorldAPI;
import game.config.GameConfig;
import game.map.MapData;
import game.map.Tile;
import game.world.Player;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class WorldAdapter implements IWorldAPI {

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
        if (!inBounds(c.x, c.y)) return false;
        return map.getTile(c.y, c.x).isWalkable();
    }

    @Override
    public boolean hasLineOfSight(Vector2i from, Vector2i to) {
        if (from == null || to == null) return false;
        if (!inBounds(from.x, from.y) || !inBounds(to.x, to.y)) return false;
        if (from.x == to.x && from.y == to.y) return true;

        float x0 = from.x + 0.5f;
        float y0 = from.y + 0.5f;
        float x1 = to.x   + 0.5f;
        float y1 = to.y   + 0.5f;

        float dx = x1 - x0;
        float dy = y1 - y0;

        int stepX = (dx > 0) ? 1 : (dx < 0) ? -1 : 0;
        int stepY = (dy > 0) ? 1 : (dy < 0) ? -1 : 0;

        float absDx = Math.abs(dx);
        float absDy = Math.abs(dy);

        float tDeltaX = (stepX == 0) ? Float.POSITIVE_INFINITY : (1f / absDx);
        float tDeltaY = (stepY == 0) ? Float.POSITIVE_INFINITY : (1f / absDy);

        int x = from.x;
        int y = from.y;

        float nextBoundaryX = (stepX > 0) ? (x + 1) : x;
        float nextBoundaryY = (stepY > 0) ? (y + 1) : y;

        float tMaxX = (stepX == 0) ? Float.POSITIVE_INFINITY : (Math.abs(nextBoundaryX - x0) / absDx);
        float tMaxY = (stepY == 0) ? Float.POSITIVE_INFINITY : (Math.abs(nextBoundaryY - y0) / absDy);

        while (true) {
            if (x == to.x && y == to.y) return true;

            if (tMaxX < tMaxY) {
                x += stepX;
                tMaxX += tDeltaX;
            } else if (tMaxY < tMaxX) {
                y += stepY;
                tMaxY += tDeltaY;
            } else {
                int nx = x + stepX;
                int ny = y + stepY;

                if (blocksSight(nx, y) || blocksSight(x, ny)) return false;

                x = nx;
                y = ny;
                tMaxX += tDeltaX;
                tMaxY += tDeltaY;
            }

            if (!inBounds(x, y)) return false;

            if (x != to.x || y != to.y) {
                if (blocksSight(x, y)) return false;
            }
        }
    }

    private boolean blocksSight(int tx, int ty) {
        if (!inBounds(tx, ty)) return true;

        Tile t = map.getTile(ty, tx);
        if (t == null) return true;

        if (t.isOpenable()) return !t.isOpen();

        return !t.isWalkable();
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
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    @Override
    public Vector2i playerCell() {
        return worldToCell(player.getX(), player.getZ());
    }

    @Override
    public Vector2i lastNoiseCell() {
        return lastNoiseCell;
    }
    @Override
    public List<Vector2i> patrolWaypoints() { return patrol; }

    public void accumulateTime(float dt) { timeSeconds += dt; }
    public void onNoise(Vector2i cell) { this.lastNoiseCell = new Vector2i(cell); }
    public void setPatrol(List<Vector2i> patrol) { this.patrol = List.copyOf(patrol); }

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
