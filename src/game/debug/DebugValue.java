package game.debug;

public enum DebugValue {
    POSITION         ("Position"),
    YAW              ("Yaw"),
    GRID_TILE        ("Grid Tile"),
    SPEED_MULT       ("Speed Multiplier"),
    ATTACK_SPEED_MULT("Attack Speed Multiplier"),
    DAMAGE_MULT      ("Damage Dealt Multiplier"),
    HEALTH_STATUS    ("Health Status"),
    DAMAGE_ACC       ("Damage Accumulation"),
    INJURIES_ACC     ("Injuries"),
    BLOODLOSS        ("Bloodloss"),
    BLEED_RATE       ("Bleed Rate"),
    HP_CLASSIC       ("HP (Classic Mode)"),
    ENEMY_COUNT      ("Enemy Count"),
    NEAREST_ENEMY    ("Nearest Enemy"),
    PLAYER_SPOTTED   ("Player Spotted"),
    FACING_TILE      ("Facing Tile"),
    FPS              ("FPS"),
    FRAME_TIME       ("Frame Time");

    public final String label;
    DebugValue(String label) { this.label = label; }
}
