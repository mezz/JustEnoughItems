package mezz.jei.common.platform;

import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.item.enchantment.Enchantment;

public interface IPlatformRecipeHelper {
	<T extends CraftingRecipe> int getWidth(T recipe);
	<T extends CraftingRecipe> int getHeight(T recipe);

	Ingredient getBase(UpgradeRecipe recipe);
	Ingredient getAddition(UpgradeRecipe recipe);

	ItemStack getGrindstoneResult(GrindstoneMenu grindstoneMenu, ItemStack input1, ItemStack input2);

	boolean isItemEnchantable(ItemStack stack, Enchantment enchantment);
}
