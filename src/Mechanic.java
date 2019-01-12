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

    public void serviceBus(Bus bus){
        System.out.println(DepotTime.getTime() + "Bus " + bus.getId() + " is being serviced by mechanic " + id);
        if(Math.random()<0.2){
            System.out.println(DepotTime.getTime() + "Oh no! Bus " + bus.getId() + " seems to be short of fuel! Getting it refilled now!");
            try {
                Thread.sleep((long)(5000 * DepotTime.TIMESCALE));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(DepotTime.getTime() + "Finished refueling bus " + bus.getId() + ". Time to resume servicing!");
        }

        try {
            Thread.sleep((long)(60000 * DepotTime.TIMESCALE));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        if(Math.random()<0.2){
            System.out.println(DepotTime.getTime() + "Oh no! Bus " + bus.getId() + " is found to have a mechanical failure! Fixing it ASAP!");
            try {
                Thread.sleep((long)(120000 * DepotTime.TIMESCALE));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(DepotTime.getTime() + "Phew! Bus " + bus.getId() + " managed to be fixed! Service completed!");
        }else {
            System.out.println(DepotTime.getTime() + "Bus " + bus.getId() + " finished servicing!");
        }

        if(Math.random()<0.2){
            System.out.println(DepotTime.getTime() + "Bus " + bus.getId() + " is too dirty to be driven!. Go to cleaning bay ASAP!");
            depot.cleanBus(bus);
            return;
        }

        depot.requestExit(bus);
    }
}
