package mezz.jei.transfer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;

public class BasicRecipeTransferInfo<C extends Container> implements IRecipeTransferInfo<C> {
	private final Class<C> containerClass;
	private final ResourceLocation recipeCategoryUid;
	private final int recipeSlotStart;
	private final int recipeSlotCount;
	private final int inventorySlotStart;
	private final int inventorySlotCount;

	public BasicRecipeTransferInfo(Class<C> containerClass, ResourceLocation recipeCategoryUid, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount) {
		this.containerClass = containerClass;
		this.recipeCategoryUid = recipeCategoryUid;
		this.recipeSlotStart = recipeSlotStart;
		this.recipeSlotCount = recipeSlotCount;
		this.inventorySlotStart = inventorySlotStart;
		this.inventorySlotCount = inventorySlotCount;
	}

	@Override
	public Class<C> getContainerClass() {
		return containerClass;
	}

	@Override
	public ResourceLocation getRecipeCategoryUid() {
		return recipeCategoryUid;
	}

	@Override
	public boolean canHandle(C container) {
		return true;
	}

	@Override
	public List<Slot> getRecipeSlots(C container) {
		List<Slot> slots = new ArrayList<>();
		for (int i = recipeSlotStart; i < recipeSlotStart + recipeSlotCount; i++) {
			Slot slot = container.getSlot(i);
			slots.add(slot);
		}
		return slots;
	}

	@Override
	public List<Slot> getInventorySlots(C container) {
		List<Slot> slots = new ArrayList<>();
		for (int i = inventorySlotStart; i < inventorySlotStart + inventorySlotCount; i++) {
			Slot slot = container.getSlot(i);
			slots.add(slot);
		}
		return slots;
	}
}
