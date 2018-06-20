package com.demo.hystrix.test.isolation;

import com.netflix.hystrix.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 *
 * 测试线程池隔离策略
 * 设置线程池里的线程数＝3，然后循环>3次和<3次，最后查看当前所有线程名称
 * 
 */
public class HystrixCommand4ThreadPoolTest extends HystrixCommand<String> {

    private final String name;

    public HystrixCommand4ThreadPoolTest(String name) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ThreadPoolTestGroup"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("testCommandKey"))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("ThreadPoolTest"))
                .andCommandPropertiesDefaults(
                	HystrixCommandProperties.Setter()
                		.withExecutionTimeoutInMilliseconds(5000)
                )
                .andThreadPoolPropertiesDefaults(
                	HystrixThreadPoolProperties.Setter()
                		.withCoreSize(3)	// 配置线程池里的线程数
                )
        );
        this.name = name;
    }

    @Override
    protected String run() throws Exception {
    	//模拟耗时操作
    	TimeUnit.MILLISECONDS.sleep(2000);
		System.out.println(Thread.currentThread().getName() + ": " + name);
		return name;
    }

    @Override
    protected String getFallback() {
        return "fallback: " + name;
    }

    public static class UnitTest {

        @Test
        public void testSynchronous() throws IOException {
        	for(int i = 0; i < 3; i++) {
	        	try {
//	        		assertEquals("fallback: Hlx", new HystrixCommand4ThreadPoolTest("Hlx").execute());
//	        		System.out.println("===========" + new HystrixCommand4ThreadPoolTest("Hlx").execute());
					//占有线程池中的线程
	        		Future<String> future = new HystrixCommand4ThreadPoolTest("get available thread" + i).queue();
	        		//强制阻塞
//					System.out.println("future返回：" + future.get());
	        	} catch(Exception e) {
	        		System.out.println("run()抛出HystrixBadRequestException时，被捕获到这里" + e.getCause());
	        	}
        	}
        	for(int i = 0; i < 10; i++) {
	        	try {
	        		System.out.println("===========" + new HystrixCommand4ThreadPoolTest(" not get available thread" + i).execute());
	        	} catch(Exception e) {
	        		System.out.println("run()抛出HystrixBadRequestException时，被捕获到这里" + e.getCause());
	        	}
        	}
        	try {
        		TimeUnit.MILLISECONDS.sleep(2000);
        	}catch(Exception e) {}
        	System.out.println("------开始打印现有线程---------");
        	Map<Thread, StackTraceElement[]> map=Thread.getAllStackTraces();
        	for (Thread thread : map.keySet()) {
				System.out.println(thread.getName());
			}
        	System.out.println(map);
        	System.out.println("thread num: " + map.size());
        }
    }

}