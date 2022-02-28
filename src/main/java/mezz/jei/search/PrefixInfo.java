package mezz.jei.search;

import mezz.jei.config.SearchMode;
import mezz.jei.ingredients.IListElementInfo;
import mezz.jei.search.suffixtree.GeneralizedSuffixTree;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class PrefixInfo {
	public static final PrefixInfo NO_PREFIX = new PrefixInfo(
		'\0',
		() -> SearchMode.ENABLED,
		i -> List.of(i.getName()),
		GeneralizedSuffixTree::new
	);
	private final char prefix;
	private final IModeGetter modeGetter;
	private final IStringsGetter stringsGetter;
	private final Supplier<ISearchStorage<IListElementInfo<?>>> storageSupplier;

	public PrefixInfo(char prefix, IModeGetter modeGetter, IStringsGetter stringsGetter, Supplier<ISearchStorage<IListElementInfo<?>>> storageSupplier) {
		this.prefix = prefix;
		this.modeGetter = modeGetter;
		this.stringsGetter = stringsGetter;
		this.storageSupplier = storageSupplier;
	}

	public char getPrefix() {
		return prefix;
	}

	public SearchMode getMode() {
		return modeGetter.getMode();
	}

	public ISearchStorage<IListElementInfo<?>> createStorage() {
		return this.storageSupplier.get();
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

	@Override
	public String toString() {
		if (this == NO_PREFIX) {
			return "PrefixInfo{NO_PREFIX}";
		} else {
			return "PrefixInfo{" + prefix + '}';
		}
	}
}
