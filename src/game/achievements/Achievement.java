package game.achievements;

public final class Achievement {
    public final String id;
    public final String title;
    public final String description;
    public final String lockedIconPath;
    public final String unlockedIconPath;
    public boolean unlocked;
    public long unlockedAt;
    public Achievement(String id, String title, String description, String iconPath) {
        this(id, title, description, iconPath, iconPath);
    }
    public Achievement(String id, String title, String description,
                       String lockedIconPath, String unlockedIconPath) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.lockedIconPath = lockedIconPath;
        this.unlockedIconPath = unlockedIconPath != null ? unlockedIconPath : lockedIconPath;
        this.unlocked = false;
        this.unlockedAt = 0L;
    }
}
