package cute.bytebuffer.adorance.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class NumberSetting extends Setting<Double> {

    private final double min;
    private final double max;
    private final double step;
    private final int decimals;

    public NumberSetting(String name, double defaultValue, double min, double max, double step) {
        super(name, snap(defaultValue, requireRange(min, max), max, requireStep(step)));
        this.min = min;
        this.max = max;
        this.step = step;
        this.decimals = decimalsOf(step);
    }

    private static double requireRange(double min, double max) {
        if (min >= max) {
            throw new IllegalArgumentException("min must be smaller than max");
        }
        return min;
    }

    private static double requireStep(double step) {
        if (step <= 0d) {
            throw new IllegalArgumentException("step must be positive");
        }
        return step;
    }

    private static int decimalsOf(double step) {
        return Math.max(0, BigDecimal.valueOf(step).stripTrailingZeros().scale());
    }

    private static double snap(double value, double min, double max, double step) {
        double snapped = min + Math.round((value - min) / step) * step;
        double clamped = Math.max(min, Math.min(max, snapped));
        return BigDecimal.valueOf(clamped).setScale(decimalsOf(step), RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    protected Double sanitize(Double value) {
        return value == null ? defaultValue() : snap(value, min, max, step);
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public double step() {
        return step;
    }

    public float floatValue() {
        return get().floatValue();
    }

    public int intValue() {
        return (int) Math.round(get());
    }

    public double progress() {
        return (get() - min) / (max - min);
    }

    public void setProgress(double progress) {
        set(min + Math.max(0d, Math.min(1d, progress)) * (max - min));
    }

    public String format(double value) {
        return BigDecimal.valueOf(value).setScale(decimals, RoundingMode.HALF_UP).toPlainString();
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(get());
    }

    @Override
    public void deserialize(JsonElement json) {
        set(json.getAsDouble());
    }
}
