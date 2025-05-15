package engine;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;

import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;

public class World {
    private final List<GameObject> objects = new ArrayList<>();

    public void add(GameObject obj) {
        objects.add(obj);
    }
    public List<GameObject> getObjects() {
        return objects;
    }
    public void remove(GameObject obj) {
        objects.remove(obj);
    }

    public boolean containsAt(Vector3f position) {
        for (GameObject obj : objects) {
            if (obj.position.equals(position)) {
                return true;
            }
        }
        return false;
    }


    public void render(Matrix4f view, Matrix4f projection, int shaderID) {
        int locView = GL20.glGetUniformLocation(shaderID, "view");
        int locProj = GL20.glGetUniformLocation(shaderID, "projection");
        int locModel = GL20.glGetUniformLocation(shaderID, "model");

        float[] viewArr = new float[16];
        float[] projArr = new float[16];
        view.get(viewArr);
        projection.get(projArr);

        GL20.glUniformMatrix4fv(locView, false, viewArr);
        GL20.glUniformMatrix4fv(locProj, false, projArr);

        for (GameObject obj : objects) {
            float[] modelArr = new float[16];
            obj.getModelMatrix().get(modelArr);
            GL20.glUniformMatrix4fv(locModel, false, modelArr);
            obj.render();
        }
    }
}
