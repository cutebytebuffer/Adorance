package cute.bytebuffer.adorance.util.render.gpu;

import com.mojang.blaze3d.buffers.Std140SizeCalculator;

public final class UboLayout {

    private final Std140SizeCalculator calculator = new Std140SizeCalculator();

    public UboLayout float1() {
        calculator.putFloat();
        return this;
    }

    public UboLayout vec2() {
        calculator.putVec2();
        return this;
    }

    public UboLayout vec3() {
        calculator.putVec3();
        return this;
    }

    public UboLayout vec4() {
        calculator.putVec4();
        return this;
    }

    public UboLayout mat4() {
        calculator.putMat4f();
        return this;
    }

    public int size() {
        return calculator.get();
    }
}
