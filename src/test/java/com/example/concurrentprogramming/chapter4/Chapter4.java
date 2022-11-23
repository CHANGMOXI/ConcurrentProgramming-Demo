package com.example.concurrentprogramming.chapter4;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author chenzhisheng
 * @date 2022/11/21 17:50
 **/
@SpringBootTest
public class Chapter4 {
    //Long型普通计数器
    private static Long count = 0L;
    //Long型原子计数器
    private static AtomicLong atomicLong = new AtomicLong();
    //创建数据源
    private static Integer[] arr1 = new Integer[]{0, 1, 2, 3, 0, 324, 0, 32, 4, 0, 24, -1, 0, 0, 23, 111};
    private static Integer[] arr2 = new Integer[]{0, 1, -1, 0, 0, 324, 0, 32, 4, 0, 24, -1, 0, 1, 23, 0};

    @Test
    void AtomicTest() throws InterruptedException {
        Thread threadOne = new Thread(() -> {
            for (int i = 0; i < arr1.length; i++) {
                if (arr1[i] == 0) {
                    count++;
                    atomicLong.incrementAndGet();
                }
            }
        });
        Thread threadTwo = new Thread(() -> {
            for (int i = 0; i < arr2.length; i++) {
                if (arr2[i] == 0) {
                    count++;
                    atomicLong.incrementAndGet();
                }
            }
        });

        threadOne.start();
        threadTwo.start();

        threadOne.join();
        threadTwo.join();

        System.out.println("普通计数器(需要加同步措施)" + "\t" + "count 0: " + count);
        System.out.println("原子计数器(不需要加同步措施)" + "\t" + "count 0: " + atomicLong.get());
    }
}
