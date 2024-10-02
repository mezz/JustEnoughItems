package mezz.jei.library.ingredients.itemStacks;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

final class NormalizedTypedItemStack extends TypedItemStack {
	private final Holder<Item> itemHolder;
	private final DataComponentPatch dataComponentPatch;

	public NormalizedTypedItemStack(
		Holder<Item> itemHolder,
		DataComponentPatch dataComponentPatch
	) {
		this.itemHolder = itemHolder;
		this.dataComponentPatch = dataComponentPatch;
	}

	static TypedItemStack create(Holder<Item> itemHolder, DataComponentPatch dataComponentPatch) {
		if (dataComponentPatch.isEmpty()) {
			return new NormalizedTypedItem(itemHolder);
		}
		return new NormalizedTypedItemStack(itemHolder, dataComponentPatch);
	}

	@Override
	protected ItemStack createItemStackUncached() {
		return new ItemStack(itemHolder, 1, dataComponentPatch);
	}

	@Override
	public TypedItemStack getNormalized() {
		return this;
	}

	@Override
	protected Item getItem() {
		return itemHolder.value();
	}

	@Override
	public String toString() {
		return "NormalizedTypedItemStack{" +
			"itemHolder=" + itemHolder +
			", dataComponentPatch=" + dataComponentPatch +
			'}';
	}
}
