package mezz.jei.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import mezz.jei.api.constants.ModIds;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.tags.ITag;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public final class ShulkerBoxColoringRecipeMaker {

    public static List<IRecipe<?>> createShulkerBoxColoringRecipes() {
        List<IRecipe<?>> recipes = new ArrayList<>();
        String group = "jei.shulker.color";
        ItemStack baseShulkerStack = new ItemStack(Blocks.SHULKER_BOX);
        Ingredient baseShulkerIngredient = Ingredient.fromStacks(baseShulkerStack);
        for (DyeColor color : DyeColor.values()) {
            DyeItem dye = DyeItem.getItem(color);
            ItemStack dyeStack = new ItemStack(dye);
            ITag<Item> colorTag = color.getTag();
            Ingredient.IItemList dyeList = new Ingredient.SingleItemList(dyeStack);
            Ingredient.IItemList colorList = new Ingredient.TagList(colorTag);
            Stream<Ingredient.IItemList> colorIngredientStream = Stream.of(dyeList, colorList);
            //Shulker box special recipe allows the matching dye item or any item in the tag.
            // we need to specify both in case someone removes the dye item from the dye tag
            // as the item will still be valid for this recipe
            Ingredient colorIngredient = Ingredient.fromItemListStream(colorIngredientStream);
            NonNullList<Ingredient> inputs = NonNullList.from(Ingredient.EMPTY, baseShulkerIngredient, colorIngredient);
            Block coloredShulkerBox = ShulkerBoxBlock.getBlockByColor(color);
            ItemStack output = new ItemStack(coloredShulkerBox);
            ResourceLocation id = new ResourceLocation(ModIds.MINECRAFT_ID, "jei.shulker.color." + output.getTranslationKey());
            ShapelessRecipe recipe = new ShapelessRecipe(id, group, output, inputs);
            recipes.add(recipe);
        }
        return recipes;
    }

    private ShulkerBoxColoringRecipeMaker() {

    }
}
