package cute.bytebuffer.adorance.gui.frame;

import cute.bytebuffer.adorance.Adorance;
import cute.bytebuffer.adorance.gui.animation.Smooth;
import cute.bytebuffer.adorance.gui.animation.Spring;
import cute.bytebuffer.adorance.gui.theme.Theme;
import cute.bytebuffer.adorance.util.render.ColorUtil;
import cute.bytebuffer.adorance.util.render.Opacity;
import cute.bytebuffer.adorance.util.render.Renderer2D;
import cute.bytebuffer.adorance.util.render.font.FontWeight;
import cute.bytebuffer.adorance.util.Rect;

import java.util.List;

final class Sidebar {

    static final float BRAND_HEIGHT = 32f;

    private static final float ITEM_HEIGHT = 19f;
    private static final float ITEM_GAP = 2f;
    private static final float PADDING = 4f;
    private static final float ITEM_RADIUS = 7f;
    private static final float ICON_SIZE = 10f;
    private static final float ICON_INSET = 8f;
    private static final float ICON_TEXT_GAP = 6f;
    private static final float TEXT_SIZE = 8.5f;
    private static final float BRAND_SIZE = 12f;
    private static final float HOVER_ALPHA = 0.45f;
    private static final float SPEED = 14f;

    private final List<Page> pages;
    private final Spring pill = new Spring(260f, 0.8f);
    private final Smooth[] hover;
    private final Smooth[] selection;
    private Rect area = new Rect(0f, 0f, 0f, 0f);

    Sidebar(List<Page> pages) {
        this.pages = pages;
        this.hover = new Smooth[pages.size()];
        this.selection = new Smooth[pages.size()];
        for (int index = 0; index < pages.size(); index++) {
            hover[index] = new Smooth(SPEED);
            selection[index] = new Smooth(SPEED);
        }
    }

    private Rect itemBounds(int index) {
        float width = area.width() - PADDING * 2f;
        if (!pages.get(index).pinned()) {
            int slot = 0;
            for (int i = 0; i < index; i++) {
                if (!pages.get(i).pinned()) {
                    slot++;
                }
            }
            return new Rect(area.x() + PADDING, area.y() + BRAND_HEIGHT + slot * (ITEM_HEIGHT + ITEM_GAP), width, ITEM_HEIGHT);
        }
        int slotFromBottom = 0;
        for (int i = pages.size() - 1; i > index; i--) {
            if (pages.get(i).pinned()) {
                slotFromBottom++;
            }
        }
        float bottom = area.bottom() - PADDING - slotFromBottom * (ITEM_HEIGHT + ITEM_GAP);
        return new Rect(area.x() + PADDING, bottom - ITEM_HEIGHT, width, ITEM_HEIGHT);
    }

    void render(Rect bounds, int selected, Entrance entrance, double mouseX, double mouseY, float dt) {
        area = bounds;
        Theme theme = Theme.get();

        Opacity.push(entrance.itemAlpha(0));
        try {
            float brandY = Renderer2D.textY(area.y(), BRAND_HEIGHT, BRAND_SIZE, FontWeight.SEMIBOLD);
            Renderer2D.text(Adorance.NAME, area.x() + PADDING + ICON_INSET + entrance.itemOffset(0), brandY,
                    BRAND_SIZE, FontWeight.SEMIBOLD, theme.accent());
        } finally {
            Opacity.pop();
        }

        drawPinnedDivider(theme);

        Rect target = itemBounds(selected);
        float pillY = area.y() + pill.update(target.y() - area.y(), dt);
        Opacity.push(entrance.itemAlpha(selected + 1));
        try {
            Renderer2D.rect(target.x(), pillY, target.width(), ITEM_HEIGHT, ITEM_RADIUS, theme.accent());
        } finally {
            Opacity.pop();
        }

        for (int index = 0; index < pages.size(); index++) {
            drawItem(index, selected, entrance, theme, mouseX, mouseY, dt);
        }
    }

    private void drawPinnedDivider(Theme theme) {
        for (int index = 0; index < pages.size(); index++) {
            if (pages.get(index).pinned()) {
                Rect first = itemBounds(index);
                Renderer2D.rect(first.x() + ICON_INSET, first.y() - 5f, first.width() - ICON_INSET * 2f, 1f, 0.5f, theme.outline());
                return;
            }
        }
    }

    private void drawItem(int index, int selected, Entrance entrance, Theme theme, double mouseX, double mouseY, float dt) {
        Page page = pages.get(index);
        Rect bounds = itemBounds(index);
        float hovered = hover[index].update(bounds.contains(mouseX, mouseY), dt);
        float chosen = selection[index].update(index == selected, dt);
        float offset = entrance.itemOffset(index + 1);

        Opacity.push(entrance.itemAlpha(index + 1));
        try {
            float hoverFill = HOVER_ALPHA * hovered * (1f - chosen);
            if (hoverFill > 0.005f) {
                Renderer2D.rect(bounds, ITEM_RADIUS, ColorUtil.multiplyAlpha(theme.control(), hoverFill));
            }
            int idle = ColorUtil.lerp(theme.mutedText(), theme.text(), hovered);
            int color = ColorUtil.lerp(idle, theme.onAccent(), chosen);
            float iconX = bounds.x() + ICON_INSET + offset;
            Renderer2D.icon(page.icon(), iconX, bounds.centerY() - ICON_SIZE * 0.5f, ICON_SIZE, color);
            float textY = Renderer2D.textY(bounds.y(), bounds.height(), TEXT_SIZE, FontWeight.MEDIUM);
            Renderer2D.text(page.title(), iconX + ICON_SIZE + ICON_TEXT_GAP, textY, TEXT_SIZE, FontWeight.MEDIUM, color);
        } finally {
            Opacity.pop();
        }
    }

    int pageAt(double mouseX, double mouseY) {
        for (int index = 0; index < pages.size(); index++) {
            if (itemBounds(index).contains(mouseX, mouseY)) {
                return index;
            }
        }
        return -1;
    }

    boolean isDragZone(double mouseX, double mouseY) {
        return area.sliceTop(BRAND_HEIGHT).contains(mouseX, mouseY);
    }
}
