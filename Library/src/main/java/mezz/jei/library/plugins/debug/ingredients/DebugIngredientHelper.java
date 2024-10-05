package mezz.jei.library.plugins.debug.ingredients;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class DebugIngredientHelper implements IIngredientHelper<DebugIngredient> {
	@Override
	public IIngredientType<DebugIngredient> getIngredientType() {
		return DebugIngredient.TYPE;
	}

	@Override
	public String getDisplayName(DebugIngredient ingredient) {
		return "JEI Debug Item #" + ingredient.number();
	}

	@SuppressWarnings("removal")
	@Override
	public String getUniqueId(DebugIngredient ingredient, UidContext context) {
		return "JEI_debug_" + ingredient.number();
	}

	@Override
	public Object getUid(DebugIngredient ingredient, UidContext context) {
		return ingredient.number();
	}

	@SuppressWarnings("removal")
	@Override
	public String getWildcardId(DebugIngredient ingredient) {
		return "JEI_debug";
	}

	@Override
	public Object getGroupingUid(DebugIngredient ingredient) {
		return DebugIngredient.class;
	}

	@Override
	public ResourceLocation getResourceLocation(DebugIngredient ingredient) {
		return ResourceLocation.fromNamespaceAndPath(ModIds.JEI_ID, "debug_" + ingredient.number());
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
