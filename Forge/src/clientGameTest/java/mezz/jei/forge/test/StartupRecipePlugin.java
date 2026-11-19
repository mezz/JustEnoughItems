package mezz.jei.forge.test;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks tag ingredients when JEI calls a plugin's recipe registration callback.
 */
@JeiPlugin
public final class StartupRecipePlugin implements IModPlugin {
	static final ResourceLocation RECIPE_ID = new ResourceLocation("jei_client_tests", "tagged_recipe");
	private static final List<Boolean> TAG_RESOLUTION_RESULTS = new ArrayList<>();

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation("jei_client_tests", "startup");
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		Ingredient ingredient = Ingredient.of(ItemTags.PLANKS);
		// Capture resolution in the callback because a later tag packet can repair the same ingredient.
		TAG_RESOLUTION_RESULTS.add(ingredient.test(new ItemStack(Items.OAK_PLANKS)));
		ShapelessRecipe recipe = new ShapelessRecipe(
			RECIPE_ID, "", CraftingBookCategory.MISC, new ItemStack(Items.DIAMOND),
			NonNullList.of(Ingredient.EMPTY, ingredient)
		);
		registration.addRecipes(RecipeTypes.CRAFTING, List.of(recipe));
	}

	static void reset() {
		TAG_RESOLUTION_RESULTS.clear();
	}

	static int registrationCount() {
		return TAG_RESOLUTION_RESULTS.size();
	}

	static void assertTagsResolved() {
		if (TAG_RESOLUTION_RESULTS.isEmpty() || TAG_RESOLUTION_RESULTS.contains(false)) {
			throw new AssertionError("A JEI plugin registered recipes before the server's plank tag was available: " + TAG_RESOLUTION_RESULTS);
		}
	}
}
