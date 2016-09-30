package mezz.jei.api;

import mezz.jei.api.ingredients.IModIngredientRegistration;

/**
 * An {@link IModPlugin} that does nothing, inherit from this to avoid implementing methods you don't need.
 * IModPlugin implementations must have the {@link JEIPlugin} annotation to get loaded by JEI.
 */
public abstract class BlankModPlugin implements IModPlugin {
	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {

	}

	@Override
	public void registerIngredients(IModIngredientRegistration ingredientRegistry) {

	}

	@Override
	public void register(IModRegistry registry) {

	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {

	}
}
