package game.audio;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.util.Map;

public final class AudioSystemData {
    public static void loadFromJson(String path,
                                    Map<String, AudioSystem.SoundDef> sfxOut,
                                    Map<String, AudioSystem.MusicDef> musicOut) {
        try (InputStream in = AudioSystemData.class.getResourceAsStream(path)) {
            JSONObject root = new JSONObject(new JSONTokener(in));

            JSONObject sfx = root.optJSONObject("sfx");
            if (sfx != null) {
                for (String key : sfx.keySet()) {
                    JSONObject o = sfx.getJSONObject(key);
                    sfxOut.put(key, new AudioSystem.SoundDef(
                            o.getString("path"),
                            (float)o.optDouble("gain", 1.0),
                            (float)o.optDouble("pitch", 1.0),
                            o.optBoolean("loop", false)
                    ));
                }
            }

            JSONObject music = root.optJSONObject("music");
            if (music != null) {
                for (String key : music.keySet()) {
                    JSONObject o = music.getJSONObject(key);
                    musicOut.put(key, new AudioSystem.MusicDef(o.getString("path")));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load audio json: " + path, e);
        }
    }
}