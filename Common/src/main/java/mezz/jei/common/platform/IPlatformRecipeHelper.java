package mezz.jei.common.platform;

import net.minecraft.core.Holder;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.FireworkRocketRecipe;
import net.minecraft.world.item.crafting.FireworkStarRecipe;
import net.minecraft.world.item.crafting.FireworkStarFadeRecipe;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IPlatformRecipeHelper {
	Ingredient getBase(SmithingRecipe recipe);
	Optional<Ingredient> getAddition(SmithingRecipe recipe);
	Optional<Ingredient> getTemplate(SmithingRecipe recipe);

	FireworkRocketRecipeData getFireworkRocketRecipeData(FireworkRocketRecipe recipe);
	FireworkStarRecipeData getFireworkStarRecipeData(FireworkStarRecipe recipe);
	FireworkStarFadeRecipeData getFireworkStarFadeRecipeData(FireworkStarFadeRecipe recipe);

	ItemStack getGrindstoneResult(GrindstoneMenu grindstoneMenu, ItemStack input1, ItemStack input2);

	String[] shrinkShapedRecipePattern(List<String> pattern);

	boolean isItemEnchantable(ItemStack stack, Holder<Enchantment> enchantment);

	record FireworkRocketRecipeData(Ingredient shell, Ingredient fuel, Ingredient star, ItemStack result) {
	}

	record FireworkStarRecipeData(Map<FireworkExplosion.Shape, Ingredient> shapes, Ingredient trail, Ingredient twinkle, Ingredient fuel, Ingredient dye, ItemStack result) {
	}

	record FireworkStarFadeRecipeData(Ingredient target, Ingredient dye, ItemStack result) {
	}
}
