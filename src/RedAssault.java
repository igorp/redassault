
import graphics.Shader;
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import maths.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWKeyCallback;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GLContext;
import static org.lwjgl.system.MemoryUtil.NULL;

/*
 * RedAssault class by Igor P.
 * Top-most class of the game which contain main() method.
 */
public class RedAssault implements Runnable {

    static boolean fullscreen;
    static int screenWidth;
    static int screenHeight;    
    // multiplier for graphics and images
    static int m = 3;
    static int tileSizeSource = 30;

    private GLFWKeyCallback keyCallback;
    private Thread thread;
    private long window;

    // One game cycle is 16.7 ms (or 1.67e7 ns) (framerate to 60 FPS)
    double CYCLE_TIME = 16666666.6667;
    static long timer = 0;

    static GameArea game;
    boolean pressedLMB = false;
    boolean pressedRMB = false;
    static boolean pressedDownRMB;

    static int mouseX;
    static int mouseY;
    Point mousePosition;
    // robot is for moving mouse when it's out of screen bounds
    Robot robot;        
    DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
    DoubleBuffer y = BufferUtils.createDoubleBuffer(1);

    public void start() {
        thread = new Thread(this, "Game");
        thread.start();
    }

    private void init() {
        fullscreen = true;
        glfwInit();
        ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

        if (!fullscreen) {
            m = 3;
            screenWidth = 640 * 2;
            screenHeight = 440 * 2;
            glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);
            glfwWindowHint(GLFW_DECORATED, GL_TRUE);
            window = glfwCreateWindow(screenWidth, screenHeight, "Red Assault", NULL, NULL);
            glfwSetWindowPos(window, GLFWvidmode.width(vidmode) - screenWidth - 7, 30);
        } else {
            screenWidth = GLFWvidmode.width(vidmode);
            screenHeight = GLFWvidmode.height(vidmode);
            glfwWindowHint(GLFW_DECORATED, GL_FALSE);
            window = glfwCreateWindow(screenWidth, screenHeight, "Red Assault", NULL, NULL);
        }
        glfwSetKeyCallback(window, keyCallback = new Input());
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        glfwMakeContextCurrent(window);
        glfwShowWindow(window);
        GLContext.createFromCurrent();

        glDepthFunc(GL_NEVER);
        glActiveTexture(GL_TEXTURE1);
        Shader.loadAll();
        glfwSwapInterval(1);

        // Set coordinate system and projection matrix
        glOrtho(0, screenWidth, screenHeight, 0, -1, 1);
        Matrix4f pr_matrix = Matrix4f.orthographic(0, screenWidth, screenHeight, 0, -1.0f, 1.0f);
        Shader.DEFAULT.setUniformMat4f("pr_matrix", pr_matrix);
        Shader.DEFAULT.setUniform1i("tex", 1);
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
        game = new GameArea();
    }

    public void run() {
        init();

        long lastTime = System.nanoTime();
        double delta = 0.0;
        long timer = System.currentTimeMillis();
        int frames = 0;

        while (glfwWindowShouldClose(window) == GL_FALSE) {
            long now = System.nanoTime();
            delta += (now - lastTime) / CYCLE_TIME;
            lastTime = now;

            if (delta >= 1.0) {
                glfwPollEvents();
                getMouseCoordinates();
                update(mouseX, mouseY);
                RedAssault.timer++;
                delta--;
            }
            render();
            frames++;

            // Calculate the fps and print it out
            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println(frames + " fps");
                frames = 0;
            }
        }
        keyCallback.release();
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    /* 
     * Receives mouse coordinates from GLFW. Also move cursor if out of screen bounds.
     */
    private void getMouseCoordinates() {
        x = BufferUtils.createDoubleBuffer(1);
        y = BufferUtils.createDoubleBuffer(1);
        glfwGetCursorPos(window, x, y);
        mouseX = (int) x.get();
        mouseY = (int) y.get();

        // If user has a multi-monitor setup, then trap mouse on the primary monitor in fullscreen
        if (fullscreen) {
            mousePosition = MouseInfo.getPointerInfo().getLocation();
            if (mousePosition.x < 0) {
                robot.mouseMove(0, mousePosition.y);
            }
            if (mousePosition.x >= screenWidth) {
                robot.mouseMove(screenWidth - 1, mousePosition.y);
            }
            if (mousePosition.y < 0) {
                robot.mouseMove(mousePosition.x, 0);
            }
            if (mousePosition.y >= screenHeight) {
                robot.mouseMove(mousePosition.x, screenHeight - 1);
            }
        }
    }

    /* 
     * Render screen by first clearing buffer and then calling the game rendering function.
     */
    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        game.renderScreen();
        glfwSwapBuffers(window);
        glFlush();
        glFinish();
    }

    /* 
     * This function is called every cycle to update the game-state and pass the mouse coordinates
     * to the gameArea class.
     */
    private void update(int x, int y) {
        getMouseInput();
        game.handleLogic(x, y);
        game.updateGraphics();
    }

    /*
     * Receives mouse input data from GLFW and modifies the following global booleans:
     * pressedRMB, pressedLMB and pressedDownRMB
     */
    private void getMouseInput() {
        GameArea.mouseButton = 0;

        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS) {
            pressedDownRMB = true;
        } else {
            pressedDownRMB = false;
        }
        // left mouse button sends signal to game logic if it's clicked
        if (!pressedLMB) {
            if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS) {
                pressedLMB = true;
                game.mouseLogic(1);
            }
        }
        // right mouse button sends signal to game logic if it's pressed
        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_2) == GLFW_PRESS) {
            pressedRMB = true;
            game.mouseLogic(3);
        }
        if (pressedLMB && glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_RELEASE) {
            pressedLMB = false;
        }
        if (pressedRMB && glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_2) == GLFW_RELEASE) {
            pressedRMB = false;
        }
    }

    public static void main(String[] args) {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        new RedAssault().start();
    }
}
