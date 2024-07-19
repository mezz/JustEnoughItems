package mezz.jei.gui.recipes.layouts;

import mezz.jei.gui.recipes.RecipeLayoutWithButtons;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UnsortedRecipeLayoutList implements IRecipeLayoutList {
	private final List<RecipeLayoutWithButtons<?>> unsortedList;

	UnsortedRecipeLayoutList(List<? extends RecipeLayoutWithButtons<?>> unsortedList) {
		this.unsortedList = new ArrayList<>(unsortedList);
	}

	@Override
	public int size() {
		return unsortedList.size();
	}

	@Override
	public List<RecipeLayoutWithButtons<?>> subList(int from, int to) {
		return unsortedList.subList(from, to);
	}

	@Override
	public Optional<RecipeLayoutWithButtons<?>> findFirst() {
		if (unsortedList.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(unsortedList.getFirst());
	}

	@Override
	public void tick() {

	}
}
