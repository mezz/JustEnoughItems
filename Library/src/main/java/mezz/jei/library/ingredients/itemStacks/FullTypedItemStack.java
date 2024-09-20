package mezz.jei.library.ingredients.itemStacks;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

final class FullTypedItemStack extends TypedItemStack {
	private final Holder<Item> itemHolder;
	private final @Nullable CompoundTag tag;
	private final int count;

	public FullTypedItemStack(
		Holder<Item> itemHolder,
		@Nullable CompoundTag tag,
		int count
	) {
		this.itemHolder = itemHolder;
		this.tag = tag;
		this.count = count;
	}

	@Override
	protected ItemStack createItemStackUncached() {
		ItemStack itemStack = new ItemStack(itemHolder, count);
		itemStack.setTag(tag);
		return itemStack;
	}

	@Override
	protected TypedItemStack getNormalized() {
		return NormalizedTypedItemStack.create(itemHolder, tag);
	}

	@Override
	public String toString() {
		return "TypedItemStack{" +
			"itemHolder=" + itemHolder +
			", tag=" + tag +
			", count=" + count +
			'}';
	}
}
