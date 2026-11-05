package cute.bytebuffer.adorance.gui.frame;

import cute.bytebuffer.adorance.gui.theme.Theme;
import cute.bytebuffer.adorance.util.render.Opacity;
import cute.bytebuffer.adorance.util.render.Renderer2D;
import cute.bytebuffer.adorance.util.render.font.FontWeight;
import cute.bytebuffer.adorance.util.Rect;

final class Header {

    private static final float ICON_SIZE = 11f;
    private static final float INSET = 10f;
    private static final float ICON_TEXT_GAP = 6f;
    private static final float TITLE_SIZE = 10f;
    private static final float SEARCH_INSET = 5f;
    private static final float TITLE_SLIDE = 6f;

    private final SearchField search;
    private Rect area = new Rect(0f, 0f, 0f, 0f);

    Header(SearchField search) {
        this.search = search;
    }

    void render(Rect bounds, Page page, float pageProgress, double mouseX, double mouseY, float dt) {
        area = bounds;
        Theme theme = Theme.get();
        Renderer2D.rect(area, Math.max(2f, theme.rounding() - 3f), theme.surface());

        Opacity.push(pageProgress);
        try {
            float offset = TITLE_SLIDE * (1f - pageProgress);
            float iconX = area.x() + INSET - offset;
            Renderer2D.icon(page.icon(), iconX, area.centerY() - ICON_SIZE * 0.5f, ICON_SIZE, theme.text());
            float textY = Renderer2D.textY(area.y(), area.height(), TITLE_SIZE, FontWeight.SEMIBOLD);
            Renderer2D.text(page.title(), iconX + ICON_SIZE + ICON_TEXT_GAP, textY, TITLE_SIZE, FontWeight.SEMIBOLD, theme.text());
        } finally {
            Opacity.pop();
        }

        search.render(area.right() - SEARCH_INSET, area.centerY(), mouseX, mouseY, dt);
    }

    boolean mouseClicked(double mouseX, double mouseY, int button) {
        return search.mouseClicked(mouseX, mouseY, button);
    }

    boolean isDragZone(double mouseX, double mouseY) {
        return area.contains(mouseX, mouseY) && !search.contains(mouseX, mouseY);
    }
}
