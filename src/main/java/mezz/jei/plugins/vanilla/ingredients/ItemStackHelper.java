package mezz.jei.plugins.vanilla.ingredients;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.StackHelper;
import mezz.jei.util.color.ColorGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ItemStackHelper implements IIngredientHelper<ItemStack> {
	@Override
	public List<ItemStack> expandSubtypes(List<ItemStack> contained) {
		return Internal.getStackHelper().getAllSubtypes(contained);
	}

	@Override
	@Nullable
	public ItemStack getMatch(Iterable<ItemStack> ingredients, ItemStack toMatch) {
		return Internal.getStackHelper().containsStack(ingredients, toMatch);
	}

	@Override
	public String getDisplayName(ItemStack ingredient) {
		String displayName = ingredient.getDisplayName();
		if (displayName == null) {
			String ingredientInfo = getErrorInfo(ingredient);
			throw new NullPointerException("No display name for itemStack. " + ingredientInfo);
		}
		return displayName;
	}

	@Override
	public String getUniqueId(ItemStack ingredient) {
		return Internal.getStackHelper().getUniqueIdentifierForStack(ingredient);
	}

	@Override
	public String getWildcardId(ItemStack ingredient) {
		return Internal.getStackHelper().getUniqueIdentifierForStack(ingredient, StackHelper.UidMode.WILDCARD);
	}

	@Override
	public String getModId(ItemStack ingredient) {
		Item item = ingredient.getItem();
		if (item == null) {
			throw new NullPointerException("Null item in ItemStack");
		}

		ResourceLocation itemName = item.getRegistryName();
		if (itemName == null) {
			String stackInfo = getErrorInfo(ingredient);
			throw new NullPointerException("item.getRegistryName() returned null for: " + stackInfo);
		}

		return itemName.getResourceDomain();
	}

	@Override
	public Iterable<Color> getColors(ItemStack ingredient) {
		return ColorGetter.getColors(ingredient, 2);
	}

	@Override
	public String getErrorInfo(ItemStack ingredient) {
		return ErrorUtil.getItemStackInfo(ingredient);
	}
}
