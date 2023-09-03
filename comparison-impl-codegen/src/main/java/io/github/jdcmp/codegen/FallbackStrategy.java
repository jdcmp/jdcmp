package io.github.jdcmp.codegen;

import io.github.jdcmp.api.documentation.ThreadSafe;
import io.github.jdcmp.codegen.Utils.ThrowableConsumer;
import io.github.jdcmp.codegen.Utils.ThrowableFunction;

import java.util.List;
import java.util.Objects;

@ThreadSafe // Assuming the provided strategies are thread-safe as well
final class FallbackStrategy<T> {

	private final List<T> strategies;

	public static <T> FallbackStrategy<T> of(Iterable<? extends T> strategies) {
		return new FallbackStrategy<>(strategies);
	}

	public FallbackStrategy(Iterable<? extends T> strategies) {
		this.strategies = Utils.immutableArrayListNonEmpty(strategies);
	}

	public <R> R applyChecked(ThrowableFunction<? super T, ? extends R> strategyRunner) throws Throwable {
		Throwable throwable = null;

		for (T strategy : strategies) {
			try {
				R value = strategyRunner.applyChecked(strategy);

				return Objects.requireNonNull(value, () -> "Strategy returned null: " + strategy);
			} catch (@SuppressWarnings("removal") ThreadDeath e) {
				throw e;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throwable = Utils.chainThrowables(throwable, e);
				break;
			} catch (Throwable e) {
				throwable = Utils.chainThrowables(throwable, e);
			}
		}

		throw Objects.requireNonNull(throwable);
	}

	public void consumeChecked(ThrowableConsumer<T> strategyRunner) throws Throwable {
		applyChecked(strategy -> {
			strategyRunner.acceptChecked(strategy);
			return strategy;
		});
	}

	public <R> R apply(ThrowableFunction<? super T, ? extends R> strategyRunner) {
		try {
			return applyChecked(strategyRunner);
		} catch (@SuppressWarnings("removal") RuntimeException | ThreadDeath e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public void consume(ThrowableConsumer<? super T> strategyRunner) {
		apply(strategy -> {
			strategyRunner.acceptChecked(strategy);
			return strategy;
		});
	}

}
