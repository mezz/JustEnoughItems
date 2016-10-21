package mezz.jei;

import javax.annotation.Nullable;
import java.util.Locale;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import mezz.jei.api.IItemRegistry;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.util.Log;
import mezz.jei.util.ModIdUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Deprecated
public class ItemRegistry implements IItemRegistry {
	private final IIngredientRegistry ingredientRegistry;
	private final ModIdUtil modIdUtil;

	public ItemRegistry(
			IIngredientRegistry ingredientRegistry,
			ModIdUtil modIdUtil
	) {
		this.ingredientRegistry = ingredientRegistry;
		this.modIdUtil = modIdUtil;
	}

	@Override
	public ImmutableList<ItemStack> getItemList() {
		return ingredientRegistry.getIngredients(ItemStack.class);
	}

	@Override
	public ImmutableList<ItemStack> getFuels() {
		return ingredientRegistry.getFuels();
	}

	@Override
	public ImmutableList<ItemStack> getPotionIngredients() {
		return ingredientRegistry.getPotionIngredients();
	}

	@Override
	public String getModNameForItem(@Nullable Item item) {
		if (item == null) {
			Log.error("Null item", new NullPointerException());
			return "";
		}
		return modIdUtil.getModNameForItem(item);
	}

	@Override
	public String getModNameForModId(@Nullable String modId) {
		if (modId == null) {
			Log.error("Null modId", new NullPointerException());
			return "";
		}
		return modIdUtil.getModNameForModId(modId);
	}

	@Override
	public ImmutableList<ItemStack> getItemListForModId(@Nullable String modId) {
		return ImmutableList.of();
	}
}
