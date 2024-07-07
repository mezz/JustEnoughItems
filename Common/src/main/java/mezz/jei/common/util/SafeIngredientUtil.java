package mezz.jei.common.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IJeiClientConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class SafeIngredientUtil {
	private static final Cache<ITypedIngredient<?>, Boolean> CRASHING_INGREDIENT_RENDER_CACHE =
		CacheBuilder.newBuilder()
			.expireAfterAccess(10, TimeUnit.SECONDS)
			.build();
	private static final Cache<ITypedIngredient<?>, Boolean> CRASHING_INGREDIENT_TOOLTIP_CACHE =
		CacheBuilder.newBuilder()
			.expireAfterAccess(10, TimeUnit.SECONDS)
			.build();

	private SafeIngredientUtil() {
	}

	public static <T> List<Component> getTooltip(IIngredientManager ingredientManager, IIngredientRenderer<T> ingredientRenderer, ITypedIngredient<T> typedIngredient) {
		Minecraft minecraft = Minecraft.getInstance();
		TooltipFlag.Default tooltipFlag = minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
		return getTooltip(ingredientManager, ingredientRenderer, typedIngredient, tooltipFlag);
	}

	public static <T> List<Component> getTooltip(
		IIngredientManager ingredientManager,
		IIngredientRenderer<T> ingredientRenderer,
		ITypedIngredient<T> typedIngredient,
		TooltipFlag.Default tooltipFlag
	) {
		if (CRASHING_INGREDIENT_TOOLTIP_CACHE.getIfPresent(typedIngredient) == Boolean.TRUE) {
			return getTooltipErrorTooltip();
		}

		T ingredient = typedIngredient.getIngredient();
		try {
			List<Component> tooltip = ingredientRenderer.getTooltip(ingredient, tooltipFlag);
			List<Component> tooltipCopy = new ArrayList<>(tooltip);
			if (CRASHING_INGREDIENT_RENDER_CACHE.getIfPresent(typedIngredient) == Boolean.TRUE) {
				tooltipCopy.addAll(getRenderErrorTooltip());
			}
			return tooltipCopy;
		} catch (RuntimeException | LinkageError e) {
			CRASHING_INGREDIENT_TOOLTIP_CACHE.put(typedIngredient, Boolean.TRUE);
			ErrorUtil.logIngredientCrash(e, "Caught an error getting an Ingredient's tooltip", ingredientManager, typedIngredient);
			return getTooltipErrorTooltip();
		}
	}

	private static List<Component> getTooltipErrorTooltip() {
		List<Component> list = new ArrayList<>();
		MutableComponent crash = Component.translatable("jei.tooltip.error.crash");
		list.add(crash.withStyle(ChatFormatting.RED));
		return list;
	}

	private static List<Component> getRenderErrorTooltip() {
		List<Component> list = new ArrayList<>();
		MutableComponent crash = Component.translatable("jei.tooltip.error.render.crash");
		list.add(crash.withStyle(ChatFormatting.RED));
		return list;
	}

	public static <T> void render(GuiGraphics guiGraphics, IIngredientRenderer<T> ingredientRenderer, ITypedIngredient<T> typedIngredient) {
		if (CRASHING_INGREDIENT_RENDER_CACHE.getIfPresent(typedIngredient) == Boolean.TRUE) {
			renderError(guiGraphics);
			return;
		}

		T ingredient = typedIngredient.getIngredient();
		try {
			ingredientRenderer.render(guiGraphics, ingredient);
		} catch (RuntimeException | LinkageError e) {
			CRASHING_INGREDIENT_RENDER_CACHE.put(typedIngredient, Boolean.TRUE);

			IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
			if (shouldCatchRenderErrors()) {
				ErrorUtil.logIngredientCrash(e, "Caught an error rendering an Ingredient", ingredientManager, typedIngredient);
				renderError(guiGraphics);
			} else {
				CrashReport crashReport = ErrorUtil.createIngredientCrashReport(e, "Rendering ingredient", ingredientManager, typedIngredient);
				throw new ReportedException(crashReport);
			}
		}
	}

	private static boolean shouldCatchRenderErrors() {
		return Internal.getOptionalJeiClientConfigs()
			.map(IJeiClientConfigs::getClientConfig)
			.map(IClientConfig::isCatchRenderErrorsEnabled)
			.orElse(false);
	}

	private static void renderError(GuiGraphics guiGraphics) {
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		guiGraphics.drawString(font, "ERR", 0, 0, 0xFFFF0000, false);
		guiGraphics.drawString(font, "OR", 0, 8, 0xFFFF0000, false);
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}

}
