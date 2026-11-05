package cute.bytebuffer.adorance.gui.animation;

public final class Smooth {

    private final float speed;
    private float value;

    public Smooth(float speed) {
        this(speed, 0f);
    }

    public Smooth(float speed, float initial) {
        this.speed = speed;
        this.value = initial;
    }

    public float update(float target, float dt) {
        value = Easing.approach(value, target, speed, dt);
        return value;
    }

    public float update(boolean active, float dt) {
        return update(active ? 1f : 0f, dt);
    }

    public void snap(float target) {
        value = target;
    }

    public float get() {
        return value;
    }
}
