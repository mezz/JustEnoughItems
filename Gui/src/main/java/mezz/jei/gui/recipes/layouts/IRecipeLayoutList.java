package mezz.jei.gui.recipes.layouts;

import mezz.jei.gui.recipes.IRecipeLayoutWithButtons;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface IRecipeLayoutList {
	static IRecipeLayoutList create(
		List<? extends IRecipeLayoutWithButtons<?>> unsortedList
	) {
		return new UnsortedRecipeLayoutList(unsortedList);
	}

	int size();

	List<IRecipeLayoutWithButtons<?>> subList(int from, int to, @Nullable AbstractContainerMenu container);

	Optional<IRecipeLayoutWithButtons<?>> findFirst(@Nullable AbstractContainerMenu container);

	void tick(@Nullable AbstractContainerMenu container);
}
