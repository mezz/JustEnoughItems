package mezz.jei.transfer;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;

public class BasicRecipeTransferInfo<C extends AbstractContainerMenu, R> implements IRecipeTransferInfo<C, R> {
	private final Class<C> containerClass;
	private final RecipeType<R> recipeType;
	private final int recipeSlotStart;
	private final int recipeSlotCount;
	private final int inventorySlotStart;
	private final int inventorySlotCount;

	public BasicRecipeTransferInfo(Class<C> containerClass, RecipeType<R> recipeType, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount) {
		this.containerClass = containerClass;
		this.recipeType = recipeType;
		this.recipeSlotStart = recipeSlotStart;
		this.recipeSlotCount = recipeSlotCount;
		this.inventorySlotStart = inventorySlotStart;
		this.inventorySlotCount = inventorySlotCount;
	}

	@Override
	public Class<C> getContainerClass() {
		return containerClass;
	}

	@SuppressWarnings({"removal"})
	@Override
	public Class<R> getRecipeClass() {
		@SuppressWarnings("unchecked")
		Class<R> cast = (Class<R>) recipeType.getRecipeClass();
		return cast;
	}

	@SuppressWarnings("removal")
	@Override
	public ResourceLocation getRecipeCategoryUid() {
		return recipeType.getUid();
	}

	@Override
	public RecipeType<R> getRecipeType() {
		return recipeType;
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
