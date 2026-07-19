package mezz.jei.common.search;

import mezz.jei.api.search.ISearchStorageBuilder;
import mezz.jei.api.search.ISearchStorageBuilderFactory;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;

public class PrefixInfo<T, I> {
	private final String id;
	private final char prefix;
	private final Component description;
	private final boolean supportsDynamicCompletion;
	private final IModeGetter modeGetter;
	private final IStringsGetter<T> stringsGetter;
	private final ISearchStorageBuilderFactory searchStorageBuilderFactory;

	public PrefixInfo(
		String id,
		char prefix,
		Component description,
		boolean supportsDynamicCompletion,
		IModeGetter modeGetter,
		IStringsGetter<T> stringsGetter,
		ISearchStorageBuilderFactory searchStorageBuilderFactory
	) {
		this.id = id;
		this.prefix = prefix;
		this.description = description;
		this.supportsDynamicCompletion = supportsDynamicCompletion;
		this.modeGetter = modeGetter;
		this.stringsGetter = stringsGetter;
		this.searchStorageBuilderFactory = searchStorageBuilderFactory;
	}

	public char getPrefix() {
		return prefix;
	}

	public Component getDescription() {
		return description;
	}

	public boolean supportsDynamicCompletion() {
		return supportsDynamicCompletion;
	}

	public SearchMode getMode() {
		return modeGetter.getMode();
	}

	public ISearchStorageBuilder<I> createStorageBuilder() {
		return searchStorageBuilderFactory.create(id);
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
		return "PrefixInfo{" + id + '}';
	}
}
