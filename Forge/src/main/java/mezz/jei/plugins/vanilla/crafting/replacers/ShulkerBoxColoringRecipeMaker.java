package mezz.jei.plugins.vanilla.crafting.replacers;

import mezz.jei.api.constants.ModIds;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public final class ShulkerBoxColoringRecipeMaker {
	private static final String group = "jei.shulker.color";

	public static List<CraftingRecipe> createRecipes() {
		ItemStack baseShulkerStack = new ItemStack(Blocks.SHULKER_BOX);
		Ingredient baseShulkerIngredient = Ingredient.of(baseShulkerStack);
		return Arrays.stream(DyeColor.values())
			.map(color -> createRecipe(color, baseShulkerIngredient))
			.toList();
	}

	private static CraftingRecipe createRecipe(DyeColor color, Ingredient baseShulkerIngredient) {
		DyeItem dye = DyeItem.byColor(color);
		ItemStack dyeStack = new ItemStack(dye);
		TagKey<Item> colorTag = color.getTag();
		Ingredient.Value dyeList = new Ingredient.ItemValue(dyeStack);
		Ingredient.Value colorList = new Ingredient.TagValue(colorTag);
		Stream<Ingredient.Value> colorIngredientStream = Stream.of(dyeList, colorList);
		// Shulker box special recipe allows the matching dye item or any item in the tag.
		// we need to specify both in case someone removes the dye item from the dye tag
		// as the item will still be valid for this recipe.
		Ingredient colorIngredient = Ingredient.fromValues(colorIngredientStream);
		NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, baseShulkerIngredient, colorIngredient);
		Block coloredShulkerBox = ShulkerBoxBlock.getBlockByColor(color);
		ItemStack output = new ItemStack(coloredShulkerBox);
		ResourceLocation id = new ResourceLocation(ModIds.MINECRAFT_ID, group + "." + output.getDescriptionId());
		return new ShapelessRecipe(id, group, output, inputs);
	}

	private ShulkerBoxColoringRecipeMaker() {

	}
}
