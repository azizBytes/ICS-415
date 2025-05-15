package engine;

import java.io.IOException;
import static org.lwjgl.opengl.GL11.*;

public class Renderer {
    private Cube cube;

    public Renderer() throws IOException {
        cube = new Cube();
    }

    public void render() {
        cube.render();
    }
}
