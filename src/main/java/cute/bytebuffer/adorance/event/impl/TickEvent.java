package cute.bytebuffer.adorance.event.impl;

import cute.bytebuffer.adorance.event.Event;

public abstract sealed class TickEvent extends Event permits TickEvent.Pre, TickEvent.Post {

    public static final class Pre extends TickEvent {

        private static final Pre INSTANCE = new Pre();

        private Pre() {
        }

        public static Pre get() {
            return INSTANCE;
        }
    }

    public static final class Post extends TickEvent {

        private static final Post INSTANCE = new Post();

        private Post() {
        }

        public static Post get() {
            return INSTANCE;
        }
    }
}
