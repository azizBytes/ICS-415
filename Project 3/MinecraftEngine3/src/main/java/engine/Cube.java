package engine;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.stb.STBImage.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.io.IOException;
import org.lwjgl.system.MemoryStack;

public class Cube {
    private final int vaoID;
    private final int vboID;
    private final int eboID;
    private final int textureID;

    // Each vertex: position (3), texCoord (2), normal (3) = 8 floats
    private static final float[] VERTICES = {
            // positions         // texCoords  // normals
            -0.5f, -0.5f, -0.5f,  0f, 0f,     0f,  0f, -1f,
            0.5f, -0.5f, -0.5f,  1f, 0f,     0f,  0f, -1f,
            0.5f,  0.5f, -0.5f,  1f, 1f,     0f,  0f, -1f,
            -0.5f,  0.5f, -0.5f,  0f, 1f,     0f,  0f, -1f,

            -0.5f, -0.5f,  0.5f,  0f, 0f,     0f,  0f, 1f,
            0.5f, -0.5f,  0.5f,  1f, 0f,     0f,  0f, 1f,
            0.5f,  0.5f,  0.5f,  1f, 1f,     0f,  0f, 1f,
            -0.5f,  0.5f,  0.5f,  0f, 1f,     0f,  0f, 1f
    };

    private static final int[] INDICES = {
            0, 1, 2, 2, 3, 0, // back
            4, 5, 6, 6, 7, 4, // front
            4, 5, 1, 1, 0, 4, // bottom
            6, 7, 3, 3, 2, 6, // top
            4, 7, 3, 3, 0, 4, // left
            1, 5, 6, 6, 2, 1  // right
    };

    public Cube() throws IOException {
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, VERTICES, GL_STATIC_DRAW);

        eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, INDICES, GL_STATIC_DRAW);

        // position (location = 0)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // texCoord (location = 1)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // normal (location = 2)
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 8 * Float.BYTES, 5 * Float.BYTES);
        glEnableVertexAttribArray(2);

        textureID = loadTexture("src/main/resources/textures/brick.png");

        glBindVertexArray(0);
    }

    public void render() {
        glBindTexture(GL_TEXTURE_2D, textureID);
        glBindVertexArray(vaoID);
        glDrawElements(GL_TRIANGLES, INDICES.length, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    private int loadTexture(String path) throws IOException {
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            stbi_set_flip_vertically_on_load(true);
            ByteBuffer image = stbi_load(path, w, h, channels, 4);
            if (image != null) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w.get(), h.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
                glGenerateMipmap(GL_TEXTURE_2D);
                stbi_image_free(image);
            } else {
                throw new IOException("Failed to load texture: " + stbi_failure_reason());
            }
        }

        return textureID;
    }

    public void cleanup() {
        glDeleteTextures(textureID);
        glDeleteVertexArrays(vaoID);
        glDeleteBuffers(vboID);
        glDeleteBuffers(eboID);
    }
}
