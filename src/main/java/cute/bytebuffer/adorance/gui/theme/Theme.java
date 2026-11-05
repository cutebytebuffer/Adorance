package cute.bytebuffer.adorance.gui.theme;

import cute.bytebuffer.adorance.util.render.ColorUtil;
import cute.bytebuffer.adorance.module.setting.BooleanSetting;
import cute.bytebuffer.adorance.module.setting.ColorSetting;
import cute.bytebuffer.adorance.module.setting.Configurable;
import cute.bytebuffer.adorance.module.setting.NumberSetting;
import cute.bytebuffer.adorance.module.setting.Setting;

public final class Theme extends Configurable {

    private static final Theme INSTANCE = new Theme();

    private final ColorSetting accent = register(new ColorSetting("Accent", 0xFFFFFFFF));
    private final ColorSetting background = register(new ColorSetting("Background", 0xA6101014));
    private final ColorSetting surface = register(new ColorSetting("Surface", 0x5C1C1C24));
    private final ColorSetting control = register(new ColorSetting("Control", 0xFF2A2A35));
    private final ColorSetting text = register(new ColorSetting("Text", 0xFFF3F2F7));
    private final ColorSetting mutedText = register(new ColorSetting("Muted Text", 0xFF8D8B99));
    private final ColorSetting outline = register(new ColorSetting("Outline", 0x1FFFFFFF));
    private final NumberSetting blur = register(new NumberSetting("Blur", 8.0, 0.0, 20.0, 0.5));
    private final NumberSetting rounding = register(new NumberSetting("Rounding", 10.0, 2.0, 16.0, 1.0));
    private final BooleanSetting shadow = register(new BooleanSetting("Shadow", true));

    private Theme() {
        super("Theme");
    }

    public static Theme get() {
        return INSTANCE;
    }

    public void reset() {
        settings().forEach(Setting::reset);
    }

    public ColorSetting accentSetting() {
        return accent;
    }

    public int accent() {
        return accent.argb();
    }

    public int onAccent() {
        return ColorUtil.readableOn(accent.argb());
    }

    public int background() {
        return background.argb();
    }

    public int surface() {
        return surface.argb();
    }

    public int control() {
        return control.argb();
    }

    public int text() {
        return text.argb();
    }

    public int mutedText() {
        return mutedText.argb();
    }

    public int outline() {
        return outline.argb();
    }

    public float blur() {
        return blur.floatValue();
    }

    public float rounding() {
        return rounding.floatValue();
    }

    public boolean shadow() {
        return shadow.get();
    }
}
