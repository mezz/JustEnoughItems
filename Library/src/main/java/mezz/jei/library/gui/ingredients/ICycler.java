package mezz.jei.library.gui.ingredients;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface ICycler {
	<T> Optional<T> getCycled(List<@Nullable T> list);
}
