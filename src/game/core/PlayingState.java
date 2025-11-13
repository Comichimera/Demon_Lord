package game.core;

import game.audio.AudioSystem;
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

        events = new EventBus();
        objectives = new ObjectiveManager();
        enemies = new EnemyManager(game.mapData, game.player, events);
        game.renderer.setEnemyManager(enemies);
        game.renderer.initEnemySpritesForMap(game.mapData.getSourcePath());

        String musicId = game.levelManager.getCurrentLevel().musicId;
        if (musicId != null) AudioSystem.playMusic(musicId);

        events.subscribe(e -> {
            switch (e.type) {
                case ENTER_TILE -> {
                    var t = game.mapData.getTile(e.y, e.x);
                    String cat = (t != null && t.getDefinition()!=null) ? t.getDefinition().getCategory() : "tile";
                    // Try category-specific first, else default:
                    String sfxId = switch (cat) {
                        case "floor" -> "footstep_floor";
                        case "door"  -> "footstep_door";
                        default -> "footstep";
                    };
                    AudioSystem.playSfx(sfxId);
                }
                case ENEMY_SPOTTED_PLAYER -> AudioSystem.playSfx("enemy_alert");
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

        objectives.attach(ctx());
        events.subscribe(objectives);

        events.post(GameEvent.simple(GameEventType.LEVEL_START));

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

        AudioSystem.setListener(game.player.getX(), game.player.getY(), game.player.getZ(), game.player.getYaw());

        // Post ENTER_TILE when player changes grid cell
        int tx = (int)(game.player.getX() / GameConfig.TILE_SIZE);
        int ty = (int)(game.player.getZ() / GameConfig.TILE_SIZE);
        if (tx != lastTx || ty != lastTy) {
            lastTx = tx; lastTy = ty;
            events.post(new GameEvent(GameEventType.ENTER_TILE, tx, ty));
        }

        if (objectives != null) {
            for (String msg : objectives.drainCompletedPopups()) {
                timer.pushPopup(msg, 2.5f);
            }
        }

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
