package game.core;

import game.achievements.AchievementsManager;
import game.audio.AudioSystem;
import game.config.GameConfig;
import game.config.GameData;
import game.config.Settings;
import game.core.state.MainMenuState;
import game.core.state.SettingsState;
import game.graphics.Renderer;
import game.input.Input;
import game.input.KeyboardInput;
import game.inventory.InventoryEntry;
import game.logic.objective.ICondition;
import game.logic.objective.IConditionFactory;
import game.logic.objective.Registries;
import game.logic.TimerService;
import game.logic.objective.cond.ReachCell;
import game.map.*;
import game.ui.VersionOverlay;
import game.util.Log;
import game.world.Player;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;


public class Game {
    public boolean running;
    public Window window;
    public Renderer renderer;
    public Player player;
    private KeyboardInput keyboard;
    public MapData mapData;
    public LevelManager levelManager;
    private IGameState currentState;

    private final Deque<IGameState> stateStack = new ArrayDeque<>();

    public final TimerService timers = new TimerService();
    public float fps;
    public float renderAlpha;

    private VersionOverlay versionOverlay;
    String formatTimeNoDecimal(double time) {
        int minutes = (int)(time / 60);
        int seconds = (int)(time % 60);
        return String.format("%d:%02d", minutes, seconds);
    }

    public void changeState(IGameState newState) {
        if (currentState != null) currentState.exit();
        while (!stateStack.isEmpty()) {
            stateStack.pop().exit();
        }
        currentState = newState;
        currentState.enter();
    }

    public void pushState(IGameState newState) {
        stateStack.push(currentState);
        currentState = newState;
        currentState.enter();
    }

    public void popState() {
        currentState.exit();
        currentState = stateStack.pop();
    }

    public void run() {
        init();
        loop();
        cleanup();
    }
    private void init() {
        Settings.load();
        window       = new Window(1280, 720, "Demon_Lord Pre Alpha");

        levelManager = new LevelManager( GameConfig.path("levels","/data/map/levels.json"));
        versionOverlay = new VersionOverlay(window, GameData.getVersion());

        if (!AudioSystem.init()) {
            Log.warn("Audio init failed; continuing without sound.");
        }

        SettingsState.applyAll(window);

        Map<Character, TileDefinition> baseDefs = TileDefinitionLoader.loadDefinitions();
        TileRegistry.initialize(baseDefs);

        Registries.CONDITIONS.put("reach", new IConditionFactory() {
            @Override public String id(){ return "reach"; }
            @Override public ICondition create(JSONObject cfg){
                return new ReachCell(cfg.getInt("x"), cfg.getInt("y"));
            }
        });

        player   = new Player();
        keyboard = new KeyboardInput();

        AchievementsManager.get().printStartupSummary();
        AchievementsManager.get().unlock("open_game");

        changeState(new MainMenuState(this));

        running = true;
    }
    private void loop() {
        long lastTime = System.nanoTime();
        float accumulator = 0f;
        while (running && !window.shouldClose()) {
            long now = System.nanoTime();
            float frameTime = (now - lastTime) / 1_000_000_000f;
            frameTime = Math.min(frameTime, 0.1f);
            lastTime = now;
            if (frameTime > 0f) fps = 1f / frameTime;

            accumulator += frameTime;
            while (accumulator >= GameConfig.TICK_LENGTH) {
                player.savePrevious();
                Input.update(window.getWindowHandle());
                currentState.update(GameConfig.TICK_LENGTH);
                accumulator -= GameConfig.TICK_LENGTH;
            }
            renderAlpha = accumulator / GameConfig.TICK_LENGTH;

            AudioSystem.update(frameTime);
            render();
            window.update();
        }
    }

    private List<InventoryEntry> levelStartItems = List.of();

    public void markLevelStart() {
        levelStartItems = player.getInventory().snapshot();
    }

    public void reloadLevel() {
        String mapPath = levelManager.getCurrentLevel().mapPath;
        mapData = MapLoader.loadMap(mapPath);

        if (renderer != null) renderer.cleanup();
        try {
            renderer = new Renderer(mapData);
        } catch (Exception e) {
            Log.error("Couldn't create the renderer", e);
            System.exit(-1);
        }

        float ts = GameConfig.TILE_SIZE;
        int sx = mapData.getPlayerSpawnX();
        int sy = mapData.getPlayerSpawnY();
        player.setPosition(sx * ts + ts / 2f, sy * ts + ts / 2f);
        player.setYaw(mapData.getCameraYaw());
        player.resetHealth();
        player.getInventory().restore(levelStartItems);
    }
    private void render() {
        if (window.getWidth() == 0 || window.getHeight() == 0) return;
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        currentState.render();
        if (versionOverlay != null) versionOverlay.render();

    }
    public void cleanup() {
        AudioSystem.shutdown();
        if (renderer != null) renderer.cleanup();
        if (versionOverlay != null) versionOverlay.cleanup();
        if (window != null) window.destroy();
    }
}