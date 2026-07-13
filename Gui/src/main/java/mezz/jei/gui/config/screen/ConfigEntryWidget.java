package mezz.jei.gui.config.screen;

import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.StringUtil;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.SameElementInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.Optional;

abstract class ConfigEntryWidget<T> {

    static final int TEXT_COLOR = 0xFFECECEC;
    static final int SECONDARY_TEXT_COLOR = 0xFFBFC7D5;
    static final int HOVER_TEXT_COLOR = 0xFFFFFFFF;

    protected static final int NAME_RIGHT_RESERVE = 95;
    private static final int NAME_LEFT_PADDING = 5;
    static final float TEXT_SCALE = 1.0f;
    private static final int RESET_BUTTON_W = 32;
    private static final int RESET_BUTTON_H = 14;

    static void drawText(GuiGraphics guiGraphics, Font font, FormattedCharSequence text, int x, int y, int color) {
        guiGraphics.drawString(font, text, x, y, color, false);
    }

    static void drawText(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int color) {
        guiGraphics.drawString(font, text, x, y, color, false);
    }

    static void drawText(GuiGraphics guiGraphics, Font font, String text, int x, int y, int color) {
        guiGraphics.drawString(font, text, x, y, color, false);
    }

    static void drawCenteredButtonText(GuiGraphics guiGraphics, Font font, Component text, ImmutableRect2i area, int color) {
        int textX = area.getX() + (area.getWidth() - font.width(text)) / 2;
        int textY = area.getY() + (area.getHeight() - font.lineHeight + 1) / 2;
        drawText(guiGraphics, font, text, textX, textY, color);
    }

    static void drawCenteredButtonText(GuiGraphics guiGraphics, Font font, String text, ImmutableRect2i area, int color) {
        int textX = area.getX() + (area.getWidth() - font.width(text)) / 2;
        int textY = area.getY() + (area.getHeight() - font.lineHeight + 1) / 2;
        drawText(guiGraphics, font, text, textX, textY, color);
    }

    final IJeiConfigValue<T> configValue;
    final Component fullName;
    private T value;

    protected List<FormattedCharSequence> nameLines = List.of();

    ImmutableRect2i area = ImmutableRect2i.EMPTY;
    ImmutableRect2i nameArea = ImmutableRect2i.EMPTY;
    private ImmutableRect2i resetArea = ImmutableRect2i.EMPTY;

    protected ConfigEntryWidget(IJeiConfigValue<T> configValue) {
        this.configValue = configValue;
        this.fullName = StringUtil.stripStyling(configValue.getLocalizedName());
        this.value = configValue.getValue();
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

    public void resetBounds() {
        this.area = ImmutableRect2i.EMPTY;
        this.nameArea = ImmutableRect2i.EMPTY;
        this.resetArea = ImmutableRect2i.EMPTY;
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

    public void unfocus() {

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
        Component label = Component.translatable("jei.config.screen.reset");
        drawCenteredButtonText(guiGraphics, font, label, resetArea, hovered ? HOVER_TEXT_COLOR : TEXT_COLOR);
    }

    public boolean isModified() {
        return !value.equals(configValue.getDefaultValue());
    }

    public boolean hasPendingChange() {
        return !value.equals(configValue.getValue());
    }

    public void applyPendingChange() {
        configValue.set(value);
    }

    public void discardPendingChange() {
        setValue(configValue.getValue());
    }

    ConfigInfo getInfo() {
        return new ConfigInfo(configValue.getLocalizedName(), configValue.getLocalizedDescription());
    }

    final void drawName(GuiGraphics guiGraphics) {
        Font font = Minecraft.getInstance().font;
        int scaledLineHeight = (int) (font.lineHeight * TEXT_SCALE);
        int y = nameArea.getY();
        for (FormattedCharSequence line : nameLines) {
            drawText(guiGraphics, font, line, nameArea.getX(), y, TEXT_COLOR);
            y += scaledLineHeight;
        }
    }

    public void resetToDefault() {
        setValue(configValue.getDefaultValue());
    }

    protected T getValue() {
        return value;
    }

    protected boolean setValue(T value) {
        if (!configValue.getSerializer().isValid(value) || this.value.equals(value)) {
            return false;
        }
        this.value = value;
        onValueChanged();
        return true;
    }

    protected void onValueChanged() {

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

        @Override
        public void unfocus() {
            ConfigEntryWidget.this.unfocus();
        }
    }
}
