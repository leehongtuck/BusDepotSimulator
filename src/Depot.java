import java.util.LinkedList;
import java.util.List;

public class Depot {
    private List<Bus> busParking = new LinkedList<>();
    private int parkingSpace;
    private List<Cleaner> cleanerList = new LinkedList<>();
    private List<Mechanic> mechanicList = new LinkedList<>();
    private DepotTime time = new DepotTime();
    private Lane lane = new Lane();
    private boolean closingTime = false;

    //To keep track of time in depot
    private class DepotTime implements Runnable {
        private double time = 0;
        private int hour = 0;
        private int minute = 0;

        @Override
        public void run() {
            for (int i = 0; i < 20; i++) {
                increaseTime();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            closeDepot();
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
    private class Lane implements Runnable {
        private List<Bus> busWaitingList = new LinkedList<>();
        private List<Bus> pendingList = new LinkedList<>();
        private int exitCount = 0;

        private synchronized void queueEntrance(Bus bus) {
            if(!closingTime){
                if (parkingSpace > 0) {
                    System.out.println("Bus " + bus.getId() + " is requesting entrance");
                    if (busWaitingList.isEmpty()) {
                        notify();
                    }
                    busWaitingList.add(bus);
                    parkingSpace--;
                } else {
                    System.out.println("Oops the parking bay is full! Bus " + bus.getId() + " might need to wait for awhile.");
                    pendingList.add(bus);
                }
            }else {
                System.out.println("Oops! The depot is closing soon! Bus " + bus.getId() + ", please come back tomorrow, sorry!");
            }
        }

        private synchronized void queueExit(Bus bus) {
            System.out.println("Bus " + bus.getId() + " is requesting exit");
            if (busWaitingList.isEmpty() || (parkingSpace == 0 && exitCount == 0 && busWaitingList.isEmpty()) ) {
                notify();
            }
            busWaitingList.add(bus);
            exitCount++;
        }

        //Manages the traffic flow of lane
        private synchronized void manageTrafficFlow() {
            Bus bus = null;

            //check whether traffic is empty or there are no parking space at the same time no buses exiting
            while (busWaitingList.isEmpty()|| (parkingSpace==0 && exitCount == 0 &&  busWaitingList.isEmpty() && !(pendingList.isEmpty()))) {
                try {
                    System.out.println("waiting");
                    wait();
                    if (Depot.this.closingTime) {
                        System.out.println("Yes closing time!");
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //if parking space is full, prioritise the exiting buses
            if (parkingSpace==0 && exitCount > 0 && !(pendingList.isEmpty())){
                for(int i = 0; i < busWaitingList.size(); i++){
                    if(busWaitingList.get(i).getState().equals(Bus.BusState.exit)){
                        bus = busWaitingList.remove(i);
                        break;
                    }
                }
            }else {
                bus = busWaitingList.remove(0);
            }

            //let the bus travel on lane
            if(bus!= null){
                System.out.println("Time " + time.getTime() + "- Bus " + bus.getId() + " is crossing the ramp");
                try {
                    if (bus instanceof MiniBus) {
                        System.out.println("Bus " + bus.getId() + " is a minibus!");
                        Thread.sleep(1000);
                    } else {
                        Thread.sleep(2000);
                    }
                } catch (InterruptedException e) {
                    System.out.println("Oops the bus is caught in an accident.");
                }

                //if the bus of the front most queue is requesting to enter, let it enter and vice versa, but whether any of the list is empty
                if (bus.getState().equals(Bus.BusState.enter)) {
                    System.out.println("Time " + time.getTime() + "- Bus " + bus.getId() + " has entered the depot.");
                    bus.setState(Bus.BusState.inside);
                    assignTask(bus);
                } else if (bus.getState().equals(Bus.BusState.exit)) {
                    System.out.println("Time " + time.getTime() + "- Bus " + bus.getId() + " has left the depot.");
                    bus.setState(Bus.BusState.outside);
                    exitCount--;
                    if(pendingList.isEmpty()){
                        parkingSpace++;
                    }else {
                        busWaitingList.add(pendingList.remove(0));
                    }
                }
            }else {
                System.out.println("There is an issue on lane, no bus exists.");
            }

        }


        @Override
        public void run() {
            while (!Depot.this.closingTime) {
                manageTrafficFlow();
            }
            System.out.println("Alright depot closed.");
        }
    }


    public Depot() {
        parkingSpace = 5;
        Thread threadT = new Thread(time);
        threadT.start();
        Thread threadL = new Thread(lane);
        threadL.start();
    }

    public void requestEntrance(Bus bus) {
        bus.setState(Bus.BusState.enter);
        lane.queueEntrance(bus);
    }

    public void requestExit(Bus bus) {
        bus.setState(Bus.BusState.exit);
        lane.queueExit(bus);
    }

    //randomly assigns a task to the bus
    private void assignTask(Bus bus) {
        if (Math.random() > 0.5) {
            System.out.println("clean bus");
        } else {
            System.out.println("repair bus");
        }
    }

    public void cleanBus(Bus bus) {
        if (cleanerList.isEmpty()) {
            parkBus();
        }
        synchronized (cleanerList) {
            Cleaner c = cleanerList.get(0);
            c.cleanBus(bus);
            cleanerList.remove(0);
            cleanerList.add(c);
        }
    }

    public void parkBus() {

    }

    public synchronized void serviceBus(Bus bus) {
        synchronized (mechanicList) {
            Mechanic m = mechanicList.get(0);
            m.repairBus(bus);
            mechanicList.remove(0);
            mechanicList.add(m);
        }

    }

    private void closeDepot() {
        synchronized (lane) {
            System.out.println("Notifying closing time!");
            closingTime = true;
            lane.notify();
        }

    }

    //Worker operations
    public void goToWork(Cleaner c) {
        cleanerList.add(c);
    }

    public void goToWork(Mechanic m) {
        mechanicList.add(m);
    }
}
