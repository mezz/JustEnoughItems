package mezz.jei.library.ingredients.itemStacks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

final class NormalizedTypedItemStack extends TypedItemStack {
	private final Item item;
	private final @Nullable CompoundTag tag;

	public NormalizedTypedItemStack(
		Item item,
		CompoundTag tag
	) {
		this.item = item;
		this.tag = copyTag(tag);
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
		itemStack.setTag(copyTag(tag));
		return itemStack;
	}

	@Override
	public TypedItemStack getNormalized() {
		return this;
	}

	@Override
	protected Item getItem() {
		return item;
	}

	@Override
	public String toString() {
		return "NormalizedTypedItemStack{" +
			"item=" + item +
			", tag=" + tag +
			'}';
	}

	@Nullable
	private static CompoundTag copyTag(@Nullable CompoundTag tag) {
		if (tag == null) {
			return null;
		}
		return tag.copy();
	}
}
