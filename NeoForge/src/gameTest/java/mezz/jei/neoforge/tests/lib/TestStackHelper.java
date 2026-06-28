package mezz.jei.neoforge.tests.lib;

import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class TestStackHelper implements IStackHelper {
	@Override
	public Object getUidForStack(ItemStack stack, UidContext context) {
		return ItemStack.hashItemAndComponents(stack);
	}

	@Override
	public Object getUidForStack(ITypedIngredient<ItemStack> stack, UidContext context) {
		return getUidForStack(stack.getIngredient(), context);
	}

	@Override
	public boolean isEquivalent(@Nullable ItemStack lhs, @Nullable ItemStack rhs, UidContext context) {
		if (lhs == null || rhs == null) {
			return lhs == rhs;
		}
		return ItemStack.isSameItemSameComponents(lhs, rhs);
	}
}
