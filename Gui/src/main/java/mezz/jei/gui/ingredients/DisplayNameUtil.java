package mezz.jei.gui.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.common.util.StringUtil;
import mezz.jei.common.util.Translator;

public final class DisplayNameUtil {
	private DisplayNameUtil() {
	}

	public static <T> String getLowercaseDisplayNameForSearch(T ingredient, IIngredientHelper<T> ingredientHelper) {
		String displayName = ingredientHelper.getDisplayName(ingredient);
		displayName = StringUtil.removeChatFormatting(displayName);
		displayName = Translator.toLowercaseWithLocale(displayName);
		return displayName;
	}

}
