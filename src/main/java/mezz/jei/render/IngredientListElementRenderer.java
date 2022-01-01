package mezz.jei.render;

import com.google.common.base.Joiner;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.Internal;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.color.ColorNamer;
import mezz.jei.config.Constants;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.config.SearchMode;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.util.ErrorUtil;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IngredientListElementRenderer<T> {
	private static final int BLACKLIST_COLOR = 0xFFFF0000;
	private static final Rect2i DEFAULT_AREA = new Rect2i(0, 0, 16, 16);

	protected final IIngredientListElement<T> element;
	protected final IIngredientRenderer<T> ingredientRenderer;
	protected final IIngredientHelper<T> ingredientHelper;
	protected Rect2i area = DEFAULT_AREA;
	protected int padding;

	public IngredientListElementRenderer(IIngredientListElement<T> element) {
		this.element = element;
		T ingredient = element.getIngredient();
		IngredientManager ingredientManager = Internal.getIngredientManager();
		IIngredientType<T> ingredientType = ingredientManager.getIngredientType(ingredient);
		this.ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		this.ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
	}

	public void setArea(Rect2i area) {
		this.area = area;
	}

	public void setPadding(int padding) {
		this.padding = padding;
	}

	public IIngredientListElement<T> getElement() {
		return element;
	}

	public Rect2i getArea() {
		return area;
	}

	public void renderSlow(PoseStack poseStack, IEditModeConfig editModeConfig, IWorldConfig worldConfig) {
		if (worldConfig.isEditModeEnabled()) {
			renderEditMode(poseStack, area, padding, editModeConfig);
		}

		try {
			T ingredient = element.getIngredient();
			ingredientRenderer.render(poseStack, area.getX() + padding, area.getY() + padding, ingredient);
		} catch (RuntimeException | LinkageError e) {
			throw ErrorUtil.createRenderIngredientException(e, element.getIngredient());
		}
	}

	/**
	 * Matches the highlight code in {@link AbstractContainerScreen#renderSlotHighlight(PoseStack, int, int, int)} but with a custom area width and height
	 */
	public void drawHighlight(PoseStack poseStack) {
		RenderSystem.disableDepthTest();
		RenderSystem.colorMask(true, true, true, false);
		GuiComponent.fill(poseStack, area.getX(), area.getY(), area.getX() + area.getWidth(), area.getY() + area.getHeight(), 0x80FFFFFF);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableDepthTest();
	}

	public void drawTooltip(PoseStack poseStack, int mouseX, int mouseY, IIngredientFilterConfig ingredientFilterConfig, IWorldConfig worldConfig) {
		T ingredient = element.getIngredient();
		List<FormattedText> tooltip = getTooltip(ingredientFilterConfig, worldConfig);
		TooltipRenderer.drawHoveringText(poseStack, tooltip, mouseX, mouseY, ingredient);
	}

	protected void renderEditMode(PoseStack poseStack, Rect2i area, int padding, IEditModeConfig editModeConfig) {
		T ingredient = element.getIngredient();

		if (editModeConfig.isIngredientOnConfigBlacklist(ingredient, ingredientHelper)) {
			GuiComponent.fill(poseStack, area.getX() + padding, area.getY() + padding, area.getX() + 16 + padding, area.getY() + 16 + padding, BLACKLIST_COLOR);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		}
	}

	private List<FormattedText> getTooltip(IIngredientFilterConfig ingredientFilterConfig, IWorldConfig worldConfig) {
		T ingredient = element.getIngredient();
		IModIdHelper modIdHelper = Internal.getHelpers().getModIdHelper();
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
			addColorSearchInfoToTooltip(tooltip, maxWidth);
		}

		if (worldConfig.isEditModeEnabled()) {
			addEditModeInfoToTooltip(tooltip, maxWidth);
		}

		return tooltip;
	}

	private void addColorSearchInfoToTooltip(List<FormattedText> tooltip, int maxWidth) {
		ColorNamer colorNamer = Internal.getColorNamer();

		T ingredient = element.getIngredient();
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

	private static void addEditModeInfoToTooltip(List<FormattedText> tooltip, int maxWidth) {
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

}
