package mezz.jei.ingredients;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.color.ColorNamer;
import mezz.jei.config.Config;
import mezz.jei.util.Translator;

public final class IngredientInformation {
	private IngredientInformation() {
	}

	public static <T> String getDisplayName(T ingredient, IIngredientHelper<T> ingredientHelper) {
		String displayName = ingredientHelper.getDisplayName(ingredient);
		return removeChatFormatting(displayName);
	}

	public static <T> List<String> getTooltipStrings(T ingredient, IIngredientRenderer<T> ingredientRenderer, Set<String> toRemove) {
		ITooltipFlag.TooltipFlags tooltipFlag = Config.getSearchAdvancedTooltips() ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;
		Minecraft minecraft = Minecraft.getMinecraft();
		List<String> tooltip = ingredientRenderer.getTooltip(minecraft, ingredient, tooltipFlag);
		List<String> cleanTooltip = new ArrayList<>(tooltip.size());
		for (String line : tooltip) {
			line = removeChatFormatting(line);
			line = Translator.toLowercaseWithLocale(line);
			for (String excludeWord : toRemove) {
				line = line.replace(excludeWord, "");
			}
			if (!StringUtils.isNullOrEmpty(line)) {
				cleanTooltip.add(line);
			}
		}
		return cleanTooltip;
	}

	private static String removeChatFormatting(String string) {
		String withoutFormattingCodes = TextFormatting.getTextWithoutFormattingCodes(string);
		return (withoutFormattingCodes == null) ? "" : withoutFormattingCodes;
	}

	public static <V> Collection<String> getColorStrings(V ingredient, IIngredientHelper<V> ingredientHelper) {
		Iterable<Color> colors = ingredientHelper.getColors(ingredient);
		ColorNamer colorNamer = Internal.getColorNamer();
		return colorNamer.getColorNames(colors, true);
	}

	public static <V> List<String> getUniqueIdsWithWildcard(IIngredientHelper<V> ingredientHelper, V ingredient) {
		String uid = ingredientHelper.getUniqueId(ingredient);
		String uidWild = ingredientHelper.getWildcardId(ingredient);

		if (uid.equals(uidWild)) {
			return Collections.singletonList(uid);
		} else {
			return Arrays.asList(uid, uidWild);
		}
	}
}
