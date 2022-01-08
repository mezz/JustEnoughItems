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

import java.util.stream.Stream;

public final class TippedArrowRecipeMaker {

	public static Stream<CraftingRecipe> createRecipes() {
		String group = "jei.tipped.arrow";
		return ForgeRegistries.POTIONS.getValues().stream()
			.map(potion -> {
				ItemStack arrowStack = new ItemStack(Items.ARROW);
				ItemStack lingeringPotion = PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), potion);
				Ingredient arrowIngredient = Ingredient.of(arrowStack);
				Ingredient potionIngredient = new NBTIngredient(lingeringPotion) {
				};
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
