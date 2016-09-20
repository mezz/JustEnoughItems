package mezz.jei.gui.ingredients;

import javax.annotation.Nullable;
import java.util.Collection;

import mezz.jei.Internal;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.gui.Focus;
import net.minecraft.item.ItemStack;

public class ItemStackHelper implements IIngredientHelper<ItemStack> {
	@Override
	public Collection<ItemStack> expandSubtypes(Collection<ItemStack> contained) {
		return Internal.getStackHelper().getAllSubtypes(contained);
	}

	@Override
	@Nullable
	public ItemStack getMatch(Iterable<ItemStack> ingredients, IFocus<ItemStack> toMatch) {
		return Internal.getStackHelper().containsStack(ingredients, toMatch.getValue());
	}

	@Override
	public Focus<ItemStack> createFocus(ItemStack ingredient) {
		return new Focus<ItemStack>(ingredient);
	}
}
