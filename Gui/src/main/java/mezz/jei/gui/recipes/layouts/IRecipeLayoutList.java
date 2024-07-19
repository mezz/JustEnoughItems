package mezz.jei.gui.recipes.layouts;

import mezz.jei.common.config.RecipeSorterStage;
import mezz.jei.gui.recipes.IRecipeLayoutWithButtons;
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
		List<? extends IRecipeLayoutWithButtons<?>> unsortedList
	) {
		if (recipeSorterStages.isEmpty()) {
			return new UnsortedRecipeLayoutList(unsortedList);
		}
		return new LazySortedRecipeLayoutList(recipeSorterStages, player, unsortedList);
	}

	int size();

	List<IRecipeLayoutWithButtons<?>> subList(int from, int to, @Nullable AbstractContainerMenu container);

	Optional<IRecipeLayoutWithButtons<?>> findFirst(@Nullable AbstractContainerMenu container);

	void tick(@Nullable AbstractContainerMenu container);
}
