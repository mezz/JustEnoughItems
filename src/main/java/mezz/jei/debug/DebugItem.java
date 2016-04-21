package mezz.jei.debug;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class DebugItem extends Item {
	private static final RandomStringMaker RANDOM_STRING_MAKER = new RandomStringMaker(12);
	private static final int MAX_DAMAGE = 1000;

	public DebugItem(String name) {
		setUnlocalizedName(name);
		setCreativeTab(CreativeTabs.SEARCH);
		setMaxDamage(MAX_DAMAGE);
		setHasSubtypes(true);
	}

	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (int i = 0; i < 10000; i++) {
			String name = RANDOM_STRING_MAKER.nextString();
			ItemStack itemStack = new ItemStack(itemIn);
			itemStack.setItemDamage((int)(Math.random() * MAX_DAMAGE));
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("name", name);
			itemStack.setTagCompound(nbt);
			subItems.add(itemStack);
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return stack.getTagCompound().getString("name");
	}
}
