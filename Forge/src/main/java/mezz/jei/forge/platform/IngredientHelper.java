package mezz.jei.forge.platform;

import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.common.platform.IPlatformIngredientHelper;
import mezz.jei.forge.ingredients.JeiIngredient;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.stream.Stream;

public class IngredientHelper implements IPlatformIngredientHelper {
    @Override
    public Ingredient createShulkerDyeIngredient(DyeColor color) {
        DyeItem dye = DyeItem.byColor(color);
        ItemStack dyeStack = new ItemStack(dye);
        TagKey<Item> colorTag = color.getTag();
        Ingredient.Value dyeList = new Ingredient.ItemValue(dyeStack);
        Ingredient.Value colorList = new Ingredient.TagValue(colorTag);
        Stream<Ingredient.Value> colorIngredientStream = Stream.of(dyeList, colorList);
        // Shulker box special recipe allows the matching dye item or any item in the tag.
        // we need to specify both in case someone removes the dye item from the dye tag
        // as the item will still be valid for this recipe.
        return Ingredient.fromValues(colorIngredientStream);
    }

    @Override
    public Ingredient createNbtIngredient(ItemStack stack, IStackHelper stackHelper) {
        return new JeiIngredient(stack, stackHelper);
    }
}
