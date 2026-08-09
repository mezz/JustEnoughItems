package mezz.jei.common.platform;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Optional;

public interface IPlatformRecipeHelper {
	<T extends CraftingRecipe> int getWidth(T recipe);
	<T extends CraftingRecipe> int getHeight(T recipe);

	Ingredient getBase(SmithingRecipe recipe);
	Ingredient getAddition(SmithingRecipe recipe);
	Ingredient getTemplate(SmithingRecipe recipe);

	Optional<ResourceLocation> getRegistryNameForRecipe(Recipe<?> recipe);

	ItemStack getGrindstoneResult(GrindstoneMenu grindstoneMenu, ItemStack input1, ItemStack input2);

	boolean isItemEnchantable(ItemStack stack, Enchantment enchantment);
}
