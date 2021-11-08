package mezz.jei.plugins.vanilla.crafting.replacers;

import mezz.jei.api.constants.ModIds;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.tags.ITag;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.stream.Stream;

public final class ShulkerBoxColoringRecipeMaker {

	public static Stream<ICraftingRecipe> createRecipes() {
		String group = "jei.shulker.color";
		ItemStack baseShulkerStack = new ItemStack(Blocks.SHULKER_BOX);
		Ingredient baseShulkerIngredient = Ingredient.of(baseShulkerStack);
		return Arrays.stream(DyeColor.values())
			.map(color -> {
				DyeItem dye = DyeItem.byColor(color);
				ItemStack dyeStack = new ItemStack(dye);
				ITag<Item> colorTag = color.getTag();
				Ingredient.IItemList dyeList = new Ingredient.SingleItemList(dyeStack);
				Ingredient.IItemList colorList = new Ingredient.TagList(colorTag);
				Stream<Ingredient.IItemList> colorIngredientStream = Stream.of(dyeList, colorList);
				// Shulker box special recipe allows the matching dye item or any item in the tag.
				// we need to specify both in case someone removes the dye item from the dye tag
				// as the item will still be valid for this recipe.
				Ingredient colorIngredient = Ingredient.fromValues(colorIngredientStream);
				NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, baseShulkerIngredient, colorIngredient);
				Block coloredShulkerBox = ShulkerBoxBlock.getBlockByColor(color);
				ItemStack output = new ItemStack(coloredShulkerBox);
				ResourceLocation id = new ResourceLocation(ModIds.MINECRAFT_ID, "jei.shulker.color." + output.getDescriptionId());
				return new ShapelessRecipe(id, group, output, inputs);
			});
	}

	private ShulkerBoxColoringRecipeMaker() {

	}
}
