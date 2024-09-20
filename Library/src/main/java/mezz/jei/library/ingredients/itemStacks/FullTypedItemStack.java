package mezz.jei.library.ingredients.itemStacks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

final class FullTypedItemStack extends TypedItemStack {
	private final Item item;
	private final @Nullable CompoundTag tag;
	private final int count;

	public FullTypedItemStack(
		Item item,
		@Nullable CompoundTag tag,
		int count
	) {
		this.item = item;
		this.tag = tag;
		this.count = count;
	}

	@Override
	protected ItemStack createItemStackUncached() {
		ItemStack itemStack = new ItemStack(item, count);
		itemStack.setTag(tag);
		return itemStack;
	}

	@Override
	protected TypedItemStack getNormalized() {
		return NormalizedTypedItemStack.create(item, tag);
	}

	@Override
	public String toString() {
		return "TypedItemStack{" +
			"item=" + item +
			", tag=" + tag +
			", count=" + count +
			'}';
	}
}
