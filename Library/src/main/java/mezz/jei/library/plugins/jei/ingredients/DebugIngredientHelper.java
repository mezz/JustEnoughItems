package mezz.jei.library.plugins.jei.ingredients;

import mezz.jei.api.constants.ModIds;
import mezz.jei.library.ingredients.IngredientHelperWithResourceLocation;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DebugIngredientHelper implements IngredientHelperWithResourceLocation<DebugIngredient> {
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
	@Deprecated(forRemoval = true, since = "9.2.2")
	public String getModId(DebugIngredient ingredient) {
		return getResourceLocation(ingredient).getNamespace();
	}

	@Override
	@Deprecated(forRemoval = true, since = "9.2.2")
	public String getResourceId(DebugIngredient ingredient) {
		return getResourceLocation(ingredient).getPath();
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
