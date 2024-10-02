package mezz.jei.library.gui.ingredients;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ICycler {
	@Nullable
	<T> T getCycled(List<@Nullable T> list);
}
