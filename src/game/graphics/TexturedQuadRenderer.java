package game.graphics;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class TexturedQuadRenderer {
    private int vaoId;
    private int vboId;
    private int vertexCount;
    private int textureId;
    private ShaderProgram shaderProgram;
    private FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    public TexturedQuadRenderer(String texturePath, float width, float height) throws Exception {
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
                "uniform sampler2D quadTexture;\n" +
                "void main() {\n" +
                "    fragColor = texture(quadTexture, passTexCoord);\n" +
                "}";
        shaderProgram = new ShaderProgram(vertexShaderSource, fragmentShaderSource);

        // Create a quad centered at the origin.
        float hw = width / 2.0f;
        float hh = height / 2.0f;
        float[] vertices = {
                -hw, 0, -hh,   0, 0,
                hw, 0, -hh,   1, 0,
                hw, 0,  hh,   1, 1,
                -hw, 0, -hh,   0, 0,
                hw, 0,  hh,   1, 1,
                -hw, 0,  hh,   0, 1
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
        int samplerLocation = shaderProgram.getUniformLocation("quadTexture");
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
