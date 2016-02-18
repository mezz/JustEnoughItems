package mezz.jei.api;

import javax.annotation.Nonnull;

public abstract class BlankModPlugin implements IModPlugin {
	@Deprecated
	@Override
	public void onJeiHelpersAvailable(IJeiHelpers jeiHelpers) {

	}

	@Deprecated
	@Override
	public void onItemRegistryAvailable(IItemRegistry itemRegistry) {

	}

	@Override
	public void register(@Nonnull IModRegistry registry) {

	}

	@Deprecated
	@Override
	public void onRecipeRegistryAvailable(@Nonnull IRecipeRegistry recipeRegistry) {

	}

	@Override
	public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {

	}
}
