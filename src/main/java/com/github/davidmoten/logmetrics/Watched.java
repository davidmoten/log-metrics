package com.github.davidmoten.logmetrics;

import static com.github.davidmoten.logmetrics.Util.createReader;

import java.io.File;
import java.nio.charset.Charset;

import rx.Observable;
import rx.functions.Func1;
import rx.observables.StringObservable;

import com.github.davidmoten.rx.FileObservable;
import com.google.common.base.Optional;

public class Watched {

	private final String category;
	private final File file;
	private final boolean tail;
	private final MetricExtractor extractor;
	private final Optional<Long> startTime;
	private final long sampleTimeMs;

	public Watched(String category, File file, boolean tail,
			MetricExtractor extractor, Optional<Long> startTime,
			long sampleTimeMs) {
		this.category = category;
		this.file = file;
		this.tail = tail;
		this.extractor = extractor;
		this.startTime = startTime;
		this.sampleTimeMs = sampleTimeMs;
	}

	public Observable<? extends Metrics> watch() {

		return tail(file)
		// extract metrics
				.flatMap(toMetrics(extractor, category))
				// include only those lines after start time
				.filter(after(startTime));

	}

	private Func1<String, ? extends Observable<? extends Metrics>> toMetrics(
			final MetricExtractor extractor, final String category) {
		return new Func1<String, Observable<? extends Metrics>>() {

			@Override
			public Observable<? extends Metrics> call(String line) {
				return extractor.extract(category, line);
			}
		};
	}

	private Func1<? super Metrics, Boolean> after(final Optional<Long> startTime) {
		return new Func1<Metrics, Boolean>() {

			@Override
			public Boolean call(Metrics metrics) {
				if (startTime.isPresent()) {
					return metrics.getTimestamp() >= startTime.get();
				} else
					return true;
			}
		};
	}

	private Observable<String> tail(final File file) {
		if (tail)
			return FileObservable.tailTextFile(file, 0, 1000,
					Charset.forName("UTF-8"));
		else
			return StringObservable.split(
					StringObservable.from(createReader(file, 0)), "\n");
	}
}
