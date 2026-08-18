package mezz.jei.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandUtilServerTest {
	@BeforeAll
	public static void bootstrapMinecraft() {
		Bootstrap.bootStrap();
	}

	@Test
	public void mousePickupDoesNotSynchronizeAModdedMenuForLocalPlayer() throws ReflectiveOperationException {
		PlayerEntity player = mock(PlayerEntity.class);
		PlayerInventory inventory = mock(PlayerInventory.class);
		AtomicReference<ItemStack> carriedStack = new AtomicReference<>(ItemStack.EMPTY);
		when(inventory.getCarried()).thenAnswer(ignored -> carriedStack.get());
		doAnswer(invocation -> {
			carriedStack.set(invocation.getArgument(0));
			return null;
		}).when(inventory).setCarried(any(ItemStack.class));

		ModdedSlotContainer menu = new ModdedSlotContainer(8);
		setField(player, "inventory", inventory);
		setField(player, "containerMenu", menu);

		CommandUtilServer.mousePickupItemStack(player, new ItemStack(Items.APPLE, 3));

		ItemStack actualCarried = carriedStack.get();
		assertTrue(ItemStack.isSame(new ItemStack(Items.APPLE), actualCarried));
		assertEquals(3, actualCarried.getCount());
		assertEquals(0, menu.broadcastChangeCount);
	}

	private static void setField(Object instance, String fieldName, Object value) throws ReflectiveOperationException {
		Class<?> type = instance.getClass();
		while (type != null) {
			try {
				Field field = type.getDeclaredField(fieldName);
				field.setAccessible(true);
				field.set(instance, value);
				return;
			} catch (NoSuchFieldException ignored) {
				type = type.getSuperclass();
			}
		}
		throw new NoSuchFieldException(fieldName);
	}

	private static class ModdedSlotContainer extends Container {
		private int broadcastChangeCount;

		private ModdedSlotContainer(int slotCount) {
			super(null, 0);
			Inventory moddedInventory = new Inventory(slotCount);
			for (int i = 0; i < slotCount; i++) {
				addSlot(new Slot(moddedInventory, i, 0, 0));
			}
		}

		@Override
		public void broadcastChanges() {
			broadcastChangeCount++;
		}

		@Override
		public ItemStack quickMoveStack(PlayerEntity player, int slotIndex) {
			return ItemStack.EMPTY;
		}

		@Override
		public boolean stillValid(PlayerEntity player) {
			return true;
		}
	}
}
