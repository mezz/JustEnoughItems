package mezz.jei.library.plugins.vanilla.cooking;

import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.library.transfer.BasicRecipeTransferInfo;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;
import java.util.function.Function;

public class CookingRecipeTransferInfo<C extends AbstractFurnaceMenu, R> extends BasicRecipeTransferInfo<C, R> {
	private final Function<R, SlotDisplay> fuelGetter;

	public CookingRecipeTransferInfo(Class<? extends C> containerClass, MenuType<C> menuType, IRecipeType<R> recipeType, Function<R, SlotDisplay> fuelGetter) {
		super(containerClass, menuType, recipeType, 0, 1, 3, 36);
		this.fuelGetter = fuelGetter;
	}

	@Override
	public List<Slot> getRecipeSlots(C container, R recipe) {
		if (CookingCategoryExtension.isSpecificFuel(fuelGetter.apply(recipe))) {
			return List.of(container.getSlot(0), container.getSlot(1));
		}
		return List.of(container.getSlot(0));
	}
}
