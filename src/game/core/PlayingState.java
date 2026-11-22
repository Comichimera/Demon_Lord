package game.core;

import game.audio.AudioSystem;
import game.config.GameConfig;
import game.debug.DebugConfig;
import game.debug.DebugOverlay;
import game.input.KeyboardInput;
import game.inventory.InventoryEntry;
import game.inventory.ItemDefinition;
import game.inventory.ItemEffect;
import game.logic.*;
import game.map.Tile;
import game.ui.HpOverlay;
import game.ui.InventoryOverlay;
import game.ui.StatusOverlay;
import game.ui.TimerOverlay;
import game.world.enemy.BaseEnemy;
import game.world.enemy.Enemy;
import game.world.enemy.EnemyManager;
import org.json.JSONObject;

import static org.lwjgl.glfw.GLFW.*;

public class PlayingState implements IGameState {
    private final Game game;
    private final KeyboardInput keyboard;
    private final TimerOverlay timer;
    private boolean interactPressed  = false;
    private boolean attackPressed    = false;
    private boolean useItemPressed   = false;
    private boolean tabPressed       = false;
    private float   playerAttackCooldown = 0f;
    private static final float PLAYER_ATTACK_RATE = 0.5f;

    private HpOverlay hpOverlay;
    private StatusOverlay statusOverlay;
    private DebugOverlay debugOverlay;
    private boolean f3Pressed = false;
    private float fps = 0f;

    private EventBus events;
    private ObjectiveManager objectives;

    private EnemyManager enemies;
    private InventoryOverlay inventoryOverlay;

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
        game.renderer.initEnemySpritesForMap(game.mapData.getSourcePath());

        String musicId = game.levelManager.getCurrentLevel().musicId;
        if (musicId != null) AudioSystem.playMusic(musicId);

        inventoryOverlay = new InventoryOverlay(game.window);
        inventoryOverlay.rebuild(game.player.getInventory());

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

        if (DebugConfig.gameplayTogglable) {
            int f3 = glfwGetKey(game.window.getWindowHandle(), GLFW_KEY_F3);
            if (f3 == GLFW_PRESS && !f3Pressed) {
                f3Pressed = true;
                DebugConfig.overlayVisible = !DebugConfig.overlayVisible;
            } else if (f3 == GLFW_RELEASE) { f3Pressed = false; }
        }

        debugOverlay.update(game.player, enemies, game.mapData, fps, dt);

        keyboard.processInput(game.player, dt, game.mapData);

        AudioSystem.setListener(game.player.getX(), game.player.getY(), game.player.getZ(), game.player.getYaw());

        playerAttackCooldown -= dt;
        int fKeyState = glfwGetKey(game.window.getWindowHandle(), GLFW_KEY_F);
        if (fKeyState == GLFW_PRESS && !attackPressed && playerAttackCooldown <= 0f && game.player.isAlive()) {
            attackPressed = true;
            playerAttackCooldown = PLAYER_ATTACK_RATE / game.player.getAttackSpeedMultiplier();
            float px = game.player.getX(), pz = game.player.getZ();
            float attackRange = 1.5f * GameConfig.TILE_SIZE;
            int finalDamage = Math.max(1, (int)(game.player.getAttackDamage() * game.player.getDamageMultiplier()));
            for (Enemy e : enemies.getEnemies()) {
                if (!(e instanceof BaseEnemy)) continue;
                BaseEnemy be = (BaseEnemy) e;
                if (!be.isAlive()) continue;
                float dx = e.getX() - px, dz = e.getZ() - pz;
                if (dx * dx + dz * dz > attackRange * attackRange) continue;
                float toAngle = (float) Math.toDegrees(Math.atan2(dx, -dz));
                float delta = Math.abs(((toAngle - game.player.getYaw() + 540f) % 360f) - 180f);
                if (delta <= 50f) {
                    be.takeDamage(finalDamage);
                    if (!be.isAlive()) {
                        int etx = (int)(e.getX() / GameConfig.TILE_SIZE);
                        int ety = (int)(e.getZ() / GameConfig.TILE_SIZE);
                        events.post(new GameEvent(GameEventType.ENEMY_KILLED, etx, ety));
                    }
                }
            }
        } else if (fKeyState == GLFW_RELEASE) {
            attackPressed = false;
        }

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

        if (ty >= 0 && ty < game.mapData.getHeight()
                && tx >= 0 && tx < game.mapData.getWidth()) {

            Tile current = game.mapData.getTile(ty, tx);

            if (current.hasItem()) {
                String itemId = current.getItemId();

                boolean added = game.player.getInventory().addItem(itemId, 1);
                if (added) {
                    current.clearItem();
                    System.out.println("Picked up item '" + itemId + "'");

                    if (inventoryOverlay != null) {
                        inventoryOverlay.rebuild(game.player.getInventory());
                    }

                    game.renderer.updateFloorGeometry();
                } else {
                    System.out.println("Inventory full, could not pick up '" + itemId + "'");
                }
            }
        }

        int interactState = glfwGetKey(game.window.getWindowHandle(), GLFW_KEY_E);
        if (interactState == GLFW_PRESS && !interactPressed) {
            interactPressed = true;

            int col = tx;
            int row = ty;
            int[][] offs = {{-1,0},{1,0},{0,-1},{0,1}};

            for (int[] off : offs) {
                int r = row + off[0], c = col + off[1];
                if (r >= 0 && r < game.mapData.getHeight() && c >= 0 && c < game.mapData.getWidth()) {
                    Tile t = game.mapData.getTile(r, c);
                    if (!t.isOpenable()) continue;

                    if (t.isLocked()) {
                        String usedKey = null;
                        for (String keyId : t.getLockedByKeys()) {
                            if (game.player.getInventory().hasItem(keyId)) {
                                usedKey = keyId;
                                break;
                            }
                        }
                        if (usedKey != null) {
                            game.player.getInventory().removeItem(usedKey, 1);
                            t.setLocked(false);
                            t.setOpen(true);
                            t.setWalkable(true);
                            game.renderer.updateFloorGeometry();
                            inventoryOverlay.rebuild(game.player.getInventory());
                            timer.pushPopup("Door unlocked!", 1.5f);
                        } else {
                            timer.pushPopup("Locked", 1.5f);
                        }
                    } else {
                        t.setOpen(!t.isOpen());
                        t.setWalkable(t.isOpen());
                        game.renderer.updateFloorGeometry();
                    }
                    break;
                }
            }
        } else if (interactState == GLFW_RELEASE) {
            interactPressed = false;
        }

        int tabState = glfwGetKey(game.window.getWindowHandle(), GLFW_KEY_TAB);
        if (tabState == GLFW_PRESS && !tabPressed) {
            tabPressed = true;
            game.player.getInventory().selectNext();
            inventoryOverlay.rebuild(game.player.getInventory());
        } else if (tabState == GLFW_RELEASE) {
            tabPressed = false;
        }

        int gKeyState = glfwGetKey(game.window.getWindowHandle(), GLFW_KEY_G);
        if (gKeyState == GLFW_PRESS && !useItemPressed && game.player.isAlive()) {
            useItemPressed = true;
            InventoryEntry selected = game.player.getInventory().getSelectedEntry();
            if (selected != null) {
                ItemDefinition def = selected.getDefinition();
                if (!def.getEffects().isEmpty()) {
                    for (ItemEffect effect : def.getEffects()) {
                        effect.apply(game.player);
                    }
                    game.player.getInventory().removeItem(def.getId(), 1);
                    inventoryOverlay.rebuild(game.player.getInventory());
                    timer.pushPopup("Used: " + def.getName(), 1.5f);
                }
            }
        } else if (gKeyState == GLFW_RELEASE) {
            useItemPressed = false;
        }

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

        if (enemies != null) {
            enemies.update(dt, game.mapData, game.player);
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
        events.post(GameEvent.simple(GameEventType.LEVEL_END));
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

    private RuntimeContext ctx() {
        return new RuntimeContext() {
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
