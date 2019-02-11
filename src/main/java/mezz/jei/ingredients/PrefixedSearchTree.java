package mezz.jei.ingredients;

import java.util.Collection;

import mezz.jei.config.SearchMode;
import mezz.jei.gui.ingredients.IIngredientListElementInfo;
import mezz.jei.suffixtree.GeneralizedSuffixTree;

class PrefixedSearchTree {
	private final GeneralizedSuffixTree tree;
	private final IStringsGetter stringsGetter;
	private final IModeGetter modeGetter;

	public PrefixedSearchTree(GeneralizedSuffixTree tree, IStringsGetter stringsGetter, IModeGetter modeGetter) {
		this.tree = tree;
		this.stringsGetter = stringsGetter;
		this.modeGetter = modeGetter;
	}

	public GeneralizedSuffixTree getTree() {
		return tree;
	}

	public IStringsGetter getStringsGetter() {
		return stringsGetter;
	}

	public SearchMode getMode() {
		return modeGetter.getMode();
	}

	@FunctionalInterface
	interface IStringsGetter {
		Collection<String> getStrings(IIngredientListElementInfo<?> element);
	}

	@FunctionalInterface
	interface IModeGetter {
		SearchMode getMode();
	}
}
