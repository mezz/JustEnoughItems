package mezz.jei.gui.config.screen;

import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class BooleanConfigEntry extends ConfigEntryWidget<Boolean> {

    private static final int BUTTON_HEIGHT = 14;
    private static final int BUTTON_PADDING = 8;

    private ImmutableRect2i buttonArea = ImmutableRect2i.EMPTY;
    private Component displayValue;

    public BooleanConfigEntry(IJeiConfigValue<Boolean> value) {
        super(value);
        displayValue = getDisplayValue();
    }

    private Component getDisplayValue() {
        return configValue.getValue()
                ? Component.translatable("jei.config.value.boolean.true")
                : Component.translatable("jei.config.value.boolean.false");
    }

    private int getButtonWidth() {
        Font font = Minecraft.getInstance().font;
        return (int) (font.width(displayValue) * TEXT_SCALE) + BUTTON_PADDING;
    }

    @Override
    public void updateBounds(ImmutableRect2i area) {
        super.updateBounds(area);
        int bw = getButtonWidth();
        buttonArea = new ImmutableRect2i(
                area.getX() + area.getWidth() - bw - 36,
                area.getY() + (area.getHeight() - BUTTON_HEIGHT) / 2,
                bw,
                BUTTON_HEIGHT
        );
        // ensure name column doesn't overlap the button
        recomputeNameArea(area, Math.max(NAME_RIGHT_RESERVE, bw + 40));
    }

    @Override
    void drawContent(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        drawName(guiGraphics);

        Textures textures = Internal.getTextures();
        boolean hovered = buttonArea.contains(mouseX, mouseY);
        DrawableNineSliceTexture bg = textures.getButtonForState(false, true, hovered);
        bg.draw(guiGraphics, buttonArea);

        int textWidth = (int) (font.width(displayValue) * TEXT_SCALE);
        int textX = buttonArea.getX() + (buttonArea.getWidth() - textWidth) / 2;
        int textY = buttonArea.getY() + (buttonArea.getHeight() - (int) (font.lineHeight * TEXT_SCALE)) / 2;
        int color = configValue.getValue() ? 0xFF55FF55 : 0xFFFF5555;
        drawScaledString(guiGraphics, font, displayValue, textX, textY, color, true);
    }

    @Override
    boolean onMouseClicked(UserInput input) {
        if (super.onMouseClicked(input)) {
            displayValue = getDisplayValue();
            return true;
        }
        if (buttonArea.contains(input.getMouseX(), input.getMouseY())) {
            if (!input.isSimulate()) {
                configValue.set(!configValue.getValue());
                displayValue = getDisplayValue();
            }
            return true;
        }
        return false;
    }
}
