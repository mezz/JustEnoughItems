package mezz.jei.neoforge.tests.lib;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Function;

public final class MenuChecker<M extends AbstractContainerMenu> {
	private final JeiGameTestHelper helper;
	private final M menu;
	private final IdentityHashMap<Slot, Integer> menuSlotIndexes;
	private final IdentityHashMap<Slot, String> assertedSlots = new IdentityHashMap<>();

	MenuChecker(JeiGameTestHelper helper, M menu) {
		this.helper = helper;
		this.menu = menu;
		this.menuSlotIndexes = new IdentityHashMap<>();
		for (int i = 0; i < menu.slots.size(); i++) {
			menuSlotIndexes.put(menu.slots.get(i), i);
		}
	}

	public MenuChecker<M> assertResults(Function<M, List<Slot>> slots, List<StackPlacement> placements) {
		assertSlotGroup("results", slots, placements);
		return this;
	}

	public MenuChecker<M> assertCraftingArea(Function<M, List<Slot>> slots, List<StackPlacement> placements) {
		assertSlotGroup("crafting area", slots, placements);
		return this;
	}

	public MenuChecker<M> assertPlayerInventory(Function<M, List<Slot>> slots, List<StackPlacement> placements) {
		assertSlotGroup("player inventory", slots, placements);
		return this;
	}

	public MenuChecker<M> assertPlayerInventory(List<StackPlacement> placements) {
		assertSlotGroup("player inventory", helper::getStandardInventorySlots, placements);
		return this;
	}

	public void assertAllSlotsChecked() {
		for (Slot slot : menu.slots) {
			if (!assertedSlots.containsKey(slot)) {
				throw helper.createFailException("Expected slot %s to be asserted".formatted(menuSlotIndexes.get(slot)));
			}
		}
	}

	private void assertSlotGroup(String name, Function<M, List<Slot>> slots, List<StackPlacement> placements) {
		List<Slot> resolvedSlots = List.copyOf(slots.apply(menu));
		for (Slot slot : resolvedSlots) {
			Integer menuSlotIndex = menuSlotIndexes.get(slot);
			if (menuSlotIndex == null) {
				throw helper.createFailException("Expected %s slot %s to belong to the menu".formatted(name, slot.index));
			}
			String previousGroup = assertedSlots.get(slot);
			if (previousGroup != null) {
				throw helper.createFailException("Expected slot %s to be asserted once, but it was in both %s and %s".formatted(
					menuSlotIndex,
					previousGroup,
					name
				));
			}
		}

		helper.assertSlots(resolvedSlots, JeiGameTestHelper.getExpectedStacks(resolvedSlots.size(), placements));
		for (Slot slot : resolvedSlots) {
			assertedSlots.put(slot, name);
		}
	}
}
