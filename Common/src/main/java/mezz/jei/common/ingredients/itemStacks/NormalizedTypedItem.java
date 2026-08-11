package mezz.jei.common.ingredients.itemStacks;

import mezz.jei.api.ingredients.IIngredientHelper;
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
	public TypedItemStack normalize(IIngredientHelper<ItemStack> ingredientHelper) {
		return this;
	}

	@Override
	protected Item getItem() {
		return item;
	}

	@Override
	public String toString() {
		return "NormalizedTypedItem{" +
			"item=" + item +
			'}';
	}
}
