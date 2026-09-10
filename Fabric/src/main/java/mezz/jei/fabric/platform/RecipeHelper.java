package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformRecipeHelper;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.FireworkRocketRecipe;
import net.minecraft.world.item.crafting.FireworkStarFadeRecipe;
import net.minecraft.world.item.crafting.FireworkStarRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Map;
import java.util.stream.Collectors;

public class RecipeHelper implements IPlatformRecipeHelper {
	@Override
	public <T extends CraftingRecipe> int getWidth(T recipe) {
		if (recipe instanceof ShapedRecipe shapedRecipe) {
			return shapedRecipe.getWidth();
		}
		return 0;
	}

	@Override
	public <T extends CraftingRecipe> int getHeight(T recipe) {
		if (recipe instanceof ShapedRecipe shapedRecipe) {
			return shapedRecipe.getHeight();
		}
		return 0;
	}

	@Override
	public FireworkRocketRecipeData getFireworkRocketRecipeData(FireworkRocketRecipe recipe) {
		return new FireworkRocketRecipeData(FireworkRocketRecipe.PAPER_INGREDIENT, FireworkRocketRecipe.GUNPOWDER_INGREDIENT, FireworkRocketRecipe.STAR_INGREDIENT, new ItemStack(Items.FIREWORK_ROCKET, 3));
	}

	@Override
	public FireworkStarRecipeData getFireworkStarRecipeData(FireworkStarRecipe recipe) {
		Map<FireworkRocketItem.Shape, Ingredient> shapes = FireworkStarRecipe.SHAPE_BY_ITEM.entrySet().stream()
			.collect(Collectors.groupingBy(Map.Entry::getValue,
				Collectors.collectingAndThen(Collectors.mapping(Map.Entry::getKey, Collectors.toList()),
					items -> Ingredient.of(items.toArray(Item[]::new)))));
		return new FireworkStarRecipeData(shapes, FireworkStarRecipe.TRAIL_INGREDIENT, FireworkStarRecipe.FLICKER_INGREDIENT,
			FireworkStarRecipe.GUNPOWDER_INGREDIENT, getFireworkDyes(), new ItemStack(Items.FIREWORK_STAR));
	}

	@Override
	public FireworkStarFadeRecipeData getFireworkStarFadeRecipeData(FireworkStarFadeRecipe recipe) {
		return new FireworkStarFadeRecipeData(FireworkStarFadeRecipe.STAR_INGREDIENT, getFireworkDyes(), new ItemStack(Items.FIREWORK_STAR));
	}

	private static Ingredient getFireworkDyes() {
		return Ingredient.of(Registry.ITEM.stream().filter(item -> item instanceof DyeItem).toArray(Item[]::new));
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
		return true;
	}
}
