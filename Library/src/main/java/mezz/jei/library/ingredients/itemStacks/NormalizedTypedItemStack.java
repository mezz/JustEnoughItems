package mezz.jei.library.ingredients.itemStacks;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

final class NormalizedTypedItemStack extends TypedItemStack {
	private final Holder<Item> itemHolder;
	private final @Nullable CompoundTag tag;

	public NormalizedTypedItemStack(
		Holder<Item> itemHolder,
		CompoundTag tag
	) {
		this.itemHolder = itemHolder;
		this.tag = copyTag(tag);
	}

	static TypedItemStack create(Holder<Item> itemHolder, @Nullable CompoundTag tag) {
		if (tag == null) {
			return new NormalizedTypedItem(itemHolder);
		}
		return new NormalizedTypedItemStack(itemHolder, tag);
	}

	@Override
	public ItemStack createItemStackUncached() {
		ItemStack itemStack = new ItemStack(itemHolder, 1);
		itemStack.setTag(copyTag(tag));
		return itemStack;
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
			", tag=" + tag +
			'}';
	}

	@Nullable
	private static CompoundTag copyTag(@Nullable CompoundTag tag) {
		return tag == null ? null : tag.copy();
	}
}
