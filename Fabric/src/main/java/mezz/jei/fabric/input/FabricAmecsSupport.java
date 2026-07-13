package mezz.jei.fabric.input;

import net.fabricmc.loader.api.FabricLoader;

public final class FabricAmecsSupport {
	public static final String AMECS_MOD_ID = "amecsapi";
	public static final String DISABLE_AMECS_SUPPORT_PROPERTY = "jei.fabric.disableAmecsSupport";
	private static final boolean ENABLED = FabricLoader.getInstance().isModLoaded(AMECS_MOD_ID) &&
		!Boolean.getBoolean(DISABLE_AMECS_SUPPORT_PROPERTY);

	private FabricAmecsSupport() {}

	public static boolean isEnabled() {
		return ENABLED;
	}
}
