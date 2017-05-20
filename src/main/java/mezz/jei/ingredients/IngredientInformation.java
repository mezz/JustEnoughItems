package mezz.jei.ingredients;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.color.ColorNamer;
import mezz.jei.config.Config;
import mezz.jei.util.LegacyUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Language;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextFormatting;

public final class IngredientInformation {
	@Nullable
	private static Language previousLanguage;

	private static final Map<Object, List<String>> TOOLTIP_CACHE = new IdentityHashMap<Object, List<String>>();
	private static final Map<Object, Collection<String>> COLOR_CACHE = new IdentityHashMap<Object, Collection<String>>();

	private IngredientInformation() {
	}

	public static <T> String getDisplayName(T ingredient, IIngredientHelper<T> ingredientHelper) {
		String displayName = ingredientHelper.getDisplayName(ingredient);
		return removeChatFormatting(displayName).toLowerCase();
	}

	public static <T> List<String> getTooltipStrings(T ingredient, IIngredientRenderer<T> ingredientRenderer, Set<String> toRemove) {
		List<String> tooltipStrings = TOOLTIP_CACHE.get(ingredient);
		if (tooltipStrings == null) {
			tooltipStrings = getTooltipStringsUncached(ingredient, ingredientRenderer, toRemove);
			TOOLTIP_CACHE.put(ingredient, tooltipStrings);
		}
		return tooltipStrings;
	}

	private static <T> List<String> getTooltipStringsUncached(T ingredient, IIngredientRenderer<T> ingredientRenderer, Set<String> excludeWords) {
		List<String> tooltip = LegacyUtil.getTooltip(ingredientRenderer, Minecraft.getMinecraft(), ingredient, Config.getSearchAdvancedTooltips());
		for (Iterator<String> iterator = tooltip.iterator(); iterator.hasNext(); ) {
			String line = iterator.next();
			line = removeChatFormatting(line).toLowerCase();
			for (String excludeWord : excludeWords) {
				line = line.replace(excludeWord, "");
			}
			if (StringUtils.isNullOrEmpty(line)) {
				iterator.remove();
			}
		}
		return tooltip;
	}

	private static String removeChatFormatting(String string) {
		String withoutFormattingCodes = TextFormatting.getTextWithoutFormattingCodes(string);
		return (withoutFormattingCodes == null) ? "" : withoutFormattingCodes;
	}

	public static <V> Collection<String> getColorStrings(V ingredient, IIngredientHelper<V> ingredientHelper) {
		Collection<String> colorStrings = COLOR_CACHE.get(ingredient);
		if (colorStrings == null) {
			Iterable<Color> colors = ingredientHelper.getColors(ingredient);
			ColorNamer colorNamer = Internal.getColorNamer();
			colorStrings = colorNamer.getColorNames(colors, true);
			COLOR_CACHE.put(ingredient, colorStrings);
		}
		return colorStrings;
	}

	public static void onStart(boolean resourceReload) {
		Language language = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage();
		if (previousLanguage != null && !previousLanguage.equals(language)) {
			TOOLTIP_CACHE.clear();
		}
		previousLanguage = language;

		if (resourceReload) {
			COLOR_CACHE.clear();
		}
	}
}
