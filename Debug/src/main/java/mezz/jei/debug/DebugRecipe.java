package mezz.jei.debug;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.gui.components.Button;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class DebugRecipe {
	private static int NEXT_ID = 0;

	private final Button button;
	private final ResourceLocation registryName;
	private final List<ItemStack> largeIngredientList;

	public DebugRecipe() {
		this(Component.literal("test"), List.of());
	}

	private DebugRecipe(Component buttonText, List<ItemStack> largeIngredientList) {
		this.button = new Button(0, 0, 40, 20, buttonText, b -> {
		});
		this.registryName = new ResourceLocation(ModIds.JEI_ID, "debug_recipe_" + NEXT_ID);
		this.largeIngredientList = List.copyOf(largeIngredientList);
		NEXT_ID++;
	}

	public static DebugRecipe createLargeIngredientList(IIngredientManager ingredientManager) {
		List<ItemStack> examples = ingredientManager.getAllItemStacks().stream()
			.filter(stack -> !stack.isEmpty() && !stack.is(Items.COAL))
			.toList();
		List<ItemStack> ingredients = new ArrayList<>(10_000);
		for (int index = 1; index <= 10_000; index++) {
			ItemStack stack;
			if (index == 10_000) {
				// Coal only appears at the end, beyond the rotation limit, to reproduce #4492.
				stack = new ItemStack(Items.COAL, 64);
			} else if (examples.isEmpty()) {
				stack = new ItemStack(Items.STONE, 64);
			} else {
				stack = examples.get((index - 1) % examples.size()).copy();
				stack.setCount(64);
			}
			stack.setHoverName(Component.literal("Candidate #" + index + " / 10000"));
			ingredients.add(stack);
		}
		return new DebugRecipe(Component.literal("10k"), ingredients);
	}

	public List<ItemStack> getLargeIngredientList() {
		return largeIngredientList;
	}

	public Button getButton() {
		return button;
	}

	public boolean checkHover(double mouseX, double mouseY) {
		return this.button.isMouseOver(mouseX, mouseY);
	}

	public ResourceLocation getRegistryName() {
		return registryName;
	}
}
