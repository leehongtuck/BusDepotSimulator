import java.util.*;

public class Depot {
    public List<Bus> busInDepot = new ArrayList<>();
    private List<Bus> busEntered = new ArrayList<>();
    private int maxSpace;
    private int parkingSpace;

    //List of available cleaner and mechanics
    private List<Cleaner> cleanerList = new ArrayList<>();
    private List<Mechanic> mechanicList = new ArrayList<>();
    private Queue<Cleaner> availableCleanerList = new LinkedList<>();
    private Queue<Mechanic> availableMechanicList = new LinkedList<>();
    private int waitClean = 0;
    private int waitRepair = 0;

    //Depot operations information
    private DepotTime time = new DepotTime(this);
    private Lane lane = new Lane();
    private boolean closingTime = false;
    private volatile boolean isEmpty = false;
    private volatile boolean isRaining = false;

    //The lane operations in depot
    private class Lane implements Runnable {
        private Queue<Bus> bridgeQueue = new LinkedList<>();
        private Queue<Bus> pendingList = new LinkedList<>();

        private synchronized void queueEntrance(Bus bus) {
            if(!closingTime){
                if (parkingSpace > 0) {
                    System.out.println(DepotTime.getTime() + bus + " is requesting entrance");
                    if (bridgeQueue.isEmpty()) {
                        notify();
                    }
                    bridgeQueue.add(bus);
                    parkingSpace--;
                } else {
                    System.out.println(DepotTime.getTime() + "Oops the parking bay is full! " + bus + " might need to wait for awhile.");
                    pendingList.offer(bus);
                }
            }else {
                System.out.println(DepotTime.getTime() + "Oops! The depot is closing soon! " + bus + ", please come back tomorrow, sorry!");
            }
        }

        private synchronized void queueExit(Bus bus) {
            System.out.println(DepotTime.getTime() + bus + " is requesting exit");
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
                    if(closingTime){
                        break;
                    }
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
            System.out.println(DepotTime.getTime() + "Closing time, no more buses allowed to queue for entrance!");
            while (!pendingList.isEmpty()){
                Bus bus = pendingList.remove();
                bus.setState(Bus.BusState.outside);
                synchronized (bus){
                    bus.notify();
                }
            }

            //While the depot still have buses, continue operating
            while (!isEmpty){
                if(parkingSpace == maxSpace && bridgeQueue.isEmpty()){
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
            }
        }

        private void useBridge(){
            System.out.println("Buses queueing for bridge: " + bridgeQueue);
            Bus bus = bridgeQueue.remove();
            //let the bus travel on lane
            System.out.println(DepotTime.getTime() + bus + " is crossing the ramp");
            try {
                if (bus instanceof MiniBus) {
                    System.out.println(bus + " is a minibus!");
                    Thread.sleep((long)(250* DepotTime.TIMESCALE));
                } else {
                    Thread.sleep((long)(500* DepotTime.TIMESCALE));
                }
            } catch (InterruptedException e) {
                System.out.println("Oops the bus is caught in an accident.");
            }

            //if the bus of the front most queue is requesting to enter, let it enter and vice versa, but whether any of the list is empty
            if (bus.getState().equals(Bus.BusState.enter)) {
                System.out.println(DepotTime.getTime() + bus + " has entered the depot.");
                bus.setState(Bus.BusState.inside);
                bus.startTimer();
                busInDepot.add(bus);
                busEntered.add(bus);
                synchronized (bus){
                    bus.notify();
                }
            } else if (bus.getState().equals(Bus.BusState.exit)) {
                System.out.println(DepotTime.getTime() + bus + " has left the depot.");
                bus.setState(Bus.BusState.outside);
                busInDepot.remove(bus);
                if(pendingList.isEmpty()){
                    parkingSpace++;
                }else {
                    bridgeQueue.offer(pendingList.remove());
                }
            }

            System.out.println("Parking space left: "+ parkingSpace);
            System.out.println("Buses in depot: " + busInDepot);
            //System.out.println(bridgeQueue.size() + "Buses left! " + parkingSpace + " parking left" );
        }


        @Override
        public void run() {
            while (!isEmpty) {
                manageTrafficFlow();
            }
            sanityCheck();


        }
    }

    public Depot() {
        maxSpace = 10;
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
            cleanerList.add(c);
        }
        standbyForWork(c);
    }

    private void standbyForWork(Cleaner c){
        synchronized (availableCleanerList){
            if (availableCleanerList.isEmpty()&& waitClean >0){
                availableCleanerList.notify();
            }
            availableCleanerList.offer(c);
        }
    }

    public void cleanBus(Bus bus) {
        System.out.println(DepotTime.getTime() + bus + " is requesting to be cleaned!");
        Cleaner c;

        //If bus requests to be cleaned after closing time, ask to come back another day.
        if(closingTime){
            System.out.println(DepotTime.getTime() +"Sorry " + bus + ". Depot is closed now. Please come back tomorrow to be cleaned.");
            requestExit(bus);
            return;
        }

        synchronized (availableCleanerList) {
            while (availableCleanerList.isEmpty() || isRaining) {
                if(availableCleanerList.isEmpty()){
                    System.out.println(DepotTime.getTime() + "Oops there are no cleaners available now! Bus "
                            + bus.getId() + " please wait!");
                }else if(isRaining){
                    System.out.println(DepotTime.getTime() + "It is raining now! Cleaning can't be conducted! Bus " +
                            bus.getId() +" please wait!");
                }

                waitClean++;
                try {
                    availableCleanerList.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //if during closing time it still rains, leave the depot
                if(closingTime && isRaining){
                    requestExit(bus);
                    return;
                }
                System.out.println(DepotTime.getTime() + "Finally there are cleaners available now! Bus "
                        + bus.getId() + " proceed!");
                waitClean--;
            }
            c = availableCleanerList.remove();
        }
        System.out.println(DepotTime.getTime() + bus + " is heading to the cleaning bay!");
        c.cleanBus(bus);
        standbyForWork(c);
    }

    public void goToWork(Mechanic m) {
        synchronized (mechanicList){
            mechanicList.add(m);
        }
        standbyForWork(m);
    }

    private void standbyForWork(Mechanic m){
        synchronized (availableMechanicList){
            if(availableMechanicList.isEmpty()&&waitRepair >0){
                availableMechanicList.notify();
            }
            availableMechanicList.offer(m);
        }
    }

    public void serviceBus(Bus bus) {
        System.out.println(DepotTime.getTime() + bus + " is requesting to be serviced!");
        if(closingTime){
            System.out.println(DepotTime.getTime() + "Sorry " + bus + ". Depot is closed now. Please come back tomorrow to be serviced.");
            requestExit(bus);
            return;
        }
        Mechanic m;
        synchronized (availableMechanicList) {
            while (availableMechanicList.isEmpty()) {
                System.out.println(DepotTime.getTime() + "Oops there are no mechanics available now! Bus "
                        + bus.getId() + " please wait!");
                waitRepair++;
                try {
                    availableMechanicList.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(DepotTime.getTime() + "Finally there are mechanics available now! Bus "
                        + bus.getId() + " proceed!");
                waitRepair--;
            }
            m = availableMechanicList.remove();
        }
        System.out.println(DepotTime.getTime() + bus + " is heading to the servicing bay!");
        m.serviceBus(bus);
        standbyForWork(m);
    }

    public boolean isEmpty(){
        return isEmpty;
    }

    public boolean isRaining(){
        return isRaining;
    }

    public void changeWeather(){
        if(!isRaining){
            isRaining = true;
            System.out.println(DepotTime.getTime()+ "Holy shit is raining!");
        }else {
            isRaining = false;
            for (Cleaner c: cleanerList) {
                synchronized (c){
                    c.notify();
                }
            }
            System.out.println(DepotTime.getTime() + "Finally it's sunny again!");
        }

    }

    public boolean getClosingTime(){
        return closingTime;
    }

    //Close depot
    public synchronized void setClosingTime(){
        synchronized (lane) {
            System.out.println(DepotTime.getTime() + "Notifying closing time!");
            closingTime = true;
            if(lane.bridgeQueue.isEmpty()){
                lane.notify();
            }
        }
        if(isRaining && waitClean > 0){
            System.out.println(DepotTime.getTime() +
                    "Sorry buses, those waiting to be cleaned please return tomorrow due to bad weather.");
            synchronized (cleanerList){
                for (Cleaner c: cleanerList) {
                    synchronized (c){
                        c.notify();
                    }
                }
            }

            synchronized (availableCleanerList){
                availableCleanerList.notifyAll();
            }

        }
    }

    public void sanityCheck(){
        System.out.println("Alright depot closed.\n");
        System.out.println("Time for sanity checks!");

        System.out.println("Total bus served: " + busEntered.size());
        int max = 0;
        int min = 0;
        int sum = 0;
        int avg;
        for(Bus bus:busEntered){
            if( max==0 && min ==0){
                max = bus.getWaitingTime();
                min = max;
            }else if(bus.getWaitingTime()>max){
                max = bus.getWaitingTime();
            }else if(bus.getWaitingTime()<min){
                min = bus.getWaitingTime();
            }

            sum+= bus.getWaitingTime();
        }
        avg = sum/busEntered.size();
        System.out.println("Bus waiting times:");
        System.out.println("Maximum - " + (int)max/60 + " hour(s) " + max%60 + " minute(s).");
        System.out.println("Minimum - " + (int)min/60 + " hour(s) " + min%60 + " minute(s).");
        System.out.println("Average - " + (int)avg/60 + " hour(s) " + avg%60 + " minute(s).");
        System.out.println();

        System.out.println("Workers present:");
        System.out.println("Cleaner(s) - " + cleanerList.size());
        System.out.println("Mechanic(s) - " + mechanicList.size());
    }



}
