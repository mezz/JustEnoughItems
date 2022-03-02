package mezz.jei.collect;

import java.util.AbstractSet;
import java.util.Optional;

public abstract class AbstractIngredientSet<V> extends AbstractSet<V> {
	public abstract Optional<V> getByUid(String uid);
}
