package mezz.jei.forge.tests.lib;

import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class TestStackHelper implements IStackHelper {
	@Override
	public Object getUidForStack(ItemStack stack, UidContext context) {
		return stack.getItem().toString() + String.valueOf(stack.getTag());
	}

	@Override
	public boolean isEquivalent(@Nullable ItemStack lhs, @Nullable ItemStack rhs, UidContext context) {
		if (lhs == null || rhs == null) {
			return lhs == rhs;
		}
		return ItemStack.isSameItemSameTags(lhs, rhs);
	}

	@SuppressWarnings("removal")
	@Override
	public String getUniqueIdentifierForStack(ItemStack stack, UidContext context) {
		return String.valueOf(getUidForStack(stack, context));
	}
}
