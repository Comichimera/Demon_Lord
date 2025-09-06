package game.core;

import game.input.KeyboardInput;
import game.ui.TimerOverlay;
import game.world.Player;
import game.config.GameConfig;
import game.graphics.Renderer;
import game.map.MapData;
import game.map.Tile;

import game.logic.EventBus;
import game.logic.ObjectiveManager;
import game.logic.RuntimeContext;
import game.logic.GameEvent;
import game.logic.GameEventType;
import game.logic.Registries;
import game.logic.Condition;
import game.logic.ConditionFactory;
import game.world.enemy.EnemyManager;

import org.json.JSONObject;

import static org.lwjgl.glfw.GLFW.*;

public class PlayingState implements IGameState {
    private final Game game;
    private final KeyboardInput keyboard;
    private final TimerOverlay timer;
    private boolean interactPressed = false;

    private EventBus events;
    private ObjectiveManager objectives;

    private EnemyManager enemies;

    private int lastTx = -1, lastTy = -1;

    public PlayingState(Game game) {
        this.game = game;
        this.keyboard = new KeyboardInput();
        this.timer    = new TimerOverlay(game.window);
    }

    @Override
    public void enter() {
        game.reloadLevel();          // builds mapData for the current level
        game.timers.startLevel();
        timer.update(0);

        // Objective system setup (safe if a level has no objectives)
        events = new EventBus();
        objectives = new ObjectiveManager();
        enemies = new EnemyManager(game.mapData, events);
        game.renderer.setEnemyManager(enemies);
        game.renderer.initEnemySpritesForMap(game.mapData.getSourcePath());

        if (game.mapData.getObjectiveSpecs() != null) {
            for (JSONObject spec : game.mapData.getObjectiveSpecs()) {
                String id = spec.optString("id", "objective");
                boolean mandatory = spec.optBoolean("mandatory", true);
                String ui = spec.optString("ui", id);

                JSONObject when = spec.getJSONObject("when");
                String type = when.getString("type");
                ConditionFactory f = Registries.CONDITIONS.get(type);
                if (f == null) {
                    System.err.println("Unknown objective type: " + type + " (skipping)");
                    continue;
                }
                Condition cond = f.create(when);
                game.logic.Objective obj = new game.logic.Objective(id, mandatory, cond, ui);
                objectives.add(obj);
            }
        }

        // attach & subscribe
        objectives.attach(ctx());
        events.subscribe(objectives);

        // level start event
        events.post(GameEvent.simple(GameEventType.LEVEL_START));

        // reset cell tracker so first movement posts an event correctly
        lastTx = lastTy = -1;
    }

    @Override
    public void update(float dt) {
        if (glfwGetKey(game.window.getWindowHandle(), GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            game.timers.pause();
            game.changeState(new MainMenuState(game));
            return;
        }

        game.timers.tick(dt);
        timer.update(game.timers.levelSeconds());

        // Process movement and interactions:
        keyboard.processInput(game.player, dt, game.mapData);

        // Post ENTER_TILE when player changes grid cell
        int tx = (int)(game.player.getX() / GameConfig.TILE_SIZE);
        int ty = (int)(game.player.getZ() / GameConfig.TILE_SIZE);
        if (tx != lastTx || ty != lastTy) {
            lastTx = tx; lastTy = ty;
            events.post(new GameEvent(GameEventType.ENTER_TILE, tx, ty));
        }

        if (objectives != null) {
            for (String msg : objectives.drainCompletedPopups()) {
                // NEW: show a toast for ~2.5 seconds
                timer.pushPopup(msg, 2.5f);
            }
        }

        // Interaction key ("E") toggles adjacent openable tiles (e.g., doors)
        int interactState = glfwGetKey(game.window.getWindowHandle(), GLFW_KEY_E);
        if (interactState == GLFW_PRESS && !interactPressed) {
            interactPressed = true;

            int col = tx;
            int row = ty;
            int[][] offs = {{-1,0},{1,0},{0,-1},{0,1}};  // NSEW

            for (int[] off : offs) {
                int r = row + off[0], c = col + off[1];
                if (r >= 0 && r < game.mapData.getHeight() && c >= 0 && c < game.mapData.getWidth()) {
                    Tile t = game.mapData.getTile(r, c);
                    if (t.isOpenable()) {
                        t.setOpen(!t.isOpen());
                        t.setWalkable(t.isOpen());
                        game.renderer.updateFloorGeometry();
                        break;
                    }
                }
            }
        } else if (interactState == GLFW_RELEASE) {
            interactPressed = false;
        }

        // Check if player is standing on an exit tile
        if (ty >= 0 && ty < game.mapData.getHeight()
                && tx >= 0 && tx < game.mapData.getWidth()) {

            Tile t = game.mapData.getTile(ty, tx);
            if (t.isEndsLevel()) {
                boolean canFinish = (objectives == null) || objectives.allMandatoryComplete();
                if (canFinish) {
                    game.changeState(new LevelCompleteState(game));
                } else {
                }
            }
        }

        if (enemies != null) {
            enemies.update(dt, game.mapData, game.player);
        }
    }

    @Override
    public void render() {
        game.renderer.render(game.player);
        timer.render();

    }

    @Override
    public void exit() {
        timer.cleanup();
        events.post(GameEvent.simple(GameEventType.LEVEL_END));
    }

    // RuntimeContext adapter (lets conditions read minimal game state)
    private RuntimeContext ctx() {
        return new RuntimeContext() {
            @Override
            public Tile tileAt(int x, int y) {
                if (x < 0 || y < 0 || y >= game.mapData.getHeight() || x >= game.mapData.getWidth()) return null;
                return game.mapData.getTile(y, x); // note: MapData.getTile(row,col) expects (y,x)
            }
            @Override
            public float elapsedSeconds() {
                return (float) game.timers.levelSeconds();
            }
        };
    }
}
