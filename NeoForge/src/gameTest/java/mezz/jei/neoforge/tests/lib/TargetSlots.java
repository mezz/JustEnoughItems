package mezz.jei.neoforge.tests.lib;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.List;

@FunctionalInterface
public interface TargetSlots<M extends AbstractContainerMenu> {
	List<Slot> get(M menu, Player player);
}
