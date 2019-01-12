public class Bus implements Runnable {
    private int id;
    private Depot depot;
    private BusState state;
    private int waitingTime = 0;

    private class BusTimer implements Runnable{
        @Override
        public void run() {
            while (!state.equals(BusState.outside)){
                waitingTime++;
                try {
                    Thread.sleep((long)(1000*DepotTime.TIMESCALE));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startTimer(){
        new Thread(new BusTimer()).start();
    }

    public int getWaitingTime(){
        return waitingTime;
    }

    public enum BusState{
        enter,exit,inside,outside,clean,repair
    }

    public Bus(int id, Depot depot) {
        this.id = id;
        this.depot = depot;
        state = BusState.outside;
    }

    public int getId() {
        return id;
    }

    public BusState getState(){
        return state;
    }

    public void setState(BusState state){
        this.state = state;
    }


    @Override
    public void run() {
        try {
            Thread.sleep((long)(Math.random()*1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        depot.requestEntrance(this);

        //While bus is not in depot, wait.
        synchronized (this){
            while (!state.equals(BusState.inside)){
                try {
                    wait();
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
                if(depot.getClosingTime()){
                    //System.out.println("Ok Bus " + id + " cant wait anymore, returning tomorrow.");
                    return;
                }
            }
        }

        //Randomly choose to clean or repair bus
        if(Math.random()>0.5)
            depot.cleanBus(this);
        else
            depot.serviceBus(this);

    }

    public String toString(){
        return "Bus "+id;
    }
}
