package mezz.jei.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class EmptyContainer extends Container {

	public EmptyContainer() {

	}

	public EmptyContainer(EntityPlayer player) {
		InventoryPlayer playerInventory = player.inventory;

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, -10000, -10000));
			}
		}

		for (int k = 0; k < 9; ++k) {
			this.addSlotToContainer(new Slot(playerInventory, k, -10000, -10000));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return false;
	}
}
