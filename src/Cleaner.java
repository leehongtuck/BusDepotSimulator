public class Cleaner implements Runnable {
    private int id;
    private Depot depot;

    public Cleaner(int id, Depot depot){
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

    public void cleanBus(Bus bus){
        System.out.println("Bus " + bus + " is being cleaned!");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Bus " + bus + " finished cleaning!");
    }
}
