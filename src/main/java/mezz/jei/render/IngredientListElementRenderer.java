package mezz.jei.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.AbstractGui;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.Style;
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

	public void renderSlow(MatrixStack matrixStack, IEditModeConfig editModeConfig, IWorldConfig worldConfig) {
		if (worldConfig.isEditModeEnabled()) {
			renderEditMode(matrixStack, area, padding, editModeConfig);
		}

		try {
			T ingredient = element.getIngredient();
			ingredientRenderer.render(matrixStack, area.getX() + padding, area.getY() + padding, ingredient);
		} catch (RuntimeException | LinkageError e) {
			throw ErrorUtil.createRenderIngredientException(e, element.getIngredient());
		}
	}

	/**
	 * Matches the highlight code in {@link ContainerScreen#render(MatrixStack, int, int, float)}
	 */
	public void drawHighlight(MatrixStack matrixStack) {
		RenderSystem.disableLighting();
		RenderSystem.disableDepthTest();
		RenderSystem.colorMask(true, true, true, false);
		GuiUtils.drawGradientRect(matrixStack.getLast().getMatrix(), 0, area.getX(), area.getY(), area.getX() + area.getWidth(), area.getY() + area.getHeight(), 0x80FFFFFF, 0x80FFFFFF);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableDepthTest();
	}

	public void drawTooltip(MatrixStack matrixStack, int mouseX, int mouseY, IIngredientFilterConfig ingredientFilterConfig, IWorldConfig worldConfig) {
		T ingredient = element.getIngredient();
		List<ITextProperties> tooltip = getTooltip(ingredientFilterConfig, worldConfig);
		Minecraft minecraft = Minecraft.getInstance();
		FontRenderer fontRenderer = ingredientRenderer.getFontRenderer(minecraft, ingredient);
		TooltipRenderer.drawHoveringText(ingredient, tooltip, mouseX, mouseY, fontRenderer, matrixStack);
	}

	protected void renderEditMode(MatrixStack matrixStack, Rectangle2d area, int padding, IEditModeConfig editModeConfig) {
		T ingredient = element.getIngredient();

		if (editModeConfig.isIngredientOnConfigBlacklist(ingredient, ingredientHelper)) {
			AbstractGui.fill(matrixStack, area.getX() + padding, area.getY() + padding, area.getX() + 16 + padding, area.getY() + 16 + padding, BLACKLIST_COLOR);
			RenderSystem.color4f(1f, 1f, 1f, 1f);
		}
	}

	private List<ITextProperties> getTooltip(IIngredientFilterConfig ingredientFilterConfig, IWorldConfig worldConfig) {
		T ingredient = element.getIngredient();
		IModIdHelper modIdHelper = Internal.getHelpers().getModIdHelper();
		List<ITextComponent> ingredientTooltipSafe = IngredientRenderHelper.getIngredientTooltipSafe(ingredient, ingredientRenderer, ingredientHelper, modIdHelper);
		List<ITextProperties> tooltip = new ArrayList<>(ingredientTooltipSafe);

		Minecraft minecraft = Minecraft.getInstance();
		int maxWidth = Constants.MAX_TOOLTIP_WIDTH;
		for (ITextProperties tooltipLine : tooltip) {
			int width = minecraft.fontRenderer.func_238414_a_(tooltipLine);
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

	private void addColorSearchInfoToTooltip(Minecraft minecraft, List<ITextProperties> tooltip, int maxWidth) {
		ColorNamer colorNamer = Internal.getColorNamer();

		T ingredient = element.getIngredient();
		Iterable<Integer> colors = ingredientHelper.getColors(ingredient);
		Collection<String> colorNames = colorNamer.getColorNames(colors, false);
		if (!colorNames.isEmpty()) {
			String colorNamesString = Joiner.on(", ").join(colorNames);
			TranslationTextComponent colorTranslation = new TranslationTextComponent("jei.tooltip.item.colors", colorNamesString);
			IFormattableTextComponent colorNamesLocalizedString = colorTranslation.mergeStyle(TextFormatting.GRAY);
			tooltip.addAll(minecraft.fontRenderer.func_238420_b_().func_238362_b_(colorNamesLocalizedString, maxWidth, Style.EMPTY));
		}
	}

	private static void addEditModeInfoToTooltip(Minecraft minecraft, List<ITextProperties> tooltip, int maxWidth) {
		tooltip.add(StringTextComponent.EMPTY);
		TranslationTextComponent description = new TranslationTextComponent("gui.jei.editMode.description");
		tooltip.add(description.mergeStyle(TextFormatting.DARK_GREEN));

		TranslationTextComponent controlKeyLocalization = new TranslationTextComponent(Minecraft.IS_RUNNING_ON_MAC ? "key.jei.ctrl.mac" : "key.jei.ctrl");

		TranslationTextComponent hide = new TranslationTextComponent("gui.jei.editMode.description.hide", controlKeyLocalization);
		IFormattableTextComponent hideMessage = hide.mergeStyle(TextFormatting.GRAY);
		tooltip.addAll(minecraft.fontRenderer.func_238420_b_().func_238362_b_(hideMessage, maxWidth, Style.EMPTY));

		TranslationTextComponent hideWild = new TranslationTextComponent("gui.jei.editMode.description.hide.wild", controlKeyLocalization);
		IFormattableTextComponent hideWildMessage = hideWild.mergeStyle(TextFormatting.GRAY);
		tooltip.addAll(minecraft.fontRenderer.func_238420_b_().func_238362_b_(hideWildMessage, maxWidth, Style.EMPTY));
	}

}
