package com.demo.hystrix.test.isolation;

import com.netflix.hystrix.*;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * 测试信号量隔离策略
 * 默认执行run()用的是主线程，为了模拟并行执行command，这里我们自己创建多个线程来执行command
 * 设置ExecutionIsolationSemaphoreMaxConcurrentRequests为3，意味着信号量最多允许执行run的并发数为3，超过则触发降级，即不执行run而执行getFallback
 * 设置FallbackIsolationSemaphoreMaxConcurrentRequests为1，意味着信号量最多允许执行fallback的并发数为1，超过则抛异常fallback execution rejected
 */
public class HystrixCommand4SemaphoreTest extends HystrixCommand<String> {

    private final String name;

    public HystrixCommand4SemaphoreTest(String name) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("SemaphoreTestGroup"))
                        .andCommandKey(HystrixCommandKey.Factory.asKey("SemaphoreTestKey"))
                        .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("SemaphoreTestThreadPoolKey"))
                        .andCommandPropertiesDefaults(    // 配置信号量隔离
                                HystrixCommandProperties.Setter()
                                        // 信号量隔离
                                        .withExecutionIsolationStrategy(ExecutionIsolationStrategy.SEMAPHORE)
                                        .withExecutionIsolationSemaphoreMaxConcurrentRequests(3)
                                        .withFallbackIsolationSemaphoreMaxConcurrentRequests(1)
                        )
        );
        this.name = name;
    }

    @Override
    protected String run() throws Exception {
        return "run(): name=" + name + "，线程名是" + Thread.currentThread().getName();
    }

    @Override
    protected String getFallback() {
        return "getFallback(): name=" + name + "，线程名是" + Thread.currentThread().getName();
    }

    public static class UnitTest {

        @Test
        public void testSynchronous() throws IOException, InterruptedException {

            try {
                Thread.sleep(2000);
                for (int i = 0; i < 5; i++) {
                    final int j = i;
                    // 自主创建线程来执行command，创造并发场景
                    Thread thread = new Thread(() -> {
                        // 这里执行两类command：HystrixCommand4SemaphoreTest设置了信号量隔离、HelloWorldHystrixCommand未设置信号量
                        System.out.println("===========" + new HystrixCommand4SemaphoreTest("HystrixCommand4SemaphoreTest" + j).execute());    // 被信号量拒绝的线程从这里抛出异常

                    });
                    thread.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(4000);
        }
    }

}