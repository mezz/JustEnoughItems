package mezz.jei.neoforge.tests;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.impl.MutableTestFramework;

@Mod(JeiTests.MOD_ID)
public final class JeiTests {
	public static final String MOD_ID = "jeitests";

	public JeiTests(IEventBus modEventBus, ModContainer modContainer) {
		MutableTestFramework framework = FrameworkConfiguration.builder(Identifier.fromNamespaceAndPath(MOD_ID, "tests"))
			.build()
			.create();
		framework.init(modEventBus, modContainer);
	}
}
