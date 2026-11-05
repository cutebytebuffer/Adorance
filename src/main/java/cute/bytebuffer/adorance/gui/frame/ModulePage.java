package cute.bytebuffer.adorance.gui.frame;

import cute.bytebuffer.adorance.gui.animation.Easing;
import cute.bytebuffer.adorance.gui.animation.Smooth;
import cute.bytebuffer.adorance.gui.animation.Spring;
import cute.bytebuffer.adorance.gui.theme.Theme;
import cute.bytebuffer.adorance.module.Category;
import cute.bytebuffer.adorance.module.ModuleManager;
import cute.bytebuffer.adorance.util.render.Opacity;
import cute.bytebuffer.adorance.util.render.Renderer2D;
import cute.bytebuffer.adorance.util.render.font.FontWeight;
import cute.bytebuffer.adorance.util.Rect;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;

final class ModulePage implements Page {

    private static final float PADDING = 6f;
    private static final float ROW_GAP = 3f;
    private static final float ROW_STRIDE = ModuleRow.HEIGHT + ROW_GAP;
    private static final float PANE_GAP = 8f;
    private static final float PANE_SLIDE = 14f;
    private static final float SCROLL_GUTTER = 7f;
    private static final float SHOW_STAGGER = 0.03f;
    private static final float FILTER_STAGGER = 0.02f;
    private static final float EMPTY_TEXT_SIZE = 8f;
    private static final Rect NONE = new Rect(0f, 0f, 0f, 0f);

    private final Category category;
    private final List<ModuleRow> rows;
    private final ScrollView scroll = new ScrollView();
    private final SettingsPane pane = new SettingsPane();
    private final Spring split = new Spring(240f, 0.9f);
    private final Smooth gutter = new Smooth(12f);
    private List<ModuleRow> visible;
    private ModuleRow expanded;
    private String query = "";
    private Rect listArea = NONE;
    private Rect paneArea = NONE;

    ModulePage(Category category) {
        this.category = category;
        this.rows = ModuleManager.get().modules(category).stream().map(ModuleRow::new).toList();
        this.visible = rows;
    }

    @Override
    public String title() {
        return category.displayName();
    }

    @Override
    public Identifier icon() {
        return category.icon();
    }

    @Override
    public boolean searchable() {
        return true;
    }

    @Override
    public void show(float delaySeconds) {
        scroll.reset();
        for (int index = 0; index < visible.size(); index++) {
            visible.get(index).reveal().start(delaySeconds + index * SHOW_STAGGER);
        }
    }

    @Override
    public void applyQuery(String next) {
        if (next.equals(query)) {
            return;
        }
        List<ModuleRow> previous = visible;
        query = next;
        visible = rows.stream().filter(row -> row.matches(next)).toList();
        scroll.reset();
        int revealIndex = 0;
        for (ModuleRow row : visible) {
            if (!previous.contains(row)) {
                row.reveal().start(revealIndex++ * FILTER_STAGGER);
            }
        }
        if (expanded != null && !visible.contains(expanded)) {
            expanded = null;
        }
    }

    @Override
    public void render(Rect area, double mouseX, double mouseY, float dt) {
        Rect inner = area.inset(PADDING);
        float progress = Easing.clamp(split.update(expanded != null ? 1f : 0f, dt));
        float paneWidth = (inner.width() - PANE_GAP) * 0.5f;
        float listWidth = inner.width() - (paneWidth + PANE_GAP) * progress;
        listArea = new Rect(inner.x(), inner.y(), listWidth, inner.height());
        renderList(mouseX, mouseY, dt);

        if (expanded == null && progress < 0.02f) {
            pane.clear();
        }
        if (pane.module() == null) {
            paneArea = NONE;
            return;
        }
        paneArea = new Rect(listArea.right() + PANE_GAP, inner.y(), paneWidth, inner.height());
        Opacity.push(progress);
        try {
            pane.render(paneArea.offset(PANE_SLIDE * (1f - progress), 0f), mouseX, mouseY, dt);
        } finally {
            Opacity.pop();
        }
    }

    private void renderList(double mouseX, double mouseY, float dt) {
        float contentHeight = Math.max(0f, visible.size() * ROW_STRIDE - ROW_GAP);
        scroll.update(contentHeight, listArea.height(), dt);
        if (visible.isEmpty()) {
            renderEmptyState();
            return;
        }
        float rowWidth = listArea.width() - gutter.update(scroll.overflows() ? SCROLL_GUTTER : 0f, dt);
        float cursor = listArea.y() - scroll.offset();
        Renderer2D.pushClip(listArea);
        try {
            for (ModuleRow row : visible) {
                if (cursor + ModuleRow.HEIGHT >= listArea.y() && cursor <= listArea.bottom()) {
                    row.render(new Rect(listArea.x(), cursor, rowWidth, ModuleRow.HEIGHT), row == expanded, mouseX, mouseY, dt);
                }
                cursor += ROW_STRIDE;
            }
        } finally {
            Renderer2D.popClip();
        }
        scroll.drawBar(listArea, listArea.contains(mouseX, mouseY), dt);
    }

    private void renderEmptyState() {
        String message = query.isBlank() ? "Nothing in " + title() + " yet" : "No modules match \"" + query.trim() + "\"";
        String fitted = Renderer2D.ellipsize(message, listArea.width() - 12f, EMPTY_TEXT_SIZE, FontWeight.REGULAR);
        float textY = Renderer2D.textY(listArea.y(), listArea.height(), EMPTY_TEXT_SIZE, FontWeight.REGULAR);
        Renderer2D.textCentered(fitted, listArea.centerX(), textY, EMPTY_TEXT_SIZE, FontWeight.REGULAR, Theme.get().mutedText());
    }

    private ModuleRow rowAt(double mouseX, double mouseY) {
        if (!listArea.contains(mouseX, mouseY)) {
            return null;
        }
        double local = mouseY - listArea.y() + scroll.offset();
        int index = (int) Math.floor(local / ROW_STRIDE);
        if (index < 0 || index >= visible.size() || local - index * ROW_STRIDE > ModuleRow.HEIGHT) {
            return null;
        }
        return visible.get(index);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (expanded != null && paneArea.contains(mouseX, mouseY)) {
            return pane.mouseClicked(mouseX, mouseY, button);
        }
        ModuleRow row = rowAt(mouseX, mouseY);
        if (row == null) {
            return false;
        }
        row.press();
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            row.module().toggle();
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            toggleExpanded(row);
        }
        return true;
    }

    private void toggleExpanded(ModuleRow row) {
        if (expanded == row) {
            expanded = null;
            return;
        }
        expanded = row;
        pane.show(row.module());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (expanded != null && paneArea.contains(mouseX, mouseY)) {
            return pane.mouseScrolled(mouseX, mouseY, amount);
        }
        if (!listArea.contains(mouseX, mouseY)) {
            return false;
        }
        scroll.scroll(amount);
        return true;
    }
}
