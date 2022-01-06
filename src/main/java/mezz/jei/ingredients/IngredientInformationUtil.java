package mezz.jei.ingredients;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.util.StringUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.color.ColorNamer;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.util.Translator;

public final class IngredientInformationUtil {
	private IngredientInformationUtil() {
	}

	public static <T> String getDisplayName(T ingredient, IIngredientHelper<T> ingredientHelper) {
		String displayName = ingredientHelper.getDisplayName(ingredient);
		return removeChatFormatting(displayName);
	}

	public static <T> List<String> getTooltipStrings(T ingredient, IIngredientRenderer<T> ingredientRenderer, Set<String> toRemove, IIngredientFilterConfig config) {
		TooltipFlag.Default tooltipFlag = config.getSearchAdvancedTooltips() ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
		List<Component> tooltip = ingredientRenderer.getTooltip(ingredient, tooltipFlag);
		return tooltip.stream()
			.map(Component::getString)
			.map(IngredientInformationUtil::removeChatFormatting)
			.map(Translator::toLowercaseWithLocale)
			.map(line -> {
				for (String excludeWord : toRemove) {
					line = line.replace(excludeWord, "");
				}
				return line;
			})
			.filter(line -> !StringUtil.isNullOrEmpty(line))
			.collect(Collectors.toList());
	}

	private static String removeChatFormatting(String string) {
		String withoutFormattingCodes = ChatFormatting.stripFormatting(string);
		return (withoutFormattingCodes == null) ? "" : withoutFormattingCodes;
	}

	public static <V> Collection<String> getColorStrings(V ingredient, IIngredientHelper<V> ingredientHelper) {
		Iterable<Integer> colors = ingredientHelper.getColors(ingredient);
		ColorNamer colorNamer = Internal.getColorNamer();
		return colorNamer.getColorNames(colors, true);
	}

	public static <V> List<String> getUniqueIdsWithWildcard(IIngredientHelper<V> ingredientHelper, V ingredient, UidContext context) {
		String uid = ingredientHelper.getUniqueId(ingredient, context);
		String uidWild = ingredientHelper.getWildcardId(ingredient);

		if (uid.equals(uidWild)) {
			return Collections.singletonList(uid);
		} else {
			return Arrays.asList(uid, uidWild);
		}
	}
}
