public class Mechanic implements Runnable {
    private int id;
    private Depot depot;

    public Mechanic(int id, Depot depot){
        this.id = id;
        this.depot = depot;
    }
    public int getId() {
        return id;
    }

    @Override
    public void run() {
        depot.goToWork(this);
    }
}
