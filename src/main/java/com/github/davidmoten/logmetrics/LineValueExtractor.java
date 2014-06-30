package com.github.davidmoten.logmetrics;

import java.util.List;

public interface LineValueExtractor<T> {
    List<LineValue<T>> extract(Line line);
}
