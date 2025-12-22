package mezz.jei.common.util;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class StackHelper implements IStackHelper {
	private final ISubtypeManager subtypeManager;

	public StackHelper(ISubtypeManager subtypeManager) {
		this.subtypeManager = subtypeManager;
	}

	@Override
	public boolean isEquivalent(@Nullable ItemStack lhs, @Nullable ItemStack rhs, UidContext context) {
		ErrorUtil.checkNotNull(context, "context");
		if (lhs == rhs) {
			return true;
		}

		if (lhs == null || rhs == null) {
			return false;
		}

		if (lhs.getItem() != rhs.getItem()) {
			return false;
		}

		Object keyLhs = subtypeManager.getSubtypeData(lhs, context);
		Object keyRhs = subtypeManager.getSubtypeData(rhs, context);
		return Objects.equals(keyLhs, keyRhs);
	}

	@Override
	public Object getUidForStack(ItemStack stack, UidContext context) {
		Item item = stack.getItem();
		Object subtypeData = subtypeManager.getSubtypeData(stack, context);
		if (subtypeData != null) {
			return List.of(item, subtypeData);
		}
		return item;
	}

	@Override
	public Object getUidForStack(ITypedIngredient<ItemStack> typedIngredient, UidContext context) {
		Item item = typedIngredient.getBaseIngredient(VanillaTypes.ITEM_STACK);
		Object subtypeData = subtypeManager.getSubtypeData(VanillaTypes.ITEM_STACK, typedIngredient, context);
		if (subtypeData != null) {
			return List.of(item, subtypeData);
		}
		return item;
	}

	public boolean hasSubtypes(ItemStack stack) {
		return subtypeManager.hasSubtypes(stack);
	}
}
