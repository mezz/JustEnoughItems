package mezz.jei.gui.recipes.layouts;

import mezz.jei.gui.recipes.RecipeLayoutWithButtons;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface IRecipeLayoutList {
	static IRecipeLayoutList create(
		@Nullable AbstractContainerMenu container,
		@Nullable Player player,
		List<? extends RecipeLayoutWithButtons<?>> unsortedList
	) {
		return new UnsortedRecipeLayoutList(unsortedList);
	}

	int size();

	List<RecipeLayoutWithButtons<?>> subList(int from, int to);

	Optional<RecipeLayoutWithButtons<?>> findFirst();

	void tick();
}
