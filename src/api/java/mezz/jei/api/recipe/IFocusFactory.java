package mezz.jei.api.recipe;

public interface IFocusFactory {
	/**
	 * Returns a new focus.
	 */
	<V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient);
}
