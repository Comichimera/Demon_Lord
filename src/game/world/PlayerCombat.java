package game.world;

import game.config.GameConfig;
import game.core.Game;
import game.input.EAction;
import game.input.Input;
import game.logic.event.EGameEventType;
import game.logic.event.EventBus;
import game.logic.event.GameEvent;
import game.world.enemy.BaseEnemy;
import game.world.enemy.EnemyManager;
import game.world.enemy.IEnemy;

public class PlayerCombat {

    private boolean attackPressed    = false;
    private float   playerAttackCooldown = 0f;
    private static final float PLAYER_ATTACK_RATE = 0.5f;

    private final Game game;
    private final EnemyManager enemies;
    private final EventBus events;

    public PlayerCombat(Game game, EnemyManager enemies, EventBus events) {
        this.game = game;
        this.enemies = enemies;
        this.events = events;
    }

    public void update(float dt) {
        playerAttackCooldown -= dt;
        boolean attackDown = Input.isDown(EAction.ATTACK);
        if (attackDown && !attackPressed && playerAttackCooldown <= 0f && game.player.isAlive()) {
            attackPressed = true;
            playerAttackCooldown = PLAYER_ATTACK_RATE / game.player.getAttackSpeedMultiplier();
            float px = game.player.getX(), pz = game.player.getZ();
            float attackRange = 1.5f * GameConfig.TILE_SIZE;
            int finalDamage = Math.max(1, (int)(game.player.getAttackDamage() * game.player.getDamageMultiplier()));
            for (IEnemy e : enemies.getEnemies()) {
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
                        events.post(new GameEvent(EGameEventType.ENEMY_KILLED, etx, ety));
                    }
                }
            }
        } else if (!attackDown) {
            attackPressed = false;
        }
    }
}
