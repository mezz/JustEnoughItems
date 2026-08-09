package mezz.jei.forge.plugins.forge;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.category.extensions.vanilla.brewing.IExtendableBrewingRecipeCategory;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.forge.platform.BrewingRecipeCategoryExtension;
import mezz.jei.forge.platform.BrewingRecipeMaker;
import mezz.jei.forge.platform.VanillaBrewingRecipeCategoryExtension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.VanillaBrewingRecipe;
import org.jetbrains.annotations.Nullable;

@JeiPlugin
public class ForgeBrewingPlugin implements IModPlugin {
	private @Nullable IExtendableBrewingRecipeCategory brewingCategory;

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(ModIds.JEI_ID, "forge_brewing");
	}

	@Override
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
		IExtendableBrewingRecipeCategory brewingCategory = registration.getBrewingCategory();
		registerCategoryExtensions(
			brewingCategory,
			registration.getJeiHelpers().getIngredientManager()
		);
		this.brewingCategory = brewingCategory;
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		IExtendableBrewingRecipeCategory brewingCategory = this.brewingCategory;
		ErrorUtil.checkNotNull(brewingCategory, "brewingCategory");
		registration.addRecipes(
			RecipeTypes.BREWING,
			BrewingRecipeMaker.getBrewingRecipes(
				registration.getVanillaRecipeFactory(),
				brewingCategory
			)
		);
	}

	public static void registerCategoryExtensions(
		IExtendableBrewingRecipeCategory brewingCategory,
		IIngredientManager ingredientManager
	) {
		IIngredientHelper<ItemStack> itemStackHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);
		brewingCategory.addExtension(
			VanillaBrewingRecipe.class,
			new VanillaBrewingRecipeCategoryExtension(ingredientManager)
		);
		brewingCategory.addExtension(BrewingRecipe.class, new BrewingRecipeCategoryExtension(itemStackHelper));
	}
}
