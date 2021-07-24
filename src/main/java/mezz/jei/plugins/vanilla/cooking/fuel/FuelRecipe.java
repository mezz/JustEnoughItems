package mezz.jei.plugins.vanilla.cooking.fuel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.world.item.ItemStack;

import com.google.common.base.Preconditions;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.config.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class FuelRecipe {
	private final List<ItemStack> inputs;
	private final Component smeltCountText;
	private final IDrawableAnimated flame;

	public FuelRecipe(IGuiHelper guiHelper, Collection<ItemStack> input, int burnTime) {
		Preconditions.checkArgument(burnTime > 0, "burn time must be greater than 0");
		this.inputs = new ArrayList<>(input);
		this.smeltCountText = createSmeltCountText(burnTime);
		this.flame = guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 82, 114, 14, 14)
			.buildAnimated(burnTime, IDrawableAnimated.StartDirection.TOP, true);
	}

	public List<ItemStack> getInputs() {
		return inputs;
	}

	public Component getSmeltCountText() {
		return smeltCountText;
	}

	public IDrawableAnimated getFlame() {
		return flame;
	}

	public static Component createSmeltCountText(int burnTime) {
		if (burnTime == 200) {
			return new TranslatableComponent("gui.jei.category.fuel.smeltCount.single");
		} else {
			NumberFormat numberInstance = NumberFormat.getNumberInstance();
			numberInstance.setMaximumFractionDigits(2);
			String smeltCount = numberInstance.format(burnTime / 200f);
			return new TranslatableComponent("gui.jei.category.fuel.smeltCount", smeltCount);
		}
	}
}
