package mezz.jei.library.plugins.vanilla.crafting.replacers;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.platform.IPlatformIngredientHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.Arrays;
import java.util.List;

public final class ShulkerBoxColoringRecipeMaker {
	private static final String group = "jei.shulker.color";

	public static List<RecipeHolder<CraftingRecipe>> createRecipes() {
		Ingredient baseShulkerIngredient = Ingredient.of(Blocks.SHULKER_BOX);
		return Arrays.stream(DyeColor.values())
			.map(color -> createRecipe(color, baseShulkerIngredient))
			.toList();
	}

	private static RecipeHolder<CraftingRecipe> createRecipe(DyeColor color, Ingredient baseShulkerIngredient) {
		IPlatformIngredientHelper ingredientHelper = Services.PLATFORM.getIngredientHelper();
		Ingredient colorIngredient = ingredientHelper.createShulkerDyeIngredient(color);
		NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, baseShulkerIngredient, colorIngredient);
		Block coloredShulkerBox = ShulkerBoxBlock.getBlockByColor(color);
		ItemStack output = new ItemStack(coloredShulkerBox);
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ModIds.MINECRAFT_ID, group + "." + output.getDescriptionId());
		CraftingRecipe recipe = new ShapelessRecipe(group, CraftingBookCategory.MISC, output, inputs);
		return new RecipeHolder<>(id, recipe);
	}

	private ShulkerBoxColoringRecipeMaker() {

	}
}
