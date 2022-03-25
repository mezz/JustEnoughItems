package mezz.jei.core.search;

import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.function.Supplier;

public class PrefixInfo<T> {
	private final char prefix;
	private final IModeGetter modeGetter;
	private final IStringsGetter<T> stringsGetter;
	private final Supplier<ISearchStorage<T>> storageSupplier;

	public PrefixInfo(char prefix, IModeGetter modeGetter, IStringsGetter<T> stringsGetter, Supplier<ISearchStorage<T>> storageSupplier) {
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

	public ISearchStorage<T> createStorage() {
		return this.storageSupplier.get();
	}

	@Unmodifiable
	public Collection<String> getStrings(T element) {
		return this.stringsGetter.getStrings(element);
	}

	@FunctionalInterface
	public interface IStringsGetter<T> {
		@Unmodifiable
		Collection<String> getStrings(T element);
	}

	@FunctionalInterface
	public interface IModeGetter {
		SearchMode getMode();
	}

	@Override
	public String toString() {
		return "PrefixInfo{" + prefix + '}';
	}
}
