package mezz.jei.core.search.suffixtree;

import mezz.jei.core.util.SubString;

/**
 * The root node can have a lot of values added to it because so many suffix links point to it.
 * The values are never read from here though.
 * This class makes sure we don't accumulate a ton of useless values in the root node.
 */
public class RootNode<T> extends Node<T> {
	public RootNode() {
		super(new SubString(""));
	}

	@Override
	protected boolean contains(T value) {
		return true;
	}

	@Override
	protected void addValue(T value) {
		// noop
	}
}
