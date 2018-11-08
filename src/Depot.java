public class Depot {
    private Clock clock = new Clock();
    private Lane lane = new Lane();

    public Depot(){
    }

    public void exitDepot(Bus bus){
        System.out.println("Time " + clock.getTime() + "- Bus " + bus.getId() + " is preparing to leave the depot");
        lane.useLane(bus, true, clock);
    }

    public void enterDepot(Bus bus){
        System.out.println("Time " + clock.getTime() + "- Bus " + bus.getId() + " is preparing to enter the depot");
        lane.useLane(bus, false, clock);
    }
}
