package mezz.jei;

import java.util.Locale;

import com.google.common.collect.ImmutableListMultimap;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.util.ModIdUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemRegistryFactory {
	public static ItemRegistry createItemRegistry(IIngredientRegistry ingredientRegistry, ModIdUtil modIdUtil) {
		IIngredientHelper<ItemStack> itemStackHelper = ingredientRegistry.getIngredientHelper(ItemStack.class);
		ImmutableListMultimap.Builder<String, ItemStack> itemsByModIdBuilder = ImmutableListMultimap.builder();
		for (ItemStack itemStack : ingredientRegistry.getIngredients(ItemStack.class)) {
			Item item = itemStack.getItem();
			if (item != null) {
				String modId = itemStackHelper.getModId(itemStack).toLowerCase(Locale.ENGLISH);
				itemsByModIdBuilder.put(modId, itemStack);
			}
		}

		return new ItemRegistry(ingredientRegistry, itemsByModIdBuilder.build(), modIdUtil);
	}
}
