package mezz.jei.render;

import java.util.Collection;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.text.TextFormatting;

import com.google.common.base.Joiner;
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
import mezz.jei.config.SearchMode;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Translator;

public class IngredientListElementRenderer<T> {
	private static final int BLACKLIST_COLOR = 0xFFFF0000;
	private static final Rectangle2d DEFAULT_AREA = new Rectangle2d(0, 0, 16, 16);

	protected final IIngredientListElement<T> element;
	protected final IIngredientRenderer<T> ingredientRenderer;
	protected final IIngredientHelper<T> ingredientHelper;
	protected Rectangle2d area = DEFAULT_AREA;
	protected int padding;

	public IngredientListElementRenderer(IIngredientListElement<T> element) {
		this.element = element;
		T ingredient = element.getIngredient();
		IngredientManager ingredientManager = Internal.getIngredientManager();
		IIngredientType<T> ingredientType = ingredientManager.getIngredientType(ingredient);
		this.ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		this.ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
	}

	public void setArea(Rectangle2d area) {
		this.area = area;
	}

	public void setPadding(int padding) {
		this.padding = padding;
	}

	public IIngredientListElement<T> getElement() {
		return element;
	}

	public Rectangle2d getArea() {
		return area;
	}

	public void renderSlow(IEditModeConfig editModeConfig, IWorldConfig worldConfig) {
		if (worldConfig.isEditModeEnabled()) {
			renderEditMode(area, padding, editModeConfig);
		}

		try {
			T ingredient = element.getIngredient();
			ingredientRenderer.render(area.getX() + padding, area.getY() + padding, ingredient);
		} catch (RuntimeException | LinkageError e) {
			throw ErrorUtil.createRenderIngredientException(e, element.getIngredient());
		}
	}

	/**
	 * Matches the highlight code in {@link ContainerScreen#render(int, int, float)}
	 */
	public void drawHighlight() {
		RenderSystem.disableLighting();
		RenderSystem.disableDepthTest();
		RenderSystem.colorMask(true, true, true, false);
		GuiUtils.drawGradientRect(0, area.getX(), area.getY(), area.getX() + area.getWidth(), area.getY() + area.getHeight(), 0x80FFFFFF, 0x80FFFFFF);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableDepthTest();
	}

	public void drawTooltip(int mouseX, int mouseY, IIngredientFilterConfig ingredientFilterConfig, IWorldConfig worldConfig) {
		T ingredient = element.getIngredient();
		List<String> tooltip = getTooltip(ingredientFilterConfig, worldConfig);
		Minecraft minecraft = Minecraft.getInstance();
		FontRenderer fontRenderer = ingredientRenderer.getFontRenderer(minecraft, ingredient);
		TooltipRenderer.drawHoveringText(ingredient, tooltip, mouseX, mouseY, fontRenderer);
	}

	protected void renderEditMode(Rectangle2d area, int padding, IEditModeConfig editModeConfig) {
		T ingredient = element.getIngredient();

		if (editModeConfig.isIngredientOnConfigBlacklist(ingredient, ingredientHelper)) {
			Screen.fill(area.getX() + padding, area.getY() + padding, area.getX() + 16 + padding, area.getY() + 16 + padding, BLACKLIST_COLOR);
			RenderSystem.color4f(1f, 1f, 1f, 1f);
		}
	}

	private List<String> getTooltip(IIngredientFilterConfig ingredientFilterConfig, IWorldConfig worldConfig) {
		T ingredient = element.getIngredient();
		IModIdHelper modIdHelper = Internal.getHelpers().getModIdHelper();
		List<String> tooltip = IngredientRenderHelper.getIngredientTooltipSafe(ingredient, ingredientRenderer, ingredientHelper, modIdHelper);

		Minecraft minecraft = Minecraft.getInstance();
		int maxWidth = Constants.MAX_TOOLTIP_WIDTH;
		for (String tooltipLine : tooltip) {
			int width = minecraft.fontRenderer.getStringWidth(tooltipLine);
			if (width > maxWidth) {
				maxWidth = width;
			}
		}

		if (ingredientFilterConfig.getColorSearchMode() != SearchMode.DISABLED) {
			addColorSearchInfoToTooltip(minecraft, tooltip, maxWidth);
		}

		if (worldConfig.isEditModeEnabled()) {
			addEditModeInfoToTooltip(minecraft, tooltip, maxWidth);
		}

		return tooltip;
	}

	private void addColorSearchInfoToTooltip(Minecraft minecraft, List<String> tooltip, int maxWidth) {
		ColorNamer colorNamer = Internal.getColorNamer();

		T ingredient = element.getIngredient();
		Iterable<Integer> colors = ingredientHelper.getColors(ingredient);
		Collection<String> colorNames = colorNamer.getColorNames(colors, false);
		if (!colorNames.isEmpty()) {
			String colorNamesString = Joiner.on(", ").join(colorNames);
			String colorNamesLocalizedString = TextFormatting.GRAY + Translator.translateToLocalFormatted("jei.tooltip.item.colors", colorNamesString);
			tooltip.addAll(minecraft.fontRenderer.listFormattedStringToWidth(colorNamesLocalizedString, maxWidth));
		}
	}

	private static void addEditModeInfoToTooltip(Minecraft minecraft, List<String> tooltip, int maxWidth) {
		tooltip.add("");
		tooltip.add(TextFormatting.DARK_GREEN + Translator.translateToLocal("gui.jei.editMode.description"));

		String controlKeyLocalization = Translator.translateToLocal(Minecraft.IS_RUNNING_ON_MAC ? "key.jei.ctrl.mac" : "key.jei.ctrl");

		String hideMessage = TextFormatting.GRAY + Translator.translateToLocalFormatted("gui.jei.editMode.description.hide", controlKeyLocalization);
		tooltip.addAll(minecraft.fontRenderer.listFormattedStringToWidth(hideMessage, maxWidth));

		String hideWildMessage = TextFormatting.GRAY + Translator.translateToLocalFormatted("gui.jei.editMode.description.hide.wild", controlKeyLocalization);
		tooltip.addAll(minecraft.fontRenderer.listFormattedStringToWidth(hideWildMessage, maxWidth));
	}

}
