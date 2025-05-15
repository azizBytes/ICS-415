package engine;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private Vector3f position;
    private float pitch, yaw;
    private float speed = 5f;
    private float sensitivity = 0.1f;

    public Camera(Vector3f position) {
        this.position = position;
        this.pitch = 0;
        this.yaw = -90;
    }

    public Matrix4f getViewMatrix() {
        Vector3f front = getFront();
        Vector3f center = new Vector3f(position).add(front);
        return new Matrix4f().lookAt(position, center, new Vector3f(0, 1, 0));
    }

    public void move(Vector3f direction) {
        position.add(new Vector3f(direction)); // clone to avoid modifying source
    }


    public void rotate(float deltaX, float deltaY) {
        yaw += deltaX * sensitivity;
        pitch -= deltaY * sensitivity;

        pitch = Math.max(-89.0f, Math.min(89.0f, pitch));
    }

    public Vector3f getFront() {
        float radPitch = (float)Math.toRadians(pitch);
        float radYaw = (float)Math.toRadians(yaw);
        return new Vector3f(
                (float)Math.cos(radPitch) * (float)Math.cos(radYaw),
                (float)Math.sin(radPitch),
                (float)Math.cos(radPitch) * (float)Math.sin(radYaw)
        ).normalize();
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }



    public Vector3f getRight() {
        return getFront().cross(new Vector3f(0, 1, 0), new Vector3f()).normalize();
    }

    public Vector3f getUp() {
        return getRight().cross(getFront(), new Vector3f()).normalize();
    }

    public Vector3f getPosition() {
        return position;
    }




}
