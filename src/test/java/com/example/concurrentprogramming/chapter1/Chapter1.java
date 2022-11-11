package com.example.concurrentprogramming.chapter1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author CZS
 * @create 2022-11-05 17:24
 **/
@SpringBootTest
public class Chapter1 {
    public static class MyThread extends Thread {
        @Override
        public void run() {
            System.out.println("I am a child thread.(extends Thread)");
        }
    }

    public static class RunnableTask implements Runnable {
        @Override
        public void run() {
            System.out.println("I am a child thread.(implements Runnable)");
        }
    }

    public static class CallerTask implements Callable<String> {
        @Override
        public String call() throws Exception {
            System.out.println("I am a child thread.(implements Callable<String>)");
            return "Return Value From Callable";
        }
    }

    /**
     * 三种线程创建方式
     */
    @Test
    void createThread() {
        //①继承 Thread类(无返回值)
        MyThread thread = new MyThread();
        thread.start();

        //②实现 Runnable接口(无返回值)
        RunnableTask task = new RunnableTask();
        new Thread(task).start();
        new Thread(task).start();

        //③使用 FutureTask 方式(有返回值)
        //使用CallerTask的实例创建一个FutureTask对象，然后使用该futureTask对象作为任务创建一个线程并启动
        FutureTask<String> futureTask = new FutureTask<>(new CallerTask());
        new Thread(futureTask).start();
        //最后通过 futureTask.get() 等待任务执行完毕并返回结果
        try {
            //等待线程执行完毕，并返回结果
            String returnValue = futureTask.get();
            System.out.println(returnValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //锁资源
    private static volatile Object resourceA = new Object();
    private static volatile Object resourceB = new Object();

    /**
     * Object.wait()
     * 以下 死锁例子 证明: 当前线程调用 某资源.wait()，当前线程阻塞，然后只会释放 某资源(当前共享对象) 的锁
     * ---> 当前线程持有的其他资源(其他共享对象) 的锁 并不会被释放
     *
     * @throws InterruptedException
     */
    @Test
    void waitAndDeadLock() throws InterruptedException {
        //线程A
        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //获取resourceA共享资源的监视器锁
                    synchronized (resourceA) {
                        System.out.println("threadA get resourceA lock");

                        //获取resourceB共享资源的监视器锁
                        synchronized (resourceB) {
                            System.out.println("threadA get resourceB lock");

                            //阻塞线程A，并释放获取到的resourceA的锁
                            System.out.println("threadA release resourceA lock");
                            resourceA.wait();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        //线程B
        Thread threadB = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //休眠1s，为了让线程A先获得resourceA和resourceB的锁
                    Thread.sleep(1000);

                    //获取resourceA共享资源的监视器锁
                    synchronized (resourceA) {
                        System.out.println("threadB get resourceA lock");

                        System.out.println("threadB try get resourceB lock...");

                        //获取resourceB共享资源的监视器锁
                        synchronized (resourceB) {
                            System.out.println("threadB get resourceB lock");

                            //阻塞线程B，并释放获取到的resourceA的锁
                            System.out.println("threadB release resourceA lock");
                            resourceA.wait();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        //启动线程
        threadA.start();
        threadB.start();
        //等待两个线程结束
        threadA.join();
        threadB.join();

        System.out.println("test over");
    }


    /**
     * Object.notify() 与 Object.notifyAll()
     *
     * @throws InterruptedException
     */
    @Test
    void notifyAndNotifyAll() throws InterruptedException {
        //线程A
        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                //获取resourceA共享资源的监视器锁
                synchronized (resourceA) {
                    System.out.println("threadA get resourceA lock");

                    try {
                        System.out.println("threadA begin wait");
                        resourceA.wait();
                        System.out.println("threadA end wait");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //线程B
        Thread threadB = new Thread(new Runnable() {
            @Override
            public void run() {
                //获取resourceA共享资源的监视器锁
                synchronized (resourceA) {
                    System.out.println("threadB get resourceA lock");

                    try {
                        System.out.println("threadB begin wait");
                        resourceA.wait();
                        System.out.println("threadB end wait");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //线程C
        Thread threadC = new Thread(new Runnable() {
            @Override
            public void run() {
                //获取resourceA共享资源的监视器锁
                synchronized (resourceA) {
                    System.out.println("threadC begin notify");
                    resourceA.notify();

//                    System.out.println("threadC begin notifyAll");
//                    resourceA.notifyAll();
                }
            }
        });

        //启动线程
        threadA.start();
        threadB.start();

        //主线程休眠1s，让线程A和线程B都执行到wait()之后再启动线程C
        Thread.sleep(1000);
        threadC.start();

        //等待线程结束
        threadA.join();
        threadB.join();
        threadC.join();

        System.out.println("test over");
    }


    /**
     * Thread.join(): 在线程A中调用 线程B.join() ---> 线程A阻塞，直到线程B执行结束
     * ---> 此时如果 线程C 调用 线程A.interrupt() ---> 线程A会抛出InterruptException异常 并 返回
     */
    @Test
    void join() {
        //线程1
        Thread threadOne = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("threadOne begin run!");
                for (; ; ) {

                }
            }
        });

        final Thread mainThread = Thread.currentThread();

        //线程2
        Thread threadTwo = new Thread(new Runnable() {
            @Override
            public void run() {
                //休眠1s
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //中断主线程
                mainThread.interrupt();
            }
        });

        //启动线程1
        threadOne.start();
        //启动线程2并且线程2先休眠1s
        threadTwo.start();

        //启动线程1和2之后，这里调用threadOne.join() ---> 当前主线程mainThread 阻塞等待 线程1 执行结束
        try {
            //在线程2中，会中断主线程，主线程就会在阻塞的这里抛出异常并返回
            threadOne.join();
        } catch (InterruptedException e) {
            System.out.println("main thread:" + e);
        }
    }


    //创建独占锁
    private static final Lock lock = new ReentrantLock();

    /**
     * Thread.sleep(long millis): 当前线程睡眠 millis ms，并且不会释放锁
     * ---> 睡眠线程 被 其他线程中断，睡眠线程会在 Thread.sleep()处 抛出InterruptException异常
     *
     * @throws InterruptedException
     */
    @Test
    void sleep() throws InterruptedException {
        //线程A
        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                //获取独占锁
                lock.lock();
                try {
                    System.out.println("child threadA sleep");
                    Thread.sleep(10000);
                    System.out.println("child threadA awake");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    //释放锁
                    lock.unlock();
                }
            }
        });
        //线程B
        Thread threadB = new Thread(new Runnable() {
            @Override
            public void run() {
                //获取独占锁
                lock.lock();
                try {
                    System.out.println("child threadB sleep");
                    Thread.sleep(10000);
                    System.out.println("child threadB awake");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    //释放锁
                    lock.unlock();
                }
            }
        });

        //启动线程
        threadA.start();
        threadB.start();

//        //主线程休眠2s
//        Thread.sleep(2000);
//        //主线程 中断 线程A
//        threadA.interrupt();

        threadA.join();
        threadB.join();
    }


    public class YieldTest implements Runnable {
        public YieldTest() {
            //创建并启动线程
            Thread thread = new Thread(this);
            thread.start();
        }

        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                //当i=0时让出CPU执行权，放弃时间片，进行下一轮调度
                if (i == 0) {
                    System.out.println(Thread.currentThread() + " yield CPU...");

                    //当前线程让出CPU执行权，放弃时间片，进行下一轮调度
                    Thread.yield();//开启或注释会有所不同
                }
            }

            System.out.println(Thread.currentThread() + " is over");
        }
    }

    /**
     * Thread.yield(): 当前调用 yield() 之后会让出 CPU 执行权，然后处于就绪状态
     * ---> 线程调度器 会从 线程就绪队列 里面获取一个线程优先级最高的线程
     * ---> 也有可能会调度到刚刚让出 CPU 的那个线程来获取 CPU 执行权
     */
    @Test
    void yield() {
        new YieldTest();
        new YieldTest();
        new YieldTest();
    }

    /**
     * Thread.sleep(long millis) 与 Thread.yield() 区别
     * 调用 sleep(): 当前线程阻塞指定时间，并且期间不会被调度
     * 调用 yield(): 当前线程让出自己剩余的时间片，但不会阻塞，而是处于就绪状态，有可能会在下一轮调度中被调度到
     */


    /**
     * 线程A.interrupt(): 中断某线程，实际上是设置线程A的 中断标志为true，实际线程A没有被中断，会继续执行
     * ---> 当线程A调用 wait、join、sleep方法而被阻塞挂起时，其他线程调用 线程A.interrupt()
     * ---> 线程A才会在调用 wait、join、sleep方法的地方 抛出InterruptException异常 并 返回 (有try-catch异常处理的话可以恢复到就绪状态，等待时间片继续执行)
     *
     * @throws InterruptedException
     */
    @Test
    void interrupt() throws InterruptedException {
        //子线程
        Thread childThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //如果当前子线程被中断则退出循环进而结束子线程
                //---> 根据中断标志判断 ---> isInterrupted(): 检测该方法的调用线程是否被中断(不会清除中断标志)
                //interrupted(): 检测当前线程(并非该方法的调用线程)是否被中断(会清除中断标志)
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.println(Thread.currentThread() + " hello");
                }
            }
        });
        //启动子线程
        childThread.start();

        //主线程休眠1s，以便中断前让子线程输出
        Thread.sleep(1000);

        //中断子线程
        System.out.println("mainThread interrupt childThread");
        childThread.interrupt();

        //等待子线程执行完毕
        childThread.join();
        System.out.println("test over");
    }

    /**
     * 线程A.interrupt() 使线程A 提前返回并等待时间片继续执行
     * 线程A调用 sleep、join、wait方法 而进入阻塞时，可以调用 线程A.interrupt() ---> 使线程A强制在 这些方法处 抛出InterruptException异常 并 返回
     * ---> 只要线程A有对 sleep、join、wait方法 进行 try-catch异常处理，就可以提前返回并恢复到就绪状态，等待时间片继续执行
     *
     * @throws InterruptedException
     */
    @Test
    void interruptWhileSleeping() throws InterruptedException {
        //子线程
        Thread childThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //对sleep、join、wait方法进行try-catch异常处理，该线程被中断后 就可以 提前 从这些方法处返回并等待时间片继续执行
                try {
                    System.out.println("childThread begin sleep for 20 seconds");
                    Thread.sleep(20000);
                    System.out.println("childThread awake");
                } catch (InterruptedException e) {
                    System.out.println("childThread is interrupted while sleeping");
                    return;
                }

                System.out.println("childThread-leaving normally");
            }
        });

        //启动线程
        childThread.start();
        //确保子线程进入休眠
        Thread.sleep(2000);

        //中断子线程的休眠，让子线程从sleep()处 提前返回并继续执行
        childThread.interrupt();

        //等待子线程执行完毕
        childThread.join();
        System.out.println("test over");
    }


    /**
     * 死锁产生的四个条件:
     * ①互斥条件: 一个资源同时只能被一个线程占用，其他线程请求获取 已被占用的资源 则 只能等待该资源被释放
     * ②请求并持有条件: 某线程因为请求获取 已被占用的资源 而阻塞，但阻塞的同时并不释放该线程自己已占用的资源
     * ③不可剥夺条件: 某线程自己占用的资源在自己使用完之前不能被其他线程抢占
     * ④环路等待条件: 发生死锁时，必然存在一个 线程——资源 的环形链
     * ---> 即线程集合{T0,T1,T2,...,Tn}，T0等待T1占用的资源 T1等待T2占用的资源 ... Tn等待T0占用的资源
     *
     * @throws InterruptedException
     */
    @Test
    void deadLockCondition() throws InterruptedException {
        /********************************************* ①互斥条件 *********************************************/
        /** resourceA、resourceB都是互斥资源，resourceA被线程A获取后，在线程A释放之前，线程B尝试获取resourceA会被阻塞 **/

        //线程A
        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                //获取resourceA的锁
                /******************************** ③不可剥夺条件 ********************************/
                /** 线程A 获取到 resourceA 之后，在自己主动释放 resourceA 之前，都不会被 线程B 抢占 **/
                synchronized (resourceA) {
                    System.out.println(Thread.currentThread() + "(threadA) get resourceA");

                    //让其他线程能获取到锁
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.out.println(Thread.currentThread() + "(threadA) waiting get resourceB");
                    //进一步获取resourceB的锁
                    /*********************************************** ②请求并持有条件 ***********************************************/
                    /** 线程A获取到resourceA之后，尝试获取 已被占用线程B占用的resourceB，因此进入阻塞等待，同时并没有释放自己占用的resourceA **/
                    synchronized (resourceB) {
                        System.out.println(Thread.currentThread() + "(threadA) get resourceB");
                    }

                    System.out.println(Thread.currentThread() + "(threadA) over");
                }
            }
        });
        //线程B
        Thread threadB = new Thread(new Runnable() {
            @Override
            public void run() {
                //获取resourceB的锁
                /******************************** ③不可剥夺条件 ********************************/
                /** 线程B 获取到 resourceB 之后，在自己主动释放 resourceB 之前，都不会被 线程A 抢占 **/
                synchronized (resourceB) {
                    System.out.println(Thread.currentThread() + "(threadB) get resourceB");

                    //让其他线程能获取到锁
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.out.println(Thread.currentThread() + "(threadB) waiting get resourceA");
                    //进一步获取resourceA的锁
                    /*********************************************** ②请求并持有条件 ***********************************************/
                    /** 线程B获取到resourceB之后，尝试获取 已被占用线程A占用的resourceA，因此进入阻塞等待，同时并没有释放自己占用的resourceB **/
                    synchronized (resourceA) {
                        System.out.println(Thread.currentThread() + "(threadB) get resourceA");
                    }

                    System.out.println(Thread.currentThread() + "(threadB) over");
                }
            }
        });

        /********************************** ④环路等待条件 **********************************/
        /** 线程A 持有 resourceA 等待获取 resourceB。线程B 持有 resourceB 等待获取 resourceA **/

        //启动线程
        threadA.start();
        threadB.start();

        threadA.join();
        threadB.join();
    }

    /**
     * 避免死锁: 破坏死锁产生的至少一个条件 ---> 目前只能破坏 请求并持有条件、环路等待条件
     * ---> 通过 资源申请的有序性 破坏这两个条件
     *
     * @throws InterruptedException
     */
    @Test
    void destroyDeadLock() throws InterruptedException {
        //线程A
        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                //获取resourceA的锁
                synchronized (resourceA) {
                    System.out.println(Thread.currentThread() + "(threadA) get resourceA");

                    //让其他线程能获取到锁
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.out.println(Thread.currentThread() + "(threadA) waiting get resourceB");
                    //进一步获取resourceB的锁
                    synchronized (resourceB) {
                        System.out.println(Thread.currentThread() + "(threadA) get resourceB");
                    }

                    System.out.println(Thread.currentThread() + "(threadA) over");
                }
            }
        });
        //线程B
        Thread threadB = new Thread(new Runnable() {
            @Override
            public void run() {
                /************************* 破坏死锁 ---> 资源申请的有序性 *************************/
                /**** 线程B获取资源的顺序 和 线程A 保持一致 ---> 按照 resourceA、resourceB 的顺序 ****/
                /**** 假如线程A、线程B 都需要 resourceA、resourceB、resourceC、 ... 、resourceN ****/
                /** 统一获取资源的顺序 ---> 所有线程 只有在获取了resourceN-1 才能去尝试获取 resourceN **/

                //获取resourceA的锁
                synchronized (resourceA) {
                    System.out.println(Thread.currentThread() + "(threadB) get resourceA");

                    //让其他线程能获取到锁
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.out.println(Thread.currentThread() + "(threadB) waiting get resourceB");
                    //进一步获取resourceB的锁
                    synchronized (resourceB) {
                        System.out.println(Thread.currentThread() + "(threadB) get resourceB");
                    }

                    System.out.println(Thread.currentThread() + "(threadB) over");
                }
            }
        });

        /**
         * 线程A 和 线程B 同时执行到了 synchronized(resourceA)，这时只有一个线程可以获取到 resourceA
         * ---> 假如 线程A 获取到了，则 线程B 就会被阻塞而不会再去获取 resourceB
         * 线程A 获取到 resourceA 之后，会去获取 resourceB，此时 线程A 可以获取到
         * ---> 线程A 获取到 resourceB 并使用后会 先释放对 resourceB 的持有，再释放对 resourceA 的持有
         * ---> 线程A 释放 resourceA 之后，线程B 才会结束阻塞状态 并可以获取到 resourceA
         */

        //启动线程
        threadA.start();
        threadB.start();

        threadA.join();
        threadB.join();
    }


    //(1)自定义print方法
    static void print(String str) {
        //1.1 打印当前线程本地内存中localVariable变量的值
        System.out.println(str + ": " + localVariable.get());
        //1.2 清除当前线程本地内存中的localVariable变量
        localVariable.remove();
    }

    //(2)创建ThreadLocal变量
    static ThreadLocal<String> localVariable = new ThreadLocal<>();

    @Test
    void threadLocal() {
        //(3)创建线程A
        Thread threadA = new Thread(() -> {
            //3.1 设置线程A中本地变量localVariable的值
            localVariable.set("threadA local variable");
            //3.2 调用打印方法
            print("threadA");
            //3.3 清除本地变量后，打印本地变量值
            System.out.println("threadA after remove: " + localVariable.get());
        });
        //(4)创建线程B
        Thread threadB = new Thread(() -> {
            //4.1 设置线程B中本地变量localVariable的值
            localVariable.set("threadB local variable");
            //4.2 调用打印方法
            print("threadB");
            //4.3 清除本地变量后，打印本地变量值
            System.out.println("threadB after remove: " + localVariable.get());
        });
        //启动线程
        threadA.start();
        threadB.start();
    }


    //①创建线程变量
    //ThreadLocal 不支持 继承性
    public static ThreadLocal<String> threadLocal = new ThreadLocal<>();
    //InheritableThreadLocal 支持 继承性
    public static ThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<>();

    /**
     * ThreadLocal类 与 InheritableThreadLocal类
     * ThreadLocal 不支持 继承性 ---> 同一个ThreadLocal变量在父线程(主线程)中被set()之后，在子线程中是获取get()不到的
     * InheritableThreadLocal 支持 继承性 ---> 子线程可以访问在父线程中set()的本地变量
     */
    @Test
    void InheritableThreadLocal() {
        //②设置父线程变量
        threadLocal.set("threadLocal hello world");
        inheritableThreadLocal.set("inheritableThreadLocal hello world");

        //③启动子线程
        Thread childThread = new Thread(() -> {
            //④子线程输出线程变量的值
            System.out.println("childThread threadLocal: " + threadLocal.get());
            System.out.println("childThread inheritableThreadLocal: " + inheritableThreadLocal.get());
        });
        childThread.start();

        //⑤父线程输出线程变量的值
        System.out.println("parentThread threadLocal: " + threadLocal.get());
        System.out.println("parentThread inheritableThreadLocal: " + inheritableThreadLocal.get());
    }
}
