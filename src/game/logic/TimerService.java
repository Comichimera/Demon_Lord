package game.logic;

public final class TimerService {
    private boolean running;
    private double levelElapsed;
    private double totalElapsed;
    private boolean levelCommitted;

    public void startRun() {
        totalElapsed = 0.0;
        startLevel();
    }

    public void startLevel() {
        levelElapsed = 0.0;
        running = true;
        levelCommitted = false;
    }

    public void tick(double dt) {
        if (running) levelElapsed += Math.max(0.0, dt);
    }

    public void pause()  { running = false; }
    public void resume() { running = true; }

    public void completeLevel() {
        if (!levelCommitted) {
            totalElapsed += levelElapsed;
            levelCommitted = true;
        }
        running = false;
    }

    public void resetLevel() {
        levelElapsed = 0.0;
        levelCommitted = false;
    }

    public double levelSeconds() { return levelElapsed; }
    public double totalSeconds() { return totalElapsed; }

    public static String mmss(double secs) {
        int m = (int)(secs / 60);
        int s = (int)(secs % 60);
        return String.format("%d:%02d", m, s);
    }

    public static String mmss_t(double secs) {
        int m = (int)(secs / 60);
        double s = secs - m * 60;
        if (s >= 59.95) { s = 0; m++; }
        return String.format("%d:%04.1f", m, s);
    }
}