import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Depot {
    private final double TIMEUNIT = 0.01;

    private List<Bus> busParking = new LinkedList<>();
    private int maxSpace;
    private int parkingSpace;

    //List of available cleaner and mechanics
    private Queue<Cleaner> cleanerList = new LinkedList<>();
    private Queue<Mechanic> mechanicList = new LinkedList<>();
    private int waitClean = 0;
    private int waitRepair = 0;

    //Depot operations information
    private DepotTime time = new DepotTime();
    private Lane lane = new Lane();
    private boolean closingTime = false;
    private boolean isEmpty = false;

    //To keep track of time in depot
    private class DepotTime implements Runnable {
        private final int OPENHOUR = 8;
        private double time = 0;
        private int hour = 0;
        private int minute = 0;

        @Override
        public void run() {
            for (int i = 0; i < 480; i++) {
                increaseTime();
                try {
                    Thread.sleep((long)(1000 * TIMEUNIT));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            synchronized (lane) {
                if(lane.bridgeQueue.isEmpty()){
                    lane.notify();
                }
                System.out.println("Notifying closing time!");
                closingTime = true;
            }
        }

        private void increaseTime() {
            time++;
            hour = OPENHOUR + (int) time / 60;
            minute = (int) time % 60;
        }

        private String getTime() {
            return String.format("%02d", hour) + ":" + String.format("%02d", minute);
        }
    }

    //The lane operations in depot
    private class Lane implements Runnable {
        private Queue<Bus> bridgeQueue = new LinkedList<>();
        private Queue<Bus> pendingList = new LinkedList<>();
        private boolean isOperating = true;

        private synchronized void queueEntrance(Bus bus) {
            if(!closingTime){
                if (parkingSpace > 0) {
                    System.out.println("Bus " + bus.getId() + " is requesting entrance");
                    if (bridgeQueue.isEmpty()) {
                        notify();
                    }
                    bridgeQueue.add(bus);
                    parkingSpace--;
                } else {
                    System.out.println("Oops the parking bay is full! Bus " + bus.getId() + " might need to wait for awhile.");
                    pendingList.offer(bus);
                }
            }else {
                System.out.println("Oops! The depot is closing soon! Bus " + bus.getId() + ", please come back tomorrow, sorry!");
            }
        }

        private synchronized void queueExit(Bus bus) {
            System.out.println("Bus " + bus.getId() + " is requesting exit");
            if (bridgeQueue.isEmpty()) {
                notify();
            }
            bridgeQueue.offer(bus);
        }

        //Manages the traffic flow of lane
        private synchronized void manageTrafficFlow() {
            //check whether traffic is empty or there are no parking space at the same time no buses exiting
            while (bridgeQueue.isEmpty()) {
                try {
                    //System.out.println("waiting");
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (Depot.this.closingTime) {
                prepareClosure();
                System.out.println("Prepare to close");
                return;
            }

            useBridge();
        }

        private void prepareClosure(){
            System.out.println("Closing time, no more buses allowed to queue for entrance!");
            while (!pendingList.isEmpty()){
                pendingList.remove();
            }

            //While the depot still have buses, continue operating
            while (!isEmpty){
                if(parkingSpace == maxSpace && bridgeQueue.isEmpty()){
                    System.out.println("Im here");
                    isEmpty = true;
                    return;
                }else if(bridgeQueue.isEmpty()){
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                useBridge();
                //System.out.println(bridgeQueue.size() + " buses left! " + parkingSpace + " parking left" );
            }
        }

        private void useBridge(){
            Bus bus = bridgeQueue.remove();
            //let the bus travel on lane
            System.out.println("Time " + time.getTime() + "- Bus " + bus.getId() + " is crossing the ramp");
            try {
                if (bus instanceof MiniBus) {
                    System.out.println("Bus " + bus.getId() + " is a minibus!");
                    Thread.sleep((long)(250*TIMEUNIT));
                } else {
                    Thread.sleep((long)(500*TIMEUNIT));
                }
            } catch (InterruptedException e) {
                System.out.println("Oops the bus is caught in an accident.");
            }

            //if the bus of the front most queue is requesting to enter, let it enter and vice versa, but whether any of the list is empty
            if (bus.getState().equals(Bus.BusState.enter)) {
                System.out.println("Time " + time.getTime() + "- Bus " + bus.getId() + " has entered the depot.");
                bus.setState(Bus.BusState.inside);
                busParking.add(bus);
                synchronized (bus){
                    bus.notify();
                }
            } else if (bus.getState().equals(Bus.BusState.exit)) {
                System.out.println("Time " + time.getTime() + "- Bus " + bus.getId() + " has left the depot.");
                bus.setState(Bus.BusState.outside);
                if(pendingList.isEmpty()){
                    parkingSpace++;
                }else {
                    bridgeQueue.offer(pendingList.remove());
                }
            }
            System.out.println(bridgeQueue.size() + " buses left! " + parkingSpace + " parking left" );
        }



        @Override
        public void run() {
            while (!isEmpty) {
                manageTrafficFlow();
            }
            System.out.println("Alright depot closed.");
        }
    }

    public Depot() {
        maxSpace = 50;
        parkingSpace = maxSpace;
        Thread threadT = new Thread(time);
        threadT.start();
        Thread threadL = new Thread(lane);
        threadL.start();
//        Thread threadTask = new Thread(task);
//        threadTask.start();
    }

    public void requestEntrance(Bus bus) {
        bus.setState(Bus.BusState.enter);
        lane.queueEntrance(bus);
    }

    public void requestExit(Bus bus) {
        bus.setState(Bus.BusState.exit);
        lane.queueExit(bus);
    }



    //Worker operations
    public void goToWork(Cleaner c) {
        synchronized (cleanerList){
            if (cleanerList.isEmpty()&& waitClean >0){
                cleanerList.notify();
            }
            cleanerList.offer(c);
        }
    }

    public void cleanBus(Bus bus) {
        System.out.println("Bus " + bus.getId() + " is requesting to be cleaned!");
        Cleaner c;
        synchronized (cleanerList) {
            while (cleanerList.isEmpty()) {
                System.out.println("Oops there are no cleaners available now!");
                waitClean++;
                try {
                    cleanerList.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                waitClean--;
            }
            c = cleanerList.remove();
        }
        c.cleanBus(bus);
        goToWork(c);
    }

    public void goToWork(Mechanic m) {
        synchronized (mechanicList){
            if(cleanerList.isEmpty()&&waitRepair >0){
                mechanicList.notify();
            }
            mechanicList.offer(m);
        }
    }

    public void repairBus(Bus bus) {
        System.out.println("Bus " + bus.getId() + " is requesting to be serviced!");
        Mechanic m;
        synchronized (mechanicList) {
            while (mechanicList.isEmpty()) {
                System.out.println("Oops there are no mechanics available now!");
                waitRepair++;
                try {
                    mechanicList.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                waitRepair--;
            }
            m = mechanicList.remove();
        }
        m.repairBus(bus);
        goToWork(m);
    }

}
