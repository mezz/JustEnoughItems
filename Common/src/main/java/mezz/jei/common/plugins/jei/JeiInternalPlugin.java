package mezz.jei.common.plugins.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.GuiProperties;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.gui.recipes.RecipesGui;
import mezz.jei.common.plugins.jei.info.IngredientInfoRecipeCategory;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class JeiInternalPlugin implements IModPlugin {
	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(ModIds.JEI_ID, "internal");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IJeiHelpers jeiHelpers = registration.getJeiHelpers();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		Textures textures = Internal.getTextures();

		registration.addRecipeCategories(
			new IngredientInfoRecipeCategory(guiHelper, textures)
		);
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGuiScreenHandler(AbstractContainerScreen.class, GuiProperties::create);
		registration.addGuiScreenHandler(RecipesGui.class, RecipesGui::getProperties);
	}
}
