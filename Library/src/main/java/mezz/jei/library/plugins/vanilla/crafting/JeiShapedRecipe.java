package mezz.jei.library.plugins.vanilla.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mezz.jei.library.recipes.RecipeSerializers;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JeiShapedRecipe implements CraftingRecipe {
	private final ShapedRecipePattern pattern;
	private final List<SlotDisplay> displays;
	private final SlotDisplay results;
	private final String group;
	private final CraftingBookCategory category;

	public JeiShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, List<SlotDisplay> displays, SlotDisplay results) {
		this.group = group;
		this.category = category;
		this.pattern = pattern;
		this.displays = displays;
		this.results = results;
	}

	@Override
	public RecipeSerializer<? extends CraftingRecipe> getSerializer() {
		return RecipeSerializers.getJeiShapedRecipeSerializer();
	}

	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.createFromOptionals(pattern.ingredients());
	}

	@Override
	public String group() {
		return group;
	}

	@Override
	public CraftingBookCategory category() {
		return this.category;
	}

	@Override
	public List<RecipeDisplay> display() {
		return List.of(
			new ShapedCraftingRecipeDisplay(
				this.pattern.width(),
				this.pattern.height(),
				displays,
				results,
				new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
			)
		);
	}

	@Override
	public boolean showNotification() {
		return false;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return this.pattern.matches(input);
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		Minecraft minecraft = Minecraft.getInstance();
		ContextMap contextmap = SlotDisplayContext.fromLevel(Objects.requireNonNull(minecraft.level));
		return this.results.resolveForFirstStack(contextmap).copy();
	}

	public int getWidth() {
		return this.pattern.width();
	}

	public int getHeight() {
		return this.pattern.height();
	}

	public static final MapCodec<JeiShapedRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
		return instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter((shapedRecipe) -> {
			return shapedRecipe.group;
		}), CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter((shapedRecipe) -> {
			return shapedRecipe.category;
		}), ShapedRecipePattern.MAP_CODEC.forGetter((shapedRecipe) -> {
			return shapedRecipe.pattern;
		}), Codec.list(SlotDisplay.CODEC).fieldOf("display").forGetter((shapedRecipe) -> {
			return shapedRecipe.displays;
		}), SlotDisplay.CODEC.fieldOf("result").forGetter((shapedRecipe) -> {
			return shapedRecipe.results;
		})).apply(instance, JeiShapedRecipe::new);
	});
	public static final StreamCodec<RegistryFriendlyByteBuf, JeiShapedRecipe> STREAM_CODEC = StreamCodec.of(JeiShapedRecipe::toNetwork, JeiShapedRecipe::fromNetwork);
	public static final RecipeSerializer<JeiShapedRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private static JeiShapedRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
		String string = buffer.readUtf();
		CraftingBookCategory craftingBookCategory = buffer.readEnum(CraftingBookCategory.class);
		ShapedRecipePattern shapedRecipePattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);

		int displayCount = buffer.readVarInt();
		if (displayCount < 9) {
			throw new IllegalArgumentException("Display count must be 9 or fewer");
		}
		List<SlotDisplay> displays = new ArrayList<>(displayCount);
		for (int i = 0; i < displayCount; i++) {
			SlotDisplay display = SlotDisplay.STREAM_CODEC.decode(buffer);
			displays.add(display);
		}

		SlotDisplay results = SlotDisplay.STREAM_CODEC.decode(buffer);
		return new JeiShapedRecipe(string, craftingBookCategory, shapedRecipePattern, displays, results);
	}

	private static void toNetwork(RegistryFriendlyByteBuf buffer, JeiShapedRecipe recipe) {
		buffer.writeUtf(recipe.group);
		buffer.writeEnum(recipe.category);
		ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);

		List<SlotDisplay> displays = recipe.displays;
		buffer.writeVarInt(displays.size());
		for (SlotDisplay display : displays) {
			SlotDisplay.STREAM_CODEC.encode(buffer, display);
		}

		SlotDisplay.STREAM_CODEC.encode(buffer, recipe.results);
	}
}
