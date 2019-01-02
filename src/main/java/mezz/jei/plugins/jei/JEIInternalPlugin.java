package mezz.jei.plugins.jei;

import javax.annotation.Nullable;

import net.minecraft.client.gui.inventory.GuiContainer;

import mezz.jei.Internal;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.gui.overlay.GuiProperties;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.plugins.jei.info.IngredientInfoRecipeCategory;

@JEIPlugin
public class JEIInternalPlugin implements IModPlugin {
	@Nullable
	public static IIngredientRegistry ingredientRegistry;
	@Nullable
	public static IJeiRuntime jeiRuntime;

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(new IngredientInfoRecipeCategory(Internal.getHelpers().getGuiHelper()));
	}

	@Override
	public void register(IModRegistry registry) {
		ingredientRegistry = registry.getIngredientRegistry();
		registry.addGuiScreenHandler(GuiContainer.class, GuiProperties::create);
		registry.addGuiScreenHandler(RecipesGui.class, GuiProperties::create);
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		jeiRuntime = runtime;
	}
}
