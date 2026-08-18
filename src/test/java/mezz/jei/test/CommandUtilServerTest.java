package mezz.jei.test;

import mezz.jei.util.CommandUtilServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandUtilServerTest {
	@BeforeClass
	public static void bootstrapMinecraft() {
		if (!Bootstrap.isRegistered()) {
			Bootstrap.register();
		}
	}

	@Test
	public void mousePickupDoesNotSynchronizeAModdedMenuForLocalPlayer() throws ReflectiveOperationException {
		EntityPlayer player = mock(EntityPlayer.class);
		InventoryPlayer inventory = mock(InventoryPlayer.class);
		AtomicReference<ItemStack> carriedStack = new AtomicReference<>(ItemStack.EMPTY);
		when(inventory.getItemStack()).thenAnswer(ignored -> carriedStack.get());
		doAnswer(invocation -> {
			carriedStack.set(invocation.getArgument(0));
			return null;
		}).when(inventory).setItemStack(any(ItemStack.class));

		ModdedSlotContainer menu = new ModdedSlotContainer(8);
		setField(player, "inventory", inventory);
		setField(player, "openContainer", menu);

		CommandUtilServer.mousePickupItemStack(player, new ItemStack(Items.APPLE, 3));

		ItemStack actualCarried = carriedStack.get();
		assertTrue(ItemStack.areItemsEqual(new ItemStack(Items.APPLE), actualCarried));
		assertEquals(3, actualCarried.getCount());
		assertEquals(8, menu.inventorySlots.size());
		assertEquals(0, menu.detectAndSendChangesCount);
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
		private int detectAndSendChangesCount;

		private ModdedSlotContainer(int slotCount) {
			InventoryBasic moddedInventory = new InventoryBasic("modded", false, slotCount);
			for (int i = 0; i < slotCount; i++) {
				addSlotToContainer(new Slot(moddedInventory, i, 0, 0));
			}
		}

		@Override
		public void detectAndSendChanges() {
			detectAndSendChangesCount++;
		}

		@Override
		public boolean canInteractWith(EntityPlayer player) {
			return true;
		}
	}
}
