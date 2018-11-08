public class ClockIncrementer implements Runnable {
    Clock clock;

    public ClockIncrementer(Clock clock){
        this.clock = clock;
    }

    @Override
    public void run() {
        for (int i = 0; i < 480; i++){
            clock.increaseTime();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
