package game.achievements;

import game.config.GameConfig;
import game.util.Log;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashMap;

public final class AchievementsManager {
    private static AchievementsManager INSTANCE;
    private final LinkedHashMap<String, Achievement> all = new LinkedHashMap<>();
    private final Path saveFile = Paths.get("achievements.json");

    private AchievementsManager() {
        register(new Achievement(
                "open_game", "God Gamer",
                "You opened the game.",
                GameConfig.path("achievement.locked","/data/assets/achievements/locked_achievement.png"),
                GameConfig.path("achievement.unlocked","/data/assets/achievements/unlocked_achievement_test.jpg")
        ));
        register(new Achievement(
                "go_outside", "God Human",
                "You quit the game.",
                GameConfig.path("achivement.locked","/data/assets/achievements/locked_achievement.png"),
                GameConfig.path("achievement.unlocked","/data/assets/achievements/unlocked_achievement_test.jpg")
        ));
        load();
    }

    public static AchievementsManager get() {
        if (INSTANCE == null) INSTANCE = new AchievementsManager();
        return INSTANCE;
    }

    public Collection<Achievement> list() { return all.values(); }

    public void unlock(String id) {
        Achievement a = all.get(id);
        if (a != null && !a.unlocked) {
            a.unlocked = true;
            a.unlockedAt = System.currentTimeMillis();
            save();
        }
    }

    public void printStartupSummary() {
        Log.info("=== Achievements at startup ===");
        for (Achievement a : list()) {
            Log.info(String.format("%s %-18s %-28s %s",
                    a.unlocked ? "✓" : "✗",
                    a.id,
                    a.title,
                    a.unlocked ? "Unlocked" : "Locked"));
        }
    }

    private void register(Achievement a) { all.put(a.id, a); }

    private void load() {
        if (!Files.exists(saveFile)) return;
        try {
            String s = Files.readString(saveFile);
            JSONObject root = new JSONObject(s);
            JSONObject unlocked = root.optJSONObject("unlocked");
            if (unlocked != null) {
                for (String id : unlocked.keySet()) {
                    Achievement a = all.get(id);
                    if (a != null) {
                        a.unlocked = true;
                        a.unlockedAt = unlocked.optLong(id, System.currentTimeMillis());
                    }
                }
            }
        } catch (Exception e) {
            Log.warn("Couldn't read achievements save, starting fresh", e);
        }
    }

    private void save() {
        try {
            JSONObject unlocked = new JSONObject();
            for (Achievement a : all.values()) {
                if (a.unlocked) unlocked.put(a.id, a.unlockedAt);
            }
            JSONObject root = new JSONObject().put("unlocked", unlocked);
            Files.writeString(saveFile, root.toString());
        } catch (IOException e) {
            Log.warn("Couldn't save achievements", e);
        }
    }
}
