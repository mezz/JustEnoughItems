package mezz.jei.plugins.jei;

import javax.annotation.Nullable;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ModIds;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.gui.overlay.GuiProperties;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.plugins.jei.info.IngredientInfoRecipeCategory;

@JEIPlugin
public class JeiInternalPlugin implements IModPlugin {
	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(ModIds.JEI_ID, "internal");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		IJeiHelpers jeiHelpers = registry.getJeiHelpers();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

		registry.addRecipeCategories(
			new IngredientInfoRecipeCategory(guiHelper)
		);
	}

	@Override
	public void register(IModRegistry registry) {
		registry.addGuiScreenHandler(GuiContainer.class, GuiProperties::create);
		registry.addGuiScreenHandler(RecipesGui.class, GuiProperties::create);
	}
}
