import java.util.concurrent.locks.LockSupport;

/* 三种线程协作通信方式：suspend/resume、wait/notify、park/unpark */
public class Demo6 {
    public static Object baozidian = null;

    public static void main (String[] args) throws InterruptedException {
        Demo6 demo = new Demo6();

        //对调用顺序有要求，必须先等待再唤醒，否则会造成永久挂起等待；不会释放锁资源；容易导致死锁
//        demo.suspendResumeTest();
//        demo.suspendResumeDeadLockTest();
//        demo.suspendResumeDeadLockTest2();

        //要求在同步关键字里面进行，免去了死锁的困扰，但调用顺序有要求，必须先等待再唤醒，否则会造成永久挂起等待
//        demo.waitNotifyTest();
//        demo.waitNotifyDeadLockTest();

        //没有顺序要求，但不会释放锁资源，容易导致死锁
//        demo.parkUnparkTest();
        demo.parkUnparkDeadLockTest();

    }

    /**
     * 正常的suspend/resume
     */
    public void suspendResumeTest () throws InterruptedException {
        Thread cunsumerThread = new Thread(() -> {
            if (baozidian == null) {
                System.out.println("1.进入等待");
                Thread.currentThread().suspend();
            }
            System.out.println("2.买到包子，回家");
        });

        cunsumerThread.start();
        //3秒之后，生产一个包子
        Thread.sleep(3000);
        baozidian = new Object();
        cunsumerThread.resume();
        System.out.println("3.通知消费者");
    }

    /**
     * 死锁的suspend/resume，suspend并不会像wait一样释放锁，容易造成死锁
     * 1.程序拿到锁，然后进行挂机等待；
     * 2.通知解锁操作无法拿到锁，无法进行唤醒通知；
     * 3.程序造成死锁。
     */
    public void suspendResumeDeadLockTest () throws InterruptedException {
        Thread cunsumeThread = new Thread(() -> {
            if (baozidian == null) {
                System.out.println("1.进入等待");
                synchronized (Demo6.class) {//拿到锁后进行挂起
                    Thread.currentThread().suspend();
                }
                System.out.println("2.买到包子，回家");
            }
        });
        cunsumeThread.start();
        //3秒后生产包子
        Thread.sleep(3000);
        baozidian = new Object();
        synchronized (Demo6.class) {
            cunsumeThread.resume();
        }
        System.out.println("3.通知消费者");
    }

    /**
     * 导致程序永久挂起的suspend/resume
     * 1.主线程休眠5秒后再进行挂起等待；
     * 2.通知线程在5秒内先进行了通知解锁；
     * 3.主线程休眠后在挂起，通知线程已通知完成，主线程无法再次收到唤醒通知。
     */
    public void suspendResumeDeadLockTest2 () throws InterruptedException {
        Thread cunsumThread = new Thread(() -> {
            if (baozidian == null) {
                System.out.println("1.进入等待");
                try {
                    Thread.sleep(5000);//线程休眠5秒钟
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Thread.currentThread().suspend();//休眠后再进行挂起
            }
            System.out.println("2.买到包子，回家");
        });

        cunsumThread.start();
        //3秒后产生包子
        Thread.sleep(3000);
        baozidian = new Object();
        cunsumThread.resume();
        System.out.println("3.通知消费者");
//        cunsumThread.join();

    }

    /**
     * 正常的wait/notify，可释放锁
     */
    public void waitNotifyTest () throws InterruptedException {
        new Thread(() -> {
            if (baozidian == null) {
                synchronized (this) {
                    System.out.println("1.进入等待");
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("2.买到包子，回家");
            }
        }).start();
        //3秒后生产包子
        Thread.sleep(3000);
        baozidian = new Object();
        synchronized (this) {
            this.notify();
            System.out.println("3.通知消费者");
        }

    }

    /**
     * 会导致程序永久等待的wait/notify
     * 1.主线程先休眠5秒再挂起等待；
     * 2.通知线程在5秒内先进行了唤醒通知；
     * 3.主线程在休眠5秒后再挂起，因通知线程已进行了通知，导致主线程无法再次收到唤醒通知。
     */
    public void waitNotifyDeadLockTest () throws InterruptedException {
        new Thread(() -> {
            if (baozidian == null) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (this) {
                    try {
                        System.out.println("1.进入等待");
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("2.买到包子，回家");
            }
        }).start();

        //3秒后生产包子
        Thread.sleep(3000);
        baozidian = new Object();
        synchronized (this) {
            this.notify();
            System.out.println("3.通知消费者");
        }
    }

    /**
     * 正常的park/unpark
     */
    public void parkUnparkTest () throws InterruptedException {
        Thread cunsumerThread = new Thread(() -> {
            if (baozidian == null) {
                System.out.println("1.进入等待");
                //不要求调用顺序
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                LockSupport.park();
            }
            System.out.println("2.买到包子，回家");
        });
        cunsumerThread.start();
        //3秒后生产包子
        Thread.sleep(3000);
        baozidian = new Object();
        LockSupport.unpark(cunsumerThread);
        System.out.println("3.通知消费者");
    }

    /**
     * 造成死锁的park/unpark
     * 1.主线程拿到锁之后进行挂起等待；
     * 2.通知线程拿不到锁，无法进行唤醒通知；
     * 3.线程造成死锁。
     */
    public void parkUnparkDeadLockTest () throws InterruptedException {
        Thread cunsumerTread = new Thread(() -> {
            if (baozidian == null) {
                System.out.println("1.进入等待");
                synchronized (this) {//拿到线程锁后进行挂起
                    LockSupport.park();
                }
            }
            System.out.println("2.买到包子，回家");
        });

        cunsumerTread.start();
        //3秒后生产包子
        Thread.sleep(3000);
        baozidian = new Object();
        synchronized (this) {
            LockSupport.unpark(cunsumerTread);

        }
        System.out.println("3.通知消费者");
    }
}
