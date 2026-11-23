package game.debug;

import java.util.EnumSet;
import java.util.Set;

public class DebugConfig {
    public static boolean gameplayTogglable = false;
    public static boolean overlayVisible    = false;

    public static final Set<EDebugValue> enabled = EnumSet.of(
            EDebugValue.POSITION,
            EDebugValue.GRID_TILE,
            EDebugValue.HEALTH_STATUS,
            EDebugValue.DAMAGE_ACC,
            EDebugValue.INJURIES_ACC,
            EDebugValue.BLOODLOSS,
            EDebugValue.BLEED_RATE,
            EDebugValue.FPS
    );
}
