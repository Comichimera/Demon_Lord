package game.debug;

import java.util.EnumSet;
import java.util.Set;

public class DebugConfig {
    public static boolean gameplayTogglable = false;
    public static boolean overlayVisible    = false;

    public static final Set<DebugValue> enabled = EnumSet.of(
            DebugValue.POSITION,
            DebugValue.GRID_TILE,
            DebugValue.HEALTH_STATUS,
            DebugValue.DAMAGE_ACC,
            DebugValue.INJURIES_ACC,
            DebugValue.BLOODLOSS,
            DebugValue.BLEED_RATE,
            DebugValue.FPS
    );
}
