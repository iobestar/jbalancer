package io.github.jbalancer.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

    private final AtomicInteger index = new AtomicInteger(0);
    private final String nameFormat;
    private final ThreadFactory backingThreadFactory = Executors.defaultThreadFactory();

    private NamedThreadFactory(String nameFormat) {
        this.nameFormat = nameFormat;
    }

    public static NamedThreadFactory create(String nameFormat) {
        return new NamedThreadFactory(nameFormat);
    }

    @Override
    public Thread newThread(Runnable runnable) {

        Thread thread = backingThreadFactory.newThread(runnable);
        if (nameFormat != null) {
            thread.setName(String.format(nameFormat, index.getAndIncrement()));
        }
        thread.setDaemon(false);
        return thread;
    }
}
