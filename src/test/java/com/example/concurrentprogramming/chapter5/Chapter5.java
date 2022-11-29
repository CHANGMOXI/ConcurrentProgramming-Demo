package com.example.concurrentprogramming.chapter5;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author chenzhisheng
 * @date 2022/11/29 11:27
 **/
@SpringBootTest
public class Chapter5 {
    /**
     * 迭代器使用 弱一致性
     */
    @Test
    void iteratorTest() {
        CopyOnWriteArrayList<String> arrayList = new CopyOnWriteArrayList<>();
        arrayList.add("hello");
        arrayList.add("wenge");

        Iterator<String> iterator = arrayList.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }

    private static volatile CopyOnWriteArrayList<String> arrayList = new CopyOnWriteArrayList<>();

    /**
     * 多线程下迭代器的弱一致性
     * <p>
     * 如果迭代期间其他线程对该list进行了增删改操作，迭代器拿到的snapshot(数组)就是快照了
     * 增删改操作的新数组替换了旧数组，而迭代器拿到的是旧数组，此时迭代器迭代元素就对该list的增删改操作 不可见
     *
     * @throws InterruptedException
     */
    @Test
    void iteratorWeakConsistencyTest() throws InterruptedException {
        arrayList.add("hello");
        arrayList.add("wenge");
        arrayList.add("welcome");
        arrayList.add("to");
        arrayList.add("shenzhen");

        Thread threadOne = new Thread(() -> {
            //修改下标为1的元素为wg
            arrayList.set(1, "wg");
            //删除元素
            arrayList.remove(2);
            arrayList.remove(3);
        });

        //保证启动修改线程前获取迭代器
        Iterator<String> iterator = arrayList.iterator();

        threadOne.start();
        threadOne.join();

        //迭代元素
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }
}
