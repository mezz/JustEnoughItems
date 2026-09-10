package mezz.jei.neoforge.platform;

import mezz.jei.common.platform.IPlatformRecipeHelper.FireworkRocketRecipeData;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.DyeItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.crafting.FireworkRocketRecipe;
import net.minecraft.world.item.crafting.FireworkStarRecipe;
import net.minecraft.world.item.crafting.FireworkStarFadeRecipe;
import java.util.Map;
import java.util.stream.Collectors;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.enchantment.Enchantment;

public class RecipeHelper implements IPlatformRecipeHelper {
	@Override
	public FireworkRocketRecipeData getFireworkRocketRecipeData(FireworkRocketRecipe recipe) {
		return new FireworkRocketRecipeData(FireworkRocketRecipe.PAPER_INGREDIENT, FireworkRocketRecipe.GUNPOWDER_INGREDIENT, FireworkRocketRecipe.STAR_INGREDIENT, new ItemStack(Items.FIREWORK_ROCKET, 3));
	}

	@Override
	public FireworkStarRecipeData getFireworkStarRecipeData(FireworkStarRecipe recipe) {
		Map<FireworkExplosion.Shape, Ingredient> shapes = FireworkStarRecipe.SHAPE_BY_ITEM.entrySet().stream()
			.collect(Collectors.groupingBy(Map.Entry::getValue,
				Collectors.collectingAndThen(Collectors.mapping(Map.Entry::getKey, Collectors.toList()),
					items -> Ingredient.of(items.toArray(Item[]::new)))));
		return new FireworkStarRecipeData(shapes, FireworkStarRecipe.TRAIL_INGREDIENT, FireworkStarRecipe.TWINKLE_INGREDIENT,
			FireworkStarRecipe.GUNPOWDER_INGREDIENT, getFireworkDyes(), new ItemStack(Items.FIREWORK_STAR));
	}

	@Override
	public FireworkStarFadeRecipeData getFireworkStarFadeRecipeData(FireworkStarFadeRecipe recipe) {
		return new FireworkStarFadeRecipeData(FireworkStarFadeRecipe.STAR_INGREDIENT, getFireworkDyes(), new ItemStack(Items.FIREWORK_STAR));
	}

	private static Ingredient getFireworkDyes() {
		// These recipes accept DyeItem instances in 1.21.1, including modded dyes.
		return Ingredient.of(BuiltInRegistries.ITEM.stream().filter(item -> item instanceof DyeItem).toArray(Item[]::new));
	}

	@Override
	public Ingredient getBase(SmithingRecipe recipe) {
		if (recipe instanceof SmithingTransformRecipe transformRecipe) {
			return transformRecipe.base;
		}
		if (recipe instanceof SmithingTrimRecipe trimRecipe) {
			return trimRecipe.base;
		}
		return Ingredient.EMPTY;
	}

	@Override
	public Ingredient getAddition(SmithingRecipe recipe) {
		if (recipe instanceof SmithingTransformRecipe transformRecipe) {
			return transformRecipe.addition;
		}
		if (recipe instanceof SmithingTrimRecipe trimRecipe) {
			return trimRecipe.addition;
		}
		return Ingredient.EMPTY;
	}

	@Override
	public Ingredient getTemplate(SmithingRecipe recipe) {
		if (recipe instanceof SmithingTransformRecipe transformRecipe) {
			return transformRecipe.template;
		}
		if (recipe instanceof SmithingTrimRecipe trimRecipe) {
			return trimRecipe.template;
		}
		return Ingredient.EMPTY;
	}

	@Override
	public ItemStack getGrindstoneResult(GrindstoneMenu grindstoneMenu, ItemStack input1, ItemStack input2) {
		return grindstoneMenu.computeResult(input1, input2);
	}

	@Override
	public boolean isItemEnchantable(ItemStack stack, Holder<Enchantment> enchantment) {
		return stack.getItem().isEnchantable(stack) && stack.supportsEnchantment(enchantment);
	}
}
