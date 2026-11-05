package cute.bytebuffer.adorance.gui;

import cute.bytebuffer.adorance.Adorance;
import cute.bytebuffer.adorance.config.ConfigManager;
import cute.bytebuffer.adorance.gui.animation.Easing;
import cute.bytebuffer.adorance.gui.animation.FrameClock;
import cute.bytebuffer.adorance.gui.frame.Frame;
import cute.bytebuffer.adorance.util.render.Opacity;
import cute.bytebuffer.adorance.util.render.RenderState;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import cute.bytebuffer.adorance.gui.ClientSettings;
import org.lwjgl.glfw.GLFW;

public final class ClickGui extends Screen {


    private static final float OPEN_SPEED = 10f;
    private static final float CLOSE_SPEED = 16f;
    private static final float CLOSED_THRESHOLD = 0.02f;

    private final Frame frame = new Frame();
    private final FrameClock clock = new FrameClock();
    private float visibility;
    private boolean displayed;
    private boolean closing;

    public ClickGui() {
        super(Text.literal(Adorance.NAME));
    }

    @Override
    protected void init() {
        if (displayed) {
            return;
        }
        displayed = true;
        closing = false;
        visibility = 0f;
        clock.reset();
        frame.open();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        float dt = clock.tick();
        visibility = Easing.approach(visibility, closing ? 0f : 1f, closing ? CLOSE_SPEED : OPEN_SPEED, dt);
        if (closing && visibility <= CLOSED_THRESHOLD) {
            closing = false;
            super.close();
            return;
        }
        Opacity.reset();
        RenderState.reset();
        Opacity.push(Easing.outCubic(visibility));
        try {
            frame.render(width, height, mouseX, mouseY, dt);
        } finally {
            Opacity.reset();
            RenderState.reset();
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
    }

    @Override
    public void close() {
        if (!closing) {
            closing = true;
            frame.close();
        }
    }

    @Override
    public void removed() {
        displayed = false;
        closing = false;
        frame.close();
        ConfigManager.flush(true);
        super.removed();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (!closing) {
            frame.mouseClicked(click.x(), click.y(), click.button());
        }
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        frame.mouseReleased();
        return true;
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (!closing) {
            frame.mouseDragged(click.x(), click.y());
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!closing) {
            frame.mouseScrolled(mouseX, mouseY, verticalAmount);
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (closing) {
            return true;
        }
        if (frame.keyPressed(input.key())) {
            return true;
        }
        int toggleKey = ClientSettings.get().clickGuiKey().get();
        if (input.key() == GLFW.GLFW_KEY_ESCAPE || input.key() == toggleKey) {
            close();
        }
        return true;
    }

    @Override
    public boolean charTyped(CharInput input) {
        return closing || frame.charTyped(input.asString());
    }
}
