package mezz.jei.fabric.platform;

import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.keys.IJeiKeyMappingCategoryBuilder;
import mezz.jei.common.platform.IPlatformInputHelper;
import mezz.jei.fabric.input.FabricJeiKeyMappingCategoryBuilder;
import net.fabricmc.fabric.api.item.v1.FabricTooltipFlag;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.TooltipFlag;

public class InputHelper implements IPlatformInputHelper {
	@Override
	public boolean isActiveAndMatches(KeyMapping keyMapping, UserInput input) {
		if (keyMapping.isUnbound()) {
			return false;
		}
		return input.ifKeyboardEvent(keyMapping::matches) ||
			input.ifMouseEvent((event, ignoredDoubleClick) -> keyMapping.matchesMouse(event));
	}

	@Override
	public IJeiKeyMappingCategoryBuilder createKeyMappingCategoryBuilder(KeyMapping.Category category) {
		return new FabricJeiKeyMappingCategoryBuilder(category);
	}

	@Override
	public TooltipFlag getSearchTooltipFlag(TooltipFlag tooltipFlag) {
		return new SearchTooltipFlag(tooltipFlag.isAdvanced(), tooltipFlag.isCreative());
	}

	private record SearchTooltipFlag(boolean advanced, boolean creative) implements TooltipFlag, FabricTooltipFlag {
		@Override
		public boolean isAdvanced() {
			return advanced;
		}

		@Override
		public boolean isCreative() {
			return creative;
		}

		@Override
		public boolean shouldDisplayAllInformation() {
			return true;
		}
	}
}
