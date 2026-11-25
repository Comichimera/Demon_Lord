package game.core;

import game.config.GameConfig;
import game.config.Settings;
import game.ui.UiScale;
import game.util.Log;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private final long window;
    private int width;
    private int height;

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

        try (var stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            try (var is = getClass().getResourceAsStream(GameConfig.path("logo","/data/assets/Logo_TransBG.png"))) {
                if (is == null) {
                    Log.warn("Icon not found in resources");
                } else {
                    byte[] data = is.readAllBytes();
                    ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
                    buffer.put(data);
                    buffer.flip();

                    ByteBuffer image = STBImage.stbi_load_from_memory(buffer, w, h, comp, 4);

                    if (image != null) {
                        GLFWImage.Buffer icon = GLFWImage.malloc(1);
                        icon.width(w.get(0));
                        icon.height(h.get(0));
                        icon.pixels(image);

                        glfwSetWindowIcon(window, icon);

                        icon.free();
                        STBImage.stbi_image_free(image);
                    } else {
                        Log.warn("Failed to decode icon image");
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glfwSwapInterval(1);
        UiScale.update(height);
        glfwSetWindowSizeCallback(window, (win, w, h) -> { this.width = w; this.height = h; UiScale.update(h); });
        glfwSetFramebufferSizeCallback(window, (win, w, h) -> glViewport(0, 0, w, h));
        glfwShowWindow(window);
    }

    public void setVsync(boolean on) {
        glfwSwapInterval(on ? 1 : 0);
    }

    public void applyWindowMode() {
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode mode = glfwGetVideoMode(monitor);
        if (mode == null) return;
        if (Settings.fullscreen) {
            glfwSetWindowMonitor(window, monitor, 0, 0, mode.width(), mode.height(), mode.refreshRate());
        } else {
            int[] size = Settings.WINDOW_SIZES[Settings.windowSize];
            int x = (mode.width() - size[0]) / 2;
            int y = (mode.height() - size[1]) / 2;
            glfwSetWindowMonitor(window, NULL, x, y, size[0], size[1], GLFW_DONT_CARE);
        }
    }

    public int getWidth()  { return width;  }
    public int getHeight() { return height; }

    public boolean shouldClose()   { return glfwWindowShouldClose(window); }
    public void    update()        { glfwSwapBuffers(window); glfwPollEvents(); }
    public void    destroy()       { glfwDestroyWindow(window); glfwTerminate(); }
    public long    getWindowHandle(){ return window; }
}