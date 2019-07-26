public class Demo3 {

    public static void main (String[] args) throws InterruptedException {
        StopThread thread = new StopThread();
        thread.start();
        Thread.sleep(1000L);
//        thread.stop();
        thread.interrupt();
        while (thread.isAlive()) {

        }
        thread.print();

    }
}
