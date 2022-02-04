package mezz.jei.gui.overlay;

import com.google.common.base.Joiner;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.Internal;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.color.ColorNamer;
import mezz.jei.config.Constants;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.config.SearchMode;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.ingredients.GuiIngredientProperties;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.ingredients.IngredientInfo;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IRecipeFocusSource;
import mezz.jei.input.MouseUtil;
import mezz.jei.render.IngredientListBatchRenderer;
import mezz.jei.render.IngredientListElementRenderer;
import mezz.jei.render.IngredientListSlot;
import mezz.jei.render.IngredientRenderHelper;
import mezz.jei.util.GiveMode;
import mezz.jei.util.MathUtil;
import mezz.jei.util.StringUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * An ingredient grid displays a rectangular area of clickable recipe ingredients.
 */
public class IngredientGrid implements IRecipeFocusSource {
	private static final int INGREDIENT_PADDING = 1;
	public static final int INGREDIENT_WIDTH = GuiIngredientProperties.getWidth(INGREDIENT_PADDING);
	public static final int INGREDIENT_HEIGHT = GuiIngredientProperties.getHeight(INGREDIENT_PADDING);
	private final IngredientManager ingredientManager;
	private final GridAlignment alignment;
	private final RecipesGui recipesGui;
	private final IModIdHelper modIdHelper;
	private final GuiScreenHelper guiScreenHelper;

	private Rect2i area = new Rect2i(0, 0, 0, 0);
	protected final IngredientListBatchRenderer guiIngredientSlots;
	private final IIngredientFilterConfig ingredientFilterConfig;
	private final IClientConfig clientConfig;
	private final IWorldConfig worldConfig;

	public IngredientGrid(
		IngredientManager ingredientManager,
		GridAlignment alignment,
		IEditModeConfig editModeConfig,
		IIngredientFilterConfig ingredientFilterConfig,
		IClientConfig clientConfig,
		IWorldConfig worldConfig,
		GuiScreenHelper guiScreenHelper,
		RecipesGui recipesGui,
		IModIdHelper modIdHelper
	) {
		this.ingredientManager = ingredientManager;
		this.alignment = alignment;
		this.recipesGui = recipesGui;
		this.modIdHelper = modIdHelper;
		this.guiIngredientSlots = new IngredientListBatchRenderer(clientConfig, editModeConfig, worldConfig, ingredientManager);
		this.ingredientFilterConfig = ingredientFilterConfig;
		this.clientConfig = clientConfig;
		this.worldConfig = worldConfig;
		this.guiScreenHelper = guiScreenHelper;
	}

	public int size() {
		return this.guiIngredientSlots.size();
	}

	public int maxWidth() {
		final int columns = this.clientConfig.getMaxColumns();
		final int ingredientsWidth = columns * INGREDIENT_WIDTH;
		final int minWidth = this.clientConfig.getMinColumns() * INGREDIENT_WIDTH;
		return Math.max(ingredientsWidth, minWidth);
	}

	public boolean updateBounds(Rect2i availableArea, Collection<Rect2i> exclusionAreas) {
		final int columns = Math.min(availableArea.getWidth() / INGREDIENT_WIDTH, this.clientConfig.getMaxColumns());
		final int rows = availableArea.getHeight() / INGREDIENT_HEIGHT;

		final int ingredientsWidth = columns * INGREDIENT_WIDTH;
		final int minWidth = this.clientConfig.getMinColumns() * INGREDIENT_WIDTH;
		final int width = Math.max(ingredientsWidth, minWidth);
		final int height = rows * INGREDIENT_HEIGHT;
		final int x;
		if (alignment == GridAlignment.LEFT) {
			x = availableArea.getX() + (availableArea.getWidth() - width);
		} else {
			x = availableArea.getX();
		}
		final int y = availableArea.getY() + (availableArea.getHeight() - height) / 2;
		final int xOffset = x + Math.max(0, (width - ingredientsWidth) / 2);

		this.area = new Rect2i(x, y, width, height);
		this.guiIngredientSlots.clear();

		if (rows == 0 || columns < this.clientConfig.getMinColumns()) {
			return false;
		}

		for (int row = 0; row < rows; row++) {
			int y1 = y + (row * INGREDIENT_HEIGHT);
			for (int column = 0; column < columns; column++) {
				int x1 = xOffset + (column * INGREDIENT_WIDTH);
				IngredientListSlot ingredientListSlot = new IngredientListSlot(x1, y1, INGREDIENT_PADDING);
				Rect2i stackArea = ingredientListSlot.getArea();
				final boolean blocked = MathUtil.intersects(exclusionAreas, stackArea);
				ingredientListSlot.setBlocked(blocked);
				this.guiIngredientSlots.add(ingredientListSlot);
			}
		}
		return true;
	}

	public Rect2i getArea() {
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
	public static void drawHighlight(PoseStack poseStack, Rect2i area) {
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
					.ifPresent(ingredient -> drawTooltip(poseStack, mouseX, mouseY, ingredientFilterConfig, worldConfig, ingredient));
			}
		}
	}

	private <T> void drawTooltip(PoseStack poseStack, int mouseX, int mouseY, IIngredientFilterConfig ingredientFilterConfig, IWorldConfig worldConfig, ITypedIngredient<T> value) {
		IIngredientType<T> ingredientType = value.getType();
		T ingredient = value.getIngredient();
		IngredientInfo<T> ingredientInfo = ingredientManager.getIngredientInfo(ingredientType);
		IIngredientRenderer<T> ingredientRenderer = ingredientInfo.getIngredientRenderer();

		List<FormattedText> tooltip = getTooltip(ingredientFilterConfig, worldConfig, ingredient, ingredientInfo);
		TooltipRenderer.drawHoveringText(poseStack, tooltip, mouseX, mouseY, ingredient, ingredientRenderer);
	}

	private <T> List<FormattedText> getTooltip(
		IIngredientFilterConfig ingredientFilterConfig,
		IWorldConfig worldConfig,
		T ingredient,
		IngredientInfo<T> ingredientInfo
	) {
		IIngredientRenderer<T> ingredientRenderer = ingredientInfo.getIngredientRenderer();
		IIngredientHelper<T> ingredientHelper = ingredientInfo.getIngredientHelper();
		List<Component> ingredientTooltipSafe = IngredientRenderHelper.getIngredientTooltipSafe(ingredient, ingredientRenderer, ingredientHelper, modIdHelper);
		List<FormattedText> tooltip = new ArrayList<>(ingredientTooltipSafe);

		Minecraft minecraft = Minecraft.getInstance();
		int maxWidth = Constants.MAX_TOOLTIP_WIDTH;
		for (FormattedText tooltipLine : tooltip) {
			int width = minecraft.font.width(tooltipLine);
			if (width > maxWidth) {
				maxWidth = width;
			}
		}

		if (ingredientFilterConfig.getColorSearchMode() != SearchMode.DISABLED) {
			addColorSearchInfoToTooltip(tooltip, maxWidth, ingredient, ingredientInfo);
		}

		if (worldConfig.isEditModeEnabled()) {
			addEditModeInfoToTooltip(tooltip, maxWidth);
		}

		return tooltip;
	}

	public static <T> void addColorSearchInfoToTooltip(List<FormattedText> tooltip, int maxWidth, T ingredient, IngredientInfo<T> ingredientInfo) {
		ColorNamer colorNamer = Internal.getColorNamer();

		IIngredientHelper<T> ingredientHelper = ingredientInfo.getIngredientHelper();
		Iterable<Integer> colors = ingredientHelper.getColors(ingredient);
		Collection<String> colorNames = colorNamer.getColorNames(colors, false);
		if (!colorNames.isEmpty()) {
			String colorNamesString = Joiner.on(", ").join(colorNames);
			Component colorTranslation = new TranslatableComponent("jei.tooltip.item.colors", colorNamesString)
				.withStyle(ChatFormatting.GRAY);
			List<FormattedText> lines = StringUtil.splitLines(colorTranslation, maxWidth);
			tooltip.addAll(lines);
		}
	}

	public static void addEditModeInfoToTooltip(List<FormattedText> tooltip, int maxWidth) {
		List<FormattedText> lines = List.of(
			TextComponent.EMPTY,
			new TranslatableComponent("gui.jei.editMode.description")
				.withStyle(ChatFormatting.DARK_GREEN),
			new TranslatableComponent(
				"gui.jei.editMode.description.hide",
				KeyBindings.toggleHideIngredient.getTranslatedKeyMessage()
			).withStyle(ChatFormatting.GRAY),
			new TranslatableComponent(
				"gui.jei.editMode.description.hide.wild",
				KeyBindings.toggleWildcardHideIngredient.getTranslatedKeyMessage()
			).withStyle(ChatFormatting.GRAY)
		);
		lines = StringUtil.splitLines(lines, maxWidth);
		tooltip.addAll(lines);
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
		if (this.recipesGui.isOpen()) {
			return false;
		}
		GiveMode giveMode = this.clientConfig.getGiveMode();
		if (giveMode == GiveMode.MOUSE_PICKUP) {
			return getIngredientUnderMouse(mouseX, mouseY)
				.map(IClickedIngredient::getCheatItemStack)
				.map(i -> !ItemHandlerHelper.canItemStacksStack(itemStack, i))
				.orElse(false);
		}
		return true;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return MathUtil.contains(area, mouseX, mouseY) &&
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

}
