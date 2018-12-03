public class Bus implements Runnable {
    private int id;
    private Depot depot;

    public Bus(int id, Depot depot) {
        this.id = id;
        this.depot = depot;
    }

    public int getId() {
        return id;
    }

    @Override
    public void run() {
        try {
            Thread.sleep((int)Math.floor(Math.random()*10000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        depot.enterDepot(this);

        depot.exitDepot(this);

    }
}
