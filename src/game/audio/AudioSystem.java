package game.audio;

import org.lwjgl.openal.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.*;
import static org.lwjgl.openal.ALC10.*;

public final class AudioSystem {
    private static long device;
    private static long context;
    private static boolean ok;

    private static float master = 1.0f, musicVol = 0.7f, sfxVol = 1.0f;

    private static final Map<String, SoundBuffer> BUFFERS = new HashMap<>();
    private static final Map<String, SoundDef>    SFX     = new HashMap<>();
    private static final Map<String, MusicDef>    MUSIC   = new HashMap<>();

    private static int musicSource = 0; // looped-music
    private static int musicBuffer = 0;

    public static boolean init() {
        device = alcOpenDevice((ByteBuffer) null);
        if (device == MemoryUtil.NULL) return false;
        context = alcCreateContext(device, (int[]) null);
        alcMakeContextCurrent(context);
        AL.createCapabilities(ALC.createCapabilities(device));
        alDistanceModel(AL_LINEAR_DISTANCE_CLAMPED);
        ok = alGetError()==AL_NO_ERROR;

        if (ok) {
            AudioSystemData.loadFromJson("/data/audio/sounds.json", SFX, MUSIC);
            // Preload SFX into OpenAL buffers:
            for (var e : SFX.entrySet()) {
                BUFFERS.put(e.getKey(), SoundBuffer.load(e.getValue().path));
            }
        }
        return ok;
    }

    public static void shutdown() {
        if (!ok) return;
        stopMusic();

        for (SoundBuffer b : BUFFERS.values()) b.destroy();
        BUFFERS.clear();

        alcMakeContextCurrent(0);
        alcDestroyContext(context);
        alcCloseDevice(device);
        ok = false;
    }

    public static void update(float dt) {
        // placeholder
    }

    public static void setListener(float x, float y, float z, float yawDegrees) {
        if (!ok) return;
        alListener3f(AL_POSITION, x, y, z);
        float rad = (float)Math.toRadians(yawDegrees);
        float fx = (float)Math.sin(rad), fz = (float)-Math.cos(rad);
        alListenerfv(AL_ORIENTATION, new float[]{ fx,0,fz,  0,1,0 });
    }

    public static void playSfx(String id) { playSfx(id, 0,0,0, false); }
    public static void playSfxAt(String id, float x, float y, float z) { playSfx(id,x,y,z,true); }

    private static void playSfx(String id, float x, float y, float z, boolean positional) {
        if (!ok) return;
        var def = SFX.get(id); if (def == null) return;
        var buf = BUFFERS.get(id); if (buf == null) return;

        int src = alGenSources();
        alSourcei(src, AL_BUFFER, buf.id());
        alSourcef(src, AL_GAIN, clamp(master * sfxVol * def.gain, 0f, 1f));
        alSourcef(src, AL_PITCH, def.pitch);
        alSourcei(src, AL_LOOPING, def.loop ? AL_TRUE : AL_FALSE);

        if (positional) alSource3f(src, AL_POSITION, x, y, z);

        alSourcePlay(src);

        new Thread(() -> {
            int state;
            do {
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                state = alGetSourcei(src, AL_SOURCE_STATE);
            } while (state == AL_PLAYING);
            alDeleteSources(src);
        }, "sfx-disposer").start();
    }

    public static void playMusic(String musicId) {
        if (!ok) return;
        stopMusic();
        var def = MUSIC.get(musicId); if (def == null) return;

        musicBuffer = SoundBuffer.load(def.path).id();
        musicSource = alGenSources();
        alSourcei(musicSource, AL_BUFFER, musicBuffer);
        alSourcef(musicSource, AL_GAIN, clamp(master * musicVol, 0f, 1f));
        alSourcei(musicSource, AL_LOOPING, AL_TRUE);
        alSourcePlay(musicSource);
    }

    public static void stopMusic() {
        if (musicSource != 0) { alSourceStop(musicSource); alDeleteSources(musicSource); musicSource = 0; }
        if (musicBuffer != 0) { alDeleteBuffers(musicBuffer); musicBuffer = 0; }
    }

    public static void setMasterVolume(float v){ master = clamp(v,0f,1f); if (musicSource!=0) alSourcef(musicSource, AL_GAIN, master*musicVol); }
    public static void setMusicVolume (float v){ musicVol = clamp(v,0f,1f); if (musicSource!=0) alSourcef(musicSource, AL_GAIN, master*musicVol); }
    public static void setSfxVolume   (float v){ sfxVol = clamp(v,0f,1f); }

    private static float clamp(float v, float a, float b){ return Math.max(a, Math.min(b,v)); }

    public static final class SoundDef { public final String path; public final float gain, pitch; public final boolean loop;
        public SoundDef(String path, float gain, float pitch, boolean loop){ this.path=path; this.gain=gain; this.pitch=pitch; this.loop=loop; } }
    public static final class MusicDef { public final String path;
        public MusicDef(String path){ this.path=path; } }
}