package mezz.jei.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import mezz.jei.api.constants.ModIds;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.tags.Tag;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;

public final class ShulkerBoxColoringRecipeMaker {

	public static List<Recipe<?>> createShulkerBoxColoringRecipes() {
		List<Recipe<?>> recipes = new ArrayList<>();
		String group = "jei.shulker.color";
		ItemStack baseShulkerStack = new ItemStack(Blocks.SHULKER_BOX);
		Ingredient baseShulkerIngredient = Ingredient.of(baseShulkerStack);
		for (DyeColor color : DyeColor.values()) {
			DyeItem dye = DyeItem.byColor(color);
			ItemStack dyeStack = new ItemStack(dye);
			Tag<Item> colorTag = color.getTag();
			Ingredient.Value dyeList = new Ingredient.ItemValue(dyeStack);
			Ingredient.Value colorList = new Ingredient.TagValue(colorTag);
			Stream<Ingredient.Value> colorIngredientStream = Stream.of(dyeList, colorList);
			//Shulker box special recipe allows the matching dye item or any item in the tag.
			// we need to specify both in case someone removes the dye item from the dye tag
			// as the item will still be valid for this recipe
			Ingredient colorIngredient = Ingredient.fromValues(colorIngredientStream);
			NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, baseShulkerIngredient, colorIngredient);
			Block coloredShulkerBox = ShulkerBoxBlock.getBlockByColor(color);
			ItemStack output = new ItemStack(coloredShulkerBox);
			ResourceLocation id = new ResourceLocation(ModIds.MINECRAFT_ID, "jei.shulker.color." + output.getDescriptionId());
			ShapelessRecipe recipe = new ShapelessRecipe(id, group, output, inputs);
			recipes.add(recipe);
		}
		return recipes;
	}

	private ShulkerBoxColoringRecipeMaker() {

	}
}
