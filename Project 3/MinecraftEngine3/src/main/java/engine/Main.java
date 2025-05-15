package engine;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    private static long window;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private static float lastMouseX = WIDTH / 2f;
    private static float lastMouseY = HEIGHT / 2f;
    private static boolean firstMouse = true;

    private static float deltaTime = 0.0f;
    private static float lastFrameTime = 0.0f;

    private static Camera camera;
    private static Shader shader;
    private static Cube cubeMesh;
    private static World world;

    public static void main(String[] args) {
        try {
            initWindow();
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

            shader = new Shader("src/main/resources/shaders/vertex.glsl", "src/main/resources/shaders/fragment.glsl");
            cubeMesh = new Cube();
            camera = new Camera(new Vector3f(0, 1, 3));

            world = new World();
            world.add(new GameObject(cubeMesh, new Vector3f(0, 0, 0)));
            world.add(new GameObject(cubeMesh, new Vector3f(2, 0, 0)));
            world.add(new GameObject(cubeMesh, new Vector3f(-2, 0, 0)));

            glEnable(GL_DEPTH_TEST);

            glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
                if (firstMouse) {
                    lastMouseX = (float) xpos;
                    lastMouseY = (float) ypos;
                    firstMouse = false;
                }

                float xoffset = (float) xpos - lastMouseX;
                float yoffset = (float) ypos - lastMouseY;
                lastMouseX = (float) xpos;
                lastMouseY = (float) ypos;

                camera.rotate(xoffset, yoffset);
            });

            // FPS counter
            int frames = 0;
            double lastTime = glfwGetTime();

            // Flat world
            for (int x = -10; x <= 10; x++) {
                for (int z = -10; z <= 10; z++) {
                    world.add(new GameObject(cubeMesh, new Vector3f(x, -1, z)));
                }
            }

            while (!glfwWindowShouldClose(window)) {
                float currentFrame = (float) glfwGetTime();
                deltaTime = currentFrame - lastFrameTime;
                lastFrameTime = currentFrame;

                glfwPollEvents();
                processInput(window, camera);
                glClearColor(0.5f, 0.8f, 0.95f, 1.0f);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                shader.use();

                int texLoc = glGetUniformLocation(shader.getID(), "texture1");
                int lightLoc = glGetUniformLocation(shader.getID(), "lightDir");
                int camLoc = glGetUniformLocation(shader.getID(), "cameraPos");

                glUniform1i(texLoc, 0);
                glUniform3f(lightLoc, -0.5f, -1.0f, -0.3f);
                glUniform3f(camLoc, camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);

                Matrix4f view = camera.getViewMatrix();
                Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(70.0f), WIDTH / (float) HEIGHT, 0.1f, 1000f);
                Vector3f rayDir = RayCaster.getRayDirection(WIDTH / 2f, HEIGHT / 2f, WIDTH, HEIGHT, projection, camera.getViewMatrix());
                Vector3f origin = camera.getPosition();

                boolean leftClick = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;
                boolean rightClick = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS;

                if (leftClick || rightClick) {
                    for (GameObject obj : new ArrayList<>(world.getObjects())) {
                        if (rayHitsCube(origin, rayDir, obj.position, 1.0f)) {
                            if (leftClick) {
                                world.remove(obj);
                                System.out.println("Removed: " + obj.position);
                            } else if (rightClick) {
                                Vector3f hitNormal = new Vector3f(rayDir).normalize();
                                Vector3f newBlock = new Vector3f(obj.position).add(hitNormal);
                                newBlock.set((float) Math.floor(newBlock.x + 0.5f),
                                        (float) Math.floor(newBlock.y + 0.5f),
                                        (float) Math.floor(newBlock.z + 0.5f));
                                if (!world.containsAt(newBlock) && newBlock.distance(camera.getPosition()) > 1.5f) {
                                    world.add(new GameObject(cubeMesh, newBlock));
                                    System.out.println("Placed: " + newBlock);
                                }
                            }
                            break;
                        }
                    }
                }

                world.render(view, projection, shader.getID());

                drawCrosshair();

                frames++;
                if (glfwGetTime() - lastTime >= 1.0) {
                    glfwSetWindowTitle(window, "3D Engine - FPS: " + frames);
                    frames = 0;
                    lastTime += 1.0;
                }

                glfwSwapBuffers(window);
            }

            cubeMesh.cleanup();
            shader.delete();
            glfwDestroyWindow(window);
            glfwTerminate();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void drawCrosshair() {
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, WIDTH, HEIGHT, 0, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glDisable(GL_DEPTH_TEST);
        glLineWidth(2.0f);
        glBegin(GL_LINES);
        glVertex2f(WIDTH / 2f, HEIGHT / 2f - 10);
        glVertex2f(WIDTH / 2f, HEIGHT / 2f + 10);
        glVertex2f(WIDTH / 2f - 10, HEIGHT / 2f);
        glVertex2f(WIDTH / 2f + 10, HEIGHT / 2f);
        glEnd();
        glEnable(GL_DEPTH_TEST);

        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
    }

    private static boolean rayHitsCube(Vector3f origin, Vector3f dir, Vector3f center, float size) {
        Vector3f min = new Vector3f(center).sub(new Vector3f(size / 2f));
        Vector3f max = new Vector3f(center).add(new Vector3f(size / 2f));

        float tMin = (min.x - origin.x) / dir.x;
        float tMax = (max.x - origin.x) / dir.x;
        if (tMin > tMax) { float tmp = tMin; tMin = tMax; tMax = tmp; }

        float tyMin = (min.y - origin.y) / dir.y;
        float tyMax = (max.y - origin.y) / dir.y;
        if (tyMin > tyMax) { float tmp = tyMin; tyMin = tyMax; tyMax = tmp; }

        if ((tMin > tyMax) || (tyMin > tMax)) return false;
        if (tyMin > tMin) tMin = tyMin;
        if (tyMax < tMax) tMax = tyMax;

        float tzMin = (min.z - origin.z) / dir.z;
        float tzMax = (max.z - origin.z) / dir.z;
        if (tzMin > tzMax) { float tmp = tzMin; tzMin = tzMax; tzMax = tmp; }

        return !(tMin > tzMax || tzMin > tMax);
    }

    private static void processInput(long window, Camera camera) {
        float cameraSpeed = camera.getSpeed() * deltaTime;

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS)
            camera.move(new Vector3f(camera.getFront()).mul(cameraSpeed));
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS)
            camera.move(new Vector3f(camera.getFront()).mul(-cameraSpeed));
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS)
            camera.move(new Vector3f(camera.getRight()).mul(-cameraSpeed));
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS)
            camera.move(new Vector3f(camera.getRight()).mul(cameraSpeed));

        // Vertical movement
        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS)
            camera.move(new Vector3f(0, cameraSpeed, 0));
        if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS)
            camera.move(new Vector3f(0, -cameraSpeed, 0));
    }

    private static void initWindow() {
        glfwInit();
        window = glfwCreateWindow(WIDTH, HEIGHT, "3D Engine - Picking", NULL, NULL);
        glfwMakeContextCurrent(window);
        glfwShowWindow(window);
        org.lwjgl.opengl.GL.createCapabilities();
    }
}
