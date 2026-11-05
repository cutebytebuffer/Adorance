package cute.bytebuffer.adorance.event.impl;

import cute.bytebuffer.adorance.event.Event;
import org.joml.Matrix4f;

public final class Render3DEvent extends Event {

    private final Matrix4f positionMatrix;
    private final Matrix4f projectionMatrix;
    private final float tickDelta;

    public Render3DEvent(Matrix4f positionMatrix, Matrix4f projectionMatrix, float tickDelta) {
        this.positionMatrix = positionMatrix;
        this.projectionMatrix = projectionMatrix;
        this.tickDelta = tickDelta;
    }

    public Matrix4f positionMatrix() {
        return positionMatrix;
    }

    public Matrix4f projectionMatrix() {
        return projectionMatrix;
    }

    public float tickDelta() {
        return tickDelta;
    }
}
