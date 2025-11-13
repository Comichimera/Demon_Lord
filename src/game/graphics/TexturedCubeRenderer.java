package game.graphics;

import game.config.GameConfig;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class TexturedCubeRenderer {
    private int vaoId;
    private int vboId;
    private int vertexCount;
    private int textureId;
    private ShaderProgram shaderProgram;
    private FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    // Cube dimensions: the cube will span from -halfSize to +halfSize in x and z, and from 0 to roomHeight in y.
    private final float halfSize;
    private final float roomHeight;

    public TexturedCubeRenderer(float tileSize, float roomHeight, String texturePath) throws Exception {
        this.halfSize = GameConfig.ROOM_HALF_SIZE;
        this.roomHeight = roomHeight;
        textureId = TextureLoader.loadTexture(texturePath);

        String vertexShaderSource = "#version 330 core\n" +
                "layout(location = 0) in vec3 position;\n" +
                "layout(location = 1) in vec2 texCoord;\n" +
                "out vec2 passTexCoord;\n" +
                "uniform mat4 projection;\n" +
                "uniform mat4 view;\n" +
                "uniform mat4 model;\n" +
                "void main() {\n" +
                "    passTexCoord = texCoord;\n" +
                "    gl_Position = projection * view * model * vec4(position, 1.0);\n" +
                "}";
        String fragmentShaderSource = "#version 330 core\n" +
                "in vec2 passTexCoord;\n" +
                "out vec4 fragColor;\n" +
                "uniform sampler2D wallTexture;\n" +
                "void main() {\n" +
                "    fragColor = texture(wallTexture, passTexCoord);\n" +
                "}";
        shaderProgram = new ShaderProgram(vertexShaderSource, fragmentShaderSource);
        generateCubeGeometry();
    }

    private void generateCubeGeometry() {
        // We define a cube with 6 faces.
        // For each face, we define two triangles.
        // Vertex format: x, y, z, u, v.
        float hs = halfSize;
        float h = roomHeight;
        float[] vertices = {
                // Front face (z = +hs)
                -hs,  h,  hs,   0, 0,
                hs,   h,  hs,   1, 0,
                hs,   0,  hs,   1, 1,
                -hs,  h,  hs,   0, 0,
                hs,   0,  hs,   1, 1,
                -hs,  0,  hs,   0, 1,

                // Back face (z = -hs) – interior ordering for consistency.
                hs,   h, -hs,   0, 0,
                -hs,  h, -hs,   1, 0,
                -hs,  0, -hs,   1, 1,
                hs,   h, -hs,   0, 0,
                -hs,  0, -hs,   1, 1,
                hs,   0, -hs,   0, 1,

                // Left face (x = -hs)
                -hs,  h, -hs,   0, 0,
                -hs,  h,  hs,   1, 0,
                -hs,  0,  hs,   1, 1,
                -hs,  h, -hs,   0, 0,
                -hs,  0,  hs,   1, 1,
                -hs,  0, -hs,   0, 1,

                // Right face (x = +hs)
                hs,   h,  hs,   0, 0,
                hs,   h, -hs,   1, 0,
                hs,   0, -hs,   1, 1,
                hs,   h,  hs,   0, 0,
                hs,   0, -hs,   1, 1,
                hs,   0,  hs,   0, 1,

                // Top face (y = h)
                -hs,  h, -hs,   0, 1,
                hs,   h, -hs,   1, 1,
                hs,   h,  hs,   1, 0,
                -hs,  h, -hs,   0, 1,
                hs,   h,  hs,   1, 0,
                -hs,  h,  hs,   0, 0,

                // Bottom face (y = 0)
                -hs, 0,  hs,    0, 1,
                hs,  0,  hs,    1, 1,
                hs,  0, -hs,    1, 0,
                -hs, 0,  hs,    0, 1,
                hs,  0, -hs,    1, 0,
                -hs, 0, -hs,    0, 0,
        };
        vertexCount = vertices.length / 5;

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        int stride = 5 * Float.BYTES;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render(Matrix4f projection, Matrix4f view, Matrix4f model) {
        shaderProgram.use();

        int projLocation = shaderProgram.getUniformLocation("projection");
        projection.get(matrixBuffer);
        glUniformMatrix4fv(projLocation, false, matrixBuffer);

        int viewLocation = shaderProgram.getUniformLocation("view");
        view.get(matrixBuffer);
        glUniformMatrix4fv(viewLocation, false, matrixBuffer);

        int modelLocation = shaderProgram.getUniformLocation("model");
        model.get(matrixBuffer);
        glUniformMatrix4fv(modelLocation, false, matrixBuffer);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        int samplerLocation = shaderProgram.getUniformLocation("wallTexture");
        glUniform1i(samplerLocation, 0);

        glBindVertexArray(vaoId);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);

        shaderProgram.stop();
    }

    public void cleanup() {
        shaderProgram.cleanup();
        glDeleteBuffers(vboId);
        glDeleteVertexArrays(vaoId);
        glDeleteTextures(textureId);
    }
}
