package mezz.jei.gui.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.network.IServerConnection;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.config.IIngredientGridConfig;
import mezz.jei.core.config.IWorldConfig;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.ingredients.GuiIngredientProperties;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IRecipeFocusSource;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.input.mouse.handlers.DeleteItemInputHandler;
import mezz.jei.render.IngredientListRenderer;
import mezz.jei.render.ElementRenderer;
import mezz.jei.render.IngredientListSlot;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * An ingredient grid displays a rectangular area of clickable recipe ingredients.
 * It does not draw a background or have external padding, those are left up to a higher-level element.
 */
public class IngredientGrid implements IRecipeFocusSource {
	private static final int INGREDIENT_PADDING = 1;
	public static final int INGREDIENT_WIDTH = GuiIngredientProperties.getWidth(INGREDIENT_PADDING);
	public static final int INGREDIENT_HEIGHT = GuiIngredientProperties.getHeight(INGREDIENT_PADDING);

	private final IIngredientGridConfig gridConfig;
	private final GuiScreenHelper guiScreenHelper;
	private final IngredientListRenderer ingredientListRenderer;
	private final DeleteItemInputHandler deleteItemHandler;
	private final IngredientGridTooltipHelper tooltipHelper;
	private ImmutableRect2i area = ImmutableRect2i.EMPTY;

	public IngredientGrid(
		RegisteredIngredients registeredIngredients,
		IIngredientGridConfig gridConfig,
		IEditModeConfig editModeConfig,
		IIngredientFilterConfig ingredientFilterConfig,
		IClientConfig clientConfig,
		IWorldConfig worldConfig,
		GuiScreenHelper guiScreenHelper,
		IModIdHelper modIdHelper,
		IServerConnection serverConnection
	) {
		this.gridConfig = gridConfig;
		this.guiScreenHelper = guiScreenHelper;
		this.ingredientListRenderer = new IngredientListRenderer(editModeConfig, worldConfig, registeredIngredients);
		this.tooltipHelper = new IngredientGridTooltipHelper(registeredIngredients, ingredientFilterConfig, worldConfig, modIdHelper);
		this.deleteItemHandler = new DeleteItemInputHandler(this, worldConfig, clientConfig, serverConnection);
	}

	public IUserInputHandler getInputHandler() {
		return deleteItemHandler;
	}

	public int size() {
		return this.ingredientListRenderer.size();
	}

	public int maxWidth() {
		return this.gridConfig.getMaxColumns() * INGREDIENT_WIDTH;
	}

	public int maxHeight() {
		return this.gridConfig.getMaxRows() * INGREDIENT_HEIGHT;
	}

	/**
	 * @return true if there is enough space for this in the given availableArea
	 */
	public boolean updateBounds(ImmutableRect2i availableArea, Collection<ImmutableRect2i> exclusionAreas) {
		this.ingredientListRenderer.clear();

		this.area = calculateBounds(this.gridConfig, availableArea, INGREDIENT_WIDTH, INGREDIENT_HEIGHT);
		if (this.area.isEmpty()) {
			return false;
		}

		for (int y = this.area.getY(); y < this.area.getY() + this.area.getHeight(); y += INGREDIENT_HEIGHT) {
			for (int x = this.area.getX(); x < this.area.getX() + this.area.getWidth(); x += INGREDIENT_WIDTH) {
				IngredientListSlot ingredientListSlot = new IngredientListSlot(x, y, INGREDIENT_WIDTH, INGREDIENT_HEIGHT, INGREDIENT_PADDING);
				ImmutableRect2i stackArea = ingredientListSlot.getArea();
				final boolean blocked = MathUtil.intersects(exclusionAreas, stackArea);
				ingredientListSlot.setBlocked(blocked);
				this.ingredientListRenderer.add(ingredientListSlot);
			}
		}

		return true;
	}

	private static ImmutableRect2i calculateBounds(IIngredientGridConfig config, ImmutableRect2i availableArea, int ingredientWidth, int ingredientHeight) {
		final int columns = Math.min(availableArea.getWidth() / ingredientWidth, config.getMaxColumns());
		final int rows = Math.min(availableArea.getHeight() / ingredientHeight, config.getMaxRows());
		if (rows < config.getMinRows() || columns < config.getMinColumns()) {
			return ImmutableRect2i.EMPTY;
		}

		final int width = columns * ingredientWidth;
		final int height = rows * ingredientHeight;

		final int x = switch (config.getHorizontalAlignment()) {
			case LEFT -> availableArea.getX();
			case CENTER -> availableArea.getX() + ((availableArea.getWidth() - width) / 2);
			case RIGHT -> availableArea.getX() + (availableArea.getWidth() - width);
		};

		final int y = switch (config.getVerticalAlignment()) {
			case TOP -> availableArea.getY();
			case CENTER -> availableArea.getY() + ((availableArea.getHeight() - height) / 2);
			case BOTTOM -> availableArea.getY() + (availableArea.getHeight() - height);
		};

		return new ImmutableRect2i(x, y, width, height);
	}

	public ImmutableRect2i getArea() {
		return area;
	}

	public void draw(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY) {
		RenderSystem.disableBlend();

		ingredientListRenderer.render(poseStack);

		if (isMouseOver(mouseX, mouseY)) {
			if (!this.deleteItemHandler.shouldDeleteItemOnClick(minecraft, mouseX, mouseY)) {
				ingredientListRenderer.getSlots()
					.filter(s -> s.isMouseOver(mouseX, mouseY))
					.map(IngredientListSlot::getIngredientRenderer)
					.flatMap(Optional::stream)
					.map(ElementRenderer::getArea)
					.findFirst()
					.ifPresent(area -> drawHighlight(poseStack, area));
			}
		}
	}

	/**
	 * Matches the highlight code in {@link AbstractContainerScreen#renderSlotHighlight(PoseStack, int, int, int)}
	 * but with a custom area width and height
	 */
	public static void drawHighlight(PoseStack poseStack, ImmutableRect2i area) {
		RenderSystem.disableDepthTest();
		RenderSystem.colorMask(true, true, true, false);
		GuiComponent.fill(poseStack, area.getX(), area.getY(), area.getX() + area.getWidth(), area.getY() + area.getHeight(), 0x80FFFFFF);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableDepthTest();
	}

	public void drawTooltips(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			if (this.deleteItemHandler.shouldDeleteItemOnClick(minecraft, mouseX, mouseY)) {
				this.deleteItemHandler.drawTooltips(poseStack, mouseX, mouseY);
			} else {
				ingredientListRenderer.getSlots()
					.filter(s -> s.isMouseOver(mouseX, mouseY))
					.map(IngredientListSlot::getTypedIngredient)
					.flatMap(Optional::stream)
					.findFirst()
					.ifPresent(ingredient -> tooltipHelper.drawTooltip(poseStack, mouseX, mouseY, ingredient));
			}
		}
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return area.contains(mouseX, mouseY) &&
			!guiScreenHelper.isInGuiExclusionArea(mouseX, mouseY);
	}

	@Override
	public Stream<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		return ingredientListRenderer.getSlots()
			.filter(s -> s.isMouseOver(mouseX, mouseY))
			.map(IngredientListSlot::getIngredientRenderer)
			.flatMap(Optional::stream);
	}

	public <T> Stream<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
		return this.ingredientListRenderer.getSlots()
			.map(IngredientListSlot::getTypedIngredient)
			.flatMap(Optional::stream)
			.map(i -> i.getIngredient(ingredientType))
			.flatMap(Optional::stream);
	}

	public void set(int firstItemIndex, List<ITypedIngredient<?>> ingredientList) {
		this.ingredientListRenderer.set(firstItemIndex, ingredientList);
	}
}
