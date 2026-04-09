package mezz.jei.gui.config.screen;

import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.StringUtil;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.SameElementInputHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.Optional;

public abstract class ConfigEntryWidget<T> {

    protected static final int NAME_RIGHT_RESERVE = 95;
    private static final int NAME_LEFT_PADDING = 5;
    static final float TEXT_SCALE = 0.8f;
    private static final int RESET_BUTTON_W = 32;
    private static final int RESET_BUTTON_H = 14;

    static void drawScaledString(GuiGraphics g, Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        g.pose().pushPose();
        g.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0f);
        g.drawString(font, text, (int) (x / TEXT_SCALE), (int) (y / TEXT_SCALE), color, shadow);
        g.pose().popPose();
    }

    static void drawScaledString(GuiGraphics g, Font font, Component text, int x, int y, int color, boolean shadow) {
        g.pose().pushPose();
        g.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0f);
        g.drawString(font, text, (int) (x / TEXT_SCALE), (int) (y / TEXT_SCALE), color, shadow);
        g.pose().popPose();
    }

    static void drawScaledString(GuiGraphics g, Font font, String text, int x, int y, int color, boolean shadow) {
        g.pose().pushPose();
        g.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0f);
        g.drawString(font, text, (int) (x / TEXT_SCALE), (int) (y / TEXT_SCALE), color, shadow);
        g.pose().popPose();
    }

    final IJeiConfigValue<T> configValue;
    final Component fullName;

    protected List<FormattedCharSequence> nameLines = List.of();

    ImmutableRect2i area = ImmutableRect2i.EMPTY;
    ImmutableRect2i nameArea = ImmutableRect2i.EMPTY;
    private ImmutableRect2i resetArea = ImmutableRect2i.EMPTY;

    protected ConfigEntryWidget(IJeiConfigValue<T> configValue) {
        this.configValue = configValue;
        this.fullName = StringUtil.stripStyling(configValue.getLocalizedName());
    }

    public int getHeight() {
        if (nameLines.isEmpty()) return 20;
        Font font = Minecraft.getInstance().font;
        int scaledLineHeight = (int) (font.lineHeight * TEXT_SCALE);
        return Math.max(20, nameLines.size() * scaledLineHeight + 8);
    }

    public void updateBounds(ImmutableRect2i area) {
        Font font = Minecraft.getInstance().font;
        this.area = area;

        int nameColWidth = Math.max(40, area.getWidth() - NAME_RIGHT_RESERVE - NAME_LEFT_PADDING);
        int wrapWidth = (int) (nameColWidth / TEXT_SCALE);
        nameLines = font.split(fullName, wrapWidth);

        int scaledLineHeight = (int) (font.lineHeight * TEXT_SCALE);
        int textHeight = nameLines.size() * scaledLineHeight;
        this.nameArea = new ImmutableRect2i(
                area.getX() + NAME_LEFT_PADDING,
                area.getY() + (area.getHeight() - textHeight) / 2,
                nameColWidth,
                textHeight
        );
        this.resetArea = new ImmutableRect2i(
                area.getX() + area.getWidth() - RESET_BUTTON_W - 2,
                area.getY() + (area.getHeight() - RESET_BUTTON_H) / 2,
                RESET_BUTTON_W,
                RESET_BUTTON_H
        );
    }

    protected void recomputeNameArea(ImmutableRect2i area, int rightReserve) {
        Font font = Minecraft.getInstance().font;
        int nameColWidth = Math.max(40, area.getWidth() - rightReserve - NAME_LEFT_PADDING);
        int wrapWidth = (int) (nameColWidth / TEXT_SCALE);
        nameLines = font.split(fullName, wrapWidth);
        int scaledLineHeight = (int) (font.lineHeight * TEXT_SCALE);
        int textHeight = nameLines.size() * scaledLineHeight;
        this.nameArea = new ImmutableRect2i(
                area.getX() + NAME_LEFT_PADDING,
                area.getY() + (area.getHeight() - textHeight) / 2,
                nameColWidth,
                textHeight
        );
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return area.contains(mouseX, mouseY);
    }

    public IUserInputHandler createInputHandler() {
        return new EntryWidgetInputHandler();
    }

    boolean onMouseClicked(UserInput input) {
        if (isModified() && resetArea.contains(input.getMouseX(), input.getMouseY())) {
            if (!input.isSimulate()) {
                resetToDefault();
            }
            return true;
        }
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public void draw(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Internal.getTextures().getConfigValueSlot().draw(guiGraphics, area);
        drawContent(guiGraphics, mouseX, mouseY);
        drawResetButton(guiGraphics, mouseX, mouseY);
    }

    private void drawResetButton(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (!isModified()) {
            return;
        }
        Textures textures = Internal.getTextures();
        boolean hovered = resetArea.contains(mouseX, mouseY);
        textures.getButtonForState(false, true, hovered).draw(guiGraphics, resetArea);
        Font font = Minecraft.getInstance().font;
        String label = "Reset";
        int textWidth = (int) (font.width(label) * TEXT_SCALE);
        int textX = resetArea.getX() + (resetArea.getWidth() - textWidth) / 2;
        int textY = resetArea.getY() + (resetArea.getHeight() - (int) (font.lineHeight * TEXT_SCALE)) / 2;
        drawScaledString(guiGraphics, font, label, textX, textY, hovered ? 0xFFFFFF55 : 0xFFFFFFFF, true);
    }

    public boolean isModified() {
        return !configValue.getValue().equals(configValue.getDefaultValue());
    }

    public void drawTooltip(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        JeiTooltip tooltip = new JeiTooltip();
        getTooltip(tooltip);
        if (!tooltip.isEmpty()) {
            tooltip.draw(guiGraphics, (int) mouseX, (int) mouseY);
        }
    }

    void getTooltip(JeiTooltip tooltip) {
        tooltip.add(configValue.getLocalizedName().copy().withStyle(ChatFormatting.YELLOW));
        tooltip.add(configValue.getLocalizedDescription().copy().withStyle(ChatFormatting.GREEN));
    }

    final void drawName(GuiGraphics guiGraphics) {
        Font font = Minecraft.getInstance().font;
        int scaledLineHeight = (int) (font.lineHeight * TEXT_SCALE);
        int y = nameArea.getY();
        for (FormattedCharSequence line : nameLines) {
            drawScaledString(guiGraphics, font, line, nameArea.getX(), y, 0xFFFFFFFF, true);
            y += scaledLineHeight;
        }
    }

    public void resetToDefault() {
        configValue.set(configValue.getDefaultValue());
    }

    abstract void drawContent(GuiGraphics guiGraphics, double mouseX, double mouseY);

    private class EntryWidgetInputHandler implements IUserInputHandler {
        @Override
        public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
            if (onMouseClicked(input)) {
                return Optional.of(new SameElementInputHandler(this, area::contains));
            }
            return Optional.empty();
        }
    }
}
