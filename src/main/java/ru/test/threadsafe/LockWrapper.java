package ru.test.threadsafe;

import ru.test.EntityLocker;
import ru.test.annotation.Id;
import ru.test.exceptions.BadConsumerException;
import ru.test.exceptions.BadEntityException;
import ru.test.exceptions.ThreadExecutionException;

import java.lang.reflect.Field;
import java.util.function.Consumer;

public class LockWrapper<V, T> {

    private final EntityLocker<T> entityLocker = new EntityLocker<>();

    public void withLockOnDo(V entity, Consumer<V> consumer)
            throws ThreadExecutionException, BadConsumerException, BadEntityException {
        withLockOnDo(entity, -1, consumer);
    }

    public void withLockOnDo(V entity, long timeToWait, Consumer<V> consumer)
            throws ThreadExecutionException, BadConsumerException, BadEntityException {
        if (consumer == null) {
            throw new BadConsumerException();
        }
        if (entity == null) {
            throw new BadEntityException();
        }

        T id = getIdToLock(entity);

        if ((timeToWait < 0 && entityLocker.lock(id)) || (entityLocker.lock(id, timeToWait))) {
            consumer.accept(entity);
        }

        entityLocker.unlock(id);
    }

    public void withGlobalLockOnDo(V entity, long millis, Consumer<V> consumer)
            throws ThreadExecutionException, BadConsumerException, BadEntityException {
        if (consumer == null) {
            throw new BadConsumerException();
        }
        if (entity == null) {
            throw new BadEntityException();
        }

        if (entityLocker.takeGlobalLock()) {
            consumer.accept(entity);
        }
        entityLocker.unlockGlobal();
    }

    private T getIdToLock(V entity) {
        try {
            for (Field field : entity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    return (T) field.get(entity);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Wrong Id field");
    }

}
