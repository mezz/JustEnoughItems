package mezz.jei.gui.recipes.layouts;

import mezz.jei.gui.recipes.IRecipeLayoutWithButtons;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UnsortedRecipeLayoutList implements IRecipeLayoutList {
	private final List<IRecipeLayoutWithButtons<?>> unsortedList;

	UnsortedRecipeLayoutList(List<? extends IRecipeLayoutWithButtons<?>> unsortedList) {
		this.unsortedList = new ArrayList<>(unsortedList);
	}

	@Override
	public int size() {
		return unsortedList.size();
	}

	@Override
	public List<IRecipeLayoutWithButtons<?>> subList(int from, int to, @Nullable AbstractContainerMenu container) {
		return unsortedList.subList(from, to);
	}

	@Override
	public Optional<IRecipeLayoutWithButtons<?>> findFirst(@Nullable AbstractContainerMenu container) {
		if (unsortedList.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(unsortedList.get(0));
	}

	@Override
	public void tick(@Nullable AbstractContainerMenu container) {

	}
}
