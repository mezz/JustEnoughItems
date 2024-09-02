package mezz.jei.library.ingredients.itemStacks;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

record NormalizedTypedItem(Holder<Item> itemHolder) implements ITypedIngredient<ItemStack> {
	@Override
	public ItemStack getIngredient() {
		return new ItemStack(itemHolder);
	}

	@Override
	public Optional<ItemStack> getItemStack() {
		return Optional.of(getIngredient());
	}

	@Override
	public IIngredientType<ItemStack> getType() {
		return VanillaTypes.ITEM_STACK;
	}

	@Override
	public String toString() {
		return "SimpleItemStack{" +
			"itemHolder=" + itemHolder +
			'}';
	}
}
