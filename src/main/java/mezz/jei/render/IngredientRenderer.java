package mezz.jei.render;

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
import mezz.jei.util.Log;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ReportedException;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IngredientRenderer<T> {
	private static final int BLACKLIST_COLOR_ITEM = Color.yellow.getRGB();
	private static final int BLACKLIST_COLOR_WILD = Color.red.getRGB();
	private static final int BLACKLIST_COLOR_MOD = Color.blue.getRGB();
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
			throw createRenderIngredientException(e, element);
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

		if (ingredient instanceof ItemStack) {
			ItemStack itemStack = (ItemStack) ingredient;
			TooltipRenderer.drawHoveringText(itemStack, minecraft, tooltip, mouseX, mouseY, fontRenderer);
		} else {
			TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY, fontRenderer);
		}
	}

	protected static <V> void renderEditMode(IIngredientListElement<V> element, Rectangle area, int padding) {
		V ingredient = element.getIngredient();
		IIngredientHelper<V> ingredientHelper = element.getIngredientHelper();

		if (Config.isIngredientOnConfigBlacklist(ingredient, Config.IngredientBlacklistType.ITEM, ingredientHelper)) {
			GuiScreen.drawRect(area.x + padding, area.y + padding, area.x + 8 + padding, area.y + 16 + padding, BLACKLIST_COLOR_ITEM);
			GlStateManager.color(1f, 1f, 1f, 1f);
		}
		if (Config.isIngredientOnConfigBlacklist(ingredient, Config.IngredientBlacklistType.WILDCARD, ingredientHelper)) {
			GuiScreen.drawRect(area.x + 8 + padding, area.y + padding, area.x + 16 + padding, area.y + 16 + padding, BLACKLIST_COLOR_WILD);
			GlStateManager.color(1f, 1f, 1f, 1f);
		}
		if (Config.isIngredientOnConfigBlacklist(ingredient, Config.IngredientBlacklistType.MOD_ID, ingredientHelper)) {
			GuiScreen.drawRect(area.x + padding, area.y + 8 + padding, area.x + 16 + padding, area.y + 16 + padding, BLACKLIST_COLOR_MOD);
			GlStateManager.color(1f, 1f, 1f, 1f);
		}
	}

	private static <V> List<String> getTooltip(Minecraft minecraft, IIngredientListElement<V> element) {
		List<String> tooltip = getIngredientTooltipSafe(minecraft, element);
		V ingredient = element.getIngredient();
		IIngredientHelper<V> ingredientHelper = element.getIngredientHelper();
		tooltip = ForgeModIdHelper.getInstance().addModNameToIngredientTooltip(tooltip, ingredient, ingredientHelper);
		if (Config.isDebugModeEnabled()) {
			tooltip.add(TextFormatting.GRAY + "JEI Debug ingredient info:");
			tooltip.add(TextFormatting.GRAY + ingredientHelper.getErrorInfo(ingredient));
		}

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
			addEditModeInfoToTooltip(minecraft, element, tooltip, maxWidth);
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

	private static <V> void addEditModeInfoToTooltip(Minecraft minecraft, IIngredientListElement<V> element, List<String> tooltip, int maxWidth) {
		V ingredient = element.getIngredient();
		IIngredientHelper<V> ingredientHelper = element.getIngredientHelper();

		tooltip.add("");
		tooltip.add(TextFormatting.ITALIC + Translator.translateToLocal("gui.jei.editMode.description"));

		String controlKeyLocalization = Translator.translateToLocal(Minecraft.IS_RUNNING_ON_MAC ? "key.jei.ctrl.mac" : "key.jei.ctrl");

		if (Config.isIngredientOnConfigBlacklist(ingredient, Config.IngredientBlacklistType.ITEM, ingredientHelper)) {
			String message = Translator.translateToLocal("gui.jei.editMode.description.show").replace("%CTRL", controlKeyLocalization);
			String description = TextFormatting.YELLOW + message;
			tooltip.addAll(minecraft.fontRenderer.listFormattedStringToWidth(description, maxWidth));
		} else {
			String message = Translator.translateToLocal("gui.jei.editMode.description.hide").replace("%CTRL", controlKeyLocalization);
			String description = TextFormatting.YELLOW + message;
			tooltip.addAll(minecraft.fontRenderer.listFormattedStringToWidth(description, maxWidth));
		}

		if (Config.isIngredientOnConfigBlacklist(ingredient, Config.IngredientBlacklistType.WILDCARD, ingredientHelper)) {
			String message = Translator.translateToLocal("gui.jei.editMode.description.show.wild").replace("%CTRL", controlKeyLocalization);
			String description = TextFormatting.RED + message;
			tooltip.addAll(minecraft.fontRenderer.listFormattedStringToWidth(description, maxWidth));
		} else {
			String message = Translator.translateToLocal("gui.jei.editMode.description.hide.wild").replace("%CTRL", controlKeyLocalization);
			String description = TextFormatting.RED + message;
			tooltip.addAll(minecraft.fontRenderer.listFormattedStringToWidth(description, maxWidth));
		}

		if (Config.isIngredientOnConfigBlacklist(ingredient, Config.IngredientBlacklistType.MOD_ID, ingredientHelper)) {
			String message = Translator.translateToLocal("gui.jei.editMode.description.show.mod.id").replace("%CTRL", controlKeyLocalization);
			String description = TextFormatting.BLUE + message;
			tooltip.addAll(minecraft.fontRenderer.listFormattedStringToWidth(description, maxWidth));
		} else {
			String message = Translator.translateToLocal("gui.jei.editMode.description.hide.mod.id").replace("%CTRL", controlKeyLocalization);
			String description = TextFormatting.BLUE + message;
			tooltip.addAll(minecraft.fontRenderer.listFormattedStringToWidth(description, maxWidth));
		}
	}

	protected static <T> ReportedException createRenderIngredientException(Throwable throwable, final IIngredientListElement<T> element) {
		final T ingredient = element.getIngredient();
		final IIngredientHelper<T> ingredientHelper = Internal.getIngredientRegistry().getIngredientHelper(ingredient);
		CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering ingredient");
		CrashReportCategory crashreportcategory = crashreport.makeCategory("Ingredient being rendered");
		crashreportcategory.addDetail("Ingredient Mod", () -> ForgeModIdHelper.getInstance().getModNameForIngredient(ingredient, ingredientHelper));
		crashreportcategory.addDetail("Ingredient Info", () -> ingredientHelper.getErrorInfo(ingredient));
		throw new ReportedException(crashreport);
	}
}
