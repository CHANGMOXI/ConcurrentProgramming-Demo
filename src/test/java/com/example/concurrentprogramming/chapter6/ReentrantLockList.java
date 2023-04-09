package com.example.concurrentprogramming.chapter6;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于ReentrantLock的线程安全的List
 *
 * @author CZS
 * @create 2023-04-09 17:40
 **/
public class ReentrantLockList<E> {
    // 线程不安全的List
    private List<E> list = new ArrayList<>();
    // 独占锁
    private volatile ReentrantLock lock = new ReentrantLock();

    /**
     * 添加元素
     *
     * @param e
     */
    public void add(E e) {
        lock.lock();
        try {
            list.add(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 删除元素
     *
     * @param e
     */
    public void remove(E e) {
        lock.lock();
        try {
            list.remove(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取元素
     *
     * @param index
     * @return
     */
    public E get(int index) {
        lock.lock();
        try {
            return list.get(index);
        } finally {
            lock.unlock();
        }
    }
}
