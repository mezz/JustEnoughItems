package mezz.jei.library.ingredients.itemStacks;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

public record TypedItemStack(
	Holder<Item> itemHolder,
	@Nullable CompoundTag tag,
	int count
) implements ITypedIngredient<ItemStack> {
	public static ITypedIngredient<ItemStack> create(ItemStack ingredient) {
		if (ingredient.getCount() == 1) {
			return NormalizedTypedItemStack.create(ingredient.getItemHolder(), ingredient.getTag());
		}
		return new TypedItemStack(
			ingredient.getItemHolder(),
			ingredient.getTag(),
			ingredient.getCount()
		);
	}

	@Override
	public ItemStack getIngredient() {
		ItemStack itemStack = new ItemStack(itemHolder, count);
		if (tag != null) {
			itemStack.setTag(tag);
		}
		return itemStack;
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
			", tag=" + tag +
			", count=" + count +
			'}';
	}
}
