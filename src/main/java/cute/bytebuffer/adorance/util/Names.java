package cute.bytebuffer.adorance.util;

import java.util.Locale;

public final class Names {

    private Names() {
    }

    public static String of(Enum<?> value) {
        String raw = value.toString();
        if (!raw.equals(value.name())) {
            return raw;
        }
        StringBuilder builder = new StringBuilder(raw.length());
        for (String word : raw.split("_")) {
            if (word.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(word.charAt(0)).append(word.substring(1).toLowerCase(Locale.ROOT));
        }
        return builder.toString();
    }
}
