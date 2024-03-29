package mezz.jei.common.util;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IJeiClientConfigs;
import mezz.jei.common.platform.IPlatformModHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.TooltipFlag;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SafeIngredientUtil {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Set<Class<?>> CRASHING_INGREDIENT_RENDER_CLASSES = new HashSet<>();
	private static final Set<Class<?>> CRASHING_INGREDIENT_TOOLTIP_CLASSES = new HashSet<>();

	private SafeIngredientUtil() {
	}

	public static <T> List<Component> getTooltip(IIngredientManager ingredientManager, IIngredientRenderer<T> ingredientRenderer, ITypedIngredient<T> typedIngredient) {
		Minecraft minecraft = Minecraft.getInstance();
		TooltipFlag.Default tooltipFlag = minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
		return getTooltip(ingredientManager, ingredientRenderer, typedIngredient, tooltipFlag);
	}

	public static <T> List<Component> getTooltip(IIngredientManager ingredientManager, IIngredientRenderer<T> ingredientRenderer, ITypedIngredient<T> typedIngredient, TooltipFlag.Default tooltipFlag) {
		T ingredient = typedIngredient.getIngredient();
		Class<?> ingredientClass = ingredient.getClass();
		if (CRASHING_INGREDIENT_TOOLTIP_CLASSES.contains(ingredientClass)) {
			return getErrorTooltip();
		}

		try {
			List<Component> tooltip = ingredientRenderer.getTooltip(ingredient, tooltipFlag);
			return new ArrayList<>(tooltip);
		} catch (RuntimeException | LinkageError e) {
			CRASHING_INGREDIENT_TOOLTIP_CLASSES.add(ingredientClass);
			if (shouldCatchTooltipErrors()) {
				logIngredientCrash(e, "Caught an error rendering an Ingredient's tooltip", ingredientManager, typedIngredient);
				return getErrorTooltip();
			} else {
				CrashReport crashReport = createIngredientCrashReport(e, "Rendering ingredient tooltip", ingredientManager, typedIngredient);
				throw new ReportedException(crashReport);
			}
		}
	}

	private static List<Component> getErrorTooltip() {
		List<Component> list = new ArrayList<>();
		MutableComponent crash = Component.translatable("jei.tooltip.error.crash");
		list.add(crash.withStyle(ChatFormatting.RED));
		return list;
	}

	public static <T> void render(IIngredientManager ingredientManager, IIngredientRenderer<T> ingredientRenderer, GuiGraphics guiGraphics, ITypedIngredient<T> typedIngredient) {
		T ingredient = typedIngredient.getIngredient();
		Class<?> ingredientClass = ingredient.getClass();
		if (CRASHING_INGREDIENT_RENDER_CLASSES.contains(ingredientClass)) {
			renderError(guiGraphics);
			return;
		}

		try {
			ingredientRenderer.render(guiGraphics, ingredient);
		} catch (RuntimeException | LinkageError e) {
			CRASHING_INGREDIENT_RENDER_CLASSES.add(ingredientClass);

			if (shouldCatchRenderErrors()) {
				logIngredientCrash(e, "Caught an error rendering an Ingredient", ingredientManager, typedIngredient);
				renderError(guiGraphics);
			} else {
				CrashReport crashReport = createIngredientCrashReport(e, "Rendering ingredient", ingredientManager, typedIngredient);
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

	private static boolean shouldCatchTooltipErrors() {
		return Internal.getOptionalJeiClientConfigs()
			.map(IJeiClientConfigs::getClientConfig)
			.map(IClientConfig::isCatchTooltipRenderErrorsEnabled)
			.orElse(false);
	}

	private static void renderError(GuiGraphics guiGraphics) {
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		guiGraphics.drawString(font, "ERR", 0, 0, 0xFFFF0000, false);
		guiGraphics.drawString(font, "OR", 0, 8, 0xFFFF0000, false);
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}

	private static <T> void logIngredientCrash(Throwable throwable, String title, IIngredientManager ingredientManager, ITypedIngredient<T> typedIngredient) {
		CrashReportCategory category = new CrashReportCategory("Ingredient");
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		setIngredientCategoryDetails(category, typedIngredient, ingredientHelper);
		LOGGER.error(crashReportToString(throwable, title, category));
	}

	private static <T> CrashReport createIngredientCrashReport(Throwable throwable, String title, IIngredientManager ingredientManager, ITypedIngredient<T> typedIngredient) {
		CrashReport crashReport = CrashReport.forThrowable(throwable, title);

		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);

		CrashReportCategory category = crashReport.addCategory("Ingredient");
		setIngredientCategoryDetails(category, typedIngredient, ingredientHelper);
		return crashReport;
	}

	private static <T> void setIngredientCategoryDetails(CrashReportCategory category, ITypedIngredient<T> typedIngredient, IIngredientHelper<T> ingredientHelper) {
		T ingredient = typedIngredient.getIngredient();
		IIngredientType<T> ingredientType = typedIngredient.getType();

		IPlatformModHelper modHelper = Services.PLATFORM.getModHelper();

		category.setDetail("Name", () -> ingredientHelper.getDisplayName(ingredient));
		category.setDetail("Mod's Name", () -> {
			String modId = ingredientHelper.getDisplayModId(ingredient);
			return modHelper.getModNameForModId(modId);
		});
		category.setDetail("Registry Name", () -> ingredientHelper.getResourceLocation(ingredient).toString());
		category.setDetail("Class Name", () -> ingredient.getClass().toString());
		category.setDetail("toString Name", ingredient::toString);
		category.setDetail("Unique Id for JEI (for JEI Blacklist)", () -> ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient));
		category.setDetail("Ingredient Type for JEI", () -> ingredientType.getIngredientClass().toString());
		category.setDetail("Error Info gathered from JEI", () -> ingredientHelper.getErrorInfo(ingredient));
	}

	private static String crashReportToString(Throwable t, String title, CrashReportCategory... categories) {
		StringBuilder sb = new StringBuilder();
		sb.append(title);
		sb.append(":\n\n");
		for (CrashReportCategory category : categories) {
			category.getDetails(sb);
			sb.append("\n\n");
		}
		sb.append("-- Stack Trace --\n\n");
		sb.append(ExceptionUtils.getStackTrace(t));
		return sb.toString();
	}
}
