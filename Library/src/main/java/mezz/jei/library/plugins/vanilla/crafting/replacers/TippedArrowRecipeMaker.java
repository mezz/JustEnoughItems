package mezz.jei.library.plugins.vanilla.crafting.replacers;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.common.platform.IPlatformIngredientHelper;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.common.platform.Services;
import mezz.jei.library.plugins.vanilla.crafting.JeiShapedRecipeBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public final class TippedArrowRecipeMaker {

	public static List<CraftingRecipe> createRecipes(IJeiHelpers jeiHelpers) {
		IStackHelper stackHelper = jeiHelpers.getStackHelper();

		String group = "jei.tipped.arrow";
		ItemStack arrowStack = new ItemStack(Items.ARROW);
		Ingredient arrowIngredient = Ingredient.of(arrowStack);

		IPlatformRegistry<Potion> potionRegistry = Services.PLATFORM.getRegistry(Registries.POTION);
		IPlatformIngredientHelper ingredientHelper = Services.PLATFORM.getIngredientHelper();
		return potionRegistry.getValues()
			.<CraftingRecipe>map(potion -> {
				ItemStack input = PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), potion);
				ItemStack output = PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW, 8), potion);

				Ingredient potionIngredient = ingredientHelper.createNbtIngredient(input, stackHelper);
				ResourceLocation id = new ResourceLocation(ModIds.MINECRAFT_ID, "jei.tipped.arrow." + output.getDescriptionId());
				return new JeiShapedRecipeBuilder(id, CraftingBookCategory.MISC, List.of(output))
					.group(group)
					.define('a', arrowIngredient)
					.define('p', potionIngredient)
					.pattern("aaa")
					.pattern("apa")
					.pattern("aaa")
					.build();
			})
			.toList();
	}

	private TippedArrowRecipeMaker() {

	}
}
