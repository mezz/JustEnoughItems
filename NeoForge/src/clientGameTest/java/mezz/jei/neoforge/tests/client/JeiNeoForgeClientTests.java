package mezz.jei.neoforge.tests.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

/**
 * Client-side test mod entrypoint for NeoForge recipe-sync coverage.
 */
@Mod(value = JeiNeoForgeClientTests.MOD_ID, dist = Dist.CLIENT)
public final class JeiNeoForgeClientTests {
	public static final String MOD_ID = "jeiclienttests";

	public JeiNeoForgeClientTests() {
		JeiNeoForgeClientRecipeSyncTests.register();
	}
}
