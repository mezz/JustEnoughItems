package mezz.jei.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.constants.ModIds;

public final class TippedArrowRecipeMaker {

	public static List<IShapedRecipe<?>> createTippedArrowRecipes() {
		List<IShapedRecipe<?>> recipes = new ArrayList<>();
		String group = "jei.tipped.arrow";
		for (Potion potion : ForgeRegistries.POTIONS.getValues()) {
			ItemStack arrowStack = new ItemStack(Items.ARROW);
			ItemStack lingeringPotion = PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), potion);
			Ingredient arrowIngredient = Ingredient.of(arrowStack);
			Ingredient potionIngredient = Ingredient.of(lingeringPotion);
			NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY,
				arrowIngredient, arrowIngredient, arrowIngredient,
				arrowIngredient, potionIngredient, arrowIngredient,
				arrowIngredient, arrowIngredient, arrowIngredient
			);
			ItemStack output = new ItemStack(Items.TIPPED_ARROW, 8);
			PotionUtils.setPotion(output, potion);
			ResourceLocation id = new ResourceLocation(ModIds.MINECRAFT_ID, "jei.tipped.arrow." + output.getDescriptionId());
			ShapedRecipe recipe = new ShapedRecipe(id, group, 3, 3, inputs, output);
			recipes.add(recipe);
		}
		return recipes;
	}

	private TippedArrowRecipeMaker() {

	}
}
