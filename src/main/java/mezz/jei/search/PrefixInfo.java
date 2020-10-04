package mezz.jei.search;

import mezz.jei.config.SearchMode;
import mezz.jei.ingredients.IIngredientListElementInfo;

import java.util.Collection;

public class PrefixInfo {
	public static final PrefixInfo NO_PREFIX = new PrefixInfo(
		() -> SearchMode.ENABLED,
		IIngredientListElementInfo::getNameStrings
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

	public Collection<String> getStrings(IIngredientListElementInfo<?> element) {
		return this.stringsGetter.getStrings(element);
	}

	@FunctionalInterface
	public interface IStringsGetter {
		Collection<String> getStrings(IIngredientListElementInfo<?> element);
	}

	@FunctionalInterface
	public interface IModeGetter {
		SearchMode getMode();
	}
}
