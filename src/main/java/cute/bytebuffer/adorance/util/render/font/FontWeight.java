package cute.bytebuffer.adorance.util.render.font;

public enum FontWeight {
    REGULAR("regular"),
    MEDIUM("medium"),
    SEMIBOLD("semibold");

    private final String file;

    FontWeight(String file) {
        this.file = file;
    }

    String path() {
        return "fonts/" + file + ".ttf";
    }

    String key() {
        return file;
    }
}
