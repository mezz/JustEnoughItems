package mezz.jei.library.ingredients.itemStacks;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

final class NormalizedTypedItem extends TypedItemStack {
	private final Item item;

	NormalizedTypedItem(Item item) {
		this.item = item;
	}

	@Override
	protected ItemStack createItemStackUncached() {
		return new ItemStack(item);
	}

	@Override
	public TypedItemStack getNormalized() {
		return this;
	}

	@Override
	public String toString() {
		return "NormalizedTypedItem{" +
			"item=" + item +
			'}';
	}
}
