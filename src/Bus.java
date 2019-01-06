public class Bus implements Runnable {
    private int id;
    private Depot depot;
    private BusState state;



    public enum BusState{
        enter,exit,inside,outside
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
        if(Math.random()<0.75)
            depot.requestEntrance(this);
        else
            depot.requestExit(this);

    }
}
