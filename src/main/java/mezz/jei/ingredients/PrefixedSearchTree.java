package mezz.jei.ingredients;

import java.util.Collection;

import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;
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

	public Config.SearchMode getMode() {
		return modeGetter.getMode();
	}

	@FunctionalInterface
	interface IStringsGetter {
		Collection<String> getStrings(IIngredientListElement<?> element);
	}

	@FunctionalInterface
	interface IModeGetter {
		Config.SearchMode getMode();
	}
}
