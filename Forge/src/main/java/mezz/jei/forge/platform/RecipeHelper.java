package mezz.jei.forge.platform;

import mezz.jei.common.platform.IPlatformRecipeHelper;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.crafting.IShapedRecipe;

public class RecipeHelper implements IPlatformRecipeHelper {
	@Override
	public <T extends CraftingRecipe> int getWidth(T recipe) {
		if (recipe instanceof IShapedRecipe<?> shapedRecipe) {
			return shapedRecipe.getRecipeWidth();
		}
		return 0;
	}

	@Override
	public <T extends CraftingRecipe> int getHeight(T recipe) {
		if (recipe instanceof IShapedRecipe<?> shapedRecipe) {
			return shapedRecipe.getRecipeHeight();
		}
		return 0;
	}

	@Override
	public Ingredient getBase(UpgradeRecipe recipe) {
		return recipe.base;
	}

	@Override
	public Ingredient getAddition(UpgradeRecipe recipe) {
		return recipe.addition;
	}

	@Override
	public ItemStack getGrindstoneResult(GrindstoneMenu grindstoneMenu, ItemStack input1, ItemStack input2) {
		grindstoneMenu.slots.get(0).set(input1.copy());
		grindstoneMenu.slots.get(1).set(input2.copy());
		grindstoneMenu.createResult();
		return grindstoneMenu.slots.get(2).getItem().copy();
	}

	@Override
	public boolean isItemEnchantable(ItemStack stack, Enchantment enchantment) {
		return stack.getItem().isEnchantable(stack);
	}
}
