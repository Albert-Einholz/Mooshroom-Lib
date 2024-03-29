package de.einholz.ehmooshroom.gui.widget;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import de.einholz.ehmooshroom.MooshroomLib;
import de.einholz.ehmooshroom.gui.gui.UnitFormatter;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WBar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class Bar extends WBar implements DynTooltip {
    private final long max;
    private final LongSupplier cur;
    private final long min;
    private final List<String> tooltips = new ArrayList<String>();
    private final Map<String, Supplier<Object>[]> advancedTooltips = new LinkedHashMap<String, Supplier<Object>[]>();
    private final String unit;

    public Bar(Identifier bg, Identifier fg, long min, LongSupplier cur, long max, Direction dir, String unit) {
        super(bg, fg, (int) cur.getAsLong(), (int) (max <= min ? min + 1 : max), dir);
        if (max <= min) {
            MooshroomLib.LOGGER.warnBug(0, "Max value of", max, "of bar with textures", fg, "and", bg,
                    "has to be larger than the min of", min + ".", "Max will be set to", min + 1);
            max = min + 1;
        }
        this.max = max;
        this.cur = cur;
        this.min = min;
        this.unit = unit;
    }

    public Bar(Identifier bg, Identifier fg, long min, LongSupplier cur, long max, Direction dir) {
        this(bg, fg, min, cur, max, dir, "");
    }

    @Deprecated(since = "0.0.5", forRemoval = false)
    @Override
    public WBar withTooltip(String label) {
        tooltips.add(label);
        return this;
    }

    @Deprecated(since = "0.0.5", forRemoval = false)
    @Override
    public WBar withTooltip(Text label) {
        return withTooltip(label.getString());
    }

    @SuppressWarnings("unchecked")
    public void addDefaultTooltip(String label) {
        advancedTooltips.put(label, new Supplier[] {
                () -> UnitFormatter.formatLong(min, unit),
                () -> UnitFormatter.formatLong(cur.getAsLong(), unit),
                () -> UnitFormatter.formatLong(max, unit),
        });
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        if (bg != null)
            ScreenDrawing.texturedRect(matrices, x, y, getWidth(), getHeight(), bg, 0xFFFFFFFF);
        else
            ScreenDrawing.coloredRect(matrices, x, y, getWidth(), getHeight(),
                    ScreenDrawing.colorAtOpacity(0x000000, 0.25f));
        float percent = (float) (cur.getAsLong() - min) / (float) (max - min);
        int barMax = Direction.RIGHT.equals(direction) || Direction.LEFT.equals(direction) ? barMax = getWidth()
                : getHeight();
        int barSize = (int) (barMax * percent);
        switch (direction) {
            case UP:
                int leftInUp = x;
                int topInUp = y + getHeight() - barSize;
                if (bar != null)
                    ScreenDrawing.texturedRect(matrices, leftInUp, topInUp, getWidth(), barSize, bar.image(), 0f,
                            1f - (float) percent, 1f, 1f, 0xFFFFFFFF);
                else
                    ScreenDrawing.coloredRect(matrices, leftInUp, topInUp, getWidth(), barSize,
                            ScreenDrawing.colorAtOpacity(0xFFFFFF, 0.5f));
                break;
            case RIGHT:
                if (bar != null)
                    ScreenDrawing.texturedRect(matrices, x, y, barSize, getHeight(), bar.image(), 0f, 0f,
                            (float) percent, 1f, 0xFFFFFFFF);
                else
                    ScreenDrawing.coloredRect(matrices, x, y, barSize, getHeight(),
                            ScreenDrawing.colorAtOpacity(0xFFFFFF, 0.5f));
                break;
            case DOWN:
                if (bar != null)
                    ScreenDrawing.texturedRect(matrices, x, y, getWidth(), barSize, bar.image(), 0f, 0f, 1f,
                            (float) percent, 0xFFFFFFFF);
                else
                    ScreenDrawing.coloredRect(matrices, x, y, getWidth(), barSize,
                            ScreenDrawing.colorAtOpacity(0xFFFFFF, 0.5f));
                break;
            case LEFT:
                int leftInLeft = x + getWidth() - barSize;
                int topInLeft = y;
                if (bar != null)
                    ScreenDrawing.texturedRect(matrices, leftInLeft, topInLeft, barSize, getHeight(), bar.image(),
                            (float) (1 - percent), 0f, 1f, 1f, 0xFFFFFFFF);
                else
                    ScreenDrawing.coloredRect(matrices, leftInLeft, topInLeft, barSize, getHeight(),
                            ScreenDrawing.colorAtOpacity(0xFFFFFF, 0.5f));
                break;
        }
    }

    @Override
    public List<String> getTooltips() {
        return tooltips;
    }

    @Override
    public Map<String, Supplier<Object>[]> getDynTooltips() {
        return advancedTooltips;
    }

    @Override
    public void addTooltip(TooltipBuilder info) {
        DynTooltip.super.addTooltip(info);
    }
}
