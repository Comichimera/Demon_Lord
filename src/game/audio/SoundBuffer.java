package game.audio;

import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;

public final class SoundBuffer {
    private final int id;
    private SoundBuffer(int id){ this.id = id; }
    public int id(){ return id; }

    public static SoundBuffer load(String classpath) {
        try {
            ByteBuffer file = readAll(classpath);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer ch = stack.mallocInt(1);
                IntBuffer sr = stack.mallocInt(1);
                ShortBuffer pcm = STBVorbis.stb_vorbis_decode_memory(file, ch, sr);
                if (pcm == null) throw new RuntimeException("Decode failed for: " + classpath);

                int channels = ch.get(0);
                int sampleRate = sr.get(0);
                int format = (channels == 1) ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;

                int buf = alGenBuffers();
                alBufferData(buf, format, pcm, sampleRate);
                MemoryUtil.memFree(pcm);
                MemoryUtil.memFree(file);
                return new SoundBuffer(buf);
            }
        } catch (Exception e) {
            throw new RuntimeException("Sound load failed: " + classpath, e);
        }
    }

    public void destroy(){ alDeleteBuffers(id); }

    private static ByteBuffer readAll(String classpath) throws Exception {
        try (InputStream in = SoundBuffer.class.getResourceAsStream(classpath)) {
            if (in == null) throw new IllegalArgumentException("No resource: " + classpath);
            byte[] bytes = in.readAllBytes();
            ByteBuffer buf = MemoryUtil.memAlloc(bytes.length);
            buf.put(bytes).flip();
            return buf;
        }
    }
}
