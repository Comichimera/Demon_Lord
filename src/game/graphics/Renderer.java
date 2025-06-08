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

public class Renderer {

    private ShaderProgram shaderProgram;
    private int floorVaoId;
    private int floorVboId;
    private int floorVertexCount;
    private FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
    private MapData mapData;

    // Tile dimensions from GameConfig.
    private final float tileSize = GameConfig.TILE_SIZE;
    private final float roomHalfSize = GameConfig.ROOM_HALF_SIZE;
    private final float roomHeight = GameConfig.ROOM_HEIGHT;

    // For version overlay.
    private int versionTexture;
    private int versionWidth, versionHeight;

    // Lists to record positions of different tile types.
    private final Map<String,TexturedCubeRenderer> cubeRenderers = new HashMap<>();
    private final Map<String,TexturedQuadRenderer> quadRenderers = new HashMap<>();

    private final Map<String,List<float[]>> cubeInstances = new HashMap<>();
    private final Map<String,List<float[]>> quadInstances = new HashMap<>();

    public Renderer(MapData mapData) throws Exception {
        this.mapData = mapData;

        // Create and compile the shader program for floor geometry.
        String vertexShaderSource = "#version 330 core\n" +
                "layout(location = 0) in vec3 position;\n" +
                "layout(location = 1) in vec3 color;\n" +
                "out vec3 vertexColor;\n" +
                "uniform mat4 projection;\n" +
                "uniform mat4 view;\n" +
                "uniform mat4 model;\n" +
                "void main() {\n" +
                "    vertexColor = color;\n" +
                "    gl_Position = projection * view * model * vec4(position, 1.0);\n" +
                "}";
        String fragmentShaderSource = "#version 330 core\n" +
                "in vec3 vertexColor;\n" +
                "out vec4 fragColor;\n" +
                "void main() {\n" +
                "    fragColor = vec4(vertexColor, 1.0);\n" +
                "}";
        shaderProgram = new ShaderProgram(vertexShaderSource, fragmentShaderSource);

        // Generate floor geometry and record positions for wall, exit, and door tiles.
        generateFloorGeometry();
        // Generate the version overlay texture.
        generateVersionTexture();
        // Initialize textured renderers using paths from mapData.
        for (String tex : cubeInstances.keySet()) {
            cubeRenderers.put(
                    tex,
                    new TexturedCubeRenderer(tileSize, roomHeight, tex)
            );
        }

        // And for each quad texture
        for (String tex : quadInstances.keySet()) {
            quadRenderers.put(
                    tex,
                    new TexturedQuadRenderer(tex, tileSize, tileSize)
            );
        }
    }

    /**
     * Generates floor geometry for floor tiles and records wall, exit, and door positions.
     */
    private void generateFloorGeometry() {
        // Clear any old data
        cubeInstances.clear();
        quadInstances.clear();

        // Build floor vertices as before
        List<Float> verticesList = new ArrayList<>();
        Tile[][] tiles = mapData.getTiles();
        int rows = tiles.length;
        int cols = tiles[0].length;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Tile t = tiles[row][col];
                float centerX = col * tileSize + roomHalfSize;
                float centerZ = row * tileSize + roomHalfSize;

                // Pull renderer type and texture straight from the definition
                TileDefinition def = t.getDefinition();
                String rendererType = def.getRenderer();  // "floor", "cube", or "quad"
                String texPath      = def.getTexture();

                // 1) Floor tiles (or open doors shown as floor)
                if ("floor".equals(rendererType) || (t.isOpenable() && t.isOpen())) {
                    addFloor(verticesList, centerX, centerZ);

                    // 2) Cube-type (walls, closed doors, new spikes, etc.)
                } else if ("cube".equals(rendererType)) {
                    cubeInstances
                            .computeIfAbsent(texPath, k -> new ArrayList<>())
                            .add(new float[]{ centerX, centerZ });

                    // 3) Quad-type (exit markers, 2D overlays, etc.)
                } else if ("quad".equals(rendererType)) {
                    quadInstances
                            .computeIfAbsent(texPath, k -> new ArrayList<>())
                            .add(new float[]{ centerX, centerZ });
                }
            }
        }

        // Upload the floor VBO exactly as you did before:
        float[] vertices = new float[verticesList.size()];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = verticesList.get(i);
        }
        floorVertexCount = vertices.length / 6;
        if (floorVaoId == 0) {
            floorVaoId = glGenVertexArrays();
            floorVboId = glGenBuffers();
        }
        glBindVertexArray(floorVaoId);
        glBindBuffer(GL_ARRAY_BUFFER, floorVboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        int stride = 6 * Float.BYTES;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }



    // Adds a floor quad for a floor tile.
    private void addFloor(List<Float> list, float centerX, float centerZ) {
        addQuad(list,
                centerX - roomHalfSize, 0f, centerZ - roomHalfSize,
                centerX + roomHalfSize, 0f, centerZ - roomHalfSize,
                centerX + roomHalfSize, 0f, centerZ + roomHalfSize,
                centerX - roomHalfSize, 0f, centerZ + roomHalfSize,
                0.5f, 0f, 0f);  // Dark red floor color.
    }

    // Helper: adds a quad (two triangles) given four vertices and a color.
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

    // Generates the version overlay texture.
    private void generateVersionTexture() {
        String versionText = game.core.GameData.getVersion();
        Font font = new Font("Rubik", Font.PLAIN, 24);
        BufferedImage image = new BufferedImage(256, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setFont(font);
        g.setColor(Color.WHITE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics metrics = g.getFontMetrics();
        versionWidth = metrics.stringWidth(versionText);
        versionHeight = metrics.getHeight();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(Color.WHITE);
        g.drawString(versionText, 10, metrics.getAscent() + 5);
        g.dispose();

        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buffer.flip();

        versionTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, versionTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    // Draws the version overlay at the top-left corner.
    private void drawVersionText() {
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, 800, 600, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, versionTexture);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glColor4f(1f, 1f, 1f, 1f);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex2f(10, 10);
        glTexCoord2f(1, 0); glVertex2f(10 + versionWidth, 10);
        glTexCoord2f(1, 1); glVertex2f(10 + versionWidth, 10 + versionHeight);
        glTexCoord2f(0, 1); glVertex2f(10, 10 + versionHeight);
        glEnd();

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
    }

    public void render(Player player) {
        glEnable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glDisable(GL_CULL_FACE);
        shaderProgram.use();

        Matrix4f projection = new Matrix4f().perspective((float)Math.toRadians(60), 800f / 600f, 0.1f, 100f);
        int projLocation = shaderProgram.getUniformLocation("projection");
        projection.get(matrixBuffer);
        glUniformMatrix4fv(projLocation, false, matrixBuffer);

        Matrix4f view = new Matrix4f().identity()
                .rotateY((float)Math.toRadians(player.getYaw()))
                .translate(-player.getX(), -player.getY(), -player.getZ());
        int viewLocation = shaderProgram.getUniformLocation("view");
        view.get(matrixBuffer);
        glUniformMatrix4fv(viewLocation, false, matrixBuffer);

        Matrix4f model = new Matrix4f().identity();
        int modelLocation = shaderProgram.getUniformLocation("model");
        model.get(matrixBuffer);
        glUniformMatrix4fv(modelLocation, false, matrixBuffer);

        // Render floor geometry.
        glBindVertexArray(floorVaoId);
        glDrawArrays(GL_TRIANGLES, 0, floorVertexCount);
        glBindVertexArray(0);
        shaderProgram.stop();

        // Cube instances
        for (var entry : cubeInstances.entrySet()) {
            String tex = entry.getKey();
            TexturedCubeRenderer rend = cubeRenderers.get(tex);
            for (float[] center : entry.getValue()) {
                Matrix4f cubeModel = new Matrix4f().translation(center[0], 0, center[1]);
                rend.render(projection, view, cubeModel);
            }
        }

// Quad instances
        for (var entry : quadInstances.entrySet()) {
            String tex = entry.getKey();
            TexturedQuadRenderer rend = quadRenderers.get(tex);
            for (float[] center : entry.getValue()) {
                Matrix4f quadModel = new Matrix4f().translation(center[0], 0, center[1]);
                rend.render(projection, view, quadModel);
            }
        }

        drawVersionText();
    }

    public void updateFloorGeometry() {
        generateFloorGeometry();
    }

    public void cleanup() {
        shaderProgram.cleanup();
        glDeleteBuffers(floorVboId);
        glDeleteVertexArrays(floorVaoId);

        // Delete all cube textures
        for (TexturedCubeRenderer r : cubeRenderers.values()) {
            r.cleanup();
        }
        // Delete all quad textures
        for (TexturedQuadRenderer r : quadRenderers.values()) {
            r.cleanup();
        }

        cubeRenderers.clear();
        quadRenderers.clear();
        cubeInstances.clear();
        quadInstances.clear();

        glDeleteTextures(versionTexture);
    }
}
