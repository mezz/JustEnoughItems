package mezz.jei.library.plugins.jei;

import mezz.jei.api.IAsyncModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.runtime.IJeiClientExecutor;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.library.plugins.jei.info.IngredientInfoRecipeCategory;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

@JeiPlugin
public class JeiInternalPlugin implements IAsyncModPlugin {
	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(ModIds.JEI_ID, "internal");
	}

	@Override
	public CompletableFuture<Void> registerCategories(IRecipeCategoryRegistration registration, IJeiClientExecutor clientExecutor) {
		IJeiHelpers jeiHelpers = registration.getJeiHelpers();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		Textures textures = Internal.getTextures();

		registration.addRecipeCategories(
			new IngredientInfoRecipeCategory(guiHelper, textures)
		);
		return CompletableFuture.completedFuture(null);
	}
}
