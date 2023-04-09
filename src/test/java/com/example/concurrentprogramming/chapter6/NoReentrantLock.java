package com.example.concurrentprogramming.chapter6;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 基于AQS实现的不可重入的独占锁
 * 由于是不可重入的独占锁，这里自定义state的含义，state为0 表示锁没有被线程持有，state为1 表示锁已经被某个线程池有
 * 由于是不可重入，无需记录持有锁的线程的重入次数，另外，这个自定义的锁支持条件变量
 *
 * @author CZS
 * @create 2023-04-09 14:23
 **/
public class NoReentrantLock implements Lock, Serializable {
    /**
     * 创建一个内部类Sync对象进行具体的锁的操作（操作state值）
     */
    private final Sync sync = new Sync();

    @Override
    public void lock() {
        // 调用AQS的acquire(int arg)方法，实际调用Sync的自定义tryAcquire(int acquires)方法
        sync.acquire(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        // 调用AQS的acquireInterruptibly(int arg)方法，实际有调用Sync的自定义tryAcquire(int acquires)方法
        sync.acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        // 调用Sync的自定义tryAcquire(int acquires)方法
        return sync.tryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        // 调用AQS的tryAcquireNanos(int arg, long nanosTimeout)方法，实际有调用Sync的自定义tryAcquire(int acquires)方法
        return sync.tryAcquireNanos(1, unit.toNanos(time));
    }

    @Override
    public void unlock() {
        // 调用AQS的release(int arg)方法，实际调用Sync的自定义tryRelease(int releases)方法
        sync.release(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }

    public boolean isLocked() {
        return sync.isHeldExclusively();
    }

    /**
     * 内部帮助类，用来进行具体的锁的操作（操作state值）
     */
    private static class Sync extends AbstractQueuedSynchronizer {
        /**
         * 锁是否已经被持有
         *
         * @return
         */
        protected boolean isHeldExclusively() {
            // AQS的getState()方法
            return getState() == 1;
        }

        /**
         * 如果state为0，则尝试CAS获取锁
         *
         * @param acquires the acquire argument. This value is always the one
         *                 passed to an acquire method, or is the value saved on entry
         *                 to a condition wait.  The value is otherwise uninterpreted
         *                 and can represent anything you like.
         * @return
         */
        public boolean tryAcquire(int acquires) {
            assert acquires == 1;
            // AQS的CAS设置state方法，设置成功，说明当前线程获取到锁
            if (compareAndSetState(0, 1)) {
                // AQS的设置当前独占线程的方法
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        /**
         * 尝试释放锁，设置state为0
         *
         * @param releases the release argument. This value is always the one
         *                 passed to a release method, or the current state value upon
         *                 entry to a condition wait.  The value is otherwise
         *                 uninterpreted and can represent anything you like.
         * @return
         */
        protected boolean tryRelease(int releases) {
            assert releases == 1;
            if (getState() == 0) {
                throw new IllegalMonitorStateException();
            }
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        /**
         * 创建条件变量的接口
         *
         * @return
         */
        Condition newCondition() {
            return new ConditionObject();
        }
    }
}
