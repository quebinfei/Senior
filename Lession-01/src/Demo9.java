import java.util.List;
import java.util.concurrent.*;

public class Demo9 {

    public static void main (String[] args) throws Exception {
        Demo9 d = new Demo9();

        //        d.threadPoolExecutorTest1();
//        d.threadPoolExecutorTest2();
//        d.threadPoolExecutorTest3();
//        d.threadPoolExecutorTest4();
//        d.threadPoolExecutorTest5();
//        d.threadPoolExecutorTest6();
//        d.threadPoolExecutorTest7();
        d.threadPoolExecutorTest8();

    }

    /**
     * 测试：提交15个执行时间需要3秒的任务，看线程池的状况
     */
    public void testCommon (ThreadPoolExecutor threadPoolExecutor) throws InterruptedException {
        //测试：提交15个执行需要3秒的任务，看超过大小的2个，应对的处理情况
        for (int i = 0; i < 15; i++) {
            int n = i;
            threadPoolExecutor.submit(new Runnable() {
                @Override
                public void run () {
                    try {
                        System.out.println("开始执行：" + n);
                        Thread.sleep(3000);
                        System.out.println("执行结束：" + n);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            System.out.println("任务提交成功：" + i);
        }
        //查看线程数量，以及队列等待数量
        Thread.sleep(500);
        System.out.println("当前线程池线程数量为：" + threadPoolExecutor.getPoolSize());
        System.out.println("当前线程池等待的数量为：" + threadPoolExecutor.getQueue().size());
        //等待15秒，查看线程数和队列数量（理论上超出核心线程数的线程会被销毁）
        Thread.sleep(15000);
        System.out.println("当前线程池线程数量为" + threadPoolExecutor.getPoolSize());
        System.out.println("当前线程池等待的数量为" + threadPoolExecutor.getQueue().size());
    }

    /**
     * 1、线程池信息：核心线程数量5，最大数量10，每个线程等待时间5，时间单位为秒（即超出核心线程数量的线程存活时间为5秒），无界队列
     *
     * @throws InterruptedException
     */
    private void threadPoolExecutorTest1 () throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        testCommon(threadPoolExecutor);
        //预计结果：线程池数量为5，超出数量的任务，进入队列中等待被执行
    }

    /**
     * 2、线程池信息：核心线程数量5，最大数量10，等待队列大小3，
     *
     * @throws InterruptedException
     */
    private void threadPoolExecutorTest2 () throws InterruptedException {
        //创建一个核心线程数量为5，最大数量为10，超出核心线程数量的线程存活时间5秒，等待队列最大线程数3的线程池，也就是能容纳最多13个任务
        //重写拒绝策略处理方法，默认是抛出RejectedExecutionException的异常，java.util.concurrent.ThreadPoolExecutor.AbortPolicy
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(3), new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution (Runnable r, ThreadPoolExecutor executor) {
                System.out.println("有任务被拒绝执行了！");
            }
        });
        testCommon(threadPoolExecutor);
        //预计结果：
        //1.    5个任务直接分配线程开始执行
        //2.    3个任务进入等待队列
        //3.    队列不够用时，临时增加（10-5）个线程来执行，若5秒没有任务调用，则进行销毁
        //4.    队列和线程都满了，剩下2个任务没有相应的资源，被拒绝执行
        //5.    任务执行5秒后，若没有任务可执行，则销毁临时创建的5个线程

    }

    /**
     * 3、线程池信息：核心线程数量为5，最大线程数量为5，超出核心线程数量的线程存活时间为0秒，无界队列
     *
     * @throws InterruptedException
     */
    private void threadPoolExecutorTest3 () throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 5, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        testCommon(threadPoolExecutor);
        //预计结果：5个线程立即执行，线程池数量为5，超出数量的线程进入队列，等待进行执行
    }

    /**
     * 4、线程池信息：核心线程数量为5，最大线程数量为Integer.MAX_VALUE，超出核心线程数量的线程存活时间为60秒，锁队列SynchronousQueue
     *
     * @throws InterruptedException
     */
    private void threadPoolExecutorTest4 () throws InterruptedException {
        //SynchronousQueue，实际上不是一个真正的队列，因为他不会为队列中元素维护储存空间。与其他队列不同的是，他维护一组线程，这些线程在等待着把元素加入或移出队列
        //在使用SynchronousQueue作为工作队列的前提下，客户端在提交新任务时，若线程池中又没有空闲的线程能够从SynchronousQueue队列实例中获取一个任务(即核心线程数已满)，
        //那么SynchronousQueue的offer方法调用就会失败（即任务没有被存入工作队列）。
        //此时，ThreadPoolExecutor会新建一个新的SynchronousQueue用于对这个队列失败的任务进行处理（假设此时的线程池大小还未达到其最大线程池大小maximumPoolSize）
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
        testCommon(threadPoolExecutor);
        //预计结果：
        //1.    所有线程池大小为15，超出数量的任务进入队列等待执行
        //2.    所有任务执行结束，60秒后，如果无可执行的任务，所有线程都将被销毁，线程池大小恢复为0
        Thread.sleep(60000);
        System.out.println("60秒后线程池大小为：" + threadPoolExecutor.getPoolSize());
    }

    /**
     * 5、定时执行线程池信息：3秒后执行，一次性任务，到点就执行
     * 核心线程数量为5
     */
    private void threadPoolExecutorTest5 () {
        ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(5);
        threadPoolExecutor.schedule(new Runnable() {
            @Override
            public void run () {
                System.out.println("任务被执行了，现在时间：" + System.currentTimeMillis());
            }
        }, 3000, TimeUnit.MILLISECONDS);
        System.out.println("定时任务，提交成功，时间是：" + System.currentTimeMillis() + ",当前线程池中线程数量：" + threadPoolExecutor.getPoolSize());
        //预计结果：任务3秒后被执行一次
    }

    /**
     * 6、定时执行线程池信息：线程固定数量为5，提交的任务需要3秒才能执行完毕
     * 核心线程数量为5，最大数量Integer.MAX_VALUE，DelayedWorkQueue延时队列，超出核心线程数量的线程存活时间：0秒
     */
    private void threadPoolExecutorTest6 () {
        ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(5);
        //周期性执行某一任务，线程池提供了两种调度方法scheduleAtFixedRate和scheduleWithFixedDelay
        //场景1：提交后2秒开始第一次执行，之后每隔1秒固定执行一次（如果上次任务还未执行完毕，且已达到下一次任务执行时间，本次任务执行完毕后立即执行下一次任务）
        //也就是说，3秒中执行一次任务（计算方式：每次执行3秒钟，间隔时间为1秒，执行结束后不等待马上开始执行下一次任务，无需等待）
        threadPoolExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run () {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("任务1被执行，现在时间：" + System.currentTimeMillis());
            }
        }, 2000, 1000, TimeUnit.MILLISECONDS);

        //场景2：提交后2秒开始第一次执行，之后每隔1秒固定执行一次（如果发现上次还未执行完，下次执行时仍然等待1秒再执行下一次任务）
        //也就是说，4秒钟执行一次任务（计算方式：每次执行3秒钟，间隔时间为1秒，执行结束后需等待1秒钟后再执行下一次任务，即3+1秒）
        threadPoolExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run () {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("任务2被执行，现在时间：" + System.currentTimeMillis());
            }
        }, 2000, 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * 7、终止线程：在线程shutdown停止后，再次进行submit提交，看是否能成功
     *
     * @throws InterruptedException
     */
    private void threadPoolExecutorTest7 () throws InterruptedException {
        //线程池信息：核心线程数5，最大线程数10，等待队列大小3，超出核心线程数量线程存活时间5秒，重写拒绝策略
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(3), new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution (Runnable r, ThreadPoolExecutor executor) {
                System.out.println("有任务被拒绝执行了");
            }
        });
        // 测试： 提交15个执行时间需要3秒的任务，看超过大小的2个，对应的处理情况
        for (int i = 0; i < 15; i++) {
            int n = i;
            threadPoolExecutor.submit(new Runnable() {
                @Override
                public void run () {
                    try {
                        System.out.println("开始执行：" + n);
                        Thread.sleep(3000L);
                        System.err.println("执行结束:" + n);
                    } catch (InterruptedException e) {
                        System.out.println("异常：" + e.getMessage());
                    }
                }
            });
            System.out.println("任务提交成功 :" + i);
        }
        //1秒后终止线程池
        Thread.sleep(1000);
        threadPoolExecutor.shutdown();
        //再次进行提交
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run () {
                System.out.println("追加一个任务");
            }
        });
        //预计结果：
        //1.    10个任务被执行，3个进入等待队列进行等待，2个任务被拒绝
        //2.    调用shutdown后，不再接收新的任务，等待13个任务执行结束
        //3.    最后追加的submit任务无法提交，将被拒绝执行
    }

    private void threadPoolExecutorTest8 () throws InterruptedException {
        //线程池信息：核心线程数5，最大线程数10，等待队列大小3，超出核心线程数量线程存活时间5秒，重写拒绝策略
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(3), new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution (Runnable r, ThreadPoolExecutor executor) {
                System.out.println("有任务被拒绝了");
            }
        });
        for (int i = 0; i < 15; i++) {
            int n = i;
            threadPoolExecutor.submit(new Runnable() {
                @Override
                public void run () {
                    try {
                        System.out.println("开始执行：" + n);
                        Thread.sleep(3000L);
                        System.out.println("执行结束:" + n);
                    } catch (InterruptedException e) {
                        System.err.println("异常：" + e.getMessage());
                    }
                }
            });
            System.out.println("任务提交成功 :" + i);
        }
        //1秒后终止线程池
        Thread.sleep(1000);
        List<Runnable> shutdownNow = threadPoolExecutor.shutdownNow();
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run () {
                System.out.println("追加一个任务");
            }
        });
        System.out.println("未结束的任务有：" + shutdownNow.size());
        //预期结果：
        //1.    10个任务被执行，3个任务在队列中进行等待，2个任务被拒绝
        //2.    因线程执行1秒后调用shutdownNow，线程马上终止，队列中的3个线程不再执行，
        //3.    执行一个线程需要三秒钟，所以没有一个任务执行完，在执行的10个任务都将被终止，且抛出异常sleep interrupted
        //4.    追加的任务在线程终止后无法再提交，会被拒绝执行
    }
}
