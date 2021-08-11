package mezz.jei.transfer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;

public class BasicRecipeTransferInfo<C extends AbstractContainerMenu, R> implements IRecipeTransferInfo<C, R> {
	private final Class<C> containerClass;
	private final Class<R> recipeClass;
	private final ResourceLocation recipeCategoryUid;
	private final int recipeSlotStart;
	private final int recipeSlotCount;
	private final int inventorySlotStart;
	private final int inventorySlotCount;

	public BasicRecipeTransferInfo(Class<C> containerClass, Class<R> recipeClass, ResourceLocation recipeCategoryUid, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount) {
		this.containerClass = containerClass;
		this.recipeClass = recipeClass;
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
	public Class<R> getRecipeClass() {
		return recipeClass;
	}

	@Override
	public ResourceLocation getRecipeCategoryUid() {
		return recipeCategoryUid;
	}

	@Override
	public boolean canHandle(C container, R recipe) {
		return true;
	}

	@Override
	public List<Slot> getRecipeSlots(C container, R recipe) {
		List<Slot> slots = new ArrayList<>();
		for (int i = recipeSlotStart; i < recipeSlotStart + recipeSlotCount; i++) {
			Slot slot = container.getSlot(i);
			slots.add(slot);
		}
		return slots;
	}

	@Override
	public List<Slot> getInventorySlots(C container, R recipe) {
		List<Slot> slots = new ArrayList<>();
		for (int i = inventorySlotStart; i < inventorySlotStart + inventorySlotCount; i++) {
			Slot slot = container.getSlot(i);
			slots.add(slot);
		}
		return slots;
	}
}
