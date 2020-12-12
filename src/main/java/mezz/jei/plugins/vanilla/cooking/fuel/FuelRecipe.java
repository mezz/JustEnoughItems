package mezz.jei.plugins.vanilla.cooking.fuel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.google.common.base.Preconditions;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.config.Constants;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class FuelRecipe {
	private final List<ItemStack> inputs;
	private final ITextComponent smeltCountText;
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

	public ITextComponent getSmeltCountText() {
		return smeltCountText;
	}

	public IDrawableAnimated getFlame() {
		return flame;
	}

	public static ITextComponent createSmeltCountText(int burnTime) {
		if (burnTime == 200) {
			return new TranslationTextComponent("gui.jei.category.fuel.smeltCount.single");
		} else {
			NumberFormat numberInstance = NumberFormat.getNumberInstance();
			numberInstance.setMaximumFractionDigits(2);
			String smeltCount = numberInstance.format(burnTime / 200f);
			return new TranslationTextComponent("gui.jei.category.fuel.smeltCount", smeltCount);
		}
	}
}
