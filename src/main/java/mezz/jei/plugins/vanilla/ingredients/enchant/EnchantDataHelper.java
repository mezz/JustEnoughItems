package mezz.jei.plugins.vanilla.ingredients.enchant;

import javax.annotation.Nullable;
import java.awt.Color;

import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.google.common.base.MoreObjects;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.IFocus;

public class EnchantDataHelper implements IIngredientHelper<EnchantmentData> {
	private final EnchantedBookCache cache;
	private final IIngredientHelper<ItemStack> itemStackHelper;

	public EnchantDataHelper(EnchantedBookCache cache, IIngredientHelper<ItemStack> itemStackHelper) {
		this.cache = cache;
		this.itemStackHelper = itemStackHelper;
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
	public IFocus<?> translateFocus(IFocus<EnchantmentData> focus, IFocusFactory focusFactory) {
		EnchantmentData enchantData = focus.getValue();
		ItemStack itemStack = cache.getEnchantedBook(enchantData);
		return focusFactory.createFocus(focus.getMode(), itemStack);
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
		ResourceLocation registryName = ingredient.enchantment.getRegistryName();
		if (registryName == null) {
			String stackInfo = getErrorInfo(ingredient);
			throw new IllegalStateException("enchantment.getRegistryName() returned null for: " + stackInfo);
		}
		return registryName.getNamespace();
	}

	@Override
	public String getDisplayModId(EnchantmentData ingredient) {
		ItemStack enchantedBook = cache.getEnchantedBook(ingredient);
		return this.itemStackHelper.getDisplayModId(enchantedBook);
	}

	@Override
	public Iterable<Color> getColors(EnchantmentData ingredient) {
		ItemStack enchantedBook = cache.getEnchantedBook(ingredient);
		return this.itemStackHelper.getColors(enchantedBook);
	}

	@Override
	public String getResourceId(EnchantmentData ingredient) {
		ResourceLocation registryName = ingredient.enchantment.getRegistryName();
		if (registryName == null) {
			String stackInfo = getErrorInfo(ingredient);
			throw new IllegalStateException("enchantment.getRegistryName() returned null for: " + stackInfo);
		}
		return registryName.getPath();
	}

	@Override
	public ItemStack getCheatItemStack(EnchantmentData ingredient) {
		return cache.getEnchantedBook(ingredient);
	}

	@Override
	public EnchantmentData copyIngredient(EnchantmentData ingredient) {
		return new EnchantmentData(ingredient.enchantment, ingredient.enchantmentLevel);
	}

	@Override
	public boolean isIngredientOnServer(EnchantmentData ingredient) {
		ItemStack enchantedBook = cache.getEnchantedBook(ingredient);
		return this.itemStackHelper.isIngredientOnServer(enchantedBook);
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
