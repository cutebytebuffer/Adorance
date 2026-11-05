package cute.bytebuffer.adorance.util.render.gpu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class RenderErrors {

    private static final Logger LOGGER = LoggerFactory.getLogger("Adorance/Render");
    private static final int FULL_REPORTS = 3;
    private static final int SUMMARY_INTERVAL = 1000;
    private static final Map<String, AtomicInteger> COUNTS = new ConcurrentHashMap<>();

    private RenderErrors() {
    }

    public static void report(String where, Throwable error) {
        int count = COUNTS.computeIfAbsent(where, key -> new AtomicInteger()).incrementAndGet();
        if (count <= FULL_REPORTS) {
            LOGGER.error("{} failed (#{})", where, count, error);
        } else if (count % SUMMARY_INTERVAL == 0) {
            LOGGER.error("{} has failed {} times, latest: {}", where, count, error.toString());
        }
    }

    public static void warnOnce(String key, String message, Object... arguments) {
        if (COUNTS.computeIfAbsent("warn:" + key, ignored -> new AtomicInteger()).getAndIncrement() == 0) {
            LOGGER.warn(message, arguments);
        }
    }
}
