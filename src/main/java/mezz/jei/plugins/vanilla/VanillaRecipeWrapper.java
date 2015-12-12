package mezz.jei.plugins.vanilla;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.recipe.BlankRecipeWrapper;

public abstract class VanillaRecipeWrapper extends BlankRecipeWrapper {
	@Nonnull
	protected final IDrawableStatic flameDrawable;

	public VanillaRecipeWrapper() {
		IGuiHelper guiHelper = JEIManager.guiHelper;
		ResourceLocation furnaceBackgroundLocation = new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

		flameDrawable = guiHelper.createDrawable(furnaceBackgroundLocation, 176, 0, 14, 14);
	}
}
