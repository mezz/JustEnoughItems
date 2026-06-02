package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

public class FabricJeiKeyMapping extends AbstractJeiKeyMapping {
	protected final FabricKeyMapping fabricMapping;

	public FabricJeiKeyMapping(FabricKeyMapping fabricMapping, JeiKeyConflictContext context) {
		super(context);
		this.fabricMapping = fabricMapping;
	}

	@Override
	protected FabricKeyMapping getMapping() {
		return this.fabricMapping;
	}

	@Override
	public boolean isActiveAndMatches(InputConstants.Key key) {
		if (isUnbound()) {
			return false;
		}
		if (!KeyMappingHelper.getBoundKeyOf(this.fabricMapping).equals(key)) {
			return false;
		}
		return context.isActive();
	}
}
