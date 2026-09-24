package mezz.jei.library.plugins.vanilla.cooking;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;
import java.util.Optional;

public class FurnaceRecipeTransferInfo implements IRecipeTransferInfo<FurnaceMenu, Object> {
	@Override
	public Class<? extends FurnaceMenu> getContainerClass() {
		return FurnaceMenu.class;
	}

	@Override
	public Optional<MenuType<FurnaceMenu>> getMenuType() {
		return Optional.of(MenuType.FURNACE);
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public IRecipeType<Object> getRecipeType() {
		return (IRecipeType) RecipeTypes.SMELTING;
	}

	@Override
	public boolean canHandle(FurnaceMenu container, Object recipe) {
		return true;
	}

	@Override
	public List<Slot> getRecipeSlots(FurnaceMenu container, Object recipe) {
		if (hasSpecificFuel(recipe)) {
			return List.of(container.getSlot(0), container.getSlot(1));
		}
		return List.of(container.getSlot(0));
	}

	@Override
	public List<Slot> getInventorySlots(FurnaceMenu container, Object recipe) {
		return container.slots.subList(3, 39);
	}

	private static boolean hasSpecificFuel(Object recipe) {
		if (recipe instanceof RecipeHolder<?> recipeHolder && recipeHolder.value() instanceof SmeltingRecipe smeltingRecipe) {
			RecipeDisplay display = smeltingRecipe.display().getFirst();
			return display instanceof FurnaceRecipeDisplay furnaceRecipeDisplay &&
				!(furnaceRecipeDisplay.fuel() instanceof SlotDisplay.AnyFuel);
		}
		return false;
	}
}
