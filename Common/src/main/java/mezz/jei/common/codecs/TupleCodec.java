package mezz.jei.common.codecs;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Stream;

public class TupleCodec<F, S> implements Codec<Pair<F, S>> {
	public static <F, S> TupleCodec<F, S> of(Codec<F> first, Codec<S> second) {
		return new TupleCodec<>(first, second);
	}

	private final Codec<F> first;
	private final Codec<S> second;

	private TupleCodec(final Codec<F> first, final Codec<S> second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public <T> DataResult<Pair<Pair<F, S>, T>> decode(final DynamicOps<T> ops, final T input) {
		return ops.getList(input)
			.setLifecycle(Lifecycle.stable())
			.flatMap(stream -> {
				final DecoderState<T> decoder = new DecoderState<>(ops);
				stream.accept(decoder::accept);
				return decoder.build();
			});
	}

	@Override
	public <T> DataResult<T> encode(final Pair<F, S> input, final DynamicOps<T> ops, final T prefix) {
		final ListBuilder<T> builder = ops.listBuilder();
		builder.add(first.encodeStart(ops, input.getFirst()));
		builder.add(second.encodeStart(ops, input.getSecond()));
		return builder.build(prefix);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof TupleCodec<?, ?> tupleCodec) {
			return Objects.equals(first, tupleCodec.first) && Objects.equals(second, tupleCodec.second);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}

	@Override
	public String toString() {
		return "TupleCodec[" + first + ", " + second + ']';
	}

	private class DecoderState<T> {
		private static final DataResult<Unit> INITIAL_RESULT = DataResult.success(Unit.INSTANCE, Lifecycle.stable());

		private final DynamicOps<T> ops;
		private final Stream.Builder<T> failed = Stream.builder();
		private DataResult<Unit> result = INITIAL_RESULT;
		private @Nullable F firstValue;
		private @Nullable S secondValue;
		private int elementCount;

		private DecoderState(final DynamicOps<T> ops) {
			this.ops = ops;
		}

		public void accept(final T value) {
			elementCount++;
			if (firstValue != null && secondValue != null) {
				failed.add(value);
				return;
			}
			if (firstValue == null) {
				final DataResult<Pair<F, T>> elementResult = first.decode(ops, value);
				elementResult.error().ifPresent(error -> failed.add(value));
				elementResult.resultOrPartial().ifPresent(pair -> firstValue = pair.getFirst());
				result = result.apply2stable((result, element) -> result, elementResult);
			} else {
				final DataResult<Pair<S, T>> elementResult = second.decode(ops, value);
				elementResult.error().ifPresent(error -> failed.add(value));
				elementResult.resultOrPartial().ifPresent(pair -> secondValue = pair.getFirst());
				result = result.apply2stable((result, element) -> result, elementResult);
			}
		}

		public DataResult<Pair<Pair<F, S>, T>> build() {
			if (elementCount < 2) {
				return createTooShortError(elementCount);
			} else if (elementCount > 2) {
				return createTooLongError(elementCount);
			}
			final T errors = ops.createList(failed.build());
			final Pair<Pair<F, S>, T> pair = Pair.of(Pair.of(firstValue, secondValue), errors);
			return result.map(ignored -> pair).setPartial(pair);
		}
	}

	private <R> DataResult<R> createTooShortError(final int size) {
		return DataResult.error(() -> "Tuple is too short: " + size + ", expected length is 2");
	}

	private <R> DataResult<R> createTooLongError(final int size) {
		return DataResult.error(() -> "Tuple is too long: " + size + ", expected length is 2");
	}
}
