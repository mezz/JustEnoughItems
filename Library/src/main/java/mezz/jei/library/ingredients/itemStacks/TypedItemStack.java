package mezz.jei.library.ingredients.itemStacks;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record TypedItemStack(
	Holder<Item> itemHolder,
	DataComponentPatch dataComponentPatch,
	int count
) implements ITypedIngredient<ItemStack> {
	public static ITypedIngredient<ItemStack> create(ItemStack ingredient) {
		if (ingredient.getCount() == 1) {
			return NormalizedTypedItemStack.create(ingredient);
		}
		return new TypedItemStack(
			ingredient.getItemHolder(),
			ingredient.getComponentsPatch(),
			ingredient.getCount()
		);
	}

	@Override
	public ItemStack getIngredient() {
		return new ItemStack(itemHolder, count, dataComponentPatch);
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
		return "TypedItemStack{" +
			"itemHolder=" + itemHolder +
			", dataComponentPatch=" + dataComponentPatch +
			", count=" + count +
			'}';
	}
}
