package mezz.jei.plugins.vanilla.ingredients;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Collections;
import java.util.List;

import com.google.common.base.MoreObjects;
import mezz.jei.api.ingredients.IIngredientHelper;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;

public class EnchantDataHelper implements IIngredientHelper<EnchantmentData> {
	@Override
	public List<EnchantmentData> expandSubtypes(List<EnchantmentData> contained) {
		return contained;
	}

	@Override
	@Nullable
	public EnchantmentData getMatch(Iterable<EnchantmentData> ingredients, EnchantmentData toMatch) {
		for (EnchantmentData enchantData : ingredients) {
			if (enchantData.enchantment.getRegistryName() == toMatch.enchantment.getRegistryName()
					&& enchantData.enchantmentLevel == toMatch.enchantmentLevel) {
				return enchantData;
			}
		}
		return null;
	}

	@Override
	public String getDisplayName(EnchantmentData ingredient) {
		return ingredient.enchantment.getTranslatedName(ingredient.enchantmentLevel);
	}

	@Override
	public String getUniqueId(EnchantmentData ingredient) {
		return "enchantment:" + ingredient.enchantment.getName() + ".lvl" + ingredient.enchantmentLevel;
	}

	@Override
	public String getWildcardId(EnchantmentData ingredient) {
		return getUniqueId(ingredient);
	}

	@Override
	public String getModId(EnchantmentData ingredient) {
		return ingredient.enchantment.getRegistryName().getNamespace();
	}

	@Override
	public Iterable<Color> getColors(EnchantmentData ingredient) {
		return Collections.emptyList();
	}

	@Override
	public String getResourceId(EnchantmentData ingredient) {
		return ingredient.enchantment.getRegistryName().getPath();
	}

	@Override
	public ItemStack getCheatItemStack(EnchantmentData ingredient) {
		return ItemEnchantedBook.getEnchantedItemStack(ingredient);
	}

	@Override
	public EnchantmentData copyIngredient(EnchantmentData ingredient) {
		return new EnchantmentData(ingredient.enchantment, ingredient.enchantmentLevel);
	}

	@Override
	public String getErrorInfo(@Nullable EnchantmentData ingredient) {
		if (ingredient == null) {
			return "null";
		}
		MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(EnchantmentData.class);

		toStringHelper.add("Enchantment", ingredient.enchantment.getName());
		toStringHelper.add("Level", ingredient.enchantmentLevel);

		return toStringHelper.toString();
	}
}
