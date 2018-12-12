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

    public void repairBus(Bus bus){
        System.out.println("Bus " + bus + " is being repaired!");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Bus " + bus + " finished repairing!");
    }
}
