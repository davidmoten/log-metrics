package com.github.davidmoten.util.rx;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;

public class WatchServiceObservable {

    public static Observable<WatchEvent<?>> from(WatchService watchService) {
        return Observable.create(new WatchServiceOnSubscribe(watchService));
    }

    /**
     * If file does not exist at subscribe time then is assumed to not be a
     * directory. If the file is not a directory (bearing in mind the aforesaid
     * assumption) then a {@link WatchService} is set up on its parent and
     * {@link WatchEvent}s of the given kinds are filtered to concern the file
     * in question. If the file is a directory then a {@link WatchService} is
     * set up on the directory and all events are passed through of the given
     * kinds.
     * 
     * @param file
     * @param kinds
     * @return
     */
    @SafeVarargs
    public static Observable<WatchEvent<?>> from(final File file, Kind<Path>... kinds) {
        return watchService(file, kinds).flatMap(TO_WATCH_EVENTS).filter(onlyRelatedTo(file));
    }

    @SafeVarargs
    public static Observable<WatchService> watchService(final File file, final Kind<Path>... kinds) {
        return Observable.create(new OnSubscribe<WatchService>() {

            @Override
            public void call(Subscriber<? super WatchService> subscriber) {
                final Path path;
                if (file.exists() && file.isDirectory())
                    path = Paths.get(file.toURI());
                else
                    path = Paths.get(file.getParentFile().toURI());
                try {
                    WatchService watchService = path.getFileSystem().newWatchService();
                    path.register(watchService, kinds);
                    subscriber.onNext(watchService);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private static Func1<WatchEvent<?>, Boolean> onlyRelatedTo(final File file) {
        return new Func1<WatchEvent<?>, Boolean>() {

            @Override
            public Boolean call(WatchEvent<?> event) {
                final boolean ok;
                if (file.isDirectory())
                    ok = true;
                else {
                    Object context = event.context();
                    if (context != null && context instanceof Path) {
                        Path p = (Path) context;
                        ok = p.toFile().getName().equals(file.getName());
                    } else
                        ok = false;
                }
                return ok;
            }
        };
    }

    public static class WatchServiceOnSubscribe implements OnSubscribe<WatchEvent<?>> {

        private final WatchService watchService;
        private final AtomicBoolean closed = new AtomicBoolean(false);

        public WatchServiceOnSubscribe(WatchService watchService) {
            this.watchService = watchService;
        }

        @Override
        public void call(Subscriber<? super WatchEvent<?>> subscriber) {

            if (closed.get()) {
                subscriber.onError(new RuntimeException(
                        "WatchService closed. You can only subscribe once to a WatchService."));
                return;
            }
            subscriber.add(createSubscriptionToCloseWatchService(watchService, closed));

            try {
                if (subscriber.isUnsubscribed())
                    return;
                // get the first event before looping
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (ClosedWatchServiceException e) {
                    // must have unsubscribed
                    return;
                }
                while (key != null) {
                    if (subscriber.isUnsubscribed())
                        return;
                    // we have a polled event, now we traverse it and
                    // receive all the states from it
                    if (key != null) {
                        for (WatchEvent<?> event : key.pollEvents()) {
                            if (subscriber.isUnsubscribed())
                                return;
                            else
                                subscriber.onNext(event);
                        }

                        boolean valid = key.reset();
                        if (!valid) {
                            subscriber.onCompleted();
                            return;
                        }
                    }
                    try {
                        key = watchService.take();
                    } catch (ClosedWatchServiceException e) {
                        // must have unsubscribed
                        return;
                    }
                }
            } catch (InterruptedException e) {
                // ignore
            }
        }

    }

    private static Subscription createSubscriptionToCloseWatchService(
            final WatchService watchService, final AtomicBoolean closed) {
        return new Subscription() {

            @Override
            public void unsubscribe() {
                try {
                    watchService.close();
                    closed.set(true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public boolean isUnsubscribed() {
                return closed.get();
            }
        };
    }

    private static Func1<WatchService, Observable<WatchEvent<?>>> TO_WATCH_EVENTS = new Func1<WatchService, Observable<WatchEvent<?>>>() {

        @Override
        public Observable<WatchEvent<?>> call(WatchService watchService) {
            return from(watchService);
        }
    };

}
