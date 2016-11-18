package mezz.jei.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;

public class UniqueItemStackListBuilder {
	private final StackHelper stackHelper;
	private final List<ItemStack> ingredients = new ArrayList<ItemStack>();
	private final Set<String> ingredientUids = new HashSet<String>();

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
		} catch (RuntimeException e) {
			Log.error("Failed to get unique identifier for stack.", e);
		} catch (LinkageError e) {
			Log.error("Failed to get unique identifier for stack.", e);
		}
	}

	public List<ItemStack> build() {
		return ingredients;
	}
}
