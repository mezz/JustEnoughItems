package mezz.jei.gui.config.screen;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.Internal;
import mezz.jei.common.config.file.serializers.IntegerSerializer;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class IntegerConfigEntry extends ConfigEntryWidget<Integer> {

    private static final int ARROW_BLOCK_WIDTH = 12;
    private static final int ARROW_BLOCK_HEIGHT = 14;
    private static final int ICON_SIZE = 9;
    private static final int VALUE_BOX_WIDTH = 36;
    private static final int VALUE_BOX_HEIGHT = 14;

    private final IntegerSerializer serializer;
    private ImmutableRect2i valueBoxArea = ImmutableRect2i.EMPTY;
    private ImmutableRect2i arrowBgArea = ImmutableRect2i.EMPTY;
    private ImmutableRect2i upArea = ImmutableRect2i.EMPTY;
    private ImmutableRect2i downArea = ImmutableRect2i.EMPTY;

    private boolean editing = false;
    private String editText = "";

    public IntegerConfigEntry(IJeiConfigValue<Integer> value) {
        super(value);
        this.serializer = (IntegerSerializer) value.getSerializer();
    }

    @Override
    public void updateBounds(ImmutableRect2i area) {
        super.updateBounds(area);
        int cy = area.getY() + (area.getHeight() - VALUE_BOX_HEIGHT) / 2;

        arrowBgArea = new ImmutableRect2i(
                area.getX() + area.getWidth() - ARROW_BLOCK_WIDTH - 36,
                cy,
                ARROW_BLOCK_WIDTH,
                ARROW_BLOCK_HEIGHT
        );

        upArea = new ImmutableRect2i(
                arrowBgArea.getX(),
                arrowBgArea.getY(),
                arrowBgArea.getWidth(),
                arrowBgArea.getHeight() / 2
        );
        downArea = new ImmutableRect2i(
                arrowBgArea.getX(),
                arrowBgArea.getY() + arrowBgArea.getHeight() / 2,
                arrowBgArea.getWidth(),
                arrowBgArea.getHeight() - arrowBgArea.getHeight() / 2
        );

        valueBoxArea = new ImmutableRect2i(
                arrowBgArea.getX() - VALUE_BOX_WIDTH - 2,
                cy,
                VALUE_BOX_WIDTH,
                VALUE_BOX_HEIGHT
        );
    }

    @Override
    void drawContent(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        Textures textures = Internal.getTextures();
        drawName(guiGraphics);

        // value box
        textures.getConfigValueSlot().draw(guiGraphics, valueBoxArea);
        int textY = valueBoxArea.getY() + (valueBoxArea.getHeight() - (int) (font.lineHeight * TEXT_SCALE)) / 2;
        if (editing) {
            String displayText = editText + "_";
            int textColor = 0xFFFFFF;
            if (!editText.isEmpty() && !editText.equals("-")) {
                try {
                    int parsed = Integer.parseInt(editText);
                    if (parsed < serializer.getMin() || parsed > serializer.getMax()) {
                        textColor = 0xFF4444;
                    }
                } catch (NumberFormatException ignored) {
                    textColor = 0xFF4444;
                }
            }
            drawScaledString(guiGraphics, font, displayText, valueBoxArea.getX() + 3, textY, textColor, true);
        } else {
            drawScaledString(guiGraphics, font, Component.literal(configValue.getValue().toString()), valueBoxArea.getX() + 3, textY, 0xFFFFFF, true);
        }

        // range hint in tooltip handled via getTooltip

        // shared arrow background
        textures.getConfigValueSlot().draw(guiGraphics, arrowBgArea);

        // up arrow
        boolean canUp = configValue.getValue() < serializer.getMax();
        boolean upHovered = canUp && upArea.contains(mouseX, mouseY);
        if (upHovered) {
            guiGraphics.fill(upArea.getX(), upArea.getY(), upArea.getX() + upArea.getWidth(), upArea.getY() + upArea.getHeight(), 0x30FFFFFF);
        }
        IDrawableStatic upIcon = textures.getArrowUp();
        int upIx = arrowBgArea.getX() + (arrowBgArea.getWidth() - ICON_SIZE) / 2;
        int upIy = upArea.getY() + (upArea.getHeight() - ICON_SIZE) / 2;
        if (canUp) {
            upIcon.draw(guiGraphics, upIx, upIy);
        } else {
            guiGraphics.pose().pushPose();
            guiGraphics.setColor(0.3f, 0.3f, 0.3f, 0.5f);
            upIcon.draw(guiGraphics, upIx, upIy);
            guiGraphics.setColor(1f, 1f, 1f, 1f);
            guiGraphics.pose().popPose();
        }

        // down arrow
        boolean canDown = configValue.getValue() > serializer.getMin();
        boolean downHovered = canDown && downArea.contains(mouseX, mouseY);
        if (downHovered) {
            guiGraphics.fill(downArea.getX(), downArea.getY(), downArea.getX() + downArea.getWidth(), downArea.getY() + downArea.getHeight(), 0x30FFFFFF);
        }
        IDrawableStatic downIcon = textures.getArrowDown();
        int downIx = arrowBgArea.getX() + (arrowBgArea.getWidth() - ICON_SIZE) / 2;
        int downIy = downArea.getY() + (downArea.getHeight() - ICON_SIZE) / 2;
        if (canDown) {
            downIcon.draw(guiGraphics, downIx, downIy);
        } else {
            guiGraphics.pose().pushPose();
            guiGraphics.setColor(0.3f, 0.3f, 0.3f, 0.5f);
            downIcon.draw(guiGraphics, downIx, downIy);
            guiGraphics.setColor(1f, 1f, 1f, 1f);
            guiGraphics.pose().popPose();
        }
    }

    @Override
    void getTooltip(mezz.jei.common.gui.JeiTooltip tooltip) {
        super.getTooltip(tooltip);
        tooltip.add(Component.literal("Range: " + serializer.getMin() + " ~ " + serializer.getMax()).withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    @Override
    boolean onMouseClicked(UserInput input) {
        if (super.onMouseClicked(input)) {
            editing = false;
            editText = "";
            return true;
        }
        // click on value box → start editing
        if (valueBoxArea.contains(input.getMouseX(), input.getMouseY())) {
            if (!input.isSimulate()) {
                startEditing();
            }
            return true;
        }
        // click elsewhere → commit edit if active
        if (editing) {
            commitEdit();
        }
        // arrow buttons
        if (configValue.getValue() < serializer.getMax() && upArea.contains(input.getMouseX(), input.getMouseY())) {
            if (!input.isSimulate()) {
                commitEdit();
                configValue.set(Math.min(serializer.getMax(), configValue.getValue() + 1));
            }
            return true;
        }
        if (configValue.getValue() > serializer.getMin() && downArea.contains(input.getMouseX(), input.getMouseY())) {
            if (!input.isSimulate()) {
                commitEdit();
                configValue.set(Math.max(serializer.getMin(), configValue.getValue() - 1));
            }
            return true;
        }
        return false;
    }

    private void startEditing() {
        editing = true;
        editText = configValue.getValue().toString();
    }

    private void commitEdit() {
        if (!editing) {
            return;
        }
        editing = false;
        try {
            int val = Integer.parseInt(editText.trim());
            val = Math.max(serializer.getMin(), Math.min(serializer.getMax(), val));
            configValue.set(val);
        } catch (NumberFormatException ignored) {
        }
        editText = "";
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!editing) {
            return false;
        }
        if (codePoint == '-' && editText.isEmpty()) {
            editText = "-";
            return true;
        }
        if (Character.isDigit(codePoint) && editText.length() < 10) {
            editText += codePoint;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!editing) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            commitEdit();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            editing = false;
            editText = "";
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !editText.isEmpty()) {
            editText = editText.substring(0, editText.length() - 1);
            return true;
        }
        return true; // consume all keys while editing
    }

}
