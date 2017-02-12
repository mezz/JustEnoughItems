package mezz.jei.plugins.vanilla.anvil;

import com.google.common.collect.Lists;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AnvilRecipeMaker
{
    public static List<AnvilRecipeWrapper> getVanillaAnvilRecipes()
    {
        List<AnvilRecipeWrapper> recipes = Lists.newArrayList();
        addBookEnchantmentRecipes(recipes);
        addRepairRecipes(recipes);
        return recipes;
    }

    public static void addBookEnchantmentRecipes(List<AnvilRecipeWrapper> recipes)
    {
        // TODO

        ItemStack original = new ItemStack(Items.DIAMOND_SWORD);
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        ItemStack withEnchant = new ItemStack(Items.DIAMOND_SWORD);
        EnchantmentHelper.setEnchantments(Collections.singletonMap(Enchantments.SHARPNESS, 5), withEnchant);
        EnchantmentHelper.setEnchantments(Collections.singletonMap(Enchantments.SHARPNESS, 5), book);

        recipes.add(new AnvilRecipeWrapper(original, book, withEnchant, 5));
    }

    public static void addRepairRecipes(List<AnvilRecipeWrapper> recipes)
    {
        // TODO
    }
}
