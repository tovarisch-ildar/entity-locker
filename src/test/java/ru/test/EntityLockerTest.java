package ru.test;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class EntityLockerTest {

    EntityLocker<String> entityLocker = new EntityLocker<>();

    @Test
    public void testHundredPercentLock() throws ExecutionException, InterruptedException {
        String id = "1";
        CompletableFuture<Void> futures = CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> testMethod(id)),
                CompletableFuture.runAsync(() -> testMethod(id))
        );
        futures.get();
    }

    private void testMethod(String id) {
        entityLocker.lock(id);
        sleepAndSOUT(id);
        entityLocker.unlock(id);
    }

    @Test
    public void testWaitTimeLock() throws ExecutionException, InterruptedException {
        String id = "1";
        long timeToWait = 1000L;
        CompletableFuture<Void> futures = CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> testMethod(id)),
                CompletableFuture.runAsync(() -> testMethod(id, timeToWait))
        );
        futures.get();
    }

    private void testMethod(String id, long wait) {
        if (entityLocker.lock(id, wait)) {
            sleepAndSOUT(id);
        }
        entityLocker.unlock(id);
    }

    private void sleepAndSOUT(String testObject) {
        try {
            System.out.println(testObject + " in on " + Thread.currentThread().getName());
            Thread.sleep(3000L);
            System.out.println(testObject + " out on " + Thread.currentThread().getName());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}