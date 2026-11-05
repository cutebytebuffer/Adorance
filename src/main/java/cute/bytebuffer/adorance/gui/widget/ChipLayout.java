package cute.bytebuffer.adorance.gui.widget;

import cute.bytebuffer.adorance.util.render.Renderer2D;
import cute.bytebuffer.adorance.util.render.font.FontWeight;

import java.util.ArrayList;
import java.util.List;

final class ChipLayout {

    record Chip(int index, float x, float width, int row) {

        float right() {
            return x + width;
        }
    }

    static final float CHIP_HEIGHT = 13f;
    static final float CHIP_GAP = 3f;
    static final float TEXT_SIZE = 7.5f;
    static final FontWeight TEXT_WEIGHT = FontWeight.MEDIUM;

    private static final float CHIP_PADDING = 7f;

    private final List<Chip> chips;
    private final int rows;

    private ChipLayout(List<Chip> chips, int rows) {
        this.chips = chips;
        this.rows = rows;
    }

    static ChipLayout of(List<String> labels, float availableWidth) {
        List<List<Integer>> rows = new ArrayList<>();
        List<Integer> current = new ArrayList<>();
        float used = 0f;
        for (int index = 0; index < labels.size(); index++) {
            float natural = naturalWidth(labels.get(index));
            float needed = current.isEmpty() ? natural : used + CHIP_GAP + natural;
            if (!current.isEmpty() && needed > availableWidth) {
                rows.add(current);
                current = new ArrayList<>();
                needed = natural;
            }
            current.add(index);
            used = needed;
        }
        if (!current.isEmpty()) {
            rows.add(current);
        }

        List<Chip> chips = new ArrayList<>(labels.size());
        for (int row = 0; row < rows.size(); row++) {
            List<Integer> members = rows.get(row);
            float natural = 0f;
            for (int index : members) {
                natural += naturalWidth(labels.get(index));
            }
            float spare = Math.max(0f, availableWidth - natural - CHIP_GAP * (members.size() - 1)) / members.size();
            float cursor = 0f;
            for (int index : members) {
                float chipWidth = naturalWidth(labels.get(index)) + spare;
                chips.add(new Chip(index, cursor, chipWidth, row));
                cursor += chipWidth + CHIP_GAP;
            }
        }
        return new ChipLayout(chips, rows.size());
    }

    private static float naturalWidth(String label) {
        return Renderer2D.textWidth(label, TEXT_SIZE, TEXT_WEIGHT) + CHIP_PADDING * 2f;
    }

    static float rowY(int row) {
        return row * (CHIP_HEIGHT + CHIP_GAP);
    }

    float height() {
        return rows == 0 ? 0f : rows * CHIP_HEIGHT + (rows - 1) * CHIP_GAP;
    }

    List<Chip> chips() {
        return chips;
    }

    Chip chip(int index) {
        for (Chip chip : chips) {
            if (chip.index() == index) {
                return chip;
            }
        }
        return chips.getFirst();
    }

    int indexAt(double localX, double localY) {
        for (Chip chip : chips) {
            float top = rowY(chip.row());
            if (localX >= chip.x() && localX <= chip.right() && localY >= top && localY <= top + CHIP_HEIGHT) {
                return chip.index();
            }
        }
        return -1;
    }
}
