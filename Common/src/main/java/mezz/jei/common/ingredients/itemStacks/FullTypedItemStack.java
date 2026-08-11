package mezz.jei.common.ingredients.itemStacks;

import mezz.jei.api.ingredients.IIngredientHelper;
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
		this.tag = copyTag(tag);
		this.count = count;
	}

	@Override
	protected ItemStack createItemStackUncached() {
		ItemStack itemStack = new ItemStack(item, count);
		itemStack.setTag(copyTag(tag));
		return itemStack;
	}

	@Override
	public TypedItemStack normalize(IIngredientHelper<ItemStack> ingredientHelper) {
		return NormalizedTypedItemStack.create(item, tag);
	}

	@Override
	protected Item getItem() {
		return item;
	}

	@Override
	public String toString() {
		return "TypedItemStack{" +
			"item=" + item +
			", tag=" + tag +
			", count=" + count +
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
