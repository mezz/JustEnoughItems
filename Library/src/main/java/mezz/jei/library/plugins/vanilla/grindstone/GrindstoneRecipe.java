package mezz.jei.library.plugins.vanilla.grindstone;

import mezz.jei.api.recipe.vanilla.IJeiGrindstoneRecipe;
import mezz.jei.common.util.MathUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;

public final class GrindstoneRecipe implements IJeiGrindstoneRecipe {
	private final List<ItemStack> topInputs;
	private final List<ItemStack> bottomInputs;
	private final List<ItemStack> outputs;
	private int minXpReward;
	private final int maxXpReward;
	private final @Nullable ResourceLocation uid;

	public GrindstoneRecipe(
		List<ItemStack> topInputs,
		List<ItemStack> bottomInputs,
		List<ItemStack> outputs,
		int minXpReward,
		int maxXpReward,
		@Nullable ResourceLocation uid
	) {
		this.topInputs = topInputs;
		this.bottomInputs = bottomInputs;
		this.outputs = outputs;
		this.minXpReward = minXpReward;
		this.maxXpReward = maxXpReward;
		this.uid = uid;
	}

	@Override
	@Unmodifiable
	@NotNull
	public List<ItemStack> getTopInputs() {
		return topInputs;
	}

	@Override
	@Unmodifiable
	@NotNull
	public List<ItemStack> getBottomInputs() {
		return bottomInputs;
	}

	@Override
	@Unmodifiable
	@NotNull
	public List<ItemStack> getOutputs() {
		return outputs;
	}

	@Override
	public int getMinXpReward() {
		if (minXpReward < 0) {
			minXpReward = getMinXp(topInputs.get(0), bottomInputs.get(0));
		}
		return minXpReward;
	}

	@Override
	public int getMaxXpReward() {
		if (maxXpReward < 0) {
			return getMinXpReward() * 2;
		}
		return maxXpReward;
	}

	@Override
	@Nullable
	public ResourceLocation getUid() {
		return uid;
	}

	@Override
	@Unmodifiable
	public boolean isOutputRenderOnly() {
		return true;
	}

	private static int getMinXp(ItemStack topItem, ItemStack bottomItem) {
		int topXp = getExperienceFromItem(topItem);
		int bottomXp = getExperienceFromItem(bottomItem);
		return MathUtil.divideCeil(topXp + bottomXp, 2);
	}

	private static int getExperienceFromItem(ItemStack stack) {
		int i = 0;
		Map<Enchantment, Integer> itemEnchantments = EnchantmentHelper.getEnchantments(stack);

		for (Map.Entry<Enchantment, Integer> entry : itemEnchantments.entrySet()) {
			Enchantment enchantment = entry.getKey();
			int j = entry.getValue();
			if (!enchantment.isCurse()) {
				i += enchantment.getMinCost(j);
			}
		}

		return i;
	}
}
