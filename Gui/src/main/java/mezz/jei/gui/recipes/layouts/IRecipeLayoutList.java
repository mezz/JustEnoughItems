package mezz.jei.gui.recipes.layouts;

import mezz.jei.common.config.RecipeSorterStage;
import mezz.jei.gui.recipes.RecipeLayoutWithButtons;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IRecipeLayoutList {
	static IRecipeLayoutList create(
		Set<RecipeSorterStage> recipeSorterStages,
		@Nullable AbstractContainerMenu container,
		@Nullable Player player,
		List<? extends RecipeLayoutWithButtons<?>> unsortedList
	) {
		if (recipeSorterStages.isEmpty()) {
			return new UnsortedRecipeLayoutList(unsortedList);
		} else {
			return new LazySortedRecipeLayoutList(recipeSorterStages, container, player, unsortedList);
		}
	}

	int size();

	List<RecipeLayoutWithButtons<?>> subList(int from, int to);

	Optional<RecipeLayoutWithButtons<?>> findFirst();

	void tick();
}
