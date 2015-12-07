package mezz.jei.api;

import java.util.Set;

public interface INbtIgnoreList {
	/**
	 * Tell JEI to ignore NBT tags when comparing items for recipes.
	 */
	void ignoreNbtTagNames(String... nbtTagNames);

	/**
	 * Check to see if an NBT tag is ignored.
	 */
	boolean isNbtTagIgnored(String nbtTagName);

	/**
	 * Get all the ignored tag names out of a set of NBT tag names.
	 */
	Set<String> getIgnoredNbtTags(Set<String> nbtTagNames);
}
