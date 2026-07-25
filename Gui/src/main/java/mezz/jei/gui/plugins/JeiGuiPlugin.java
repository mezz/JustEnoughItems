package mezz.jei.gui.plugins;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IJeiFeatures;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

@JeiPlugin
public class JeiGuiPlugin implements IModPlugin {
	private @Nullable IJeiFeatures jeiFeatures;

	@Override
	public Identifier getPluginUid() {
		return Identifier.fromNamespaceAndPath(ModIds.JEI_ID, "gui");
	}

	@Override
	public void configureJei(IJeiFeatures jeiFeatures) {
		this.jeiFeatures = jeiFeatures;
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		if (!isJeiGuiEnabled()) {
			return;
		}

		IIngredientManager ingredientManager = registration.getJeiHelpers().getIngredientManager();
		registration.addGenericGuiScreenHandler(AbstractContainerScreen.class, new AbstractContainerScreenHandler<>());
		registration.addGuiScreenHandler(ChatScreen.class, new ChatScreenHandler(ingredientManager));
		registration.addGuiScreenHandler(RecipesGui.class, RecipesGui::getProperties);
	}

	private boolean isJeiGuiEnabled() {
		IJeiFeatures jeiFeatures = this.jeiFeatures;
		return jeiFeatures == null || jeiFeatures.isJeiGuiEnabled();
	}
}
