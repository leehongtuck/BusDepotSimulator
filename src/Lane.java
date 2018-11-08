public class Lane {
    public synchronized void useLane(Bus bus, Boolean exit, Clock clock){
        System.out.println("Time " + clock.getTime() + "- Bus " + bus.getId() + " is on the ramp" );
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.out.println("Oops the bus is caught in an accident.");
        }
        if(exit == true){
            System.out.println("Time " + clock.getTime() + "- Bus " + bus.getId() + " has left the depot.");
        }else {
            System.out.println("Time " + clock.getTime() + "- Bus " + bus.getId() + " has entered the depot.");
        }
    }
}
