package game.map;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    public static class Level {
        public final String id;
        public final String name;
        public final String mapPath;
        public final String target;

        public Level(String id, String name, String mapPath, String target) {
            this.id = id;
            this.name = name;
            this.mapPath = mapPath;
            this.target  = target;
        }
    }

    private List<Level> levels;
    private int currentLevelIndex;

    public LevelManager(String levelsFilePath) {
        levels = new ArrayList<>();
        currentLevelIndex = 0;
        loadLevels(levelsFilePath);
    }

    private void loadLevels(String path) {
        try {
            InputStream is = LevelManager.class.getResourceAsStream(path);
            if (is == null) {
                throw new RuntimeException("Levels file not found: " + path);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONObject json = new JSONObject(sb.toString());
            JSONArray levelsArray = json.getJSONArray("levels");
            for (int i = 0; i < levelsArray.length(); i++) {
                JSONObject obj = levelsArray.getJSONObject(i);
                String id = obj.getString("id");
                String name = obj.getString("name");
                String mapPath = obj.getString("mapPath");
                String target  = obj.optString("target", "");
                levels.add(new Level(id, name, mapPath, target));
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load levels", e);
        }
    }

    public Level getCurrentLevel() {
        if (levels.isEmpty()) return null;
        return levels.get(currentLevelIndex);
    }

    public int getCurrentIndex() {
        return currentLevelIndex;
    }

    public List<Level> getLevels() {
        return levels;
    }

    public boolean hasNextLevel() {
        return currentLevelIndex + 1 < levels.size();
    }

    public boolean advanceToNextLevel() {
        if (hasNextLevel()) {
            currentLevelIndex++;
            return true;
        }
        return false;
    }

    public boolean advanceToLevel(int index) {
        if (index >= 0 && index < levels.size()) {
            currentLevelIndex = index;
            return true;
        }
        return false;
    }

    public void reset() {
        currentLevelIndex = 0;
    }
}
