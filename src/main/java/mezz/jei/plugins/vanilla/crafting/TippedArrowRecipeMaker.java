package mezz.jei.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.constants.ModIds;

public final class TippedArrowRecipeMaker {

	public static List<IShapedRecipe> createTippedArrowRecipes() {
		List<IShapedRecipe> recipes = new ArrayList<>();
		String group = "jei.tipped.arrow";
		for (Potion potionType : ForgeRegistries.POTION_TYPES.getValues()) {
			ItemStack arrowStack = new ItemStack(Items.ARROW);
			ItemStack lingeringPotion = PotionUtils.addPotionToItemStack(new ItemStack(Items.LINGERING_POTION), potionType);
			Ingredient arrowIngredient = Ingredient.fromStacks(arrowStack);
			Ingredient potionIngredient = Ingredient.fromStacks(lingeringPotion);
			NonNullList<Ingredient> inputs = NonNullList.from(Ingredient.EMPTY,
				arrowIngredient, arrowIngredient, arrowIngredient,
				arrowIngredient, potionIngredient, arrowIngredient,
				arrowIngredient, arrowIngredient, arrowIngredient
			);
			ItemStack output = new ItemStack(Items.TIPPED_ARROW, 8);
			PotionUtils.addPotionToItemStack(output, potionType);
			ResourceLocation id = new ResourceLocation(ModIds.MINECRAFT_ID, "jei.tipped.arrow." + output.getTranslationKey());
			ShapedRecipe recipe = new ShapedRecipe(id, group, 3, 3, inputs, output);
			recipes.add(recipe);
		}
		return recipes;
	}

	private TippedArrowRecipeMaker() {

	}
}
