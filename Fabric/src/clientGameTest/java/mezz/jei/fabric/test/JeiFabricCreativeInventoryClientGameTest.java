package mezz.jei.fabric.test;

import mezz.jei.common.Internal;
import mezz.jei.common.config.GiveMode;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.gui.util.CommandUtil;
import mezz.jei.gui.util.GiveAmount;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Objects;

/**
 * Reproduces a creative inventory crash caused by equipment mods adding player inventory slots.
 */
@SuppressWarnings("UnstableApiUsage")
final class JeiFabricCreativeInventoryClientGameTest {
	private static final Duration ASSERTION_TIMEOUT = Duration.ofSeconds(60);

	private JeiFabricCreativeInventoryClientGameTest() {

	}

	public static void register() {
		FabricClientTestRunner.register(getTestCase());
	}

	public static FabricClientTestRunner.ClientTestCase getTestCase() {
		return new FabricClientTestRunner.ClientTestCase(
			"fabric-client-creative-inventory",
			JeiFabricCreativeInventoryClientGameTest.class.getSimpleName(),
			JeiFabricCreativeInventoryClientGameTest::runTest
		);
	}

	private static void runTest() {
		try (FabricClientTestWorld ignored = FabricClientTestWorld.create(GameType.CREATIVE)) {
			ClientTestUtil.waitUntil(
				() -> ClientTestUtil.computeOnClient(client -> Internal.getOptionalJeiRuntime().isPresent()),
				ASSERTION_TIMEOUT,
				() -> "Timed out waiting for JEI to start in the Fabric creative inventory test world."
			);
			assertLocalPlayerMousePickupWithModdedInventorySlotsWorks();
		}
	}

	private static void assertLocalPlayerMousePickupWithModdedInventorySlotsWorks() {
		CreativeInventoryTestState state = ClientTestUtil.computeOnClient(client -> {
			LocalPlayer player = Objects.requireNonNull(client.player);
			if (!player.isCreative()) {
				throw new AssertionError("Expected a creative LocalPlayer for the creative inventory mouse-pickup test");
			}
			Screen originalScreen = client.screen;
			AbstractContainerMenu originalMenu = player.containerMenu;
			ItemStack originalCarried = player.inventoryMenu.getCarried().copy();
			int originalInventorySlotCount = player.inventoryMenu.slots.size();

			CreativeModeInventoryScreen screen = new CreativeModeInventoryScreen(player);
			int trackedCreativeSlotCount = screen.getMenu().slots.size();
			int moddedSlotCount = trackedCreativeSlotCount - originalInventorySlotCount;
			if (moddedSlotCount <= 0) {
				throw new AssertionError("Expected the creative menu to start with more tracked slots than the player inventory");
			}

			addModdedInventorySlots(player, moddedSlotCount);
			return new CreativeInventoryTestState(
				screen,
				originalScreen,
				originalMenu,
				originalCarried,
				originalInventorySlotCount,
				trackedCreativeSlotCount
			);
		});

		try {
			ClientTestUtil.runOnClient(client -> client.setScreen(state.screen()));
			ClientTestUtil.waitUntil(
				() -> ClientTestUtil.computeOnClient(client -> client.screen instanceof CreativeModeInventoryScreen),
				ASSERTION_TIMEOUT,
				() -> "Timed out opening the creative inventory screen."
			);
			ClientTestUtil.runOnClient(client -> {
				LocalPlayer player = Objects.requireNonNull(client.player);
				CreativeModeInventoryScreen screen = state.screen();
				selectInventoryTab(screen);
				if (player.containerMenu != screen.getMenu()) {
					throw new AssertionError("Expected the LocalPlayer to use the creative inventory menu");
				}

				int visibleCreativeSlotCount = screen.getMenu().slots.size();
				if (visibleCreativeSlotCount != state.trackedCreativeSlotCount() + 1) {
					throw new AssertionError(
						"Expected the inventory tab to expose one more slot than the creative menu tracks, tracked: " +
							state.trackedCreativeSlotCount() + ", visible: " + visibleCreativeSlotCount
					);
				}

				IClientConfig clientConfig = Internal.getJeiClientConfigs().getClientConfig();
				if (clientConfig.getGiveMode() != GiveMode.MOUSE_PICKUP) {
					throw new AssertionError("Expected the client test configuration to use mouse-pickup give mode");
				}
				CommandUtil commandUtil = new CommandUtil(clientConfig, Internal.getServerConnection());
				assertLocalPlayerGiveResult(
					player,
					commandUtil,
					ItemStack.EMPTY,
					new ItemStack(Items.APPLE),
					GiveAmount.ONE,
					new ItemStack(Items.APPLE),
					"place an item on an empty cursor"
				);
				assertLocalPlayerGiveResult(
					player,
					commandUtil,
					new ItemStack(Items.APPLE, 60),
					new ItemStack(Items.APPLE),
					GiveAmount.ONE,
					new ItemStack(Items.APPLE, 61),
					"stack a matching item"
				);
				assertLocalPlayerGiveResult(
					player,
					commandUtil,
					new ItemStack(Items.APPLE, 63),
					new ItemStack(Items.APPLE),
					GiveAmount.MAX,
					new ItemStack(Items.APPLE, 64),
					"cap a matching stack at its maximum size"
				);
				assertLocalPlayerGiveResult(
					player,
					commandUtil,
					new ItemStack(Items.APPLE, 64),
					new ItemStack(Items.APPLE),
					GiveAmount.ONE,
					new ItemStack(Items.APPLE, 64),
					"leave a full matching stack unchanged"
				);
				assertLocalPlayerGiveResult(
					player,
					commandUtil,
					new ItemStack(Items.DIAMOND_SWORD),
					new ItemStack(Items.DIAMOND_SWORD),
					GiveAmount.ONE,
					new ItemStack(Items.DIAMOND_SWORD),
					"leave a full matching unstackable item unchanged"
				);
				assertLocalPlayerGiveResult(
					player,
					commandUtil,
					new ItemStack(Items.CARROT, 2),
					new ItemStack(Items.APPLE),
					GiveAmount.ONE,
					new ItemStack(Items.APPLE),
					"replace a different cursor item"
				);
			});
		} finally {
			ClientTestUtil.runOnClient(client -> {
				client.setScreen(state.originalScreen());
				LocalPlayer player = Objects.requireNonNull(client.player);
				player.containerMenu = state.originalMenu();
				player.inventoryMenu.setCarried(state.originalCarried());
				player.inventoryMenu.slots.subList(state.originalInventorySlotCount(), player.inventoryMenu.slots.size()).clear();
			});
		}
	}

	private static void assertLocalPlayerGiveResult(
		LocalPlayer player,
		CommandUtil commandUtil,
		ItemStack initialCarried,
		ItemStack givenStack,
		GiveAmount giveAmount,
		ItemStack expectedCarried,
		String behavior
	) {
		player.containerMenu.setCarried(initialCarried.copy());
		commandUtil.giveStack(givenStack, giveAmount);

		ItemStack actualCarried = player.containerMenu.getCarried();
		if (!ItemStack.matches(expectedCarried, actualCarried)) {
			throw new AssertionError(
				"Expected mouse pickup to " + behavior + ", expected: " + expectedCarried + ", got: " + actualCarried
			);
		}
	}

	private static void addModdedInventorySlots(Player player, int slotCount) {
		SimpleContainer moddedInventory = new SimpleContainer(slotCount);
		try {
			Method addSlot = AbstractContainerMenu.class.getDeclaredMethod("addSlot", Slot.class);
			addSlot.setAccessible(true);
			for (int i = 0; i < slotCount; i++) {
				addSlot.invoke(player.inventoryMenu, new Slot(moddedInventory, i, 0, 0));
			}
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Failed to simulate modded player inventory slots", e);
		}
	}

	private static void selectInventoryTab(CreativeModeInventoryScreen screen) {
		try {
			Method selectTab = CreativeModeInventoryScreen.class.getDeclaredMethod("selectTab", CreativeModeTab.class);
			selectTab.setAccessible(true);
			selectTab.invoke(screen, CreativeModeTab.TAB_INVENTORY);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Failed to select the creative inventory tab", e);
		}
		if (screen.getSelectedTab() != CreativeModeTab.TAB_INVENTORY.getId()) {
			throw new AssertionError("Expected the creative inventory tab to be open");
		}
	}

	private record CreativeInventoryTestState(
		CreativeModeInventoryScreen screen,
		Screen originalScreen,
		AbstractContainerMenu originalMenu,
		ItemStack originalCarried,
		int originalInventorySlotCount,
		int trackedCreativeSlotCount
	) {
	}
}
