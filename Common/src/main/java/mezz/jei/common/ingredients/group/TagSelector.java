package mezz.jei.common.ingredients.group;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.resources.Identifier;

public record TagSelector(Identifier tagId) implements IIngredientGroupSelector {

	@Override
	public IngredientGroupType getType() {
		return IngredientGroupType.TAG;
	}

	@Override
	public boolean test(ITypedIngredient<?> ingredient, IIngredientManager ingredientManager) {
		return hasTag(ingredient, ingredientManager);
	}

	private <T> boolean hasTag(ITypedIngredient<?> ingredient, IIngredientManager ingredientManager) {
		@SuppressWarnings("unchecked")
		IIngredientType<T> type = (IIngredientType<T>) ingredient.getType();
		IIngredientHelper<T> helper = ingredientManager.getIngredientHelper(type);
		@SuppressWarnings("unchecked")
		T raw = (T) ingredient.getIngredient();
		return helper.getTagStream(raw).anyMatch(tagId::equals);
	}
}
