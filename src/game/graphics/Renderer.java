package game.graphics;

import game.config.GameConfig;
import game.map.MapData;
import game.map.Tile;
import game.map.TileDefinition;
import game.world.Player;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import java.awt.RenderingHints;
import java.util.Map;

import game.world.enemy.EnemyManager;
import game.graphics.EnemySpriteRenderer;
import game.graphics.SpriteDefsLoader;

public class Renderer {
    private MapData mapData;

    private final float tileSize = GameConfig.TILE_SIZE;
    private final float roomHalfSize = GameConfig.ROOM_HALF_SIZE;
    private final float roomHeight = GameConfig.ROOM_HEIGHT;

    private final Map<String,TexturedCubeRenderer> cubeRenderers = new HashMap<>();
    private final Map<String,TexturedQuadRenderer> quadRenderers = new HashMap<>();

    private final Map<String,List<float[]>> cubeInstances = new HashMap<>();
    private final Map<String,List<float[]>> quadInstances = new HashMap<>();
    private final Map<String, TexturedQuadRenderer> floorRenderers = new HashMap<>();
    private final Map<String, List<float[]>> floorInstances = new HashMap<>();
    private EnemySpriteRenderer enemySprites;
    private EnemyManager enemyManager;

    public Renderer(MapData mapData) throws Exception {
        this.mapData = mapData;

        generateFloorGeometry();

        for (String tex : cubeInstances.keySet()) {
            cubeRenderers.put(tex, new TexturedCubeRenderer(tileSize, roomHeight, tex));
        }

        for (String tex : quadInstances.keySet()) {
            quadRenderers.put(tex, new TexturedQuadRenderer(tex, tileSize, tileSize));
        }

        for (String tex : floorInstances.keySet()) {
            floorRenderers.put(tex, new TexturedQuadRenderer(tex, tileSize, tileSize));
        }
    }
    private void generateFloorGeometry() {
        cubeInstances.clear();
        quadInstances.clear();
        floorInstances.clear();

        Tile[][] tiles = mapData.getTiles();
        int rows = tiles.length;
        int cols = tiles[0].length;

        String defaultFloorTex = null;
        for (int r = 0; r < rows && defaultFloorTex == null; r++) {
            for (int c = 0; c < cols && defaultFloorTex == null; c++) {
                TileDefinition def = tiles[r][c].getDefinition();
                if ("floor".equals(def.getRenderer())
                        && def.getTexture() != null
                        && !def.getTexture().isEmpty()) {
                    defaultFloorTex = def.getTexture();
                }
            }
        }

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Tile t = tiles[row][col];
                float centerX = col * tileSize + roomHalfSize;
                float centerZ = row * tileSize + roomHalfSize;

                TileDefinition def = t.getDefinition();
                String rendererType = def.getRenderer();  // "floor", "cube", or "quad"
                String texPath      = def.getTexture();

                boolean isOpenDoor = t.isOpenable() && t.isOpen();

                if ("floor".equals(rendererType) || isOpenDoor) {
                    String floorTex = texPath;

                    if (isOpenDoor) {
                        floorTex = resolveDoorFloorTexture(row, col, tiles, defaultFloorTex);
                    }

                    if (floorTex == null || floorTex.isEmpty()) {
                        floorTex = defaultFloorTex;
                    }
                    if (floorTex == null) {
                        continue;
                    }

                    floorInstances
                            .computeIfAbsent(floorTex, k -> new ArrayList<>())
                            .add(new float[]{ centerX, centerZ });

                } else if ("cube".equals(rendererType)) {
                    cubeInstances
                            .computeIfAbsent(texPath, k -> new ArrayList<>())
                            .add(new float[]{ centerX, centerZ });

                } else if ("quad".equals(rendererType)) {
                    quadInstances
                            .computeIfAbsent(texPath, k -> new ArrayList<>())
                            .add(new float[]{ centerX, centerZ });
                }
            }
        }
    }



    private void addFloor(List<Float> list, float centerX, float centerZ) {
        addQuad(list,
                centerX - roomHalfSize, 0f, centerZ - roomHalfSize,
                centerX + roomHalfSize, 0f, centerZ - roomHalfSize,
                centerX + roomHalfSize, 0f, centerZ + roomHalfSize,
                centerX - roomHalfSize, 0f, centerZ + roomHalfSize,
                0.5f, 0f, 0f);  // Dark red floor color.
    }

    private void addQuad(List<Float> list,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float x3, float y3, float z3,
                         float x4, float y4, float z4,
                         float r, float g, float b) {
        addVertex(list, x1, y1, z1, r, g, b);
        addVertex(list, x2, y2, z2, r, g, b);
        addVertex(list, x3, y3, z3, r, g, b);
        addVertex(list, x1, y1, z1, r, g, b);
        addVertex(list, x3, y3, z3, r, g, b);
        addVertex(list, x4, y4, z4, r, g, b);
    }

    private void addVertex(List<Float> list, float x, float y, float z,
                           float r, float g, float b) {
        list.add(x);
        list.add(y);
        list.add(z);
        list.add(r);
        list.add(g);
        list.add(b);
    }

    public void render(Player player) {
        glEnable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glDisable(GL_CULL_FACE);

        Matrix4f projection = new Matrix4f()
                .perspective((float) Math.toRadians(60), 800f / 600f, 0.1f, 100f);

        Matrix4f view = new Matrix4f().identity()
                .rotateY((float) Math.toRadians(player.getYaw()))
                .translate(-player.getX(), -player.getY(), -player.getZ());

        for (var entry : floorInstances.entrySet()) {
            String tex = entry.getKey();
            TexturedQuadRenderer rend = floorRenderers.get(tex);
            if (rend == null) continue;

            for (float[] center : entry.getValue()) {
                Matrix4f model = new Matrix4f().translation(center[0], 0f, center[1]);
                rend.render(projection, view, model);
            }
        }

        for (var entry : cubeInstances.entrySet()) {
            String tex = entry.getKey();
            TexturedCubeRenderer rend = cubeRenderers.get(tex);
            if (rend == null) continue;

            for (float[] center : entry.getValue()) {
                Matrix4f cubeModel = new Matrix4f().translation(center[0], 0f, center[1]);
                rend.render(projection, view, cubeModel);
            }
        }

        for (var entry : quadInstances.entrySet()) {
            String tex = entry.getKey();
            TexturedQuadRenderer rend = quadRenderers.get(tex);
            if (rend == null) continue;

            for (float[] center : entry.getValue()) {
                Matrix4f quadModel = new Matrix4f().translation(center[0], 0f, center[1]);
                rend.render(projection, view, quadModel);
            }
        }

        if (enemySprites != null && enemyManager != null) {
            enemySprites.render(enemyManager, player, projection, view);
        }
    }
    private String resolveDoorFloorTexture(int row, int col,
                                           Tile[][] tiles,
                                           String defaultFloorTex) {
        int rows = tiles.length;
        int cols = tiles[0].length;

        // 4-neighbors
        int[][] offsets = {
                { 1, 0 }, { -1, 0 },
                { 0, 1 }, { 0, -1 }
        };

        for (int[] off : offsets) {
            int nr = row + off[0];
            int nc = col + off[1];
            if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;

            Tile neighbor = tiles[nr][nc];
            TileDefinition ndef = neighbor.getDefinition();
            if ("floor".equals(ndef.getRenderer())
                    && ndef.getTexture() != null
                    && !ndef.getTexture().isEmpty()) {
                return ndef.getTexture();
            }
        }

        return defaultFloorTex;
    }

    public void updateFloorGeometry() {
        generateFloorGeometry();
    }

    public void initEnemySpritesForMap(String mapJsonPath) {
        if (enemySprites != null) {
            enemySprites.cleanup();
            enemySprites = null;
        }
        SpriteDefsLoader defs = SpriteDefsLoader.loadForMap(mapJsonPath);
        this.enemySprites = new EnemySpriteRenderer(defs);
    }

    public void setEnemyManager(EnemyManager mgr) {
        this.enemyManager = mgr;
    }

    public void cleanup() {
        // floors
        for (TexturedQuadRenderer r : floorRenderers.values()) {
            r.cleanup();
        }
        floorRenderers.clear();
        floorInstances.clear();

        // cubes
        for (TexturedCubeRenderer r : cubeRenderers.values()) {
            r.cleanup();
        }
        cubeRenderers.clear();
        cubeInstances.clear();

        // other quads
        for (TexturedQuadRenderer r : quadRenderers.values()) {
            r.cleanup();
        }
        quadRenderers.clear();
        quadInstances.clear();

        if (enemySprites != null) {
            enemySprites.cleanup();
            enemySprites = null;
        }
        enemyManager = null;
    }

}
