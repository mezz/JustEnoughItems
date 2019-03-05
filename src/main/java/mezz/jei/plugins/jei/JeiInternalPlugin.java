package mezz.jei.plugins.jei;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
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
	public void registerCategories(IRecipeCategoryRegistration registration, IJeiHelpers jeiHelpers) {
		JeiHelpers internalJeiHelpers = Internal.getHelpers();
		GuiHelper guiHelper = internalJeiHelpers.getGuiHelper();

		registration.addRecipeCategories(
			new IngredientInfoRecipeCategory(guiHelper)
		);
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGuiScreenHandler(GuiContainer.class, GuiProperties::create);
		registration.addGuiScreenHandler(RecipesGui.class, GuiProperties::create);
	}
}
