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
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.color.ColorNamer;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.ingredients.IngredientRegistry;
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
	private Object ingredient;
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

	public void setIngredient(Object ingredient) {
		this.ingredient = ingredient;
	}

	@Nullable
	public Object getIngredient() {
		return ingredient;
	}

	public void clear() {
		this.ingredient = null;
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return (ingredient != null) && area.contains(mouseX, mouseY);
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
		if (ingredient == null) {
			return;
		}

		if (!(ingredient instanceof ItemStack)) {
			return;
		}

		final ItemStack itemStack = (ItemStack) ingredient;

		try {
			renderItemAndEffectIntoGUI(itemStack);
		} catch (RuntimeException e) {
			throw createRenderIngredientException(e, itemStack);
		} catch (LinkageError e) {
			throw createRenderIngredientException(e, itemStack);
		}
	}

	private void renderItemAndEffectIntoGUI(ItemStack itemStack) {
		IBakedModel bakedModel = itemModelMesher.getItemModel(itemStack);
		bakedModel = bakedModel.getOverrides().handleItemState(bakedModel, itemStack, null, null);

		if (Config.isEditModeEnabled()) {
			renderEditMode(itemStack, area, padding);
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
		if (ingredient != null) {
			if (Config.isEditModeEnabled()) {
				renderEditMode(ingredient, area, padding);
			}

			try {
				renderSlow(ingredient, area, padding);
			} catch (RuntimeException e) {
				throw createRenderIngredientException(e, ingredient);
			} catch (LinkageError e) {
				throw createRenderIngredientException(e, ingredient);
			}
		}
	}

	private static <T> void renderSlow(T ingredient, Rectangle area, int padding) {
		IngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		IIngredientRenderer<T> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredient);
		ingredientRenderer.render(Minecraft.getMinecraft(), area.x + padding, area.y + padding, ingredient);
	}

	public void renderOverlay(Minecraft minecraft) {
		if (ingredient == null) {
			return;
		}

		if (!(ingredient instanceof ItemStack)) {
			return;
		}

		ItemStack itemStack = (ItemStack) ingredient;
		try {
			renderOverlay(minecraft, itemStack);
		} catch (RuntimeException e) {
			throw createRenderIngredientException(e, itemStack);
		} catch (LinkageError e) {
			throw createRenderIngredientException(e, itemStack);
		}
	}

	private void renderOverlay(Minecraft minecraft, ItemStack itemStack) {
		FontRenderer font = getFontRenderer(minecraft, itemStack);
		RenderItem renderItem = minecraft.getRenderItem();
		renderItem.renderItemOverlayIntoGUI(font, itemStack, area.x + padding, area.y + padding, null);
	}

	private static <V> void renderEditMode(V ingredient, Rectangle area, int padding) {
		IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		IIngredientHelper ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);

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
		if (ingredient == null) {
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
		if (ingredient == null) {
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
		if (ingredient == null) {
			return;
		}

		drawTooltip(minecraft, ingredient, mouseX, mouseY);
	}

	private static <V> void drawTooltip(Minecraft minecraft, V ingredient, int mouseX, int mouseY) {
		IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredient);
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
		List<String> tooltip = getTooltip(minecraft, ingredient, ingredientRenderer, ingredientHelper);
		FontRenderer fontRenderer = ingredientRenderer.getFontRenderer(minecraft, ingredient);

		if (ingredient instanceof ItemStack) {
			ItemStack itemStack = (ItemStack) ingredient;
			TooltipRenderer.drawHoveringText(itemStack, minecraft, tooltip, mouseX, mouseY, fontRenderer);
		} else {
			TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY, fontRenderer);
		}
	}

	private static <V> List<String> getTooltip(Minecraft minecraft, V ingredient, IIngredientRenderer<V> ingredientRenderer, IIngredientHelper<V> ingredientHelper) {
		List<String> tooltip = getIngredientTooltipSafe(minecraft, ingredient, ingredientRenderer);
		tooltip = Internal.getModIdHelper().addModNameToIngredientTooltip(tooltip, ingredient, ingredientHelper);

		int maxWidth = Constants.MAX_TOOLTIP_WIDTH;
		for (String tooltipLine : tooltip) {
			int width = minecraft.fontRenderer.getStringWidth(tooltipLine);
			if (width > maxWidth) {
				maxWidth = width;
			}
		}

		if (Config.getColorSearchMode() != Config.SearchMode.DISABLED) {
			addColorSearchInfoToTooltip(minecraft, ingredient, ingredientHelper, tooltip, maxWidth);
		}

		if (Config.isEditModeEnabled()) {
			addEditModeInfoToTooltip(minecraft, ingredient, ingredientHelper, tooltip, maxWidth);
		}

		return tooltip;
	}

	private static <V> List<String> getIngredientTooltipSafe(Minecraft minecraft, V ingredient, IIngredientRenderer<V> ingredientRenderer) {
		try {
			return ingredientRenderer.getTooltip(minecraft, ingredient);
		} catch (RuntimeException e) {
			Log.error("Tooltip crashed.", e);
		} catch (LinkageError e) {
			Log.error("Tooltip crashed.", e);
		}

		List<String> tooltip = new ArrayList<String>();
		tooltip.add(TextFormatting.RED + Translator.translateToLocal("jei.tooltip.error.crash"));
		return tooltip;
	}

	private static <V> void addColorSearchInfoToTooltip(Minecraft minecraft, V ingredient, IIngredientHelper<V> ingredientHelper, List<String> tooltip, int maxWidth) {
		ColorNamer colorNamer = Internal.getColorNamer();

		Iterable<Color> colors = ingredientHelper.getColors(ingredient);
		Collection<String> colorNames = colorNamer.getColorNames(colors);
		if (!colorNames.isEmpty()) {
			String colorNamesString = Joiner.on(", ").join(colorNames);
			String colorNamesLocalizedString = TextFormatting.GRAY + Translator.translateToLocalFormatted("jei.tooltip.item.colors", colorNamesString);
			tooltip.addAll(minecraft.fontRenderer.listFormattedStringToWidth(colorNamesLocalizedString, maxWidth));
		}
	}

	private static <V> void addEditModeInfoToTooltip(Minecraft minecraft, V ingredient, IIngredientHelper<V> ingredientHelper, List<String> tooltip, int maxWidth) {
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

	private static <T> ReportedException createRenderIngredientException(Throwable throwable, final T ingredient) {
		final IIngredientHelper<T> ingredientHelper = Internal.getIngredientRegistry().getIngredientHelper(ingredient);
		CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering ingredient");
		CrashReportCategory crashreportcategory = crashreport.makeCategory("Ingredient being rendered");
		crashreportcategory.setDetail("Ingredient Mod", new ICrashReportDetail<String>() {
			@Override
			public String call() throws Exception {
				return Internal.getModIdHelper().getModNameForIngredient(ingredient, ingredientHelper);
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
