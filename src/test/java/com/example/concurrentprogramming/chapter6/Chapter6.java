package com.example.concurrentprogramming.chapter6;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.locks.LockSupport;

/**
 * @author chenzhisheng
 * @date 2022/11/29 17:26
 **/
@SpringBootTest
public class Chapter6 {
    /**
     * 默认情况下调用LockSupport类的方法的线程是不持有与LockSupport类关联的许可证的
     * <p>
     * 调用park()方法的线程如果已经拿到许可证，则调用LockSupport.park()时会立刻返回
     * 否则会该线程会被阻塞
     *
     * @throws InterruptedException
     */
    @Test
    void parkTest() throws InterruptedException {
        Thread childThread = new Thread(() -> {
            System.out.println("childThread begin park!");

            LockSupport.park();

            System.out.println("childThread end park!");
        });

        System.out.println("mainThread begin!");

        childThread.start();
        Thread.sleep(2000);

        //因调用park方法而阻塞的线程被其他线程(主线程)中断而返回时，不会抛出InterruptedException异常
        childThread.interrupt();
        childThread.join();

        System.out.println("mainThread end!");
    }

    /**
     * 对于调用unpark方法传入的参数thread线程
     * <p>
     * 如果thread没有许可证，则让thread持有
     * 如果thread因调用park方法而被阻塞，则唤醒thread
     * 如果thread没有调用过park方法，则调用unpark方法后再调用park方法，会立刻返回
     *
     * @throws InterruptedException
     */
    @Test
    void parkAndUnparkTest() throws InterruptedException {
        Thread childThread = new Thread(() -> {
            System.out.println("childThread begin park!");

            //调用park方法，阻塞自己
            LockSupport.park();
            /** park方法返回时不会告诉我们返回的原因，可以加上检查条件，不满足则继续调用park方法 **/
            //比如通过判断中断状态，只有在调用了当前线程.interrupt()方法，被中断后才返回(退出循环不再继续调用park方法)
//            while (!Thread.currentThread().isInterrupted()){
//                LockSupport.park();
//            }

            System.out.println("childThread unpark!");
        });

        childThread.start();
        //让子线程先阻塞
        Thread.sleep(2000);

        System.out.println("mainThread begin unpark!");
        //调用unpark方法让 childThread子线程 持有许可证，之后子线程就会从调用的park方法中返回了
        LockSupport.unpark(childThread);
//        childThread.interrupt();

        childThread.join();
    }
}
