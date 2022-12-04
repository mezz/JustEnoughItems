package mezz.jei.library.plugins.jei.ingredients;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DebugIngredientHelper implements IIngredientHelper<DebugIngredient> {
	@Override
	public IIngredientType<DebugIngredient> getIngredientType() {
		return DebugIngredient.TYPE;
	}

	@Override
	public String getDisplayName(DebugIngredient ingredient) {
		return "JEI Debug Item #" + ingredient.getNumber();
	}

	@Override
	public String getUniqueId(DebugIngredient ingredient, UidContext context) {
		return "JEI_debug_" + ingredient.getNumber();
	}

	@Override
	public ResourceLocation getResourceLocation(DebugIngredient ingredient) {
		return new ResourceLocation(ModIds.JEI_ID, "debug_" + ingredient.getNumber());
	}

	@Override
	public ItemStack getCheatItemStack(DebugIngredient ingredient) {
		return ItemStack.EMPTY;
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
