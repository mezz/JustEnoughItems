package mezz.jei.common.ingredients.group;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.resources.Identifier;

import java.util.regex.Pattern;

public record RegExpSelector(String pattern) implements IIngredientGroupSelector {

	@Override
	public IngredientGroupType getType() {
		return IngredientGroupType.REGEXP;
	}

	@Override
	public boolean test(ITypedIngredient<?> ingredient, IIngredientManager ingredientManager) {
		return matchesPattern(ingredient, ingredientManager);
	}

	private <T> boolean matchesPattern(ITypedIngredient<T> typedIngredient, IIngredientManager ingredientManager) {
		IIngredientType<T> type = typedIngredient.getType();
		IIngredientHelper<T> helper = ingredientManager.getIngredientHelper(type);
		T ingredient = typedIngredient.getIngredient();
		Identifier id = helper.getIdentifier(ingredient);
		return Pattern.matches(pattern, id.toString());
	}
}
