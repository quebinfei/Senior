public class Demo4 extends Thread {

    private volatile static boolean flag = true;

    public static void main (String[] args) throws InterruptedException {
        new Thread(() -> {
            while (flag) {
                System.out.println("运行中");
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Thread.sleep(3000L);
        flag = false;
        System.out.println("程序运行结束");
    }
}
