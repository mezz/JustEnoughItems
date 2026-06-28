package mezz.jei.neoforge.tests.lib;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

@FunctionalInterface
public interface MenuFactory<M extends AbstractContainerMenu> {
	M create(int containerId, Inventory inventory);
}
