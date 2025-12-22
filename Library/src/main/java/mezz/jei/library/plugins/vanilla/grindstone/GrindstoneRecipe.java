package mezz.jei.library.plugins.vanilla.grindstone;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import mezz.jei.api.recipe.vanilla.IJeiGrindstoneRecipe;
import mezz.jei.common.util.MathUtil;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public final class GrindstoneRecipe implements IJeiGrindstoneRecipe {
	private final List<ItemStack> topInputs;
	private final List<ItemStack> bottomInputs;
	private final List<ItemStack> outputs;
	private int minXpReward;
	private final int maxXpReward;
	private final @Nullable Identifier uid;

	public GrindstoneRecipe(
		List<ItemStack> topInputs,
		List<ItemStack> bottomInputs,
		List<ItemStack> outputs,
		int minXpReward,
		int maxXpReward,
		@Nullable Identifier uid
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
	public List<ItemStack> getTopInputs() {
		return topInputs;
	}

	@Override
	@Unmodifiable
	public List<ItemStack> getBottomInputs() {
		return bottomInputs;
	}

	@Override
	@Unmodifiable
	public List<ItemStack> getOutputs() {
		return outputs;
	}

	@Override
	public int getMinXpReward() {
		if (minXpReward < 0) {
			minXpReward = getMinXp(topInputs.getFirst(), bottomInputs.getFirst());
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
	public Identifier getUid() {
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
		ItemEnchantments itemEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);

		for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
			Holder<Enchantment> holder = entry.getKey();
			int j = entry.getIntValue();
			if (!holder.is(EnchantmentTags.CURSE)) {
				i += holder.value().getMinCost(j);
			}
		}

		return i;
	}
}
