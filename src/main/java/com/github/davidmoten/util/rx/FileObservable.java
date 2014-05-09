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

public class FileObservable {

    /**
     * Returns an {@link Observable} that uses NIO WatchService (and a dedicated
     * thread) to push modify events to an observable that reads and reports new
     * lines to a subscriber. The NIO WatchService events are sampled according
     * to <code>sampleTimeMs</code> so that lots of discrete activity on a file
     * (for example a log file with very frequent entries) does not prompt an
     * inordinate number of file reads to pick up changes.
     * 
     * @param file
     * @param startPosition
     * @param sampleTimeMs
     * @return
     */
    public static Observable<String> tailFile(File file, long startPosition, long sampleTimeMs) {
        return new FileTailer(file, startPosition).tail(sampleTimeMs);
    }

    /**
     * Returns an {@link Observable} of {@link WatchEvent}s from a
     * {@link WatchService}.
     * 
     * @param watchService
     * @return
     */
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

    /**
     * Creates a {@link WatchService} on subscribe for the given file and event
     * kinds.
     * 
     * @param file
     * @param kinds
     * @return
     */
    @SafeVarargs
    public static Observable<WatchService> watchService(final File file, final Kind<Path>... kinds) {
        return Observable.create(new OnSubscribe<WatchService>() {

            @Override
            public void call(Subscriber<? super WatchService> subscriber) {
                final Path path = getBasePath(file);
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

    private static Path getBasePath(final File file) {
        final Path path;
        if (file.exists() && file.isDirectory())
            path = Paths.get(file.toURI());
        else
            path = Paths.get(file.getParentFile().toURI());
        return path;
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
                        Path basePath = getBasePath(file);
                        File pFile = new File(basePath.toFile(), p.toString());
                        ok = pFile.getAbsolutePath().equals(file.getAbsolutePath());
                    } else
                        ok = false;
                }
                return ok;
            }
        };
    }

    static class WatchServiceOnSubscribe implements OnSubscribe<WatchEvent<?>> {

        private final WatchService watchService;
        private final AtomicBoolean unsubscribed = new AtomicBoolean(false);

        WatchServiceOnSubscribe(WatchService watchService) {
            this.watchService = watchService;
        }

        @Override
        public void call(Subscriber<? super WatchEvent<?>> subscriber) {

            if (unsubscribed.get()) {
                subscriber.onError(new RuntimeException(
                        "WatchService closed. You can only subscribe once to a WatchService."));
                return;
            }
            subscriber.add(createSubscriptionToCloseWatchService(watchService, unsubscribed,
                    subscriber));

            // get the first event before looping
            WatchKey key = nextKey(subscriber);

            while (key != null) {
                if (subscriber.isUnsubscribed())
                    return;
                // we have a polled event, now we traverse it and
                // receive all the states from it
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (subscriber.isUnsubscribed())
                        return;
                    else
                        subscriber.onNext(event);
                }

                boolean valid = key.reset();
                if (!valid && !unsubscribed.get()) {
                    subscriber.onCompleted();
                    return;
                } else if (!valid)
                    return;

                key = nextKey(subscriber);
            }
        }

        private WatchKey nextKey(Subscriber<? super WatchEvent<?>> subscriber) {
            try {
                // this command blocks but unsubscribe close the watch
                // service and interrupt it
                return watchService.take();
            } catch (ClosedWatchServiceException e) {
                // must have unsubscribed
                if (!unsubscribed.get())
                    subscriber.onCompleted();
                return null;
            } catch (InterruptedException e) {
                subscriber.onError(e);
                try {
                    watchService.close();
                } catch (IOException e1) {

                }
                return null;
            }
        }

    }

    private static Subscription createSubscriptionToCloseWatchService(
            final WatchService watchService, final AtomicBoolean unsubscribed,
            final Subscriber<? super WatchEvent<?>> subscriber) {
        return new Subscription() {

            @Override
            public void unsubscribe() {
                try {
                    watchService.close();
                    unsubscribed.set(true);
                } catch (ClosedWatchServiceException e) {
                    // do nothing
                    unsubscribed.set(true);
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }

            @Override
            public boolean isUnsubscribed() {
                return unsubscribed.get();
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
