public class StopThread extends Thread {
    private int i = 0, j = 0;

    public void run () {
//        synchronized (StopThread.class){
        ++i;
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ++j;
//        }
    }

    public void print () {
        System.out.println("i=" + i + ",j=" + j);
    }
}
