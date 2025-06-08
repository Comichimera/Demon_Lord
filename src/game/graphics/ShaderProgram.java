package game.graphics;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class ShaderProgram {
    private int programId;
    private int vertexShaderId;
    private int fragmentShaderId;

    public ShaderProgram(String vertexSource, String fragmentSource) throws Exception {
        programId = glCreateProgram();
        if (programId == 0) {
            throw new Exception("Could not create Shader");
        }
        vertexShaderId = createShader(vertexSource, GL_VERTEX_SHADER);
        fragmentShaderId = createShader(fragmentSource, GL_FRAGMENT_SHADER);
        glAttachShader(programId, vertexShaderId);
        glAttachShader(programId, fragmentShaderId);
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }
        if (vertexShaderId != 0) {
            glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            glDetachShader(programId, fragmentShaderId);
        }
        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }
    }

    private int createShader(String shaderCode, int shaderType) throws Exception {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new Exception("Error creating shader. Type: " + shaderType);
        }
        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
        }
        return shaderId;
    }

    public void use() {
        glUseProgram(programId);
    }

    public void stop() {
        glUseProgram(0);
    }

    public int getUniformLocation(String name) {
        return glGetUniformLocation(programId, name);
    }

    public void cleanup() {
        stop();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }
}
