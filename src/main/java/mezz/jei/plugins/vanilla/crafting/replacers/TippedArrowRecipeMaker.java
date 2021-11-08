package mezz.jei.plugins.vanilla.crafting.replacers;

import mezz.jei.api.constants.ModIds;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.stream.Stream;

public final class TippedArrowRecipeMaker {

	public static Stream<ICraftingRecipe> createRecipes() {
		String group = "jei.tipped.arrow";
		return ForgeRegistries.POTION_TYPES.getValues().stream()
			.map(potion -> {
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
				return new ShapedRecipe(id, group, 3, 3, inputs, output);
			});
	}

	private TippedArrowRecipeMaker() {

	}
}
