package mezz.jei.gui.config.screen;

import mezz.jei.api.runtime.config.IJeiConfigListValueSerializer;
import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ListConfigEntry<T> extends ConfigEntryWidget<List<T>> {

    private static final int ENTRY_ROW_HEIGHT = 16;
    private static final int BUTTON_SIZE = 14;

    private final List<ListValueRow> valueRows = new ArrayList<>();
    private final List<T> allValidValues;
    private final Consumer<ConfigValueSelector<?>> valueSelectorOpener;
    private final Runnable layoutUpdater;
    private ImmutableRect2i addButtonArea = ImmutableRect2i.EMPTY;

    public ListConfigEntry(IJeiConfigValue<List<T>> listValue, Consumer<ConfigValueSelector<?>> valueSelectorOpener, Runnable layoutUpdater) {
        super(listValue);
        this.valueSelectorOpener = valueSelectorOpener;
        this.layoutUpdater = layoutUpdater;
        List<T> validValues = List.of();
        IJeiConfigValueSerializer<List<T>> serializer = listValue.getSerializer();
        if (serializer instanceof IJeiConfigListValueSerializer<T> listSerializer) {
            IJeiConfigValueSerializer<T> elementSerializer = listSerializer.getListValueSerializer();
            validValues = elementSerializer.getAllValidValues()
                                           .map(List::copyOf)
                                           .orElse(List.of());
        }
        this.allValidValues = validValues;
        rebuildRows();
    }

    private void rebuildRows() {
        valueRows.clear();
        List<T> currentValues = configValue.getValue();
        for (int i = 0; i < currentValues.size(); i++) {
            valueRows.add(new ListValueRow(currentValues.get(i), i));
        }
    }

    private boolean canAddMore() {
        if (allValidValues.isEmpty()) return false;
        List<T> current = configValue.getValue();
        return allValidValues.stream().anyMatch(v -> !current.contains(v));
    }

    @Override
    public int getHeight() {
        return 20 + valueRows.size() * ENTRY_ROW_HEIGHT + 2;
    }

    @Override
    public void updateBounds(ImmutableRect2i area) {
        super.updateBounds(new ImmutableRect2i(area.getX(), area.getY(), area.getWidth(), 20));
        this.area = area;
        if (canAddMore()) {
            addButtonArea = new ImmutableRect2i(
                    area.getX() + area.getWidth() - 32 - 2 - BUTTON_SIZE - 2,
                    area.getY() + (20 - BUTTON_SIZE) / 2,
                    BUTTON_SIZE,
                    BUTTON_SIZE
            );
        } else {
            addButtonArea = ImmutableRect2i.EMPTY;
        }

        int y = area.getY() + 20;
        for (ListValueRow row : valueRows) {
            row.updateBounds(new ImmutableRect2i(area.getX() + 4, y, area.getWidth() - 8, ENTRY_ROW_HEIGHT));
            y += ENTRY_ROW_HEIGHT;
        }
    }

    @Override
    void drawContent(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawName(guiGraphics);

        if (!addButtonArea.equals(ImmutableRect2i.EMPTY)) {
            Textures textures = Internal.getTextures();
            boolean hovered = addButtonArea.contains(mouseX, mouseY);
            textures.getButtonForState(false, true, hovered).draw(guiGraphics, addButtonArea);
            Font addFont = Minecraft.getInstance().font;
            int aix = addButtonArea.getX() + (addButtonArea.getWidth() - addFont.width("+")) / 2;
            int aiy = addButtonArea.getY() + (addButtonArea.getHeight() - addFont.lineHeight) / 2;
            guiGraphics.drawString(addFont, "+", aix, aiy, hovered ? 0xFFFFFF55 : 0xFFFFFFFF, false);
        }

        for (ListValueRow row : valueRows) {
            row.draw(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    public IUserInputHandler createInputHandler() {
        return new ListEntryInputHandler();
    }

    @Override
    public void resetToDefault() {
        super.resetToDefault();
        rebuildRows();
        layoutUpdater.run();
    }

    private void addValue(T value) {
        List<T> current = new ArrayList<>(configValue.getValue());
        current.add(value);
        configValue.set(current);
        rebuildRows();
        layoutUpdater.run();
    }

    private void removeValue(int index) {
        List<T> current = new ArrayList<>(configValue.getValue());
        if (index >= 0 && index < current.size()) {
            current.remove(index);
            configValue.set(current);
            rebuildRows();
            layoutUpdater.run();
        }
    }

    private class ListEntryInputHandler implements IUserInputHandler {
        @Override
        public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
            if (!area.contains(input.getMouseX(), input.getMouseY())) {
                return Optional.empty();
            }
            if (!input.is(keyBindings.getLeftClick())) {
                return Optional.empty();
            }

            if (onMouseClicked(input)) {
                return Optional.of(this);
            }
            if (!addButtonArea.equals(ImmutableRect2i.EMPTY) && addButtonArea.contains(input.getMouseX(), input.getMouseY())) {
                if (!input.isSimulate()) {
                    List<T> currentValues = configValue.getValue();
                    List<T> available = allValidValues.stream()
                                                      .filter(v -> !currentValues.contains(v))
                                                      .toList();
                    if (!available.isEmpty()) {
                        ConfigValueSelector<T> selector = new ConfigValueSelector<>(available, ListConfigEntry.this::addValue);
                        selector.updateBounds((int) input.getMouseX(), addButtonArea.getY() + addButtonArea.getHeight() + 2);
                        valueSelectorOpener.accept(selector);
                    }
                }
                return Optional.of(this);
            }

            // check row delete buttons
            for (ListValueRow row : valueRows) {
                if (row.deleteArea.contains(input.getMouseX(), input.getMouseY())) {
                    if (!input.isSimulate()) {
                        removeValue(row.index);
                    }
                    return Optional.of(this);
                }
            }

            return Optional.empty();
        }
    }

    private class ListValueRow {
        final T value;
        int index;
        ImmutableRect2i area = ImmutableRect2i.EMPTY;
        ImmutableRect2i deleteArea = ImmutableRect2i.EMPTY;

        ListValueRow(T value, int index) {
            this.value = value;
            this.index = index;
        }

        void updateBounds(ImmutableRect2i area) {
            this.area = area;
            int cy = area.getY() + (area.getHeight() - BUTTON_SIZE) / 2;
            deleteArea = new ImmutableRect2i(
                    area.getX() + area.getWidth() - BUTTON_SIZE,
                    cy,
                    BUTTON_SIZE,
                    BUTTON_SIZE
            );
        }

        void draw(GuiGraphics guiGraphics, double mouseX, double mouseY) {
            Font font = Minecraft.getInstance().font;
            Textures textures = Internal.getTextures();

            guiGraphics.fill(area.getX(), area.getY(),
                    area.getX() + area.getWidth(), area.getY() + area.getHeight(),
                    0x30000000);

            int textX = area.getX() + 4;
            int textY = area.getY() + (area.getHeight() - (int) (font.lineHeight * ConfigEntryWidget.TEXT_SCALE)) / 2;
            ConfigEntryWidget.drawScaledString(guiGraphics, font, Component.literal(value.toString()),
                    textX, textY, 0xFFFFFF, true);

            // delete button
            boolean deleteHovered = deleteArea.contains(mouseX, mouseY);
            DrawableNineSliceTexture deleteBg = textures.getButtonForState(false, true, deleteHovered);
            deleteBg.draw(guiGraphics, deleteArea);
            int dix = deleteArea.getX() + (deleteArea.getWidth() - font.width("x")) / 2;
            int diy = deleteArea.getY() + (deleteArea.getHeight() - font.lineHeight) / 2;
            guiGraphics.drawString(font, "x", dix, diy, deleteHovered ? 0xFFFFFF55 : 0xFFFFFFFF, false);
        }
    }
}
