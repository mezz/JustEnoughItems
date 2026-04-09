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

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class EnumConfigEntry<T extends Enum<T>> extends ConfigEntryWidget<T> {

    private static final int ARROW_SIZE = 9;
    private static final int ARROW_PADDING = 3;

    private final List<T> validValues;
    private final Consumer<ConfigValueSelector<?>> valueSelectorOpener;
    private ImmutableRect2i valueArea = ImmutableRect2i.EMPTY;

    public EnumConfigEntry(IJeiConfigValue<T> value, Consumer<ConfigValueSelector<?>> valueSelectorOpener) {
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
        int textWidth = (int) (font.width(configValue.getValue().toString()) * TEXT_SCALE);
        int totalWidth = textWidth + ARROW_SIZE + ARROW_PADDING * 2 + 8;
        totalWidth = Math.min(totalWidth, area.getWidth() / 2);
        int height = 14;
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

        // value background (same nine-slice as integer value box)
        Textures textures = Internal.getTextures();
        textures.getConfigValueSlot().draw(guiGraphics, valueArea);

        // value text
        int textY = valueArea.getY() + (valueArea.getHeight() - (int) (font.lineHeight * TEXT_SCALE)) / 2;
        String valueString = configValue.getValue().toString();
        drawScaledString(guiGraphics, font, Component.literal(valueString), valueArea.getX() + 4, textY, 0xFFFFFF, true);

        // down arrow icon on the right
        IDrawableStatic arrowDown = Internal.getTextures().getArrowDown();
        int arrowX = valueArea.getX() + valueArea.getWidth() - ARROW_SIZE - ARROW_PADDING;
        int arrowY = valueArea.getY() + (valueArea.getHeight() - ARROW_SIZE) / 2;
        arrowDown.draw(guiGraphics, arrowX, arrowY);
    }

    @Override
    boolean onMouseClicked(UserInput input) {
        if (super.onMouseClicked(input)) {
            return true;
        }
        if (valueArea.contains(input.getMouseX(), input.getMouseY())) {
            if (!input.isSimulate()) {
                ConfigValueSelector<T> selector = new ConfigValueSelector<>(validValues, value -> {
                    configValue.set(value);
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
