package com.atguigu.lock.test;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * 4中
 * 1）、继承Thread
 * 2）、实现Runnable
 * 无返回值
 * <p>
 * 3）、实现Callable
 * 4）、实现Future
 * 有返回值
 */
public class ThreadMainTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> price = CompletableFuture.supplyAsync(() -> {
            return 800;
        });

        CompletableFuture<String> coupon = CompletableFuture.supplyAsync(() -> {
            return "满1000减100";
        });

        CompletableFuture<String> baseAttr = CompletableFuture.supplyAsync(() -> {
            return "黑色，128G";
        });


        CompletableFuture<Void> future = CompletableFuture.allOf(price, coupon, baseAttr);

        Void aVoid = future.get();

        Integer integer = price.get();

    }


    public void main222(String[] args) throws Exception {
        System.out.println("=============");
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            return UUID.randomUUID().toString();
        }).thenApply((t)->{
            //thenApply干预异步任务的结果
            System.out.println("第一个参数："+t);
            return t.replace("-","");
        }).thenApply((t)->{
            return t.substring(0,5);
        });

        //thenAccept和thenRun不会干预异步任务的结果
        future.thenAccept((t)->{
            System.out.println("将"+t+"保存到数据库");
        });
        future.thenRun(()->{
            System.out.println("日志记录干完了....");
        });
        //========================================================

        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            return 1;
        });

        CompletableFuture<String> combine = future.thenCombine(future2, (t, u) -> {
            System.out.println("第一个异步任务的返回值：" + t);
            System.out.println("第二个异步任务的返回值：" + u);
            return t + "--->" + u;
        });


//        String s1 = future.get();
//        Integer integer = future2.get();
        String s1 = combine.get();

        String s = future.get();
        System.out.println("异步任务1的返回值："+s);
        System.out.println("总任务返回值："+s1);

        //合并两个异步任务的结果

//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("int i = 10 / 0");
//            int i = 10 / 0;
//            return i;
//        }).whenComplete((result,ex)->{
//            System.out.println("方法的结果："+result);
//            System.out.println("方法的异常："+ex);
//        });


//        future.exceptionally((e)->{
//            System.out.println("异常个信息是："+e.getMessage());
//            return ;
//        });
//        System.out.println("=============");


    }

    /**
     * 一个异步任务不应该由Thread.start()起来；
     * <p>
     * 应该用线程池控制住所有的线程
     *
     * @param args
     */
    public void mainTest222(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        List<Future<Integer>> futures = new ArrayList<>();
        System.out.println("主线程开始" + Thread.currentThread().getId());
        //给线程池提交任务。
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            Future<Integer> submit = executorService.submit(() -> {
                Thread.sleep(2000);
                System.out.println("提交的任务执行." + finalI + "==》" + Thread.currentThread().getId());
                return finalI;
            });

            futures.add(submit);

        }




        Future<Integer> future = futures.get(0);
        System.out.println("第一个任务的结果："+future.get());

        System.out.println("主线程结束" + Thread.currentThread().getId());

        System.out.println("11111111");
        try {
            new Thread(()->{
                int i = 10/0;
            }).start();
        }catch (Exception e){
            System.out.println("错误。。。"+e.getMessage());
        }

        System.out.println("22222222");

//        Future<Integer> future = futures.get(0);
//        Integer integer = future.get();
//
//        System.out.println("第一个任务的结果："+integer);

    }

    public void mainTest(String[] args) throws Exception {
        System.out.println("主线程开始" + Thread.currentThread().getId());

//        new Thread(new HelloThread()).start();


        FutureTask<Object> task = new FutureTask<>(() -> {
            Thread.sleep(3000);
            System.out.println("FutureTask线程.." + Thread.currentThread().getId());
            return 10;
        });

        Thread thread = new Thread(task);
        thread.start();


//
//        System.out.println("最终的结果"+o);
        //在线等结果
        Object o = task.get();


        System.out.println("主线程技术" + Thread.currentThread().getId());
    }


}

class HelloThread implements Runnable {

    @Override
    public void run() {
        int i = 1 + 1;
        System.out.println("副线程：" + Thread.currentThread().getId());
    }
}


