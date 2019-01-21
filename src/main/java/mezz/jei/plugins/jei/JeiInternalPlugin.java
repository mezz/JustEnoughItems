package mezz.jei.plugins.jei;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ModIds;
import mezz.jei.api.recipe.category.IRecipeCategoryRegistration;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.overlay.GuiProperties;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.plugins.jei.info.IngredientInfoRecipeCategory;
import mezz.jei.runtime.JeiHelpers;

@JeiPlugin
public class JeiInternalPlugin implements IModPlugin {
	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(ModIds.JEI_ID, "internal");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		JeiHelpers jeiHelpers = Internal.getHelpers();
		GuiHelper guiHelper = jeiHelpers.getGuiHelper();

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
