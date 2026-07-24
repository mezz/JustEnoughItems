package mezz.jei.library.gui.ingredients;

import java.util.List;
import java.util.Optional;

public interface ICycler {
	<T> Optional<T> getCycled(List<Optional<T>> list);
}
