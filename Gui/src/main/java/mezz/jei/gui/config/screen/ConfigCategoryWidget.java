package mezz.jei.gui.config.screen;

import mezz.jei.api.runtime.config.IJeiConfigCategory;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.CombinedInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

final class ConfigCategoryWidget {

    private final IJeiConfigCategory category;
    private final List<ConfigEntryWidget<?>> entryWidgets = new ArrayList<>();
    private final Supplier<ImmutableRect2i> displayAreaSupplier;
    private final Runnable layoutUpdater;

    boolean expanded;
    ImmutableRect2i area = ImmutableRect2i.EMPTY;
    ImmutableRect2i clickArea = ImmutableRect2i.EMPTY;
    ImmutableRect2i nameArea = ImmutableRect2i.EMPTY;

    ConfigCategoryWidget(
            IJeiConfigCategory category,
            Supplier<ImmutableRect2i> displayAreaSupplier,
            Runnable layoutUpdater,
            ConfigEntryWidgetFactory entryWidgetFactory
    ) {
        this.category = category;
        this.displayAreaSupplier = displayAreaSupplier;
        this.layoutUpdater = layoutUpdater;
        category.getConfigValues()
                .stream()
                .map(entryWidgetFactory::create)
                .forEach(entryWidgets::add);
    }

    public List<ConfigEntryWidget<?>> getEntryWidgets() {
        return entryWidgets;
    }

    public void resetBounds() {
        this.area = ImmutableRect2i.EMPTY;
        this.clickArea = ImmutableRect2i.EMPTY;
        this.nameArea = ImmutableRect2i.EMPTY;
        for (ConfigEntryWidget<?> entryWidget : entryWidgets) {
            entryWidget.resetBounds();
        }
    }

    public void updateBounds(ImmutableRect2i area) {
        Font font = Minecraft.getInstance().font;
        this.area = area;
        clickArea = new ImmutableRect2i(area.getX() + 6, area.getY() + 6, 16, 16);
        int nameWidth = font.width(category.getLocalizedName());
        nameWidth = Math.min(nameWidth, area.getWidth() - 20);
        nameArea = new ImmutableRect2i(area.getX() + 20, area.getY() + 7, nameWidth, 16);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return area.contains(mouseX, mouseY);
    }

    public IUserInputHandler createInputHandler() {
        List<IUserInputHandler> entryHandlers =
                entryWidgets.stream()
                            .map(entry -> {
                                final IUserInputHandler entryInputHandler = entry.createInputHandler();
                                return new IUserInputHandler() {
                                    @Override
                                    public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
                                        ImmutableRect2i displayArea = displayAreaSupplier.get();
                                        boolean entryVisible = !entry.area.equals(ImmutableRect2i.EMPTY);
                                        if ((expanded || entryVisible) &&
                                                displayArea.contains(input.getMouseX(), input.getMouseY()) &&
                                                entry.isMouseOver(input.getMouseX(), input.getMouseY())) {
                                            return entryInputHandler.handleUserInput(screen, input, keyBindings);
                                        }
                                        return Optional.empty();
                                    }

                                    @Override
                                    public void unfocus() {
                                        entryInputHandler.unfocus();
                                    }

                                    @Override
                                    public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
                                        boolean entryVisible = !entry.area.equals(ImmutableRect2i.EMPTY);
                                        if (expanded || entryVisible) {
                                            return entryInputHandler.handleMouseScrolled(mouseX, mouseY, scrollDeltaX, scrollDeltaY);
                                        }
                                        return Optional.empty();
                                    }
                                };
                            }).collect(Collectors.toList());

        entryHandlers.addFirst(new CategoryInputHandler());
        return new CombinedInputHandler("ConfigCategory:" + category.getName(), entryHandlers);
    }

    public ConfigInfo getInfo() {
        return new ConfigInfo(category.getLocalizedName(), category.getDescription());
    }

    public void draw(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        guiGraphics.fill(area.getX(), area.getY(), area.getX() + area.getWidth(), area.getY() + area.getHeight(), 0x28000000);
        guiGraphics.fill(area.getX(), area.getY() + area.getHeight() - 1, area.getX() + area.getWidth(), area.getY() + area.getHeight(), 0x20FFFFFF);

        ConfigEntryWidget.drawText(guiGraphics, font, category.getLocalizedName(), nameArea.getX(), nameArea.getY(), ConfigEntryWidget.SECONDARY_TEXT_COLOR);
    }

    private class CategoryInputHandler implements IUserInputHandler {
        @Override
        public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
            ImmutableRect2i displayArea = displayAreaSupplier.get();
            if (displayArea.contains(input.getMouseX(), input.getMouseY()) && area.contains(input.getMouseX(), input.getMouseY())) {
                if (input.is(keyBindings.getLeftClick())) {
                    if (!input.isSimulate()) {
                        expanded = !expanded;
                        layoutUpdater.run();
                    }
                    return Optional.of(this);
                }
            }
            return Optional.empty();
        }
    }
}
