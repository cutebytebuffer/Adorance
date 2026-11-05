package cute.bytebuffer.adorance.event.impl;

import cute.bytebuffer.adorance.event.CancellableEvent;

public final class KeyPressEvent extends CancellableEvent {

    private final int key;

    public KeyPressEvent(int key) {
        this.key = key;
    }

    public int key() {
        return key;
    }
}
