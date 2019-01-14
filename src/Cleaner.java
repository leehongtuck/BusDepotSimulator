public class Cleaner implements Runnable {
    private int id;
    private Depot depot;
    private int duration;

    public Cleaner(int id, Depot depot){
        this.id = id;
        this.depot = depot;
        duration = depot.getCleanDuration();
    }
    public int getId() {
        return id;
    }

    @Override
    public void run() {
        depot.goToWork(this);
    }

    public void cleanBus(Bus bus){
        System.out.println(DepotTime.getTime() + "Bus " + bus.getId() + " is being cleaned by cleaner " + id);
        for(int i = 0; i < 10; i++){
            try {
                Thread.sleep((long)((duration * 1000) * DepotTime.TIMESCALE)/10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (this){
                while (depot.isRaining()){
                    System.out.println(DepotTime.getTime() + "Oops it's raining, the cleaning of bus " + bus.getId() + " cannot continue!");
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(depot.isRaining() && depot.getClosingTime()){
                        depot.requestExit(bus);
                        return;
                    }
                    System.out.println(DepotTime.getTime() + "Yay the rain stopped, continue cleaning bus " + bus.getId() + "!");
                }
            }
        }

        System.out.println(DepotTime.getTime() + "Bus " + bus.getId() + " finished cleaning!");
        depot.standbyForWork(this);
        depot.requestExit(bus);
    }
}
