package mezz.jei.common.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public class EnumCodec {
	public static <T extends Enum<T>> Codec<T> create(Class<T> enumClass) {
		return Codec.STRING.flatXmap(
			name -> {
				try {
					T e = Enum.valueOf(enumClass, name);
					return DataResult.success(e);
				} catch (IllegalArgumentException ignored) {
					return DataResult.error(() -> "Unknown enum name: '" + name + "' for enum class: " + enumClass);
				}
			},
			e -> {
				String name = e.name();
				return DataResult.success(name);
			}
		);
	}
}
