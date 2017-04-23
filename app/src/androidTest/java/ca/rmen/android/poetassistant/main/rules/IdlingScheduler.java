package ca.rmen.android.poetassistant.main.rules;

import android.support.test.espresso.idling.CountingIdlingResource;

import java.util.concurrent.TimeUnit;

import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;

// copy/paste/adapt from https://github.com/ReactiveX/RxAndroid/issues/149
class IdlingScheduler extends Scheduler {

    private final CountingIdlingResource countingIdlingResource;

    private final Scheduler scheduler;

    IdlingScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        String resourceName = scheduler.getClass().getSimpleName() + scheduler.hashCode();
        countingIdlingResource = new CountingIdlingResource(resourceName, true);
    }

    @Override
    public Worker createWorker() {
        return new IdlingWorker(scheduler.createWorker());
    }

    CountingIdlingResource countingIdlingResource() {
        return countingIdlingResource;
    }

    private class IdlingWorker extends Worker {

        private final Worker worker;
        private boolean recursive;

        IdlingWorker(Worker worker) {
            this.worker = worker;
        }


        @Override
        public Disposable schedule(Runnable runnable) {
            return recursive ?
                    worker.schedule(runnable) :
                    worker.schedule(decorateRunnable(runnable));
        }

        @Override
        public Disposable schedule(Runnable runnable, long delayTime, TimeUnit unit) {
            return recursive ?
                    worker.schedule(runnable, delayTime, unit) :
                    worker.schedule(decorateRunnable(runnable), delayTime, unit);
        }

        @Override
        public Disposable schedulePeriodically(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
            recursive = true;
            return worker.schedulePeriodically(decorateRunnable(runnable), initialDelay, period, unit);
        }


        @Override
        public void dispose() {
            worker.dispose();
        }

        @Override
        public boolean isDisposed() {
            return worker.isDisposed();
        }

        private Runnable decorateRunnable(Runnable runnable) {
            return () -> {
                countingIdlingResource.increment();
                try {
                    runnable.run();
                } finally {
                    countingIdlingResource.decrement();
                }
            };
        }
    }
}
