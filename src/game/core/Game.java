package game.core;

import game.config.GameConfig;
import game.graphics.Renderer;
import game.input.InputEdge;
import game.input.KeyboardInput;
import game.map.*;
import game.world.Player;

import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import game.logic.Registries;
import game.logic.ConditionFactory;
import game.logic.Condition;
import game.logic.cond.ReachCell;
import org.json.JSONObject;

import game.logic.TimerService;
import game.ui.VersionOverlay;


public class Game {
    boolean running;
    Window window;
    Renderer renderer;
    Player player;
    private KeyboardInput keyboard;
    MapData mapData;
    LevelManager levelManager;
    private IGameState currentState;

    public final TimerService timers = new TimerService();

    private VersionOverlay versionOverlay;
    String formatTimeNoDecimal(double time) {
        int minutes = (int)(time / 60);
        int seconds = (int)(time % 60);
        return String.format("%d:%02d", minutes, seconds);
    }

    void changeState(IGameState newState) {
        if (currentState != null) currentState.exit();
        currentState = newState;
        currentState.enter();
        InputEdge.onStateEnter(window.getWindowHandle());
    }
    public void run() {
        init();
        loop();
        cleanup();
    }
    private void init() {
        window       = new Window(800, 600, "pre alpha");
        levelManager = new LevelManager("/data/map/levels.json");
        versionOverlay = new VersionOverlay(window, game.core.GameData.getVersion());

        if (!game.audio.AudioSystem.init()) {
            System.err.println("Audio init failed; continuing without sound.");
        }

        // Initialize tile definitions
        Map<Character, TileDefinition> baseDefs = TileDefinitionLoader.loadDefinitions();
        TileRegistry.initialize(baseDefs);

        Registries.CONDITIONS.put("reach", new ConditionFactory() {
            @Override public String id(){ return "reach"; }
            @Override public Condition create(JSONObject cfg){
                return new ReachCell(cfg.getInt("x"), cfg.getInt("y"));
            }
        });

        player   = new Player();
        keyboard = new KeyboardInput();

        game.achievements.AchievementsManager.get().printStartupSummary();
        game.achievements.AchievementsManager.get().unlock("open_game");

        changeState(new MainMenuState(this));

        running = true;
    }
    private void loop() {
        long lastTime = System.nanoTime();
        while (running && !window.shouldClose()) {
            long now = System.nanoTime();
            float deltaTime = (now - lastTime) / 1_000_000_000f;
            lastTime = now;

            currentState.update(deltaTime);
            game.audio.AudioSystem.update(deltaTime);
            render();
            window.update();
        }
    }
    void reloadLevel() {
        String mapPath = levelManager.getCurrentLevel().mapPath;
        mapData = MapLoader.loadMap(mapPath);

        if (renderer != null) renderer.cleanup();
        try {
            renderer = new Renderer(mapData);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // Reset player position based on map config
        float ts = GameConfig.TILE_SIZE;
        int sx = mapData.getPlayerSpawnX();
        int sy = mapData.getPlayerSpawnY();
        player.setPosition(sx * ts + ts / 2f, sy * ts + ts / 2f);
        player.setYaw(mapData.getCameraYaw());
    }
    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        currentState.render();
        if (versionOverlay != null) versionOverlay.render();

    }
    public void cleanup() {
        game.audio.AudioSystem.shutdown();
        if (renderer != null) renderer.cleanup();
        if (versionOverlay != null) versionOverlay.cleanup();
        if (window != null) window.destroy();
    }
}