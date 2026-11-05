package cute.bytebuffer.adorance.gui.frame;

import cute.bytebuffer.adorance.gui.animation.Easing;
import cute.bytebuffer.adorance.gui.animation.Smooth;
import cute.bytebuffer.adorance.gui.theme.Theme;
import cute.bytebuffer.adorance.gui.widget.SettingWidgets;
import cute.bytebuffer.adorance.gui.widget.Widget;
import cute.bytebuffer.adorance.module.Module;
import cute.bytebuffer.adorance.util.render.ColorUtil;
import cute.bytebuffer.adorance.util.render.Opacity;
import cute.bytebuffer.adorance.util.render.Renderer2D;
import cute.bytebuffer.adorance.util.render.font.FontWeight;
import cute.bytebuffer.adorance.module.setting.Setting;
import cute.bytebuffer.adorance.util.Rect;

import java.util.ArrayList;
import java.util.List;

final class SettingsPane {

    private static final float HEADER_HEIGHT = 30f;
    private static final float RADIUS = 8f;
    private static final float CARD_ALPHA = 0.22f;
    private static final float TEXT_INSET = 10f;
    private static final float TITLE_SIZE = 9.5f;
    private static final float DESCRIPTION_SIZE = 7f;
    private static final float SLIDE = 6f;

    private final Smooth reveal = new Smooth(12f);
    private Module module;
    private WidgetList widgets;
    private Rect listArea = new Rect(0f, 0f, 0f, 0f);

    Module module() {
        return module;
    }

    void show(Module next) {
        if (next == module) {
            return;
        }
        module = next;
        widgets = new WidgetList(widgetsFor(next));
        reveal.snap(0f);
    }

    void clear() {
        module = null;
        widgets = null;
    }

    private static List<Widget> widgetsFor(Module module) {
        List<Widget> result = new ArrayList<>();
        for (Setting<?> setting : module.settings()) {
            if (setting != module.keybind()) {
                result.add(SettingWidgets.create(setting));
            }
        }
        result.add(SettingWidgets.create(module.keybind()));
        return result;
    }

    void render(Rect area, double mouseX, double mouseY, float dt) {
        if (module == null) {
            return;
        }
        Theme theme = Theme.get();
        float shown = Easing.outCubic(reveal.update(1f, dt));
        Renderer2D.rect(area, RADIUS, ColorUtil.multiplyAlpha(theme.control(), CARD_ALPHA));

        Opacity.push(shown);
        try {
            Rect content = area.offset(0f, SLIDE * (1f - shown));
            float titleY = content.y() + 6f;
            Renderer2D.text(module.name(), content.x() + TEXT_INSET, titleY, TITLE_SIZE, FontWeight.SEMIBOLD, theme.text());
            String description = Renderer2D.ellipsize(module.description(), content.width() - TEXT_INSET * 2f,
                    DESCRIPTION_SIZE, FontWeight.REGULAR);
            Renderer2D.text(description, content.x() + TEXT_INSET, titleY + 11.5f, DESCRIPTION_SIZE, FontWeight.REGULAR,
                    theme.mutedText());
            Renderer2D.rect(content.x() + TEXT_INSET - 2f, content.y() + HEADER_HEIGHT, content.width() - (TEXT_INSET - 2f) * 2f,
                    1f, 0.5f, theme.outline());

            listArea = new Rect(content.x(), content.y() + HEADER_HEIGHT + 3f, content.width(),
                    area.bottom() - content.y() - HEADER_HEIGHT - 6f);
            widgets.render(listArea, mouseX, mouseY, dt);
        } finally {
            Opacity.pop();
        }
    }

    boolean mouseClicked(double mouseX, double mouseY, int button) {
        return widgets != null && widgets.mouseClicked(mouseX, mouseY, button);
    }

    boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return widgets != null && widgets.mouseScrolled(mouseX, mouseY, amount);
    }
}
