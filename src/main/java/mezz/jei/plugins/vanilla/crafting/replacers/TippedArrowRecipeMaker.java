package mezz.jei.plugins.vanilla.crafting.replacers;

import mezz.jei.api.constants.ModIds;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.common.crafting.NBTIngredient;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public final class TippedArrowRecipeMaker {

	public static List<CraftingRecipe> createRecipes() {
		String group = "jei.tipped.arrow";
		ItemStack arrowStack = new ItemStack(Items.ARROW);
		Ingredient arrowIngredient = Ingredient.of(arrowStack);

		return ForgeRegistries.POTIONS.getValues().stream()
			.<CraftingRecipe>map(potion -> {
				ItemStack input = PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), potion);
				ItemStack output = PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW, 8), potion);

				Ingredient potionIngredient = new NBTIngredient(input) {};
				NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY,
					arrowIngredient, arrowIngredient, arrowIngredient,
					arrowIngredient, potionIngredient, arrowIngredient,
					arrowIngredient, arrowIngredient, arrowIngredient
				);
				ResourceLocation id = new ResourceLocation(ModIds.MINECRAFT_ID, "jei.tipped.arrow." + output.getDescriptionId());
				return new ShapedRecipe(id, group, 3, 3, inputs, output);
			})
			.toList();
	}

	private TippedArrowRecipeMaker() {

	}
}
