package mezz.jei.common.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.common.color.ColorNamer;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.util.Translator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public final class IngredientInformationUtil {
	private IngredientInformationUtil() {
	}

	public static <T> String getDisplayName(T ingredient, IIngredientHelper<T> ingredientHelper) {
		String displayName = ingredientHelper.getDisplayName(ingredient);
		return removeChatFormatting(displayName);
	}

	@Unmodifiable
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
			.toList();
	}

	private static String removeChatFormatting(String string) {
		String withoutFormattingCodes = ChatFormatting.stripFormatting(string);
		return (withoutFormattingCodes == null) ? "" : withoutFormattingCodes;
	}

	public static <V> Collection<String> getColorStrings(V ingredient, IIngredientHelper<V> ingredientHelper) {
		Iterable<Integer> colors = ingredientHelper.getColors(ingredient);
		ColorNamer colorNamer = ColorNamer.getInstance();
		return colorNamer.getColorNames(colors)
				.map(Translator::toLowercaseWithLocale)
				.distinct()
				.toList();
	}
}
