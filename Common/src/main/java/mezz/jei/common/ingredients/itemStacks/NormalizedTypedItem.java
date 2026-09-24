package mezz.jei.common.ingredients.itemStacks;

import mezz.jei.api.ingredients.IIngredientHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

final class NormalizedTypedItem extends TypedItemStack {
	private final Holder<Item> itemHolder;

	NormalizedTypedItem(Holder<Item> itemHolder) {
		this.itemHolder = itemHolder;
	}

	@Override
	protected ItemStack createItemStackUncached() {
		return new ItemStack(itemHolder);
	}

	@Override
	public TypedItemStack normalize(IIngredientHelper<ItemStack> ingredientHelper) {
		return this;
	}

	@Override
	protected Item getItem() {
		return itemHolder.value();
	}

	@Override
	public String toString() {
		return "NormalizedTypedItem{" +
			"itemHolder=" + itemHolder +
			'}';
	}
}
