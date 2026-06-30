package mezz.jei.neoforge.tests;

import java.nio.file.Path;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.impl.MutableTestFramework;
import net.neoforged.testframework.summary.JUnitSummaryDumper;

@Mod(JeiTests.MOD_ID)
public final class JeiTests {
	public static final String MOD_ID = "jeitests";
	private static final String JUNIT_OUTPUT_DIR_PROPERTY = "jei.gameTest.junitDir";

	public JeiTests(IEventBus modEventBus, ModContainer modContainer) {
		MutableTestFramework framework = FrameworkConfiguration.builder(Identifier.fromNamespaceAndPath(MOD_ID, "tests"))
			.dumpers(new JUnitSummaryDumper(Path.of(
				System.getProperty(JUNIT_OUTPUT_DIR_PROPERTY, "../../build/test-results/gameTest")
			)))
			.build()
			.create();
		framework.init(modEventBus, modContainer);
	}
}
