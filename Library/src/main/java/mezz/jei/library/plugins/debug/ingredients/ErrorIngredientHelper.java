package mezz.jei.library.plugins.debug.ingredients;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

public class ErrorIngredientHelper implements IIngredientHelper<ErrorIngredient> {
	@Override
	public IIngredientType<ErrorIngredient> getIngredientType() {
		return ErrorIngredient.TYPE;
	}

	@Override
	public String getDisplayName(ErrorIngredient ingredient) {
		return "JEI Error Item #" + ingredient.crashType();
	}

	@Override
	public Object getUid(ErrorIngredient ingredient, UidContext context) {
		return ingredient.crashType();
	}

	@Override
	public Object getGroupingUid(ErrorIngredient ingredient) {
		return IIngredientHelper.super.getGroupingUid(ingredient);
	}

	@Override
	public Identifier getIdentifier(ErrorIngredient ingredient) {
		return Identifier.fromNamespaceAndPath(ModIds.JEI_ID, "error_" + ingredient.crashType().toString().toLowerCase(Locale.ROOT));
	}

	@Override
	public ErrorIngredient copyIngredient(ErrorIngredient ingredient) {
		return ingredient;
	}

	@Override
	public String getErrorInfo(@Nullable ErrorIngredient ingredient) {
		if (ingredient == null) {
			return "error ingredient: null";
		}
		return getDisplayName(ingredient);
	}
}
