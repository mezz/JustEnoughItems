package mezz.jei.forge.platform;

import mezz.jei.common.platform.IPlatformRecipeHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.crafting.IShapedRecipe;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

	@SuppressWarnings("OptionalOfNullableMisuse")
	@Override
	public Optional<ResourceLocation> getRegistryNameForRecipe(Recipe<?> recipe) {
		return Optional.ofNullable(recipe.getId());
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
