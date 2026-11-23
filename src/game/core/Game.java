package game.core;

import game.config.GameConfig;
import game.config.GameData;
import game.core.state.MainMenuState;
import game.graphics.Renderer;
import game.input.InputEdge;
import game.input.InputGate;
import game.input.KeyboardInput;
import game.logic.objective.ICondition;
import game.logic.objective.IConditionFactory;
import game.logic.objective.Registries;
import game.logic.TimerService;
import game.logic.objective.cond.ReachCell;
import game.map.*;
import game.ui.VersionOverlay;
import game.world.Player;
import org.json.JSONObject;

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

    public final TimerService timers = new TimerService();

    private VersionOverlay versionOverlay;
    String formatTimeNoDecimal(double time) {
        int minutes = (int)(time / 60);
        int seconds = (int)(time % 60);
        return String.format("%d:%02d", minutes, seconds);
    }

    public void changeState(IGameState newState) {
        if (currentState != null) currentState.exit();
        currentState = newState;
        currentState.enter();

        long win = window.getWindowHandle();
        InputEdge.onStateEnter(win);
        InputGate.arm();
    }
    public void run() {
        init();
        loop();
        cleanup();
    }
    private void init() {
        window       = new Window(800, 600, "Demon_Lord Pre Alpha");

        levelManager = new LevelManager( GameConfig.path("levels","/data/map/levels.json"));
        versionOverlay = new VersionOverlay(window, GameData.getVersion());

        if (!game.audio.AudioSystem.init()) {
            System.err.println("Audio init failed; continuing without sound.");
        }

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
    public void reloadLevel() {
        String mapPath = levelManager.getCurrentLevel().mapPath;
        mapData = MapLoader.loadMap(mapPath);

        if (renderer != null) renderer.cleanup();
        try {
            renderer = new Renderer(mapData);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        float ts = GameConfig.TILE_SIZE;
        int sx = mapData.getPlayerSpawnX();
        int sy = mapData.getPlayerSpawnY();
        player.setPosition(sx * ts + ts / 2f, sy * ts + ts / 2f);
        player.setYaw(mapData.getCameraYaw());
        player.resetHealth();
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