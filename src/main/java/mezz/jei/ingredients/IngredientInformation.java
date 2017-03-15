package mezz.jei.ingredients;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.color.ColorNamer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Language;
import net.minecraft.util.text.TextFormatting;

public final class IngredientInformation {
	@Nullable
	private static Language previousLanguage;

	private static final Map<Object, String> TOOLTIP_CACHE = new IdentityHashMap<Object, String>();
	private static final Map<Object, String> COLOR_CACHE = new IdentityHashMap<Object, String>();

	private IngredientInformation() {
	}

	public static <T> String getTooltipString(T ingredient, IIngredientRenderer<T> ingredientRenderer, String modId, String modName, String displayName) {
		String tooltipString = TOOLTIP_CACHE.get(ingredient);
		if (tooltipString == null) {
			List<String> tooltip = ingredientRenderer.getTooltip(Minecraft.getMinecraft(), ingredient);
			tooltipString = Joiner.on(' ').join(tooltip).toLowerCase();
			tooltipString = removeChatFormatting(tooltipString);
			tooltipString = tooltipString.replace(modId, "");
			tooltipString = tooltipString.replace(modName, "");
			tooltipString = tooltipString.replace(displayName, "");
			TOOLTIP_CACHE.put(ingredient, tooltipString);
		}

		return tooltipString;
	}

	private static String removeChatFormatting(String string) {
		String withoutFormattingCodes = TextFormatting.getTextWithoutFormattingCodes(string);
		return (withoutFormattingCodes == null) ? string : withoutFormattingCodes;
	}

	public static <V> String getColorString(V ingredient, IIngredientHelper<V> ingredientHelper) {
		String colorString = COLOR_CACHE.get(ingredient);
		if (colorString == null) {
			Iterable<Color> colors = ingredientHelper.getColors(ingredient);
			ColorNamer colorNamer = Internal.getColorNamer();
			Collection<String> colorNames = colorNamer.getColorNames(colors);
			colorString = Joiner.on(' ').join(colorNames).toLowerCase();
			COLOR_CACHE.put(ingredient, colorString);
		}
		return colorString;
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
