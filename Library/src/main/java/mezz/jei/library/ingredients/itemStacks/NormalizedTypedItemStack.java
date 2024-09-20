package mezz.jei.library.ingredients.itemStacks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

final class NormalizedTypedItemStack extends TypedItemStack {
	private final Item item;
	private final CompoundTag tag;

	public NormalizedTypedItemStack(
		Item item,
		CompoundTag tag
	) {
		this.item = item;
		this.tag = tag;
	}

	static TypedItemStack create(Item item, @Nullable CompoundTag tag) {
		if (tag == null) {
			return new NormalizedTypedItem(item);
		}
		return new NormalizedTypedItemStack(item, tag);
	}

	@Override
	public ItemStack createItemStackUncached() {
		ItemStack itemStack = new ItemStack(item, 1);
		itemStack.setTag(tag);
		return itemStack;
	}

	@Override
	public TypedItemStack getNormalized() {
		return this;
	}

	@Override
	public String toString() {
		return "NormalizedTypedItemStack{" +
			"item=" + item +
			", tag=" + tag +
			'}';
	}
}
