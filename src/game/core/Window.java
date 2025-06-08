package game.core;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Window {
    private final long window;
    private final int width;
    private final int height;

    public Window(int width, int height, String title) {
        this.width  = width;
        this.height = height;
        if (!glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }
        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create window.");
        }
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glfwSwapInterval(1);
        glfwShowWindow(window);
    }

    public int getWidth()  { return width;  }
    public int getHeight() { return height; }

    public boolean shouldClose()   { return glfwWindowShouldClose(window); }
    public void    update()        { glfwSwapBuffers(window); glfwPollEvents(); }
    public void    destroy()       { glfwDestroyWindow(window); glfwTerminate(); }
    public long    getWindowHandle(){ return window; }
}