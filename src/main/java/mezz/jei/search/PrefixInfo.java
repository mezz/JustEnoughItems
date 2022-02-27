package mezz.jei.search;

import mezz.jei.config.SearchMode;
import mezz.jei.ingredients.IListElementInfo;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;

public class PrefixInfo {
	public static final PrefixInfo NO_PREFIX = new PrefixInfo(
		() -> SearchMode.ENABLED,
		i -> List.of(i.getName())
	);
	private final IModeGetter modeGetter;
	private final IStringsGetter stringsGetter;

	public PrefixInfo(IModeGetter modeGetter, IStringsGetter stringsGetter) {
		this.modeGetter = modeGetter;
		this.stringsGetter = stringsGetter;
	}

	public SearchMode getMode() {
		return modeGetter.getMode();
	}

	@Unmodifiable
	public Collection<String> getStrings(IListElementInfo<?> element) {
		return this.stringsGetter.getStrings(element);
	}

	@FunctionalInterface
	public interface IStringsGetter {
		@Unmodifiable
		Collection<String> getStrings(IListElementInfo<?> element);
	}

	@FunctionalInterface
	public interface IModeGetter {
		SearchMode getMode();
	}
}
