package game.core;

import game.config.GameConfig;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

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

        try (var stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            try (var is = getClass().getResourceAsStream(GameConfig.path("logo","/data/assets/Logo_TransBG.png"))) {
                if (is == null) {
                    System.err.println("Icon not found in resources");
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
                        System.err.println("Failed to decode icon image");
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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