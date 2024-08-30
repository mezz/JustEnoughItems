package mezz.jei.library.plugins.vanilla.crafting.replacers;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.common.platform.IPlatformIngredientHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.library.plugins.vanilla.crafting.FakeShapedRecipeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public final class TippedArrowRecipeMaker {

	public static List<RecipeHolder<CraftingRecipe>> createRecipes(IStackHelper stackHelper) {
		String group = "jei.tipped.arrow";
		ItemStack arrowStack = new ItemStack(Items.ARROW);
		Ingredient arrowIngredient = Ingredient.of(arrowStack);

		Registry<Potion> potionRegistry = RegistryUtil.getRegistry(Registries.POTION);
		IPlatformIngredientHelper ingredientHelper = Services.PLATFORM.getIngredientHelper();
		return potionRegistry.holders()
			.map(potion -> {
				ItemStack input = PotionContents.createItemStack(Items.LINGERING_POTION, potion);
				ItemStack output = PotionContents.createItemStack(Items.TIPPED_ARROW, potion);
				output.setCount(8);

				Ingredient potionIngredient = ingredientHelper.createNbtIngredient(input, stackHelper);
				ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ModIds.MINECRAFT_ID, "jei.tipped.arrow." + output.getDescriptionId());
				CraftingRecipe recipe = new FakeShapedRecipeBuilder(CraftingBookCategory.MISC, output)
					.group(group)
					.define('a', arrowIngredient)
					.define('p', potionIngredient)
					.pattern("aaa")
					.pattern("apa")
					.pattern("aaa")
					.build();
				return new RecipeHolder<>(id, recipe);
			})
			.toList();
	}

	private TippedArrowRecipeMaker() {

	}
}
