package engine;

import org.joml.Vector3f;
import org.joml.Matrix4f;

public class RayCaster {

    public static Vector3f getRayDirection(float mouseX, float mouseY, int width, int height, Matrix4f projection, Matrix4f view) {
        // Convert mouse coordinates to NDC
        float x = (2.0f * mouseX) / width - 1.0f;
        float y = 1.0f - (2.0f * mouseY) / height;
        Vector3f rayNDC = new Vector3f(x, y, -1.0f);

        // Convert to eye space
        Vector3f rayClip = new Vector3f(rayNDC.x, rayNDC.y, -1.0f);
        Matrix4f invProj = new Matrix4f(projection).invert();
        Vector3f rayEye = invProj.transformDirection(rayClip);

        // Convert to world space
        Matrix4f invView = new Matrix4f(view).invert();
        Vector3f rayWorld = invView.transformDirection(rayEye).normalize();

        return rayWorld;
    }
}
