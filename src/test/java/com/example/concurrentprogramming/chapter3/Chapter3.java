package com.example.concurrentprogramming.chapter3;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author CZS
 * @create 2022-11-19 23:23
 **/
@SpringBootTest
public class Chapter3 {
    @Test
    void RandomTest() {
        //创建一个默认种子的随机数生成器
        Random random = new Random();
        //输出5个0~5(包含0不包含5)之间的随机数
        for (int i = 0; i < 5; i++) {
            System.out.println(random.nextInt(5));
        }
    }

    @Test
    void ThreadLocalRandomTest() {
        //获取一个随机数生成器
        ThreadLocalRandom random = ThreadLocalRandom.current();
        //输出5个0~5(包含0不包含5)之间的随机数
        for (int i = 0; i < 5; i++) {
            System.out.println(random.nextInt(5));
        }
    }
}
