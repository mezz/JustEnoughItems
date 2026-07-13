package mezz.jei.gui.config.screen;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

final class EnumConfigEntry<T extends Enum<T>> extends ConfigEntryWidget<T> {

    private static final int ARROW_SIZE = 9;
    private static final int ARROW_PADDING = 3;

    private final List<T> validValues;
    private final Consumer<ConfigValueSelector<?>> valueSelectorOpener;
    private ImmutableRect2i valueArea = ImmutableRect2i.EMPTY;

    EnumConfigEntry(IJeiConfigValue<T> value, Consumer<ConfigValueSelector<?>> valueSelectorOpener) {
        super(value);
        this.valueSelectorOpener = valueSelectorOpener;
        this.validValues = value.getSerializer()
                                .getAllValidValues()
                                .stream()
                                .flatMap(Collection::stream)
                                .toList();
    }

    @Override
    public void updateBounds(ImmutableRect2i area) {
        super.updateBounds(area);
        Font font = Minecraft.getInstance().font;
        T value = getValue();
        int textWidth = (int) (font.width(value.toString()) * TEXT_SCALE);
        int totalWidth = textWidth + ARROW_SIZE + ARROW_PADDING * 2 + 8;
        totalWidth = Math.min(totalWidth, area.getWidth() / 2);
        int height = 18;
        valueArea = new ImmutableRect2i(
                area.getX() + area.getWidth() - totalWidth - 36,
                area.getY() + (area.getHeight() - height) / 2,
                totalWidth,
                height
        );
        // ensure name column doesn't overlap the value control
        recomputeNameArea(area, Math.max(NAME_RIGHT_RESERVE, totalWidth + 40));
    }

    @Override
    void drawContent(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        drawName(guiGraphics);

        Textures textures = Internal.getTextures();
        boolean hovered = valueArea.contains(mouseX, mouseY);
        drawButtonBackground(guiGraphics, textures, valueArea, true, hovered);

        // value text
        int textY = getCenteredTextY(font, valueArea);
        T value = getValue();
        String valueString = value.toString();
        int textX = valueArea.getX() + 4;
        drawText(guiGraphics, font, Component.literal(valueString), textX, textY, TEXT_COLOR);

        // down arrow icon on the right
        IDrawableStatic arrowDown = Internal.getTextures().getArrowDown();
        int arrowX = valueArea.getX() + valueArea.getWidth() - ARROW_SIZE - ARROW_PADDING;
        int arrowY = valueArea.getY() + (valueArea.getHeight() - ARROW_SIZE) / 2;
        arrowDown.draw(guiGraphics, arrowX, arrowY);
    }

    @Override
    @Nullable
    ConfigInfo getTooltipInfo(double mouseX, double mouseY) {
        if (valueArea.contains(mouseX, mouseY)) {
            return ConfigValueInfoFactory.create(configValue, getValue());
        }
        return null;
    }

    @Override
    boolean onMouseClicked(UserInput input) {
        if (super.onMouseClicked(input)) {
            return true;
        }
        if (valueArea.contains(input.getMouseX(), input.getMouseY())) {
            if (!input.isSimulate()) {
                ConfigValueSelector<T> selector = new ConfigValueSelector<>(configValue, validValues, value -> {
                    setValue(value);
                    this.updateBounds(this.area);
                });
                selector.updateBounds(valueArea.getX(), valueArea.getY() + valueArea.getHeight() + 2);
                valueSelectorOpener.accept(selector);
            }
            return true;
        }
        return false;
    }
}
