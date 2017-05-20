package mezz.jei.util;

import java.util.List;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;

public final class LegacyUtil {
	private LegacyUtil() {
	}

	public static <T> List<String> getTooltip(IIngredientRenderer<T> ingredientRenderer, Minecraft minecraft, T ingredient, boolean advanced) {
		try {
			return ingredientRenderer.getTooltip(minecraft, ingredient, advanced);
		} catch (AbstractMethodError ignored) { // old ingredient renderers do not have the new getTooltip method
			return ingredientRenderer.getTooltip(minecraft, ingredient);
		}
	}
}
