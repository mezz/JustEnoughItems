package mezz.jei.startup;

import java.util.HashSet;
import java.util.Set;

import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class UniqueItemStackListBuilder {
	private final StackHelper stackHelper;
	private final NonNullList<ItemStack> ingredients = NonNullList.create();
	private final Set<String> ingredientUids = new HashSet<>();

	public UniqueItemStackListBuilder(StackHelper stackHelper) {
		this.stackHelper = stackHelper;
	}

	public void add(ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return;
		}
		try {
			String uid = stackHelper.getUniqueIdentifierForStack(itemStack);
			if (!ingredientUids.contains(uid)) {
				ingredientUids.add(uid);
				ingredients.add(itemStack);
			}
		} catch (RuntimeException | LinkageError e) {
			Log.error("Failed to get unique identifier for stack.", e);
		}
	}

	public NonNullList<ItemStack> build() {
		return ingredients;
	}
}
