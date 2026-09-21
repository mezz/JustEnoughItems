package mezz.jei.neoforge.tests;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
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
	private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);
	private static final int BACKPACK_SLOT_COUNT = 9;
	private static final Map<UUID, ItemStacksResourceHandler> BACKPACK_STORAGE = new ConcurrentHashMap<>();
	private static final ItemStacksResourceHandler EMPTY_BACKPACK_HANDLER = new ItemStacksResourceHandler(0);
	private static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> BACKPACK_STORAGE_ID = DATA_COMPONENTS.registerComponentType(
		"backpack_storage_id",
		builder -> builder
			.persistent(UUIDUtil.CODEC)
			.networkSynchronized(UUIDUtil.STREAM_CODEC)
	);
	public static final DeferredItem<Item> BACKPACK = ITEMS.registerSimpleItem(
		"backpack",
		properties -> properties.stacksTo(1)
	);

	static {
		ITEMS.registerItem("null_default_stack", NullDefaultStackItem::new);
	}

	public JeiTests(IEventBus modEventBus, ModContainer modContainer) {
		DATA_COMPONENTS.register(modEventBus);
		ITEMS.register(modEventBus);
		modEventBus.addListener(false, RegisterCapabilitiesEvent.class, JeiTests::registerCapabilities);
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

	public static ItemStack createBackpack(ItemStack... contents) {
		UUID storageId = UUID.randomUUID();
		ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(BACKPACK_SLOT_COUNT);
		for (int i = 0; i < Math.min(contents.length, BACKPACK_SLOT_COUNT); i++) {
			ItemStack content = contents[i];
			itemHandler.set(i, ItemResource.of(content), content.getCount());
		}
		BACKPACK_STORAGE.put(storageId, itemHandler);
		ItemStack backpack = new ItemStack(BACKPACK.get());
		backpack.set(BACKPACK_STORAGE_ID.get(), storageId);
		return backpack;
	}

	public static List<ItemStack> getBackpackContents(ItemStack backpack) {
		UUID storageId = backpack.get(BACKPACK_STORAGE_ID.get());
		ItemStacksResourceHandler itemHandler = null;
		if (storageId != null) {
			itemHandler = BACKPACK_STORAGE.get(storageId);
		}
		if (itemHandler == null) {
			return List.of();
		}
		return List.copyOf(itemHandler.copyToList());
	}

	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerItem(
			Capabilities.Item.ITEM,
			(stack, itemAccess) -> {
				UUID storageId = stack.get(BACKPACK_STORAGE_ID.get());
				if (storageId == null) {
					return EMPTY_BACKPACK_HANDLER;
				}
				return BACKPACK_STORAGE.getOrDefault(storageId, EMPTY_BACKPACK_HANDLER);
			},
			BACKPACK.get()
		);
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
