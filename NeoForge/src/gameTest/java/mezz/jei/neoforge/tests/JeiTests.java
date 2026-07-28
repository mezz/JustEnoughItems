package mezz.jei.neoforge.tests;

import java.nio.file.Path;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.impl.MutableTestFramework;
import net.neoforged.testframework.summary.GitHubActionsStepSummaryDumper;
import net.neoforged.testframework.summary.JUnitSummaryDumper;
import mezz.jei.neoforge.tests.lib.FailedTestExceptionSummaryDumper;
import org.jspecify.annotations.Nullable;

@Mod(JeiTests.MOD_ID)
public final class JeiTests {
	public static final String MOD_ID = "jeitests";
	private static final String JUNIT_OUTPUT_DIR_PROPERTY = "jei.gameTest.junitDir";
	private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

	static {
		ITEMS.registerItem("null_default_stack", NullDefaultStackItem::new);
	}

	public JeiTests(IEventBus modEventBus, ModContainer modContainer) {
		ITEMS.register(modEventBus);
		MutableTestFramework framework = FrameworkConfiguration.builder(Identifier.fromNamespaceAndPath(MOD_ID, "tests"))
			.dumpers(
				new FailedTestExceptionSummaryDumper(),
				new JUnitSummaryDumper(Path.of(
					System.getProperty(JUNIT_OUTPUT_DIR_PROPERTY, "../../build/test-results/gameTest")
				)),
				new GitHubActionsStepSummaryDumper()
			)
			.build()
			.create();
		framework.init(modEventBus, modContainer);
	}

	/**
	 * Regression fixture for broken mod items that violate the Minecraft API contract.
	 * JEI should not call this when generating built-in recipes from the item registry.
	 *
	 * @see <a href="https://github.com/mezz/JustEnoughItems/issues/4395">Issue #4395</a>
	 */
	private static final class NullDefaultStackItem extends Item {
		private NullDefaultStackItem(Properties properties) {
			super(properties);
		}

		@Override
		public @Nullable ItemStack getDefaultInstance() {
			return null;
		}
	}
}
