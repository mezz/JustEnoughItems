package mezz.jei.render;

import javax.annotation.Nullable;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import mezz.jei.util.LegacyUtil;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.client.config.GuiUtils;

public class GuiIngredientFast {
	private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
	private static final int blacklistItemColor = Color.yellow.getRGB();
	private static final int blacklistWildColor = Color.red.getRGB();
	private static final int blacklistModColor = Color.blue.getRGB();

	private final Rectangle area;
	private final int padding;
	private final ItemModelMesher itemModelMesher;

	@Nullable
	private IIngredientListElement element;
	private boolean blocked = false;

	public GuiIngredientFast(int xPosition, int yPosition, int padding) {
		this.padding = padding;
		final int size = 16 + (2 * padding);
		this.area = new Rectangle(xPosition, yPosition, size, size);
		this.itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
	}

	public Rectangle getArea() {
		return area;
	}

	public void setElement(IIngredientListElement element) {
		this.element = element;
	}

	@Nullable
	public IIngredientListElement getElement() {
		return element;
	}

	public void clear() {
		this.element = null;
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return (element != null) && area.contains(mouseX, mouseY);
	}

	/**
	 * Set true if this ingredient is blocked by an extra gui area from a mod.
	 */
	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}

	/**
	 * true if this ingredient is blocked by an extra gui area from a mod.
	 */
	public boolean isBlocked() {
		return blocked;
	}

	public void renderItemAndEffectIntoGUI() {
		if (element == null) {
			return;
		}

		Object ingredient = element.getIngredient();
		if (!(ingredient instanceof ItemStack)) {
			return;
		}

		try {
			//noinspection unchecked
			renderItemAndEffectIntoGUI((IIngredientListElement<ItemStack>) element);
		} catch (RuntimeException e) {
			throw createRenderIngredientException(e, element);
		} catch (LinkageError e) {
			throw createRenderIngredientException(e, element);
		}
	}

	private void renderItemAndEffectIntoGUI(IIngredientListElement<ItemStack> element) {
		ItemStack itemStack = element.getIngredient();
		IBakedModel bakedModel = itemModelMesher.getItemModel(itemStack);
		bakedModel = bakedModel.getOverrides().handleItemState(bakedModel, itemStack, null, null);

		if (Config.isEditModeEnabled()) {
			renderEditMode(element, area, padding);
			GlStateManager.enableBlend();
		}

		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(area.x + padding + 8.0f, area.y + padding + 8.0f, 150.0F);
			GlStateManager.scale(16F, -16F, 16F);

			bakedModel = ForgeHooksClient.handleCameraTransforms(bakedModel, ItemCameraTransforms.TransformType.GUI, false);

			GlStateManager.translate(-0.5F, -0.5F, -0.5F);

			Minecraft minecraft = Minecraft.getMinecraft();
			RenderItem renderItem = minecraft.getRenderItem();
			renderItem.renderModel(bakedModel, itemStack);

			if (itemStack.hasEffect()) {
				renderEffect(bakedModel);
			}
		}
		GlStateManager.popMatrix();
	}

	private void renderEffect(IBakedModel model) {
		Minecraft minecraft = Minecraft.getMinecraft();
		TextureManager textureManager = minecraft.getTextureManager();
		RenderItem renderItem = minecraft.getRenderItem();

		GlStateManager.depthMask(false);
		GlStateManager.depthFunc(514);
		GlStateManager.blendFunc(768, 1);
		textureManager.bindTexture(RES_ITEM_GLINT);
		GlStateManager.matrixMode(5890);

		GlStateManager.pushMatrix();
		GlStateManager.scale(8.0F, 8.0F, 8.0F);
		float f = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
		GlStateManager.translate(f, 0.0F, 0.0F);
		GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
		renderItem.renderModel(model, -8372020);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.scale(8.0F, 8.0F, 8.0F);
		float f1 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
		GlStateManager.translate(-f1, 0.0F, 0.0F);
		GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
		renderItem.renderModel(model, -8372020);
		GlStateManager.popMatrix();

		GlStateManager.matrixMode(5888);
		GlStateManager.blendFunc(770, 771);
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);
		textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
	}

	public void renderSlow() {
		if (element != null) {
			if (Config.isEditModeEnabled()) {
				renderEditMode(element, area, padding);
			}

			try {
				renderSlow(element, area, padding);
			} catch (RuntimeException e) {
				throw createRenderIngredientException(e, element);
			} catch (LinkageError e) {
				throw createRenderIngredientException(e, element);
			}
		}
	}

	private static <T> void renderSlow(IIngredientListElement<T> element, Rectangle area, int padding) {
		IIngredientRenderer<T> ingredientRenderer = element.getIngredientRenderer();
		T ingredient = element.getIngredient();
		ingredientRenderer.render(Minecraft.getMinecraft(), area.x + padding, area.y + padding, ingredient);
	}

	public void renderOverlay(Minecraft minecraft) {
		if (element == null) {
			return;
		}

		Object ingredient = element.getIngredient();
		if (!(ingredient instanceof ItemStack)) {
			return;
		}

		ItemStack itemStack = (ItemStack) ingredient;
		try {
			renderOverlay(minecraft, itemStack);
		} catch (RuntimeException e) {
			throw createRenderIngredientException(e, element);
		} catch (LinkageError e) {
			throw createRenderIngredientException(e, element);
		}
	}

	private void renderOverlay(Minecraft minecraft, ItemStack itemStack) {
		FontRenderer font = getFontRenderer(minecraft, itemStack);
		RenderItem renderItem = minecraft.getRenderItem();
		renderItem.renderItemOverlayIntoGUI(font, itemStack, area.x + padding, area.y + padding, null);
	}

	private static <V> void renderEditMode(IIngredientListElement<V> element, Rectangle area, int padding) {
		V ingredient = element.getIngredient();
		IIngredientHelper ingredientHelper = element.getIngredientHelper();

		if (Config.isIngredientOnConfigBlacklist(ingredient, Config.IngredientBlacklistType.ITEM, ingredientHelper)) {
			GuiScreen.drawRect(area.x + padding, area.y + padding, area.x + 8 + padding, area.y + 16 + padding, blacklistItemColor);
			GlStateManager.color(1f, 1f, 1f, 1f);
		}
		if (Config.isIngredientOnConfigBlacklist(ingredient, Config.IngredientBlacklistType.WILDCARD, ingredientHelper)) {
			GuiScreen.drawRect(area.x + 8 + padding, area.y + padding, area.x + 16 + padding, area.y + 16 + padding, blacklistWildColor);
			GlStateManager.color(1f, 1f, 1f, 1f);
		}
		if (Config.isIngredientOnConfigBlacklist(ingredient, Config.IngredientBlacklistType.MOD_ID, ingredientHelper)) {
			GuiScreen.drawRect(area.x + padding, area.y + 8 + padding, area.x + 16 + padding, area.y + 16 + padding, blacklistModColor);
			GlStateManager.color(1f, 1f, 1f, 1f);
		}
	}

	public static FontRenderer getFontRenderer(Minecraft minecraft, ItemStack itemStack) {
		Item item = itemStack.getItem();
		FontRenderer fontRenderer = item.getFontRenderer(itemStack);
		if (fontRenderer == null) {
			fontRenderer = minecraft.fontRenderer;
		}
		return fontRenderer;
	}

	public void drawHovered(Minecraft minecraft) {
		if (element == null) {
			return;
		}

		renderSlow();
		renderOverlay(minecraft);
		drawHighlight();
	}

	/**
	 * Matches the highlight code in {@link GuiContainer#drawScreen(int, int, float)}
	 */
	public void drawHighlight() {
		if (element == null) {
			return;
		}

		GlStateManager.disableLighting();
		GlStateManager.disableDepth();
		GlStateManager.colorMask(true, true, true, false);
		GuiUtils.drawGradientRect(0, area.x, area.y, area.x + area.width, area.y + area.height, 0x80FFFFFF, 0x80FFFFFF);
		GlStateManager.colorMask(true, true, true, true);
		GlStateManager.enableDepth();
	}

	public void drawTooltip(Minecraft minecraft, int mouseX, int mouseY) {
		if (element == null) {
			return;
		}

		drawTooltip(minecraft, element, mouseX, mouseY);
	}

	private static <V> void drawTooltip(Minecraft minecraft, IIngredientListElement<V> element, int mouseX, int mouseY) {
		V ingredient = element.getIngredient();
		IIngredientRenderer<V> ingredientRenderer = element.getIngredientRenderer();
		List<String> tooltip = getTooltip(minecraft, element);
		FontRenderer fontRenderer = ingredientRenderer.getFontRenderer(minecraft, ingredient);

		if (ingredient instanceof ItemStack) {
			ItemStack itemStack = (ItemStack) ingredient;
			TooltipRenderer.drawHoveringText(itemStack, minecraft, tooltip, mouseX, mouseY, fontRenderer);
		} else {
			TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY, fontRenderer);
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
			addEditModeInfoToTooltip(minecraft, element, tooltip, maxWidth);
		}

		return tooltip;
	}

	private static <V> List<String> getIngredientTooltipSafe(Minecraft minecraft, IIngredientListElement<V> element) {
		IIngredientRenderer<V> ingredientRenderer = element.getIngredientRenderer();
		V ingredient = element.getIngredient();
		try {
			return LegacyUtil.getTooltip(ingredientRenderer, minecraft, ingredient, minecraft.gameSettings.advancedItemTooltips);
		} catch (RuntimeException e) {
			Log.error("Tooltip crashed.", e);
		} catch (LinkageError e) {
			Log.error("Tooltip crashed.", e);
		}

		List<String> tooltip = new ArrayList<String>();
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

	private static <T> ReportedException createRenderIngredientException(Throwable throwable, final IIngredientListElement<T> element) {
		final T ingredient = element.getIngredient();
		final IIngredientHelper<T> ingredientHelper = Internal.getIngredientRegistry().getIngredientHelper(ingredient);
		CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering ingredient");
		CrashReportCategory crashreportcategory = crashreport.makeCategory("Ingredient being rendered");
		crashreportcategory.setDetail("Ingredient Mod", new ICrashReportDetail<String>() {
			@Override
			public String call() throws Exception {
				return ForgeModIdHelper.getInstance().getModNameForIngredient(ingredient, ingredientHelper);
			}
		});
		crashreportcategory.setDetail("Ingredient Info", new ICrashReportDetail<String>() {
			@Override
			public String call() throws Exception {
				return ingredientHelper.getErrorInfo(ingredient);
			}
		});
		throw new ReportedException(crashreport);
	}
}
