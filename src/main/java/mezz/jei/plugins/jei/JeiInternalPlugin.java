package mezz.jei.plugins.jei;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.util.ResourceLocation;

import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.gui.overlay.GuiProperties;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.gui.textures.Textures;
import mezz.jei.plugins.jei.info.IngredientInfoRecipeCategory;

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
		registration.addGuiScreenHandler(ContainerScreen.class, GuiProperties::create);
		registration.addGuiScreenHandler(RecipesGui.class, GuiProperties::create);
	}
}
