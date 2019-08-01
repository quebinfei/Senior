public class Demo7 {
    /**
     * threadlocal变量，每一个线程都有一个副本，互不干扰
     */
    public static ThreadLocal<String> value = new ThreadLocal<>();

    public void threadLocalTest () {
        value.set("这是主线程设置的123");
        String v = value.get();
        System.out.println("线程1执行之前，主线程取到的值是：" + v);

        new Thread(new Runnable() {
            @Override
            public void run () {
                String v = value.get();
                System.out.println("线程1取到的值：" + v);
                //设置threadlocal值
                value.set("这是线程1设置的456");

                v = value.get();
                System.out.println("重新设置之后，线程1取到的值：" + v);
                System.out.println("线程1执行结束");
            }
        }).start();
    }

    public static void main (String[] args) {
        new Demo7().threadLocalTest();
    }
}
