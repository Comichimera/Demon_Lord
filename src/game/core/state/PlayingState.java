package game.core.state;

import game.audio.AudioSystem;
import game.config.GameConfig;
import game.core.Game;
import game.core.IGameState;
import game.debug.DebugConfig;
import game.debug.DebugOverlay;
import game.input.InputEdge;
import game.input.KeyboardInput;
import game.inventory.InventoryController;
import game.logic.event.EventBus;
import game.logic.event.GameEvent;
import game.logic.event.EGameEventType;
import game.logic.objective.*;
import game.map.Tile;
import game.ui.HpOverlay;
import game.ui.InventoryOverlay;
import game.ui.StatusOverlay;
import game.ui.TimerOverlay;
import game.world.TileInteraction;
import game.world.PlayerCombat;
import game.world.enemy.EnemyManager;
import org.json.JSONObject;

import static org.lwjgl.glfw.GLFW.*;

public class PlayingState implements IGameState {
    private final Game game;
    private final KeyboardInput keyboard;
    private final TimerOverlay timer;

    private HpOverlay hpOverlay;
    private StatusOverlay statusOverlay;
    private DebugOverlay debugOverlay;
    private float fps = 0f;

    private EventBus events;
    private ObjectiveManager objectives;

    private EnemyManager enemies;
    private InventoryOverlay inventoryOverlay;
    private PlayerCombat combat;
    private TileInteraction interaction;
    private InventoryController inventory;

    private int lastTx = -1, lastTy = -1;

    public PlayingState(Game game) {
        this.game = game;
        this.keyboard = new KeyboardInput();
        this.timer    = new TimerOverlay(game.window);
    }

    @Override
    public void enter() {
        game.reloadLevel();
        game.timers.startLevel();
        timer.update(0);

        events = new EventBus();
        objectives = new ObjectiveManager();
        enemies = new EnemyManager(game.mapData, game.player, events);
        game.renderer.setEnemyManager(enemies);
        combat = new PlayerCombat(game, enemies, events);
        game.renderer.initEnemySpritesForMap(game.mapData.getSourcePath());

        String musicId = game.levelManager.getCurrentLevel().musicId;
        if (musicId != null) AudioSystem.playMusic(musicId);

        inventoryOverlay = new InventoryOverlay(game.window);
        inventoryOverlay.rebuild(game.player.getInventory());
        interaction = new TileInteraction(game, inventoryOverlay, timer);
        inventory = new InventoryController(game, inventoryOverlay, timer);

        if (GameConfig.healthSystem == GameConfig.HealthSystem.CLASSIC) {
            hpOverlay = new HpOverlay(game.window);
        } else {
            statusOverlay = new StatusOverlay(game.window);
        }
        debugOverlay = new DebugOverlay(game.window);


        events.subscribe(e -> {
            switch (e.type) {
                case ENTER_TILE -> {
                    var t = game.mapData.getTile(e.y, e.x);
                    String cat = (t != null && t.getDefinition()!=null) ? t.getDefinition().getCategory() : "tile";
                    String sfxId = switch (cat) {
                        case "floor" -> "footstep_floor";
                        case "door"  -> "footstep_door";
                        default -> "footstep";
                    };
                    AudioSystem.playSfx(sfxId);
                }
                case ENEMY_SPOTTED_PLAYER -> AudioSystem.playSfx("enemy_alert");
                case PLAYER_TOOK_DAMAGE -> timer.pushPopup("You were hit!", 1.0f);
                case PLAYER_DEFEATED_BY_ENEMY -> AudioSystem.playSfx("player_down");
                case LEVEL_END -> AudioSystem.stopMusic();
                default -> {}
            }
        });

        if (game.mapData.getObjectiveSpecs() != null) {
            for (JSONObject spec : game.mapData.getObjectiveSpecs()) {
                String id = spec.optString("id", "objective");
                boolean mandatory = spec.optBoolean("mandatory", true);
                String ui = spec.optString("ui", id);

                JSONObject when = spec.getJSONObject("when");
                String type = when.getString("type");
                IConditionFactory f = Registries.CONDITIONS.get(type);
                if (f == null) {
                    System.err.println("Unknown objective type: " + type + " (skipping)");
                    continue;
                }
                ICondition cond = f.create(when);
                Objective obj = new Objective(id, mandatory, cond, ui);
                objectives.add(obj);
            }
        }

        objectives.attach(ctx());
        events.subscribe(objectives);

        events.post(GameEvent.simple(EGameEventType.LEVEL_START));

        lastTx = lastTy = -1;
    }

    @Override
    public void update(float dt) {
        if (enemies != null && enemies.isPlayerDefeated()) {
            game.changeState(new PlayerDefeatedState(game));
            return;
        }

        if (glfwGetKey(game.window.getWindowHandle(), GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            game.timers.pause();
            game.changeState(new MainMenuState(game));
            return;
        }

        fps = (dt > 0f) ? 1f / dt : fps;

        game.timers.tick(dt);
        timer.update(game.timers.levelSeconds());
        game.player.tickHealth(dt);

        handleDebugToggle();

        debugOverlay.update(game.player, enemies, game.mapData, fps, dt);

        keyboard.processInput(game.player, dt, game.mapData);

        AudioSystem.setListener(game.player.getX(), game.player.getY(), game.player.getZ(), game.player.getYaw());

        combat.update(dt);

        int tx = (int)(game.player.getX() / GameConfig.TILE_SIZE);
        int ty = (int)(game.player.getZ() / GameConfig.TILE_SIZE);
        if (tx != lastTx || ty != lastTy) {
            lastTx = tx; lastTy = ty;
            events.post(new GameEvent(EGameEventType.ENTER_TILE, tx, ty));
        }

        if (objectives != null) {
            for (String msg : objectives.drainCompletedPopups()) {
                timer.pushPopup(msg, 2.5f);
            }
        }

        inventory.handlePickups(ty, tx);

        interaction.update(tx, ty);

        inventory.handleKeys();

        checkLevelExit(ty, tx);

        if (enemies != null) {
            enemies.update(dt, game.mapData, game.player);
        }
    }

    private void handleDebugToggle() {
        if (DebugConfig.gameplayTogglable && InputEdge.pressedOnce(game.window.getWindowHandle(), GLFW_KEY_F3)) {
            DebugConfig.overlayVisible = !DebugConfig.overlayVisible;
        }
    }

    private void checkLevelExit(int ty, int tx) {
        if (ty >= 0 && ty < game.mapData.getHeight()
                && tx >= 0 && tx < game.mapData.getWidth()) {

            Tile t = game.mapData.getTile(ty, tx);
            if (t.isEndsLevel()) {
                boolean canFinish = (objectives == null) || objectives.allMandatoryComplete();
                if (canFinish) {
                    game.changeState(new LevelCompleteState(game));
                }
            }
        }
    }



    @Override
    public void render() {
        game.renderer.render(game.player, (float)game.timers.levelSeconds());

        if (timer != null) {
            timer.render();
        }

        if (inventoryOverlay != null) {
            inventoryOverlay.render();
        }

        if (hpOverlay != null) {
            hpOverlay.render(game.player);
        }

        if (statusOverlay != null) {
            statusOverlay.render(game.player);
        }

        debugOverlay.render();
    }

    @Override
    public void exit() {
        timer.cleanup();
        events.post(GameEvent.simple(EGameEventType.LEVEL_END));
        if (inventoryOverlay != null) {
            inventoryOverlay.cleanup();
            inventoryOverlay = null;
        }
        if (hpOverlay != null) {
            hpOverlay.cleanup();
            hpOverlay = null;
        }
        if (statusOverlay != null) {
            statusOverlay.cleanup();
            statusOverlay = null;
        }
        if (debugOverlay != null) {
            debugOverlay.cleanup();
            debugOverlay = null;
        }
    }

    private IRuntimeContext ctx() {
        return new IRuntimeContext() {
            @Override
            public Tile tileAt(int x, int y) {
                if (x < 0 || y < 0 || y >= game.mapData.getHeight() || x >= game.mapData.getWidth()) return null;
                return game.mapData.getTile(y, x);
            }
            @Override
            public float elapsedSeconds() {
                return (float) game.timers.levelSeconds();
            }
        };
    }
}
