import java.util.LinkedList;
import java.util.List;

public class Depot {
    private List<Bus> busQueue = new LinkedList<>();

    private List<Cleaner> cleanerList = new LinkedList<>();
    private List<Mechanic> mechanicList = new LinkedList<>();
    private DepotTime time = new DepotTime();
    private Lane lane = new Lane();
    private boolean closingTime = false;
    private Semaphore parkingSpace = new Semaphore(25);

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

        private void useLane(Bus bus) {
            System.out.println("Time " + time.getTime() + "- Bus " + bus.getId() + " is crossing the ramp");
            try {
                if (bus instanceof MiniBus) {
                    Thread.sleep(2500);
                } else {
                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {
                System.out.println("Oops the bus is caught in an accident.");
            }
        }

        public void manageEntrance() {
            synchronized (busWaitingList) {
                Bus b;
                if (busWaitingList.isEmpty()) {
                    try {
                        busWaitingList.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (busWaitingList.get(0).equals(entranceList.get(0))) {
                    b = busWaitingList.get(0);
                    useLane(b);
                    busWaitingList.remove(0);
                    entranceList.remove(0);
                    System.out.println("Time " + time.getTime() + "- Bus " + b.getId() + " has entered the depot.");
                }
            }
        }

        public void manageExit() {
            synchronized (busWaitingList) {
                Bus b;
                if (busWaitingList.isEmpty()) {
                    try {
                        busWaitingList.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (busWaitingList.get(0).equals(exitList.get(0))) {
                    b = busWaitingList.get(0);
                    useLane(b);
                    busWaitingList.remove(0);
                    exitList.remove(0);
                    System.out.println("Time " + time.getTime() + "- Bus " + b.getId() + " has left the depot.");
                }
            }
        }

        @Override
        public void run() {
            while(!closingTime){
                manageEntrance();
                manageExit();
            }

        }
    }

    //Bus operations
    public void requestExit(Bus bus) {

    }

    public synchronized void requestEnter(Bus bus) {
        System.out.println("Time " + time.getTime() + "- Bus " + bus.getId() + " is requesting to enter the depot");
        try {
            wait();
            busQueue.add(bus);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void cleanBus(Bus bus) {
        synchronized (this) {
            if (cleanerList.size() == 0) {
                try {
                    bus.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        synchronized (cleanerList) {
            Cleaner c = cleanerList.get(0);
            c.cleanBus(bus);
            cleanerList.remove(0);
            cleanerList.add(c);
            exitDepot(bus);
        }

    }

    public synchronized void serviceBus(Bus bus) {
        if (cleanerList.size() > 0) {
            cleanerList.get(0).cleanBus(bus);
            cleanerList.remove(0);
        } else {
            System.out.println("Bus " + bus + " is waiting to be cleaned at the waiting bay.");
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
