package mezz.jei.transfer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import mezz.jei.api.recipe.IRecipeTransferHelper;

public class BasicRecipeTransferHelper implements IRecipeTransferHelper {
	private final Class<? extends Container> containerClass;
	private final String recipeCategoryUid;
	private final int recipeSlotStart;
	private final int recipeSlotCount;
	private final int inventorySlotStart;
	private final int inventorySlotCount;

	public BasicRecipeTransferHelper(Class<? extends Container> containerClass, String recipeCategoryUid, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount) {
		this.containerClass = containerClass;
		this.recipeCategoryUid = recipeCategoryUid;
		this.recipeSlotStart = recipeSlotStart;
		this.recipeSlotCount = recipeSlotCount;
		this.inventorySlotStart = inventorySlotStart;
		this.inventorySlotCount = inventorySlotCount;
	}

	@Override
	public Class<? extends Container> getContainerClass() {
		return containerClass;
	}

	@Override
	public String getRecipeCategoryUid() {
		return recipeCategoryUid;
	}

	@Override
	public List<Slot> getRecipeSlots(Container container) {
		List<Slot> slots = new ArrayList<>();
		for (int i = recipeSlotStart; i < recipeSlotStart + recipeSlotCount; i++) {
			Slot slot = container.getSlot(i);
			slots.add(slot);
		}
		return slots;
	}

	@Override
	public List<Slot> getInventorySlots(Container container) {
		List<Slot> slots = new ArrayList<>();
		for (int i = inventorySlotStart; i < inventorySlotStart + inventorySlotCount; i++) {
			Slot slot = container.getSlot(i);
			slots.add(slot);
		}
		return slots;
	}
}
