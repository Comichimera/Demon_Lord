package game.logic;

public final class TimerService {
    private boolean running;
    private double levelElapsed;    // seconds for the current level
    private double totalElapsed;    // cumulative seconds across completed levels in this run
    private boolean levelCommitted; // guard so we don't double-add on level complete

    public void startRun() {
        totalElapsed = 0.0;
        startLevel();               // new run starts with a fresh level timer
    }

    /** Use when entering a playable level. */
    public void startLevel() {
        levelElapsed = 0.0;
        running = true;
        levelCommitted = false;
    }

    /** Advance time for the current frame. */
    public void tick(double dt) {
        if (running) levelElapsed += Math.max(0.0, dt);
    }

    public void pause()  { running = false; }
    public void resume() { running = true; }

    /** Call once when the level is actually finished. */
    public void completeLevel() {
        if (!levelCommitted) {
            totalElapsed += levelElapsed;
            levelCommitted = true;
        }
        running = false;
    }

    /** Optional: if player restarts the level without finishing it. */
    public void resetLevel() {
        levelElapsed = 0.0;
        levelCommitted = false;
    }

    public double levelSeconds() { return levelElapsed; }
    public double totalSeconds() { return totalElapsed; }

    /** mm:ss (no decimals) for end-of-level, summaries, etc. */
    public static String mmss(double secs) {
        int m = (int)(secs / 60);
        int s = (int)(secs % 60);
        return String.format("%d:%02d", m, s);
    }

    /** mm:ss.t for the live HUD. */
    public static String mmss_t(double secs) {
        int m = (int)(secs / 60);
        double s = secs - m * 60;
        if (s >= 59.95) { s = 0; m++; }
        return String.format("%d:%04.1f", m, s);
    }
}