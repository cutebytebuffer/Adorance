package cute.bytebuffer.adorance.gui.frame;

import cute.bytebuffer.adorance.gui.widget.Widget;
import cute.bytebuffer.adorance.util.render.Opacity;
import cute.bytebuffer.adorance.util.render.Renderer2D;
import cute.bytebuffer.adorance.util.Rect;

import java.util.List;

final class WidgetList {

    private static final float GAP = 2f;
    private static final float GUTTER = 6f;

    private final List<Widget> widgets;
    private final ScrollView scroll = new ScrollView();
    private Rect viewport = new Rect(0f, 0f, 0f, 0f);

    WidgetList(List<Widget> widgets) {
        this.widgets = widgets;
    }

    void render(Rect area, double mouseX, double mouseY, float dt) {
        viewport = area;
        float contentHeight = 0f;
        for (Widget widget : widgets) {
            widget.update(dt);
            contentHeight += widget.layoutHeight() + GAP * widget.visibility();
        }
        scroll.update(contentHeight, area.height(), dt);

        float rowWidth = area.width() - GUTTER;
        float cursor = area.y() - scroll.offset();
        Renderer2D.pushClip(area);
        try {
            for (Widget widget : widgets) {
                float height = widget.layoutHeight();
                widget.layout(area.x(), cursor, rowWidth);
                if (height > 0.5f && cursor + height >= area.y() && cursor <= area.bottom()) {
                    drawClipped(widget, new Rect(area.x(), cursor, rowWidth, height), mouseX, mouseY, dt);
                }
                cursor += height + GAP * widget.visibility();
            }
        } finally {
            Renderer2D.popClip();
        }
        scroll.drawBar(area, area.contains(mouseX, mouseY), dt);
    }

    private static void drawClipped(Widget widget, Rect bounds, double mouseX, double mouseY, float dt) {
        Renderer2D.pushClip(bounds);
        Opacity.push(widget.visibility());
        try {
            widget.render(mouseX, mouseY, dt);
        } finally {
            Opacity.pop();
            Renderer2D.popClip();
        }
    }

    boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!viewport.contains(mouseX, mouseY)) {
            return false;
        }
        for (Widget widget : widgets) {
            if (widget.interactive() && widget.contains(mouseX, mouseY) && widget.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!viewport.contains(mouseX, mouseY)) {
            return false;
        }
        for (Widget widget : widgets) {
            if (widget.interactive() && widget.contains(mouseX, mouseY) && widget.mouseScrolled(mouseX, mouseY, amount)) {
                return true;
            }
        }
        scroll.scroll(amount);
        return true;
    }

    void resetScroll() {
        scroll.reset();
    }
}
