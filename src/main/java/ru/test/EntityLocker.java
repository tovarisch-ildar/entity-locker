package ru.test;

import ru.test.exceptions.ThreadExecutionException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class EntityLocker<I> {
    private final Map<I, ReentrantLock> locksMap = new ConcurrentHashMap<>();

    private final ReentrantLock globalLock = new ReentrantLock();

    public boolean lock(I entityId) {
        getLock(entityId).lock();
        return true;
    }

    public boolean lock(I entityId, long millis) {
        if (globalLock.isLocked()) {
            return false;
        }
        ReentrantLock lock = getLock(entityId);
        try {
            return lock.tryLock(millis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
//            I don't like Handled exception because they force you to handle exception in lambdas.
//            If you want to handle exception check method signature and add try/catch block. python style
            throw new ThreadExecutionException(e);
        }
    }

    public boolean takeGlobalLock() {
        globalLock.lock();
        releaseAllLocks();
        return true;
    }

    private void releaseAllLocks() {
        locksMap.values().forEach(ReentrantLock::lock);
        locksMap.keySet().forEach(this::unlock);
    }

    public void unlockGlobal() {
        if (!globalLock.isHeldByCurrentThread()) {
            return;
        }
        globalLock.unlock();
    }

    public void unlock(I entityId) {
        ReentrantLock lock = locksMap.get(entityId);
        if (lock != null) {
            if (!lock.isHeldByCurrentThread()) {
                return;
            }

            if (!lock.hasQueuedThreads()) {
                locksMap.remove(entityId);
            }
            lock.unlock();
        }
    }

    private ReentrantLock getLock(I entityId) {
        return locksMap.computeIfAbsent(entityId, id -> new ReentrantLock());
    }
}
