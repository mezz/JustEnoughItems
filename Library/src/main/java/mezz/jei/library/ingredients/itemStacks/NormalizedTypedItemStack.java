package mezz.jei.library.ingredients.itemStacks;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

public record NormalizedTypedItemStack(
	Item item,
	CompoundTag tag
) implements ITypedIngredient<ItemStack> {
	public static ITypedIngredient<ItemStack> normalize(ITypedIngredient<ItemStack> typedIngredient) {
		if (typedIngredient instanceof NormalizedTypedItemStack normalized) {
			return normalized;
		} else if (typedIngredient instanceof NormalizedTypedItem normalized) {
			return normalized;
		} else if (typedIngredient instanceof TypedItemStack typedItemStack) {
			return create(typedItemStack.item(), typedItemStack.tag());
		}
		ItemStack itemStack = typedIngredient.getIngredient();
		return create(itemStack.getItem(), itemStack.getTag());
	}

	public static ITypedIngredient<ItemStack> create(Item item, @Nullable CompoundTag tag) {
		if (tag == null) {
			return new NormalizedTypedItem(item);
		}
		return new NormalizedTypedItemStack(item, tag);
	}

	public static ITypedIngredient<ItemStack> create(ItemStack itemStack) {
		return create(
			itemStack.getItem(),
			itemStack.getTag()
		);
	}

	@Override
	public ItemStack getIngredient() {
		ItemStack itemStack = new ItemStack(item, 1);
		itemStack.setTag(tag);
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
		return "NormalizedTypedItemStack{" +
			"item=" + item +
			", tag=" + tag +
			'}';
	}
}
