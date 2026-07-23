package mezz.jei.common.ingredients.group;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record IngredientGroupInfo(
	Identifier id,
	List<IIngredientGroupSelector> selectors,
	boolean override
) {
	public IngredientGroupInfo {
		selectors = new ArrayList<>(selectors);
	}

	public Component getName() {
		return Component.translatable(getNameTranslationKey(id));
	}

	/**
	 * Group name translation keys are of the form:
	 * {@code jei.group.<namespace>.<path>}, with {@code /} in the path replaced by {@code .}.
	 */
	private static String getNameTranslationKey(Identifier id) {
		String path = id.getPath().replace('/', '.');
		return "jei.group." + id.getNamespace() + "." + path;
	}

	public void add(IIngredientGroupSelector selector) {
		selectors.add(selector);
	}

	public boolean isMember(ITypedIngredient<?> ingredient, IIngredientManager ingredientManager) {
		for (IIngredientGroupSelector selector : selectors) {
			if (selector.test(ingredient, ingredientManager)) {
				return true;
			}
		}
		return false;
	}
}
