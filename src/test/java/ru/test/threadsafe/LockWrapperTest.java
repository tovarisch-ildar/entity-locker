package ru.test.threadsafe;

import org.junit.jupiter.api.Test;
import ru.test.models.FirstClass;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class LockWrapperTest {
    LockWrapper<FirstClass, String> underTest = new LockWrapper<>();

    @Test
    public void testNoOrNegativeWaitTime() throws InterruptedException, ExecutionException {
        long timeToWait = -1L;
        testMethod(timeToWait);
    }

    @Test
    public void testPositiveWaitTime() throws InterruptedException, ExecutionException {
        long timeToWait = 1500L;
        testMethod(timeToWait);
    }

    private void testMethod(long timeToWait) throws InterruptedException, ExecutionException {
        FirstClass one = new FirstClass("1");
        FirstClass two = new FirstClass("2");


        CompletableFuture<Void> firstExecution = CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> underTest.withLockOnDo(one, timeToWait, this::sleepAndSOUT)),
                CompletableFuture.runAsync(() -> underTest.withLockOnDo(one, timeToWait, this::sleepAndSOUT)),
                CompletableFuture.runAsync(() -> underTest.withLockOnDo(one, timeToWait, this::sleepAndSOUT))
        );

        CompletableFuture<Void> secondExecution = CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> underTest.withLockOnDo(two, timeToWait, this::sleepAndSOUT)),
                CompletableFuture.runAsync(() -> underTest.withLockOnDo(two, timeToWait, this::sleepAndSOUT)),
                CompletableFuture.runAsync(() -> underTest.withLockOnDo(two, timeToWait, this::sleepAndSOUT))
        );

        firstExecution.get();
        secondExecution.get();
    }

    private void sleepAndSOUT(FirstClass testObject) {
        try {
            System.out.println(testObject + " in on " + Thread.currentThread().getName());
            Thread.sleep(1000L);
            System.out.println(testObject + " out on " + Thread.currentThread().getName());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}