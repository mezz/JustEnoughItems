package mezz.jei;

import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import mezz.jei.api.INbtIgnoreList;
import mezz.jei.config.Config;

public class NbtIgnoreList implements INbtIgnoreList {
	private final Set<String> nbtTagNameBlacklist = new HashSet<>();

	public void ignoreNbtTagNames(String... nbtTagNames) {
		Collections.addAll(nbtTagNameBlacklist, nbtTagNames);
	}

	public boolean isNbtTagIgnored(String nbtTagName) {
		return Config.nbtKeyIgnoreList.contains(nbtTagName) || nbtTagNameBlacklist.contains(nbtTagName);
	}

	public Set<String> getIgnoredNbtTags(Set<String> nbtTagNames) {
		Set<String> ignoredKeysConfig = Sets.intersection(nbtTagNames, Config.nbtKeyIgnoreList);
		Set<String> ignoredKeysApi = Sets.intersection(nbtTagNames, nbtTagNameBlacklist);
		return Sets.union(ignoredKeysConfig, ignoredKeysApi);
	}
}
