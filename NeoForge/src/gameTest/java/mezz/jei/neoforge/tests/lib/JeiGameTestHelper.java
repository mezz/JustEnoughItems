package mezz.jei.neoforge.tests.lib;

import mezz.jei.common.network.packets.PlayToServerPacket;
import net.minecraft.gametest.framework.GameTestException;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class JeiGameTestHelper extends ExtendedGameTestHelper {
	@Nullable
	private ServerPlayer player;

	public JeiGameTestHelper(GameTestInfo info) {
		super(info);
	}

	@SuppressWarnings("removal")
	public ServerPlayer getPlayer() {
		if (player == null) {
			player = makeMockServerPlayerInLevel();
		}
		return player;
	}

	public GameTestException createFailException(String message) {
		return assertionException(Component.literal(message));
	}

	public void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw createFailException(message);
		}
	}

	public void assertEquals(Object expected, Object actual, String message) {
		if (!Objects.equals(expected, actual)) {
			throw createFailException("%s expected <%s> but was <%s>".formatted(message, expected, actual));
		}
	}

	public void assertSameStack(ItemStack expected, ItemStack actual, String message) {
		assertTrue(isSameStack(expected, actual), "%s expected <%s> but was <%s>".formatted(message, expected, actual));
	}

	public static boolean isSameStack(ItemStack expected, ItemStack actual) {
		return expected.getCount() == actual.getCount() && ItemStack.isSameItemSameComponents(expected, actual);
	}

	public <M extends AbstractContainerMenu> MenuChecker<M> createMenuChecker(M menu) {
		return new MenuChecker<>(this, menu);
	}

	public <M extends AbstractContainerMenu> M openMenu(MenuFactory<M> menuFactory) {
		ServerPlayer player = getPlayer();
		M menu = menuFactory.create(0, player.getInventory());
		player.containerMenu = menu;
		return menu;
	}

	public <M extends AbstractContainerMenu> M openMenu(MenuFactory<M> menuFactory, ItemStack... inventoryStacks) {
		M menu = openMenu(menuFactory);
		List<Slot> inventorySlots = getStandardInventorySlots(menu);
		for (int i = 0; i < inventoryStacks.length; i++) {
			Slot inventorySlot = inventorySlots.get(i);
			ItemStack stack = inventoryStacks[i].copy();
			inventorySlot.set(stack);
		}
		return menu;
	}

	public TestConnectionToServer createServerConnection() {
		return new TestConnectionToServer();
	}

	public TestConnectionToServer createServerConnection(CustomPacketPayload.Type<?> unsupportedPacket) {
		TestConnectionToServer serverConnection = createServerConnection();
		serverConnection.addUnsupportedPacket(unsupportedPacket);
		return serverConnection;
	}

	public <R> R runWithServerConnection(TestConnectionToServer serverConnection, Function<TestConnectionToServer, R> action) {
		serverConnection.setPlayer(getPlayer());
		try {
			return action.apply(serverConnection);
		} finally {
			serverConnection.clearPlayer();
		}
	}

	public <T extends PlayToServerPacket<T>> void sendPacketToServer(T packet) {
		runWithServerConnection(createServerConnection(), connection -> {
			connection.sendPacketToServer(packet);
			return null;
		});
	}

	public List<Slot> getStandardInventorySlots(AbstractContainerMenu menu) {
		return getStandardInventorySlots(menu, getPlayer());
	}

	public static List<Slot> getStandardInventorySlots(AbstractContainerMenu menu, Player player) {
		return menu.slots.stream()
			.filter(slot -> slot.container == player.getInventory())
			.filter(slot -> slot.getContainerSlot() < Inventory.INVENTORY_SIZE)
			.toList();
	}

	public List<Slot> getCraftingGridSlots(AbstractCraftingMenu menu, Player player) {
		return menu.getInputGridSlots();
	}

	public List<Slot> getCrafterSlots(CrafterMenu menu, Player player) {
		return menu.slots.stream()
			.filter(slot -> slot.container == menu.getContainer())
			.toList();
	}

	public List<Slot> getFurnaceIngredientSlots(AbstractFurnaceMenu menu, Player player) {
		return List.of(menu.getSlot(AbstractFurnaceMenu.INGREDIENT_SLOT));
	}

	public List<Slot> getFurnaceFuelSlots(AbstractFurnaceMenu menu, Player player) {
		return List.of(menu.getSlot(AbstractFurnaceMenu.FUEL_SLOT));
	}

	public List<Slot> getBrewingRecipeSlots(BrewingStandMenu menu, Player player) {
		return menu.slots.stream()
			.filter(slot -> slot.container != player.getInventory())
			.limit(4)
			.toList();
	}

	public List<Slot> getAnvilSlots(AnvilMenu menu, Player player) {
		return List.of(
			menu.getSlot(AnvilMenu.INPUT_SLOT),
			menu.getSlot(AnvilMenu.ADDITIONAL_SLOT)
		);
	}

	public List<Slot> getSmithingSlots(SmithingMenu menu, Player player) {
		return List.of(
			menu.getSlot(SmithingMenu.TEMPLATE_SLOT),
			menu.getSlot(SmithingMenu.BASE_SLOT),
			menu.getSlot(SmithingMenu.ADDITIONAL_SLOT)
		);
	}

	public void assertSlots(List<Slot> slots, List<ItemStack> expectedStacks) {
		if (expectedStacks.size() > slots.size()) {
			throw createFailException("Expected at most %s slots, got %s".formatted(slots.size(), expectedStacks.size()));
		}

		// Expected stacks are indexed by the given slot list.
		// Missing entries are expected to be empty.
		for (int i = 0; i < slots.size(); i++) {
			ItemStack expectedStack = i < expectedStacks.size() ? expectedStacks.get(i) : ItemStack.EMPTY;
			Slot slot = slots.get(i);
			if (expectedStack.isEmpty()) {
				assertEmptySlot(slot);
			} else {
				assertSlot(slot, expectedStack);
			}
		}
	}

	public void assertSlot(Slot slot, Item item, int count) {
		ItemStack stack = slot.getItem();
		if (!stack.is(item) || stack.getCount() != count) {
			throw createFailException("Expected slot %s to contain %s x%s, got %s x%s".formatted(
				slot.index,
				item.getDescriptionId(),
				count,
				stack.getItem().getDescriptionId(),
				stack.getCount()
			));
		}
	}

	public void assertSlot(Slot slot, ItemStack expectedStack) {
		ItemStack stack = slot.getItem();
		if (!ItemStack.isSameItemSameComponents(stack, expectedStack) || stack.getCount() != expectedStack.getCount()) {
			throw createFailException("Expected slot %s to contain %s, got %s".formatted(
				slot.index,
				expectedStack,
				stack
			));
		}
	}

	public void assertEmptySlot(Slot slot) {
		ItemStack stack = slot.getItem();
		if (!stack.isEmpty()) {
			throw createFailException("Expected slot %s to be empty, got %s x%s".formatted(
				slot.index,
				stack.getItem().getDescriptionId(),
				stack.getCount()
			));
		}
	}

	public ItemStack craftInCraftingTable(List<ItemStack> inputs) {
		CraftingMenu menu = createCraftingMenu();
		List<Slot> inputSlots = menu.getInputGridSlots();
		for (int i = 0; i < inputSlots.size(); i++) {
			ItemStack input = i < inputs.size() ? inputs.get(i).copy() : ItemStack.EMPTY;
			inputSlots.get(i).set(input);
		}
		menu.slotsChanged(inputSlots.getFirst().container);
		return menu.getResultSlot().getItem().copy();
	}

	public CraftingMenu createCraftingMenu() {
		ServerPlayer player = getPlayer();
		ContainerLevelAccess access = ContainerLevelAccess.create(player.level(), player.blockPosition());
		return new CraftingMenu(0, player.getInventory(), access);
	}

	public static List<ItemStack> getExpectedStacks(int size, List<StackPlacement> placements) {
		List<ItemStack> stacks = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			stacks.add(ItemStack.EMPTY);
		}

		boolean[] assignedIndexes = new boolean[size];
		for (StackPlacement placement : placements) {
			validatePlacement(placement, stacks.size(), assignedIndexes);
			stacks.set(placement.index(), placement.stack().copy());
		}
		return stacks;
	}

	public static List<ItemStack> getFilledStacks(int size, ItemStack stack, List<StackPlacement> placements) {
		List<ItemStack> stacks = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			stacks.add(stack.copy());
		}

		boolean[] assignedIndexes = new boolean[size];
		for (StackPlacement placement : placements) {
			validatePlacement(placement, stacks.size(), assignedIndexes);
			stacks.set(placement.index(), placement.stack().copy());
		}
		return stacks;
	}

	private static void validatePlacement(StackPlacement placement, int size, boolean[] assignedIndexes) {
		if (placement.index() < 0 || placement.index() >= size) {
			throw new IllegalArgumentException("Expected slot index from 0 to %s, got %s".formatted(size - 1, placement.index()));
		}
		if (assignedIndexes[placement.index()]) {
			throw new IllegalArgumentException("Duplicate expected slot %s".formatted(placement.index()));
		}
		assignedIndexes[placement.index()] = true;
	}
}
