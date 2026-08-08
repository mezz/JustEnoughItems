package mezz.jei.neoforge.tests.client;

import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;

/**
 * Verifies that NeoForge development runs include JEI's shared client resources.
 */
public final class JeiNeoForgeClientResourceTests {
	private static final String JUNIT_SUITE_NAME = "neoforge-client-resources";
	private static final Identifier GUI_BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath("jei", "textures/jei/atlas/gui/gui_background_v2.png");
	private static final String FOCUS_SEARCH_TRANSLATION_KEY = "key.jei.focusSearch";

	private JeiNeoForgeClientResourceTests() {

	}

	public static void run() {
		JUnitXmlTestReporter.runAndReport(
			JUNIT_SUITE_NAME,
			JeiNeoForgeClientResourceTests.class.getSimpleName(),
			() -> ClientTestUtil.runOnClient(JeiNeoForgeClientResourceTests::assertJeiResourcesAreLoaded)
		);
	}

	private static void assertJeiResourcesAreLoaded(Minecraft client) {
		if (client.getResourceManager().getResource(GUI_BACKGROUND_TEXTURE).isEmpty()) {
			throw new AssertionError("Expected the NeoForge development mod to include JEI's Common textures.");
		}
		if (!Language.getInstance().has(FOCUS_SEARCH_TRANSLATION_KEY)) {
			throw new AssertionError("Expected the NeoForge development mod to include JEI's Common translations.");
		}
	}
}
