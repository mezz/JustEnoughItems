package mezz.jei.api;

import javax.annotation.Nonnull;

public abstract class BlankModPlugin implements IModPlugin {
	@Override
	public void register(@Nonnull IModRegistry registry) {

	}

	@Override
	public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {

	}
}
