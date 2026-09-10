package mezz.jei.common.platform;

import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.FireworkRocketRecipe;
import net.minecraft.world.item.crafting.FireworkStarFadeRecipe;
import net.minecraft.world.item.crafting.FireworkStarRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Map;

public interface IPlatformRecipeHelper {
	<T extends CraftingRecipe> int getWidth(T recipe);
	<T extends CraftingRecipe> int getHeight(T recipe);

	Ingredient getBase(UpgradeRecipe recipe);
	Ingredient getAddition(UpgradeRecipe recipe);

	FireworkRocketRecipeData getFireworkRocketRecipeData(FireworkRocketRecipe recipe);
	FireworkStarRecipeData getFireworkStarRecipeData(FireworkStarRecipe recipe);
	FireworkStarFadeRecipeData getFireworkStarFadeRecipeData(FireworkStarFadeRecipe recipe);

	ItemStack getGrindstoneResult(GrindstoneMenu grindstoneMenu, ItemStack input1, ItemStack input2);

	boolean isItemEnchantable(ItemStack stack, Enchantment enchantment);

	record FireworkRocketRecipeData(Ingredient shell, Ingredient fuel, Ingredient star, ItemStack result) {
	}

	record FireworkStarRecipeData(Map<FireworkRocketItem.Shape, Ingredient> shapes, Ingredient trail, Ingredient twinkle, Ingredient fuel, Ingredient dye, ItemStack result) {
	}

	record FireworkStarFadeRecipeData(Ingredient target, Ingredient dye, ItemStack result) {
	}
}
