package mezz.jei.render;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import com.google.common.base.Joiner;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.color.ColorNamer;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.startup.ForgeModIdHelper;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;

public class IngredientRenderer<T> {
	private static final int BLACKLIST_COLOR = Color.red.getRGB();
	private static final Rectangle DEFAULT_AREA = new Rectangle(0, 0, 16, 16);

	protected final IIngredientListElement<T> element;
	protected Rectangle area = DEFAULT_AREA;
	protected int padding;

	public IngredientRenderer(IIngredientListElement<T> element) {
		this.element = element;
	}

	public void setArea(Rectangle area) {
		this.area = area;
	}

	public void setPadding(int padding) {
		this.padding = padding;
	}

	public IIngredientListElement<T> getElement() {
		return element;
	}

	public Rectangle getArea() {
		return area;
	}

	public void renderSlow() {
		if (Config.isEditModeEnabled()) {
			renderEditMode(element, area, padding);
		}

		try {
			IIngredientRenderer<T> ingredientRenderer = element.getIngredientRenderer();
			T ingredient = element.getIngredient();
			ingredientRenderer.render(Minecraft.getMinecraft(), area.x + padding, area.y + padding, ingredient);
		} catch (RuntimeException | LinkageError e) {
			throw ErrorUtil.createRenderIngredientException(e, element.getIngredient());
		}
	}

	/**
	 * Matches the highlight code in {@link GuiContainer#drawScreen(int, int, float)}
	 */
	public void drawHighlight() {
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();
		GlStateManager.colorMask(true, true, true, false);
		GuiUtils.drawGradientRect(0, area.x, area.y, area.x + area.width, area.y + area.height, 0x80FFFFFF, 0x80FFFFFF);
		GlStateManager.colorMask(true, true, true, true);
		GlStateManager.enableDepth();
	}

	public void drawTooltip(Minecraft minecraft, int mouseX, int mouseY) {
		T ingredient = element.getIngredient();
		IIngredientRenderer<T> ingredientRenderer = element.getIngredientRenderer();
		List<String> tooltip = getTooltip(minecraft, element);
		FontRenderer fontRenderer = ingredientRenderer.getFontRenderer(minecraft, ingredient);

		IIngredientHelper<T> ingredientHelper = element.getIngredientHelper();
		ItemStack itemStack = ingredientHelper.getCheatItemStack(ingredient);
		TooltipRenderer.drawHoveringText(itemStack, minecraft, tooltip, mouseX, mouseY, fontRenderer);
	}

	protected static <V> void renderEditMode(IIngredientListElement<V> element, Rectangle area, int padding) {
		V ingredient = element.getIngredient();
		IIngredientHelper<V> ingredientHelper = element.getIngredientHelper();

		if (Config.isIngredientOnConfigBlacklist(ingredient, ingredientHelper)) {
			GuiScreen.drawRect(area.x + padding, area.y + padding, area.x + 16 + padding, area.y + 16 + padding, BLACKLIST_COLOR);
			GlStateManager.color(1f, 1f, 1f, 1f);
		}
	}

	private static <V> List<String> getTooltip(Minecraft minecraft, IIngredientListElement<V> element) {
		List<String> tooltip = getIngredientTooltipSafe(minecraft, element);
		V ingredient = element.getIngredient();
		IIngredientHelper<V> ingredientHelper = element.getIngredientHelper();
		tooltip = ForgeModIdHelper.getInstance().addModNameToIngredientTooltip(tooltip, ingredient, ingredientHelper);

		int maxWidth = Constants.MAX_TOOLTIP_WIDTH;
		for (String tooltipLine : tooltip) {
			int width = minecraft.fontRenderer.getStringWidth(tooltipLine);
			if (width > maxWidth) {
				maxWidth = width;
			}
		}

		if (Config.getColorSearchMode() != Config.SearchMode.DISABLED) {
			addColorSearchInfoToTooltip(minecraft, element, tooltip, maxWidth);
		}

		if (Config.isEditModeEnabled()) {
			addEditModeInfoToTooltip(minecraft, tooltip, maxWidth);
		}

		return tooltip;
	}

	private static <V> List<String> getIngredientTooltipSafe(Minecraft minecraft, IIngredientListElement<V> element) {
		IIngredientRenderer<V> ingredientRenderer = element.getIngredientRenderer();
		V ingredient = element.getIngredient();
		try {
			ITooltipFlag.TooltipFlags tooltipFlag = minecraft.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;
			return ingredientRenderer.getTooltip(minecraft, ingredient, tooltipFlag);
		} catch (RuntimeException | LinkageError e) {
			Log.get().error("Tooltip crashed.", e);
		}

		List<String> tooltip = new ArrayList<>();
		tooltip.add(TextFormatting.RED + Translator.translateToLocal("jei.tooltip.error.crash"));
		return tooltip;
	}

	private static <V> void addColorSearchInfoToTooltip(Minecraft minecraft, IIngredientListElement<V> element, List<String> tooltip, int maxWidth) {
		ColorNamer colorNamer = Internal.getColorNamer();

		V ingredient = element.getIngredient();
		IIngredientHelper<V> ingredientHelper = element.getIngredientHelper();
		Iterable<Color> colors = ingredientHelper.getColors(ingredient);
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

		String hideMessage = TextFormatting.GRAY + Translator.translateToLocal("gui.jei.editMode.description.hide").replace("%CTRL", controlKeyLocalization);
		tooltip.addAll(minecraft.fontRenderer.listFormattedStringToWidth(hideMessage, maxWidth));

		String hideWildMessage = TextFormatting.GRAY + Translator.translateToLocal("gui.jei.editMode.description.hide.wild").replace("%CTRL", controlKeyLocalization);
		tooltip.addAll(minecraft.fontRenderer.listFormattedStringToWidth(hideWildMessage, maxWidth));
	}

}
