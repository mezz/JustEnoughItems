package mezz.jei.common.platform;

import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.core.Holder;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.Optional;

public interface IPlatformRecipeHelper {
	Ingredient getBase(SmithingRecipe recipe);
	Optional<Ingredient> getAddition(SmithingRecipe recipe);
	Optional<Ingredient> getTemplate(SmithingRecipe recipe);

	ItemStack getGrindstoneResult(GrindstoneMenu grindstoneMenu, ItemStack input1, ItemStack input2);

	List<IJeiBrewingRecipe> getBrewingRecipes(IIngredientManager ingredientManager, IVanillaRecipeFactory vanillaRecipeFactory, PotionBrewing potionBrewing);

	String[] shrinkShapedRecipePattern(List<String> pattern);

	boolean isItemEnchantable(ItemStack stack, Holder<Enchantment> enchantment);
}
