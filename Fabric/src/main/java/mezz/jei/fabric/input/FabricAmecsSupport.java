package mezz.jei.fabric.input;

import net.fabricmc.loader.api.FabricLoader;

public final class FabricAmecsSupport {
	public static final String AMECS_KEY_MODIFIERS_MOD_ID = "amecs_key_modifiers";
	public static final String DISABLE_AMECS_SUPPORT_PROPERTY = "jei.fabric.disableAmecsSupport";
	private static final boolean ENABLED = FabricLoader.getInstance().isModLoaded(AMECS_KEY_MODIFIERS_MOD_ID) &&
		!Boolean.getBoolean(DISABLE_AMECS_SUPPORT_PROPERTY);

	private FabricAmecsSupport() {}

	public static boolean isEnabled() {
		return ENABLED;
	}
}
