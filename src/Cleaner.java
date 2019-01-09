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
        System.out.println("Bus " + bus.getId() + " is being cleaned by cleaner " + id);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Bus " + bus.getId() + " finished cleaning!");
        depot.requestExit(bus);
    }
}
