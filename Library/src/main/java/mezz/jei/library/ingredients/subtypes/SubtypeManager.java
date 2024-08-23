package mezz.jei.library.ingredients.subtypes;

import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.util.ErrorUtil;
import org.jetbrains.annotations.Nullable;

public class SubtypeManager implements ISubtypeManager {
	private final SubtypeInterpreters interpreters;

	public SubtypeManager(SubtypeInterpreters interpreters) {
		this.interpreters = interpreters;
	}

	@Override
	@Nullable
	public <T> Object getSubtypeData(IIngredientTypeWithSubtypes<?, T> ingredientType, T ingredient, UidContext context) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ErrorUtil.checkNotNull(context, "type");

		return interpreters.get(ingredientType, ingredient)
			.map(subtypeInterpreter -> subtypeInterpreter.getSubtypeData(ingredient, context))
			.orElse(null);
	}

	@SuppressWarnings({"removal", "deprecation"})
	@Override
	public <T> String getSubtypeInfo(IIngredientTypeWithSubtypes<?, T> ingredientType, T ingredient, UidContext context) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ErrorUtil.checkNotNull(context, "context");

		return interpreters.get(ingredientType, ingredient)
			.map(subtypeInterpreter -> subtypeInterpreter.getLegacyStringSubtypeInfo(ingredient, context))
			.orElse(mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter.NONE);
	}

	@Override
	public <T, B> boolean hasSubtypes(IIngredientTypeWithSubtypes<B, T> ingredientType, T ingredient) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		return interpreters.contains(ingredientType, ingredient);
	}
}
