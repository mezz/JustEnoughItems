package mezz.jei.load.registration;

import com.google.common.collect.ImmutableSetMultimap;
import mezz.jei.api.registration.IModInfoRegistration;
import mezz.jei.util.ErrorUtil;

import java.util.Collection;

public class ModInfoRegistration implements IModInfoRegistration {
	private final ImmutableSetMultimap.Builder<String, String> modAliases = ImmutableSetMultimap.builder();

	@Override
	public void addModAliases(String modId, Collection<String> aliases) {
		ErrorUtil.checkNotNull(modId, "modId");
		ErrorUtil.checkNotNull(aliases, "aliases");
		modAliases.putAll(modId, aliases);
	}

	public ImmutableSetMultimap<String, String> getModAliases() {
		return modAliases.build();
	}
}
