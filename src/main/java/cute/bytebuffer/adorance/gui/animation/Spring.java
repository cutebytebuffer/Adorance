package cute.bytebuffer.adorance.gui.animation;

public final class Spring {

    private static final float STEP = 1f / 240f;
    private static final float MAX_DT = 0.1f;

    private final float stiffness;
    private final float dampingRatio;
    private float value;
    private float velocity;
    private boolean initialized;

    public Spring(float stiffness, float dampingRatio) {
        this.stiffness = stiffness;
        this.dampingRatio = dampingRatio;
    }

    public float update(float target, float dt) {
        return update(target, dt, stiffness);
    }

    public float update(float target, float dt, float stiffnessOverride) {
        if (!initialized) {
            snap(target);
            return value;
        }
        float damping = 2f * dampingRatio * (float) Math.sqrt(stiffnessOverride);
        float remaining = Math.min(Math.max(dt, 0f), MAX_DT);
        while (remaining > 0f) {
            float step = Math.min(remaining, STEP);
            float acceleration = stiffnessOverride * (target - value) - damping * velocity;
            velocity += acceleration * step;
            value += velocity * step;
            remaining -= step;
        }
        if (Math.abs(target - value) < 1e-4f && Math.abs(velocity) < 1e-3f) {
            value = target;
            velocity = 0f;
        }
        return value;
    }

    public void snap(float target) {
        value = target;
        velocity = 0f;
        initialized = true;
    }

    public void impulse(float amount) {
        velocity += amount;
    }

    public float get() {
        return value;
    }
}
