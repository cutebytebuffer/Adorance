package cute.bytebuffer.adorance.gui.frame;

import cute.bytebuffer.adorance.gui.animation.Easing;
import cute.bytebuffer.adorance.gui.animation.Smooth;
import cute.bytebuffer.adorance.gui.input.Interaction;
import cute.bytebuffer.adorance.gui.theme.Theme;
import cute.bytebuffer.adorance.module.Category;
import cute.bytebuffer.adorance.util.render.Opacity;
import cute.bytebuffer.adorance.util.render.RenderState;
import cute.bytebuffer.adorance.util.render.Renderer2D;
import cute.bytebuffer.adorance.util.Rect;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class Frame {

    private static final float WIDTH = 420f;
    private static final float HEIGHT = 290f;
    private static final float SIDEBAR_WIDTH = 108f;
    private static final float HEADER_HEIGHT = 26f;
    private static final float INSET = 6f;
    private static final float GAP = 6f;
    private static final float SHADOW_SOFTNESS = 22f;
    private static final int SHADOW_COLOR = 0x73000000;
    private static final float PAGE_SPEED = 11f;
    private static final float PAGE_SLIDE = 6f;
    private static final float TAB_SWITCH_DELAY = 0.03f;

    private final List<Page> pages;
    private final SearchField search;
    private final Sidebar sidebar;
    private final Header header;
    private final Entrance entrance = new Entrance();
    private final Smooth pageTransition = new Smooth(PAGE_SPEED, 1f);
    private int selected;
    private float x = Float.NaN;
    private float y = Float.NaN;
    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;

    public Frame() {
        List<Page> list = new ArrayList<>();
        for (Category category : Category.values()) {
            list.add(new ModulePage(category));
        }
        list.add(new ClientPage());
        this.pages = List.copyOf(list);
        this.search = new SearchField(query -> page().applyQuery(query));
        this.sidebar = new Sidebar(pages);
        this.header = new Header(search);
    }

    public void open() {
        entrance.restart();
        pageTransition.snap(1f);
        Page page = page();
        search.setActive(page.searchable());
        page.show(entrance.contentDelay());
    }

    public void close() {
        dragging = false;
        Interaction.reset();
    }

    private Page page() {
        return pages.get(selected);
    }

    private void select(int index) {
        if (index == selected) {
            return;
        }
        selected = index;
        Page page = page();
        search.setActive(page.searchable());
        if (page.searchable()) {
            page.applyQuery(search.query());
        }
        pageTransition.snap(0f);
        page.show(TAB_SWITCH_DELAY);
    }

    private Rect bounds() {
        return new Rect(x, y, WIDTH, HEIGHT);
    }

    private Rect sidebarArea() {
        return new Rect(x + INSET, y + INSET, SIDEBAR_WIDTH, HEIGHT - INSET * 2f);
    }

    private Rect headerArea() {
        float left = x + INSET + SIDEBAR_WIDTH + GAP;
        return new Rect(left, y + INSET, x + WIDTH - INSET - left, HEADER_HEIGHT);
    }

    private Rect contentArea() {
        Rect header = headerArea();
        float top = header.bottom() + GAP;
        return new Rect(header.x(), top, header.width(), y + HEIGHT - INSET - top);
    }

    private void place(int screenWidth, int screenHeight) {
        if (Float.isNaN(x) || Float.isNaN(y)) {
            x = (screenWidth - WIDTH) * 0.5f;
            y = (screenHeight - HEIGHT) * 0.5f;
        }
        x = clampToScreen(x, WIDTH, screenWidth);
        y = clampToScreen(y, HEIGHT, screenHeight);
    }

    private static float clampToScreen(float position, float size, int screenSize) {
        float max = screenSize - size;
        return max <= 0f ? max * 0.5f : Math.max(0f, Math.min(max, position));
    }

    private double toLocalX(double mouseX) {
        float centerX = x + WIDTH * 0.5f;
        return centerX + (mouseX - centerX) / entrance.frameScale();
    }

    private double toLocalY(double mouseY) {
        float centerY = y + HEIGHT * 0.5f;
        return centerY + (mouseY - centerY) / entrance.frameScale();
    }

    public void render(int screenWidth, int screenHeight, double mouseX, double mouseY, float dt) {
        place(screenWidth, screenHeight);
        Rect bounds = bounds();
        double localX = toLocalX(mouseX);
        double localY = toLocalY(mouseY);
        RenderState.pushTransform(bounds.centerX(), bounds.centerY(), entrance.frameScale());
        Opacity.push(entrance.frameAlpha());
        try {
            drawShell(bounds);
            sidebar.render(sidebarArea(), selected, entrance, localX, localY, dt);
            float transition = Easing.outCubic(pageTransition.update(1f, dt));
            header.render(headerArea(), page(), transition, localX, localY, dt);
            drawContent(transition, localX, localY, dt);
        } finally {
            Opacity.pop();
            RenderState.popTransform();
        }
    }

    private void drawShell(Rect bounds) {
        Theme theme = Theme.get();
        float radius = theme.rounding();
        if (theme.shadow()) {
            Renderer2D.shadow(bounds, radius, SHADOW_COLOR, SHADOW_SOFTNESS);
        }
        Renderer2D.blurredRect(bounds, radius, theme.background(), theme.blur());
        Renderer2D.outline(bounds, radius, theme.outline(), 1f);
    }

    private void drawContent(float transition, double mouseX, double mouseY, float dt) {
        Theme theme = Theme.get();
        Rect content = contentArea();
        Renderer2D.rect(content, Math.max(2f, theme.rounding() - 3f), theme.surface());
        Opacity.push(entrance.contentAlpha() * transition);
        Renderer2D.pushClip(content);
        try {
            page().render(content.offset(0f, PAGE_SLIDE * (1f - transition)), mouseX, mouseY, dt);
        } finally {
            Renderer2D.popClip();
            Opacity.pop();
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        double localX = toLocalX(mouseX);
        double localY = toLocalY(mouseY);
        Interaction.pointerDown();
        try {
            if (!bounds().contains(localX, localY)) {
                return;
            }
            int index = sidebar.pageAt(localX, localY);
            if (index >= 0) {
                select(index);
            } else if (header.mouseClicked(localX, localY, button)) {
                return;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT
                    && (sidebar.isDragZone(localX, localY) || header.isDragZone(localX, localY))) {
                dragging = true;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
            } else if (contentArea().contains(localX, localY)) {
                page().mouseClicked(localX, localY, button);
            }
        } finally {
            Interaction.pointerHandled();
        }
    }

    public void mouseDragged(double mouseX, double mouseY) {
        if (dragging) {
            x = (float) (mouseX - dragOffsetX);
            y = (float) (mouseY - dragOffsetY);
            return;
        }
        Interaction.drag(toLocalX(mouseX), toLocalY(mouseY));
    }

    public void mouseReleased() {
        dragging = false;
        Interaction.release();
    }

    public void mouseScrolled(double mouseX, double mouseY, double amount) {
        double localX = toLocalX(mouseX);
        double localY = toLocalY(mouseY);
        if (contentArea().contains(localX, localY)) {
            page().mouseScrolled(localX, localY, amount);
        }
    }

    public boolean keyPressed(int key) {
        if (Interaction.keyPressed(key)) {
            return true;
        }
        if (key == GLFW.GLFW_KEY_F && Interaction.isControlDown() && search.isActive()) {
            search.focus();
            return true;
        }
        return false;
    }

    public boolean charTyped(String characters) {
        if (Interaction.charTyped(characters)) {
            return true;
        }
        if (search.isActive() && !characters.isBlank()) {
            search.focus();
            return search.charTyped(characters);
        }
        return false;
    }
}
