package mezz.jei.fabric.input;

import net.fabricmc.loader.api.FabricLoader;

public final class FabricAmecsSupport {
	public static final String AMECS_MOD_ID = "amecsapi";
	private static final boolean ENABLED = FabricLoader.getInstance().isModLoaded(AMECS_MOD_ID);

	private FabricAmecsSupport() {}

	public static boolean isEnabled() {
		return ENABLED;
	}
}
