package cute.bytebuffer.adorance.gui.widget;

import cute.bytebuffer.adorance.module.setting.BooleanSetting;
import cute.bytebuffer.adorance.module.setting.ColorSetting;
import cute.bytebuffer.adorance.module.setting.KeybindSetting;
import cute.bytebuffer.adorance.module.setting.ModeSetting;
import cute.bytebuffer.adorance.module.setting.MultiModeSetting;
import cute.bytebuffer.adorance.module.setting.NumberSetting;
import cute.bytebuffer.adorance.module.setting.Setting;
import cute.bytebuffer.adorance.module.setting.StringSetting;

import java.util.List;

public final class SettingWidgets {

    private SettingWidgets() {
    }

    public static Widget create(Setting<?> setting) {
        return switch (setting) {
            case BooleanSetting toggle -> new BooleanWidget(toggle);
            case NumberSetting number -> new SliderWidget(number);
            case ModeSetting<?> mode -> new ModeWidget(mode);
            case MultiModeSetting<?> modes -> new MultiModeWidget(modes);
            case ColorSetting color -> new ColorWidget(color);
            case StringSetting text -> new TextWidget(text);
            case KeybindSetting keybind -> new KeybindWidget(keybind);
        };
    }

    public static List<Widget> create(List<Setting<?>> settings) {
        return settings.stream().map(SettingWidgets::create).toList();
    }
}
