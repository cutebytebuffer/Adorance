package cute.bytebuffer.adorance.gui.frame;

import cute.bytebuffer.adorance.Adorance;
import cute.bytebuffer.adorance.gui.theme.Theme;
import cute.bytebuffer.adorance.gui.ClientSettings;
import cute.bytebuffer.adorance.gui.widget.ButtonWidget;
import cute.bytebuffer.adorance.gui.widget.SectionWidget;
import cute.bytebuffer.adorance.gui.widget.SettingWidgets;
import cute.bytebuffer.adorance.gui.widget.Widget;
import cute.bytebuffer.adorance.module.setting.ColorSetting;
import cute.bytebuffer.adorance.module.setting.Setting;
import cute.bytebuffer.adorance.util.Rect;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

final class ClientPage implements Page {

    private static final Identifier ICON = Adorance.id("icons/client.png");
    private static final float PADDING = 6f;

    private final WidgetList widgets;

    ClientPage() {
        Theme theme = Theme.get();
        List<Widget> list = new ArrayList<>();
        list.add(new SectionWidget("Colors"));
        for (Setting<?> setting : theme.settings()) {
            if (setting instanceof ColorSetting) {
                list.add(SettingWidgets.create(setting));
            }
        }
        list.add(new SectionWidget("Surface"));
        for (Setting<?> setting : theme.settings()) {
            if (!(setting instanceof ColorSetting)) {
                list.add(SettingWidgets.create(setting));
            }
        }
        list.add(new ButtonWidget("Reset to defaults", theme::reset));
        list.add(new SectionWidget("Interface"));
        list.add(SettingWidgets.create(ClientSettings.get().clickGuiKey()));
        this.widgets = new WidgetList(list);
    }

    @Override
    public String title() {
        return "Client";
    }

    @Override
    public Identifier icon() {
        return ICON;
    }

    @Override
    public boolean pinned() {
        return true;
    }

    @Override
    public void show(float delaySeconds) {
        widgets.resetScroll();
    }

    @Override
    public void render(Rect area, double mouseX, double mouseY, float dt) {
        widgets.render(area.inset(PADDING), mouseX, mouseY, dt);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return widgets.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return widgets.mouseScrolled(mouseX, mouseY, amount);
    }
}
