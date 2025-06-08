package game.core;

import game.config.GameConfig;
import game.graphics.Renderer;
import game.input.KeyboardInput;
import game.map.*;
import game.world.Player;

import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
public class Game {
    boolean running;
    Window window;
    Renderer renderer;
    Player player;
    private KeyboardInput keyboard;
    MapData mapData;
    LevelManager levelManager;

    // Timer UI and end-of-level overlay
    double elapsedTime = 0;
    double totalTime = 0;
    private IGameState currentState;

    /**
     * Formats a time value into mm:ss format with no decimal.
     */
    String formatTimeNoDecimal(double time) {
        int minutes = (int)(time / 60);
        int seconds = (int)(time % 60);
        return String.format("%d:%02d", minutes, seconds);
    }

    void changeState(IGameState newState) {
        if (currentState != null) currentState.exit();
        currentState = newState;
        currentState.enter();
    }


    /**
     * Entry point for running the game.
     */
    public void run() {
        init();
        loop();
        cleanup();
    }

    /**
     * Initializes the game window, player, menus, levels, tile definitions, and UI overlays.
     */
    private void init() {
        window       = new Window(800, 600, "pre alpha");
        levelManager = new LevelManager("/data/map/levels.json");

        // Initialize tile definitions
        Map<Character, TileDefinition> baseDefs = TileDefinitionLoader.loadDefinitions();
        TileRegistry.initialize(baseDefs);

        player   = new Player();
        keyboard = new KeyboardInput();

        changeState(new MainMenuState(this));

        running = true;
    }

    /**
     * Main game loop. Processes input, updates state, handles rendering and UI logic.
     */
    private void loop() {
        long lastTime = System.nanoTime();
        while (running && !window.shouldClose()) {
            long now = System.nanoTime();
            float deltaTime = (now - lastTime) / 1_000_000_000f;
            lastTime = now;

            currentState.update(deltaTime);
            render();
            window.update();
        }
    }

    /**
     * Loads current level data, resets player, timers, and creates the renderer.
     */
    void reloadLevel() {
        String mapPath = levelManager.getCurrentLevel().mapPath;
        TileDefinitionLoader.loadAndMergeMapDefinitions(mapPath);
        mapData = MapLoader.loadMap(mapPath);

        if (renderer != null) renderer.cleanup();
        try {
            renderer = new Renderer(mapData);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        elapsedTime = 0;

        // Reset player position based on map config
        float ts = GameConfig.TILE_SIZE;
        int sx = mapData.getPlayerSpawnX();
        int sy = mapData.getPlayerSpawnY();
        player.setPosition(sx * ts + ts / 2f, sy * ts + ts / 2f);
        player.setYaw(mapData.getCameraYaw());
    }
    private void render() {
        // clear happens inside each state's render if needed, or you can do it here:
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        currentState.render();
    }

    /**
     * Releases GL and window resources.
     */
    public void cleanup() {
        if (renderer != null) renderer.cleanup();
        if (window != null) window.destroy();
    }
}