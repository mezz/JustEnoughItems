package mezz.jei.ingredients;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mezz.jei.api.IItemBlacklist;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.config.Config;
import mezz.jei.util.ErrorUtil;
import net.minecraft.item.ItemStack;

public class IngredientBlacklist implements IIngredientBlacklist, IItemBlacklist {
	private final IIngredientRegistry ingredientRegistry;
	private final Set<String> ingredientBlacklist = new HashSet<String>();

	public IngredientBlacklist(IIngredientRegistry ingredientRegistry) {
		this.ingredientRegistry = ingredientRegistry;
	}

	@Override
	public <V> void addIngredientToBlacklist(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
		String uniqueName = ingredientHelper.getUniqueId(ingredient);
		ingredientBlacklist.add(uniqueName);
	}

	@Override
	public <V> void removeIngredientFromBlacklist(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
		String uniqueName = ingredientHelper.getUniqueId(ingredient);
		ingredientBlacklist.remove(uniqueName);
	}

	@Override
	public <V> boolean isIngredientBlacklisted(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		if (isIngredientBlacklistedByApi(ingredient)) {
			return true;
		}

		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
		return Config.isIngredientOnConfigBlacklist(ingredient, ingredientHelper);
	}

	public <V> boolean isIngredientBlacklistedByApi(V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
		List<String> uids = IngredientUtil.getUniqueIdsWithWildcard(ingredientHelper, ingredient);

		for (String uid : uids) {
			if (ingredientBlacklist.contains(uid)) {
				return true;
			}
		}

		return false;
	}

	@Override
	@Deprecated
	public void addItemToBlacklist(ItemStack itemStack) {
		ErrorUtil.checkNotEmpty(itemStack);
		addIngredientToBlacklist(itemStack);
	}

	@Override
	@Deprecated
	public void removeItemFromBlacklist(ItemStack itemStack) {
		ErrorUtil.checkNotEmpty(itemStack);
		removeIngredientFromBlacklist(itemStack);
	}

	@Override
	@Deprecated
	public boolean isItemBlacklisted(ItemStack itemStack) {
		ErrorUtil.checkNotEmpty(itemStack);
		return isIngredientBlacklisted(itemStack);
	}

	@Deprecated
	public boolean isItemBlacklistedByApi(ItemStack itemStack) {
		return isIngredientBlacklistedByApi(itemStack);
	}
}
