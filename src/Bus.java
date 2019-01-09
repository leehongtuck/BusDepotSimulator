public class Bus implements Runnable {
    private int id;
    private Depot depot;
    private BusState state;



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
        depot.requestEntrance(this);

        //While bus is not in depot, wait.
        synchronized (this){
            while (!state.equals(BusState.inside)){
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        //Randomly choose to clean or repair bus
        if(Math.random()>0.5)
            depot.cleanBus(this);
        else
            depot.repairBus(this);

    }
}
