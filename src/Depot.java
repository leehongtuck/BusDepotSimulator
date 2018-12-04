import java.util.LinkedList;
import java.util.List;

public class Depot {
    private List<Bus> busQueue = new LinkedList<>();
    private List<Bus> busWaitingList = new LinkedList<>();
    private List<Cleaner> cleanerList = new LinkedList<>();
    private List<Mechanic> mechanicList = new LinkedList<>();
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

        private void increaseTime(){
            time++;
            hour = (int)time/60;
            minute = (int)time%60;
        }

        private String getTime() {
            return String.format("%02d", hour) + ":" + String.format("%02d", minute);
        }
        

    }

    //Bus operations
    public void exitDepot(Bus bus){
        synchronized (this){
            System.out.println("Time " + t.getTime() + "- Bus " + bus.getId() + " is requesting to leave the depot");
            try {
                bus.wait();
                busQueue.add(bus);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        synchronized (this){
            useLane(bus, t);
            System.out.println("Time " + t.getTime() + "- Bus " + bus.getId() + " has left the depot.");
            if(busQueue.size()>0){
                busQueue.get(0).notify();
                busQueue.remove(0);
            }

        }

    }

    public void enterDepot(Bus bus){
        synchronized (this) {
            System.out.println("Time " + t.getTime() + "- Bus " + bus.getId() + " is requesting to enter the depot");
            try {
                wait();
                busQueue.add(bus);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        synchronized (this){
            useLane(bus,t);
            System.out.println("Time " + t.getTime() + "- Bus " + bus.getId() + " has entered the depot.");
            if(busQueue.size()>0){
                busQueue.get(0).notify();
                busQueue.remove(0);
            }
            //Randomise clean or service
            cleanBus(bus);
//            if(Math.random()>0.5){
//                cleanBus(bus);
//            }else {
//                serviceBus(bus);
//            }
        }

    }

    public void cleanBus(Bus bus){
        synchronized (this){
            if(cleanerList.size()==0){
                try {
                    bus.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        synchronized (this){
            Cleaner c = cleanerList.get(0);
            c.cleanBus(bus);
            cleanerList.remove(0);
            cleanerList.add(c);
            exitDepot(bus);
        }

    }

    public synchronized void serviceBus(Bus bus){
        if(cleanerList.size()>0){
            cleanerList.get(0).cleanBus(bus);
            cleanerList.remove(0);
        }else{
            System.out.println("Bus " + bus + " is waiting to be cleaned at the waiting bay.");
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

    //Worker operations
    public void goToWork(Cleaner c){
        cleanerList.add(c);
    }

    public void goToWork(Mechanic m){
        mechanicList.add(m);
    }
}
