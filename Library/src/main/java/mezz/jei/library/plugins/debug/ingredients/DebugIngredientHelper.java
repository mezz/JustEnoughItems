package mezz.jei.library.plugins.debug.ingredients;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class DebugIngredientHelper implements IIngredientHelper<DebugIngredient> {
	@Override
	public IIngredientType<DebugIngredient> getIngredientType() {
		return DebugIngredient.TYPE;
	}

	@Override
	public String getDisplayName(DebugIngredient ingredient) {
		return "JEI Debug Item #" + ingredient.number();
	}

	@Override
	public Object getUid(DebugIngredient ingredient, UidContext context) {
		return ingredient.number();
	}

	@Override
	public Object getGroupingUid(DebugIngredient ingredient) {
		return DebugIngredient.class;
	}

	@Override
	public Identifier getIdentifier(DebugIngredient ingredient) {
		return Identifier.fromNamespaceAndPath(ModIds.JEI_ID, "debug_" + ingredient.number());
	}

	@Override
	public DebugIngredient copyIngredient(DebugIngredient ingredient) {
		return ingredient.copy();
	}

	@Override
	public String getErrorInfo(@Nullable DebugIngredient ingredient) {
		if (ingredient == null) {
			return "debug ingredient: null";
		}
		return getDisplayName(ingredient);
	}
}
