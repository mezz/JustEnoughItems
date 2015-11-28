package mezz.jei.gui.ingredients;

import javax.annotation.Nonnull;
import java.util.Collection;

import net.minecraft.item.ItemStack;

import mezz.jei.gui.Focus;
import mezz.jei.util.StackUtil;

public class ItemStackHelper implements IIngredientHelper<ItemStack> {
	@Override
	public Collection<ItemStack> expandSubtypes(Collection<ItemStack> contained) {
		return StackUtil.getAllSubtypes(contained);
	}

	@Override
	public ItemStack getMatch(Iterable<ItemStack> contained, @Nonnull Focus toMatch) {
		return StackUtil.containsStack(contained, toMatch.getStack());
	}
}
