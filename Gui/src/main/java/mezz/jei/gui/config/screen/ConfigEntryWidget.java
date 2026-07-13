package mezz.jei.gui.config.screen;

import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

abstract class ConfigEntryWidget<T> {

    static final int TEXT_COLOR = 0xFFFFFFFF;
    static final int SECONDARY_TEXT_COLOR = 0xFFE0E0E0;
    static final int HOVER_TEXT_COLOR = 0xFFFFFFFF;
    static final int DISABLED_TEXT_COLOR = 0xFFA0A0A0;

    protected static final int NAME_RIGHT_RESERVE = 95;
    private static final int NAME_LEFT_PADDING = 5;
    private static final int MIN_NAME_LINES = 2;
    private static final int NAME_VERTICAL_PADDING = 8;
    static final float TEXT_SCALE = 1.0f;
    private static final int RESET_BUTTON_W = 32;
    private static final int RESET_BUTTON_H = 18;
    private static final int ROW_HOVER_COLOR = 0x18FFFFFF;
    private static final int PENDING_BACKGROUND_COLOR = 0x302F5F8E;
    private static final int PENDING_ACCENT_COLOR = 0xFF5E9AD6;
    private static final int BUTTON_UNDERLAY_COLOR = 0xFF111216;

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
        ImmutableRect2i textArea = MathUtil.centerTextArea(area, font, text.getVisualOrderText());
        int textX = textArea.getX();
        int textY = textArea.getY();
        drawText(guiGraphics, font, text, textX, textY, color);
    }

    static void drawCenteredButtonText(GuiGraphics guiGraphics, Font font, String text, ImmutableRect2i area, int color) {
        ImmutableRect2i textArea = MathUtil.centerTextArea(area, font, text);
        int textX = textArea.getX();
        int textY = textArea.getY();
        drawText(guiGraphics, font, text, textX, textY, color);
    }

    static int getCenteredTextY(Font font, ImmutableRect2i area) {
        return area.getY() + Math.round((area.getHeight() - font.lineHeight) / 2.0f);
    }

    static int getMinimumHeight() {
        Font font = Minecraft.getInstance().font;
        return getScaledLineHeight(font) * MIN_NAME_LINES + NAME_VERTICAL_PADDING;
    }

    private static int getScaledLineHeight(Font font) {
        return (int) (font.lineHeight * TEXT_SCALE);
    }

    static void drawButtonBackground(
            GuiGraphics guiGraphics,
            Textures textures,
            ImmutableRect2i area,
            boolean active,
            boolean hovered
    ) {
        guiGraphics.fill(area.getX(), area.getY(), area.getX() + area.getWidth(), area.getY() + area.getHeight(), BUTTON_UNDERLAY_COLOR);
        textures.getButtonForState(false, active, hovered).draw(guiGraphics, area);
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
        if (nameLines.isEmpty()) return getMinimumHeight();
        Font font = Minecraft.getInstance().font;
        int scaledLineHeight = getScaledLineHeight(font);
        return Math.max(getMinimumHeight(), nameLines.size() * scaledLineHeight + NAME_VERTICAL_PADDING);
    }

    public void updateBounds(ImmutableRect2i area) {
        Font font = Minecraft.getInstance().font;
        this.area = area;

        int nameColWidth = Math.max(40, area.getWidth() - NAME_RIGHT_RESERVE - NAME_LEFT_PADDING);
        int wrapWidth = (int) (nameColWidth / TEXT_SCALE);
        nameLines = font.split(fullName, wrapWidth);

        int scaledLineHeight = getScaledLineHeight(font);
        int textHeight = nameLines.size() * scaledLineHeight;
        this.nameArea = new ImmutableRect2i(
                area.getX() + NAME_LEFT_PADDING,
                area.getY() + Math.round((area.getHeight() - textHeight) / 2.0f),
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
        int scaledLineHeight = getScaledLineHeight(font);
        int textHeight = nameLines.size() * scaledLineHeight;
        this.nameArea = new ImmutableRect2i(
                area.getX() + NAME_LEFT_PADDING,
                area.getY() + Math.round((area.getHeight() - textHeight) / 2.0f),
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

    public void draw(GuiGraphics guiGraphics, double mouseX, double mouseY, boolean allowHover) {
        if (allowHover && area.contains(mouseX, mouseY)) {
            guiGraphics.fill(
                    area.getX() + 1,
                    area.getY(),
                    area.getX() + area.getWidth() - 1,
                    area.getY() + area.getHeight(),
                    ROW_HOVER_COLOR
            );
        }
        if (hasPendingChange()) {
            guiGraphics.fill(
                    area.getX() + 1,
                    area.getY(),
                    area.getX() + area.getWidth() - 1,
                    area.getY() + area.getHeight(),
                    PENDING_BACKGROUND_COLOR
            );
            guiGraphics.fill(
                    area.getX() + 1,
                    area.getY(),
                    area.getX() + 3,
                    area.getY() + area.getHeight(),
                    PENDING_ACCENT_COLOR
            );
        }
        double drawMouseX = allowHover ? mouseX : Double.NaN;
        double drawMouseY = allowHover ? mouseY : Double.NaN;
        drawContent(guiGraphics, drawMouseX, drawMouseY);
        drawResetButton(guiGraphics, drawMouseX, drawMouseY);
    }

    private void drawResetButton(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Textures textures = Internal.getTextures();
        boolean active = isModified();
        boolean hovered = active && resetArea.contains(mouseX, mouseY);
        drawButtonBackground(guiGraphics, textures, resetArea, active, hovered);
        Font font = Minecraft.getInstance().font;
        Component label = Component.translatable("jei.config.screen.reset");
        int textColor = active ? (hovered ? HOVER_TEXT_COLOR : TEXT_COLOR) : DISABLED_TEXT_COLOR;
        drawCenteredButtonText(guiGraphics, font, label, resetArea, textColor);
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

    ConfigInfo getInfo(double mouseX, double mouseY) {
        if (resetArea.contains(mouseX, mouseY)) {
            return new ConfigInfo(
                    Component.translatable("jei.config.screen.reset"),
                    Component.translatable("jei.config.screen.reset.value.info")
            );
        }
        return getInfo();
    }

    @Nullable
    ConfigInfo getTooltipInfo(double mouseX, double mouseY) {
        return null;
    }

    final void drawName(GuiGraphics guiGraphics) {
        Font font = Minecraft.getInstance().font;
        int scaledLineHeight = getScaledLineHeight(font);
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
