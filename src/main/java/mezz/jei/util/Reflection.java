package mezz.jei.util;

import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityFurnace;

import java.lang.reflect.Field;

public class Reflection {
	private static Object getDeclaredField(Object obj, String name) {
		try {
			Field f = obj.getClass().getDeclaredField(name);
			if (f.isAccessible())
				throw new IllegalArgumentException("Attempting reflection on an accessible field: " + name + " on " + obj);

			f.setAccessible(true);
			Object fieldValue = f.get(obj);
			f.setAccessible(false);
			return fieldValue;
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException("Failed reflection on a field: " + name + " on " + obj, e);
		}
	}

	public static IInventory getUpperChestInventory(GuiChest chest) {
		return (IInventory)getDeclaredField(chest, "upperChestInventory");
	}

	public static IInventory getLowerChestInventory(GuiChest chest) {
		return (IInventory)getDeclaredField(chest, "lowerChestInventory");
	}

	public static EntityPlayer getPlayer(ContainerPlayer container) {
		return (EntityPlayer)getDeclaredField(container, "thePlayer");
	}

	public static Integer[] getPosition(ContainerWorkbench workbench) {
		return new Integer[] {
				(Integer)getDeclaredField(workbench, "posX"),
				(Integer)getDeclaredField(workbench, "posY"),
				(Integer)getDeclaredField(workbench, "posZ")
		};
	}

	public static TileEntityFurnace getTileEntityFurnace(GuiFurnace furnace) {
		return (TileEntityFurnace)getDeclaredField(furnace, "tileFurnace");
	}

	public static TileEntityBrewingStand getTileEntityBrewingStand(GuiBrewingStand brewingStand) {
		return (TileEntityBrewingStand)getDeclaredField(brewingStand, "tileBrewingStand");
	}

}
