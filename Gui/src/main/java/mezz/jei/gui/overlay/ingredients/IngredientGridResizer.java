package mezz.jei.gui.overlay.ingredients;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.gui.elements.ResizeDrag;
import mezz.jei.gui.elements.ResizeHandle;
import net.mezzdev.config.api.value.IConfigValue;
import net.mezzdev.config.api.value.serializer.ConfigValueRange;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

final class IngredientGridResizer implements IUserInputHandler {
	private final IngredientGridWithNavigation grid;
	private final IIngredientGridConfig config;
	private final IClientConfig clientConfig;
	private @Nullable ResizeDrag drag;
	private ImmutableSize2i maximum = ImmutableSize2i.EMPTY;
	private int initialColumns;
	private int initialRows;

	IngredientGridResizer(IngredientGridWithNavigation grid, IIngredientGridConfig config, IClientConfig clientConfig) {
		this.grid = grid;
		this.config = config;
		this.clientConfig = clientConfig;
	}

	private ImmutableRect2i getArea() {
		ImmutableRect2i area = grid.getBackgroundArea();
		if (!area.isEmpty() && !config.backgroundStyle().get().isEnabled()) {
			// Unframed grids reserve their surrounding margin for resizing, never ingredient pixels.
			return area.expandBy(ResizeHandle.SIZE);
		}
		return area;
	}

	private ResizeHandle getHandle(double mouseX, double mouseY) {
		if (!clientConfig.guiResizeEnabled().get() || !grid.hasRoom() || grid.isResizeExcluded(mouseX, mouseY)) {
			return ResizeHandle.NONE;
		}
		ResizeHandle handle = ResizeHandle.at(getArea(), mouseX, mouseY);
		return new ResizeHandle(
			handle.left() && config.horizontalAlignment().get() != HorizontalAlignment.LEFT,
			handle.right() && config.horizontalAlignment().get() != HorizontalAlignment.RIGHT,
			handle.top() && grid.getResizeVerticalAlignment() != VerticalAlignment.TOP,
			handle.bottom() && grid.getResizeVerticalAlignment() != VerticalAlignment.BOTTOM
		);
	}

	boolean isMouseOver(double mouseX, double mouseY) {
		return drag != null || getHandle(mouseX, mouseY).isPresent();
	}

	void requestCursor(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		ResizeHandle handle = getHandle(mouseX, mouseY);
		if (drag != null) {
			handle = drag.handle();
		}
		handle.requestCursor(graphics);
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
		if (!clientConfig.guiResizeEnabled().get() || !input.is(keyBindings.getLeftClick())) {
			return Optional.empty();
		}
		if (input.isSimulate()) {
			ResizeHandle handle = getHandle(input.getMouseX(), input.getMouseY());
			if (!handle.isPresent()) {
				return Optional.empty();
			}
			drag = new ResizeDrag(handle, input.getMouseX(), input.getMouseY(), grid.getIngredientGridArea().getSize());
			maximum = grid.getMaximumResizeSize();
			initialColumns = config.maxColumns().get();
			initialRows = config.maxRows().get();
			return Optional.of(this);
		}
		if (this.drag == null) {
			return Optional.empty();
		}
		this.drag = null;
		return Optional.of(this);
	}

	@Override
	public Optional<IUserInputHandler> handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
		ResizeDrag drag = this.drag;
		if (drag == null || !clientConfig.guiResizeEnabled().get() || mouseKey.getValue() != InputConstants.MOUSE_BUTTON_LEFT) {
			return Optional.empty();
		}
		int slotWidth = IngredientGridLayout.INGREDIENT_WIDTH;
		int slotHeight = IngredientGridLayout.INGREDIENT_HEIGHT;
		ImmutableSize2i minimum = new ImmutableSize2i(config.getMinColumns() * slotWidth, config.getMinRows() * slotHeight);
		ImmutableSize2i size = drag.resize(mouseX, mouseY,
			config.horizontalAlignment().get() == HorizontalAlignment.CENTER,
			grid.getResizeVerticalAlignment() == VerticalAlignment.CENTER, minimum, maximum);
		int columns = initialColumns;
		if (drag.handle().horizontal()) {
			columns = Math.clamp(Math.round((float) size.width() / slotWidth), config.getMinColumns(), getMaximum(config.maxColumns()));
		}
		int rows = initialRows;
		if (drag.handle().vertical()) {
			rows = Math.clamp(Math.round((float) size.height() / slotHeight), config.getMinRows(), getMaximum(config.maxRows()));
		}
		if (columns == Math.round((float) drag.size().width() / slotWidth)) {
			columns = initialColumns;
		}
		if (rows == Math.round((float) drag.size().height() / slotHeight)) {
			rows = initialRows;
		}
		if (config.maxColumns().get() != columns) {
			config.maxColumns().set(columns);
		}
		if (config.maxRows().get() != rows) {
			config.maxRows().set(rows);
		}
		return Optional.of(this);
	}

	private static int getMaximum(IConfigValue<Integer> value) {
		return value.getEditorInfo().getSerializer().getRange()
			.map(ConfigValueRange::max)
			.orElse(100);
	}

	@Override
	public void unfocus() {
		drag = null;
	}
}
