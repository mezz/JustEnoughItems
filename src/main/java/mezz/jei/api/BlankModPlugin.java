package mezz.jei.api;

/**
 * An {@link IModPlugin} that does nothing, inherit from this to avoid implementing methods you don't need.
 * IModPlugin implementations must have the {@link JEIPlugin} annotation to get loaded by JEI.
 */
public abstract class BlankModPlugin implements IModPlugin {
	@Override
	public void register(IModRegistry registry) {

	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {

	}
}
