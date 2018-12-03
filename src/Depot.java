import java.time.Clock;
import java.util.LinkedList;
import java.util.List;

public class Depot {
    private List<Bus> busQueue = new LinkedList<>();
    private DepotTime t = new DepotTime();
    private Semaphore parkingSpace = new Semaphore(25);

    public Depot(){
        Thread threadT = new Thread(t);
        threadT.start();
    }
 
    //To keep track of time in depot
    private class DepotTime implements Runnable{
        private double time = 0;
        private int hour = 0;
        private int minute = 0;

        @Override
        public void run() {
            for (int i = 0; i < 480; i++){
                    increaseTime();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Depot.this.closeDepot();
        }

        private synchronized void increaseTime(){
            time++;
            hour = (int)time/60;
            minute = (int)time%60;
        }

        private String getTime() {
            return String.format("%02d", hour) + ":" + String.format("%02d", minute);
        }
        

    }


    public void exitDepot(Bus bus){
        System.out.println("Time " + t.getTime() + "- Bus " + bus.getId() + " is requesting to leave the depot");
        synchronized (this){
            useLane(bus, t);
            System.out.println("Time " + t.getTime() + "- Bus " + bus.getId() + " has left the depot.");
        }

    }

    public void enterDepot(Bus bus){
        System.out.println("Time " + t.getTime() + "- Bus " + bus.getId() + " is requesting to enter the depot");
        synchronized (this){
            useLane(bus,t);
            System.out.println("Time " + t.getTime() + "- Bus " + bus.getId() + " has entered the depot.");
        }

    }
 
    private void closeDepot(){
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        notify();
    }

    private void useLane(Bus bus, DepotTime time){
        System.out.println("Time " + time.getTime() + "- Bus " + bus.getId() + " is on the ramp" );
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.out.println("Oops the bus is caught in an accident.");
        }
    }
}
