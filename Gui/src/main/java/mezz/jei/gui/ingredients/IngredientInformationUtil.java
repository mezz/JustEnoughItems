package mezz.jei.gui.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.common.util.Translator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Unmodifiable;

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
	public static <T> List<String> getTooltipStrings(IIngredientManager ingredientManager, ITypedIngredient<T> typedIngredient, IIngredientRenderer<T> ingredientRenderer, Set<String> toRemove, IIngredientFilterConfig config) {
		TooltipFlag.Default tooltipFlag = config.getSearchAdvancedTooltips() ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
		List<Component> tooltip = SafeIngredientUtil.getTooltip(ingredientManager, ingredientRenderer, typedIngredient, tooltipFlag);
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

}
