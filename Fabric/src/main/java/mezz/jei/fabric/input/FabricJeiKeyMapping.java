package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.JeiKeyConflictContext;

public class FabricJeiKeyMapping extends AbstractJeiKeyMapping {
	protected final FabricKeyMapping fabricMapping;

	public FabricJeiKeyMapping(FabricKeyMapping fabricMapping, JeiKeyConflictContext context) {
		super(context);
		this.fabricMapping = fabricMapping;
	}

	protected FabricKeyMapping getMapping() {
		return this.fabricMapping;
	}

	protected InputConstants.Key getMappedKey() {
		return this.fabricMapping.realKey;
	}
}
