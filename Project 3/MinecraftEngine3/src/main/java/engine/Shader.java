package engine;

import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private final int programID;

    public Shader(String vertexPath, String fragmentPath) throws IOException {
        String vertexCode = new String(Files.readAllBytes(Paths.get(vertexPath)));
        String fragmentCode = new String(Files.readAllBytes(Paths.get(fragmentPath)));

        int vertexID = compileShader(vertexCode, GL_VERTEX_SHADER);
        int fragmentID = compileShader(fragmentCode, GL_FRAGMENT_SHADER);

        programID = glCreateProgram();
        glAttachShader(programID, vertexID);
        glAttachShader(programID, fragmentID);
        glLinkProgram(programID);

        if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Program linking failed: " + glGetProgramInfoLog(programID));
        }

        glDeleteShader(vertexID);
        glDeleteShader(fragmentID);
    }

    private int compileShader(String code, int type) {
        int id = glCreateShader(type);
        glShaderSource(id, code);
        glCompileShader(id);

        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader compile error: " + glGetShaderInfoLog(id));
        }

        return id;
    }

    public void use() {
        glUseProgram(programID);
    }

    public void delete() {
        glDeleteProgram(programID);
    }

    public int getID() {
        return programID;
    }
}
