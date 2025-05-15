package engine;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class GameObject {
    public Vector3f position;
    public Vector3f rotation;
    public Vector3f scale;
    private final Cube cube;

    public GameObject(Cube cube, Vector3f position) {
        this.cube = cube;
        this.position = position;
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);
    }

    public Matrix4f getModelMatrix() {
        return new Matrix4f()
                .translate(position)
                .rotateX((float) Math.toRadians(rotation.x))
                .rotateY((float) Math.toRadians(rotation.y))
                .rotateZ((float) Math.toRadians(rotation.z))
                .scale(scale);
    }

    public void render() {
        cube.render();
    }
}
