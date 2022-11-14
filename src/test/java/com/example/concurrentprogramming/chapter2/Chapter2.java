package com.example.concurrentprogramming.chapter2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author CZS
 * @create 2022-11-13 12:03
 **/
@SpringBootTest
public class Chapter2 {
    //共享变量value线程不安全
    public class ThreadNotSafeInteger {
        private Integer value;

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }

    /**
     * 解决内存可见性问题 方式一: 使用 synchronized 关键字进行同步
     * <p>
     * 劣势: 独占锁，同时只能有一个线程调用get()方法，其他调用线程会被阻塞，同时存在线程上下文切换和线程重新调度的开销
     */
    public class ThreadSafeIntegerUsingSynchronized {
        private Integer value;

        public synchronized Integer getValue() {
            return value;
        }

        public synchronized void setValue(Integer value) {
            this.value = value;
        }
    }

    /**
     * 解决内存可见性问题 方式二: 使用 volatile 关键字进行同步
     * 非阻塞算法，不会造成线程上下文切换的开销
     * <p>
     * 劣势: 虽然提供了可见性保证，但并不保证操作的原子性
     */
    public class ThreadSafeIntegerUsingVolatile {
        private volatile Integer value;

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }


    private static int num = 0;
    private static boolean ready = false;
//    private static volatile boolean ready = false;

    /**
     * 多线程下 指令重排序 可能会出现的问题
     *
     * 解决: 使用 volatile 修饰 ready，避免指令重排序和内存可见性问题
     * @throws InterruptedException
     */
    @Test
    void InstructionReorderingProblem() throws InterruptedException {
        //读线程
        Thread readThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                /** 有可能在 写线程 执行④之后，在执行③之前，读线程 ①已经执行了 **/
                /** 同时有可能在 写线程 执行③之前，读线程 就开始执行② **/
                /** 导致输出 0 而不是 4 **/
                if (ready) {//①
                    System.out.println(num + num);//②
                }
                System.out.println("readThread read over");
            }
        });
        //写线程
        Thread writeThread = new Thread(() -> {
            /** 有可能会发生指令重排序，先执行④再执行③ **/
            num = 2;//③
            ready = true;//④
            System.out.println("writeThread set over");
        });

        readThread.start();
        writeThread.start();

        Thread.sleep(10);
        writeThread.interrupt();
        System.out.println("mainThread over");
    }
}
