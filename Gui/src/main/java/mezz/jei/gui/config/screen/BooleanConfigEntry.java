package mezz.jei.gui.config.screen;

import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

final class BooleanConfigEntry extends ConfigEntryWidget<Boolean> {

    private static final int BUTTON_WIDTH = 24;
    private static final int BUTTON_HEIGHT = 18;

    private ImmutableRect2i buttonArea = ImmutableRect2i.EMPTY;

    BooleanConfigEntry(IJeiConfigValue<Boolean> value) {
        super(value);
    }

    @Override
    public void updateBounds(ImmutableRect2i area) {
        super.updateBounds(area);
        buttonArea = new ImmutableRect2i(
                area.getX() + area.getWidth() - BUTTON_WIDTH - 36,
                area.getY() + (area.getHeight() - BUTTON_HEIGHT) / 2,
                BUTTON_WIDTH,
                BUTTON_HEIGHT
        );
        recomputeNameArea(area, Math.max(NAME_RIGHT_RESERVE, BUTTON_WIDTH + 40));
    }

    @Override
    void drawContent(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawName(guiGraphics);

        Textures textures = Internal.getTextures();
        boolean hovered = buttonArea.contains(mouseX, mouseY);
        drawButtonBackground(guiGraphics, textures, buttonArea, true, hovered);

        int iconX = buttonArea.getX() + (buttonArea.getWidth() - ConfigValueIcon.ICON_SIZE) / 2;
        int iconY = buttonArea.getY() + (buttonArea.getHeight() - ConfigValueIcon.ICON_SIZE) / 2;
        ConfigValueIcon.draw(guiGraphics, getValue(), iconX, iconY);
    }

    @Override
    @Nullable
    ConfigInfo getTooltipInfo(double mouseX, double mouseY) {
        if (buttonArea.contains(mouseX, mouseY)) {
            return ConfigValueInfoFactory.create(configValue, getValue());
        }
        return null;
    }

    @Override
    boolean onMouseClicked(UserInput input) {
        if (super.onMouseClicked(input)) {
            return true;
        }
        if (buttonArea.contains(input.getMouseX(), input.getMouseY())) {
            if (!input.isSimulate()) {
                setValue(!getValue());
            }
            return true;
        }
        return false;
    }
}
