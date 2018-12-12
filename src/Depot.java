import java.util.LinkedList;
import java.util.List;

public class Depot {
    private List<Bus> busQueue = new LinkedList<>();

    private List<Cleaner> cleanerList = new LinkedList<>();
    private List<Mechanic> mechanicList = new LinkedList<>();
    private DepotTime time = new DepotTime();
    private Lane lane = new Lane();
    private boolean closingTime = false;
    private Semaphore parkingSpace;

    public Depot() {
        Thread threadT = new Thread(time);
        threadT.start();
        Thread threadL = new Thread(lane);
        threadL.start();
    }

    //To keep track of time in depot
    private class DepotTime implements Runnable {
        private double time = 0;
        private int hour = 0;
        private int minute = 0;

        @Override
        public void run() {
            for (int i = 0; i < 480; i++) {
                increaseTime();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Depot.this.closeDepot();
        }

        private void increaseTime() {
            time++;
            hour = (int) time / 60;
            minute = (int) time % 60;
        }

        private String getTime() {
            return String.format("%02d", hour) + ":" + String.format("%02d", minute);
        }

    }

    //The lane operations in depot
    private class Lane implements Runnable{
        private List<Bus> busWaitingList = new LinkedList<>();
        private List<Bus> entranceList = new LinkedList<>();
        private List<Bus> exitList = new LinkedList<>();

        public void queueEntrance(Bus bus){
            synchronized (busWaitingList){
                System.out.println("Bus " + bus.getId() + " is requesting entrance");
                busWaitingList.add(bus);
                entranceList.add(bus);
            }
        }

        public void queueExit(Bus bus){
            synchronized (busWaitingList){
                busWaitingList.add(bus);
                exitList.add(bus);
//                if (busWaitingList.isEmpty()) {
//                    busWaitingList.notify();
//                }
            }
        }

        private void useLane(Bus bus) {
            System.out.println("Time " + time.getTime() + "- Bus " + bus.getId() + " is crossing the ramp");
            try {
                if (bus instanceof MiniBus) {
                    Thread.sleep(1000);
                } else {
                    Thread.sleep(2000);
                }
            } catch (InterruptedException e) {
                System.out.println("Oops the bus is caught in an accident.");
            }
        }

        //Manages the traffic flow of lane
        private void manageTrafficFlow() {
            synchronized (busWaitingList) {
                Bus b;
                if (busWaitingList.isEmpty()) {
//                    try {
//                        busWaitingList.wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    return;
                }
                b = busWaitingList.get(0);
                useLane(b);
                //if the bus of the frontmost queue is requesting to enter, let it enter and vice versa
                if (b.equals(entranceList.get(0))) {
                    busWaitingList.remove(0);
                    entranceList.remove(0);
                    System.out.println("Time " + time.getTime() + "- Bus " + b.getId() + " has entered the depot.");
                    assignTask(b);
                }else if(b.equals(exitList.get(0))){
                    busWaitingList.remove(0);
                    exitList.remove(0);
                    System.out.println("Time " + time.getTime() + "- Bus " + b.getId() + " has left the depot.");
                }
            }
        }

        @Override
        public void run() {
            while(!closingTime){
                manageTrafficFlow();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    //Bus operations
    public void requestExit(Bus bus) {
        lane.queueExit(bus);
    }

    public void requestEntrance(Bus bus) {
        lane.queueEntrance(bus);
    }

    //randomly assigns a task to the bus
    private void assignTask(Bus bus){
        if(Math.random()> 0.5){
            System.out.println("clean bus");
        }else {
            System.out.println("repair bus");
        }
    }


    public void cleanBus(Bus bus) {
        if(cleanerList.isEmpty()){
            parkBus();
        }
        synchronized (cleanerList) {
            Cleaner c = cleanerList.get(0);
            c.cleanBus(bus);
            cleanerList.remove(0);
            cleanerList.add(c);
            requestExit(bus);
        }

    }

    public void parkBus(){

    }

    public synchronized void serviceBus(Bus bus) {
        synchronized (mechanicList) {
            Mechanic m = mechanicList.get(0);
            m.repairBus(bus);
            mechanicList.remove(0);
            mechanicList.add(m);
            requestExit(bus);
        }

    }

    private void closeDepot() {
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        notify();
    }

    //Worker operations
    public void goToWork(Cleaner c) {
        cleanerList.add(c);
    }

    public void goToWork(Mechanic m) {
        mechanicList.add(m);
    }
}
