package mezz.jei.gui.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.config.IIngredientGridConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.ingredients.GuiIngredientProperties;
import mezz.jei.ingredients.IngredientInfo;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IRecipeFocusSource;
import mezz.jei.input.MouseUtil;
import mezz.jei.render.IngredientListBatchRenderer;
import mezz.jei.render.IngredientListElementRenderer;
import mezz.jei.render.IngredientListSlot;
import mezz.jei.util.GiveMode;
import mezz.jei.util.ImmutableRect2i;
import mezz.jei.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * An ingredient grid displays a rectangular area of clickable recipe ingredients.
 */
public class IngredientGrid implements IRecipeFocusSource {
	private static final int INGREDIENT_PADDING = 1;
	public static final int INGREDIENT_WIDTH = GuiIngredientProperties.getWidth(INGREDIENT_PADDING);
	public static final int INGREDIENT_HEIGHT = GuiIngredientProperties.getHeight(INGREDIENT_PADDING);

	private final RegisteredIngredients registeredIngredients;
	private final IIngredientGridConfig gridConfig;
	private final IModIdHelper modIdHelper;
	private final GuiScreenHelper guiScreenHelper;
	private final IngredientListBatchRenderer guiIngredientSlots;

	private ImmutableRect2i area = ImmutableRect2i.EMPTY;
	private final IIngredientFilterConfig ingredientFilterConfig;
	private final IClientConfig clientConfig;
	private final IWorldConfig worldConfig;

	public IngredientGrid(
		RegisteredIngredients registeredIngredients,
		IIngredientGridConfig gridConfig,
		IEditModeConfig editModeConfig,
		IIngredientFilterConfig ingredientFilterConfig,
		IClientConfig clientConfig,
		IWorldConfig worldConfig,
		GuiScreenHelper guiScreenHelper,
		IModIdHelper modIdHelper
	) {
		this.registeredIngredients = registeredIngredients;
		this.gridConfig = gridConfig;
		this.modIdHelper = modIdHelper;
		this.guiIngredientSlots = new IngredientListBatchRenderer(clientConfig, editModeConfig, worldConfig, registeredIngredients);
		this.ingredientFilterConfig = ingredientFilterConfig;
		this.clientConfig = clientConfig;
		this.worldConfig = worldConfig;
		this.guiScreenHelper = guiScreenHelper;
	}

	public int size() {
		return this.guiIngredientSlots.size();
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
		this.guiIngredientSlots.clear();

		this.area = this.gridConfig.calculateBounds(availableArea, INGREDIENT_WIDTH, INGREDIENT_HEIGHT);
		if (this.area.isEmpty()) {
			return false;
		}

		for (int y = this.area.getY(); y < this.area.getY() + this.area.getHeight(); y += INGREDIENT_HEIGHT) {
			for (int x = this.area.getX(); x < this.area.getX() + this.area.getWidth(); x += INGREDIENT_WIDTH) {
				IngredientListSlot ingredientListSlot = new IngredientListSlot(x, y, INGREDIENT_WIDTH, INGREDIENT_HEIGHT, INGREDIENT_PADDING);
				ImmutableRect2i stackArea = ingredientListSlot.getArea();
				final boolean blocked = MathUtil.intersects(exclusionAreas, stackArea);
				ingredientListSlot.setBlocked(blocked);
				this.guiIngredientSlots.add(ingredientListSlot);
			}
		}

		return true;
	}

	public ImmutableRect2i getArea() {
		return area;
	}

	public void draw(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY) {
		RenderSystem.disableBlend();

		guiIngredientSlots.render(minecraft, poseStack);

		if (!shouldDeleteItemOnClick(minecraft, mouseX, mouseY) && isMouseOver(mouseX, mouseY)) {
			guiIngredientSlots.getHovered(mouseX, mouseY)
				.map(IngredientListElementRenderer::getArea)
				.ifPresent(area -> drawHighlight(poseStack, area));
		}
	}

	/**
	 * Matches the highlight code in {@link AbstractContainerScreen#renderSlotHighlight(PoseStack, int, int, int)} but with a custom area width and height
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
			if (shouldDeleteItemOnClick(minecraft, mouseX, mouseY)) {
				TranslatableComponent deleteItem = new TranslatableComponent("jei.tooltip.delete.item");
				TooltipRenderer.drawHoveringText(poseStack, List.of(deleteItem), mouseX, mouseY);
			} else {
				guiIngredientSlots.getHovered(mouseX, mouseY)
					.map(IngredientListElementRenderer::getTypedIngredient)
					.ifPresent(ingredient -> drawTooltip(poseStack, mouseX, mouseY, ingredient));
			}
		}
	}

	private <T> void drawTooltip(PoseStack poseStack, int mouseX, int mouseY, ITypedIngredient<T> value) {
		IIngredientType<T> ingredientType = value.getType();
		T ingredient = value.getIngredient();
		IngredientInfo<T> ingredientInfo = registeredIngredients.getIngredientInfo(ingredientType);
		IIngredientRenderer<T> ingredientRenderer = ingredientInfo.getIngredientRenderer();

		List<Component> tooltip = IngredientGridTooltip.getTooltip(ingredient, ingredientInfo, ingredientFilterConfig, worldConfig, modIdHelper);
		TooltipRenderer.drawHoveringText(poseStack, tooltip, mouseX, mouseY, ingredient, ingredientRenderer);
	}

	public boolean shouldDeleteItemOnClick(Minecraft minecraft, double mouseX, double mouseY) {
		if (!worldConfig.isDeleteItemsInCheatModeActive()) {
			return false;
		}
		Player player = minecraft.player;
		if (player == null) {
			return false;
		}
		ItemStack itemStack = player.containerMenu.getCarried();
		if (itemStack.isEmpty()) {
			return false;
		}
		GiveMode giveMode = this.clientConfig.getGiveMode();
		if (giveMode == GiveMode.MOUSE_PICKUP) {
			return getIngredientUnderMouse(mouseX, mouseY)
				.map(IClickedIngredient::getCheatItemStack)
				.map(i -> !ItemHandlerHelper.canItemStacksStack(itemStack, i))
				.orElse(true);
		}
		return true;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return area.contains(mouseX, mouseY) &&
			!guiScreenHelper.isInGuiExclusionArea(mouseX, mouseY);
	}

	public <T> Optional<ITypedIngredient<T>> getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		return this.guiIngredientSlots.getHovered(MouseUtil.getX(), MouseUtil.getY(), ingredientType)
			.map(IngredientListElementRenderer::getTypedIngredient);
	}

	@Override
	public Optional<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		return guiIngredientSlots.getHovered(mouseX, mouseY)
			.map(hovered -> new ClickedIngredient<>(hovered.getTypedIngredient(), hovered.getArea(), true, true));
	}

	public <T> Stream<ITypedIngredient<T>> getVisibleIngredients(IIngredientType<T> ingredientType) {
		return this.guiIngredientSlots.getAllGuiIngredientSlots()
			.map(slot -> slot.getIngredientRenderer(ingredientType))
			.flatMap(Optional::stream)
			.map(IngredientListElementRenderer::getTypedIngredient);
	}

	public void set(int firstItemIndex, List<ITypedIngredient<?>> ingredientList) {
		this.guiIngredientSlots.set(firstItemIndex, ingredientList);
	}
}
