package com.github.davidmoten.util.rx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import rx.Observable;

public class IoObservableTest {

    @Test
    public void testIgnoreZeroLengthAtEndDoesNothingIfNonZeroLengthAtEnd() {
        List<String> list = IoObservable.ignoreZeroLengthAtEnd(Observable.from("a", "b")).toList()
                .toBlockingObservable().single();
        assertEquals(Arrays.asList("a", "b"), list);
    }

    @Test
    public void testIgnoreZeroLengthAtEndIgnoresLastIfZeroLengthAtEnd() {
        List<String> list = IoObservable.ignoreZeroLengthAtEnd(Observable.from("a", "b", ""))
                .toList().toBlockingObservable().single();
        assertEquals(Arrays.asList("a", "b"), list);
    }

    @Test
    public void testIgnoreZeroLengthAtEndDoesNothingEmptySource() {
        List<String> list = IoObservable.ignoreZeroLengthAtEnd(Observable.<String> empty())
                .toList().toBlockingObservable().single();
        assertTrue(list.isEmpty());
    }

}
