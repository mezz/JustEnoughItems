package mezz.jei.library.ingredients.itemStacks;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record NormalizedTypedItemStack(
	Holder<Item> itemHolder,
	DataComponentPatch dataComponentPatch
) implements ITypedIngredient<ItemStack> {
	public static ITypedIngredient<ItemStack> normalize(ITypedIngredient<ItemStack> typedIngredient) {
		switch (typedIngredient) {
			case NormalizedTypedItemStack normalized -> {
				return normalized;
			}
			case NormalizedTypedItem normalized -> {
				return normalized;
			}
			case TypedItemStack typedItemStack -> {
				return create(typedItemStack.itemHolder(), typedItemStack.dataComponentPatch());
			}
			default -> {
				ItemStack itemStack = typedIngredient.getIngredient();
				return create(itemStack.getItemHolder(), itemStack.getComponentsPatch());
			}
		}
	}

	public static ITypedIngredient<ItemStack> create(Holder<Item> itemHolder, DataComponentPatch dataComponentPatch) {
		if (dataComponentPatch.isEmpty()) {
			return new NormalizedTypedItem(itemHolder);
		}
		return new NormalizedTypedItemStack(itemHolder, dataComponentPatch);
	}

	public static ITypedIngredient<ItemStack> create(ItemStack itemStack) {
		return create(
			itemStack.getItemHolder(),
			itemStack.getComponentsPatch()
		);
	}

	@Override
	public ItemStack getIngredient() {
		return new ItemStack(itemHolder, 1, dataComponentPatch);
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
			"itemHolder=" + itemHolder +
			", dataComponentPatch=" + dataComponentPatch +
			'}';
	}
}
