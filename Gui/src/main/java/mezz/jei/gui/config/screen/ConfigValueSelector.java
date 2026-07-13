package mezz.jei.gui.config.screen;

import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

final class ConfigValueSelector<T> {

    private final IJeiConfigValue<?> configValue;
    private final List<ValueEntry> valueEntries;
    private final Consumer<T> setter;

    ImmutableRect2i area = ImmutableRect2i.EMPTY;

    public ConfigValueSelector(IJeiConfigValue<?> configValue, List<T> allValues, Consumer<T> setter) {
        this.configValue = configValue;
        this.valueEntries = allValues.stream()
                                     .map(ValueEntry::new)
                                     .toList();
        this.setter = setter;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return area.contains(mouseX, mouseY);
    }

    public ConfigInfo getInfo() {
        return new ConfigInfo(configValue.getLocalizedName(), configValue.getLocalizedDescription());
    }

    @Nullable
    public ConfigInfo getTooltipInfo(double mouseX, double mouseY) {
        for (ValueEntry entry : valueEntries) {
            if (entry.isMouseOver(mouseX, mouseY)) {
                return ConfigValueInfoFactory.create(configValue, entry.value);
            }
        }
        return null;
    }

    public void updateBounds(int x, int y) {
        Font font = Minecraft.getInstance().font;
        final int entryHeight = 20;
        int width = valueEntries.stream()
                                .mapToInt(entry -> {
                                    int textWidth = (int) (font.width(entry.value.toString()) * ConfigEntryWidget.TEXT_SCALE);
                                    return textWidth + ConfigValueIcon.getTextOffset(entry.value);
                                })
                                .max().orElse(50);
        int entryWidth = width + 12;
        area = new ImmutableRect2i(x, y, entryWidth, valueEntries.size() * entryHeight);
        for (int i = 0; i < valueEntries.size(); i++) {
            ValueEntry entry = valueEntries.get(i);
            entry.area = new ImmutableRect2i(
                    area.getX(),
                    area.getY() + i * entryHeight,
                    entryWidth,
                    entryHeight
            );
        }
    }

    public void draw(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        Textures textures = Internal.getTextures();
        for (ValueEntry entry : valueEntries) {
            ImmutableRect2i valueArea = entry.area;
            boolean hovered = entry.isMouseOver(mouseX, mouseY);
            ConfigEntryWidget.drawButtonBackground(guiGraphics, textures, valueArea, true, hovered);
            int contentX = valueArea.getX() + 4;
            int iconY = valueArea.getY() + (valueArea.getHeight() - ConfigValueIcon.ICON_SIZE) / 2;
            ConfigValueIcon.draw(guiGraphics, entry.value, contentX, iconY);
            int textX = contentX + ConfigValueIcon.getTextOffset(entry.value);
            int textY = ConfigEntryWidget.getCenteredTextY(font, valueArea);
            ConfigEntryWidget.drawText(guiGraphics, font, entry.value.toString(), textX, textY, hovered ? ConfigEntryWidget.HOVER_TEXT_COLOR : ConfigEntryWidget.TEXT_COLOR);
        }
    }

    public boolean onMouseClicked(UserInput input) {
        for (ValueEntry entry : valueEntries) {
            if (entry.isMouseOver(input.getMouseX(), input.getMouseY())) {
                if (!input.isSimulate()) {
                    setter.accept(entry.value);
                }
                return true;
            }
        }
        return false;
    }

    private class ValueEntry {
        final T value;
        ImmutableRect2i area = ImmutableRect2i.EMPTY;

        ValueEntry(T value) {
            this.value = value;
        }

        boolean isMouseOver(double mouseX, double mouseY) {
            return area.contains(mouseX, mouseY);
        }
    }
}
