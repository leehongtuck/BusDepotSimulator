//To keep track of time in depot
public class DepotTime implements Runnable {
    //simulated 1 min =  actual 0.01 secs
    final static double TIMESCALE = 0.01;

    private Depot depot;
    private static final int OPENHOURS = 8;
    private static double time = 0;
    private static int hour = 0;
    private static int minute = 0;

    public DepotTime(Depot depot){
        this.depot = depot;
    }

    @Override
    public void run() {
        for (int i = 0; i < (7.5* 60); i++){
            if(Math.random()<0.002){
                depot.changeWeather();
            }
            increaseTime();
            try {
            Thread.sleep((long)(1000 * TIMESCALE));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        depot.setClosingTime();

        while (!depot.isEmpty()){
            increaseTime();
            try {
                Thread.sleep((long)(1000* TIMESCALE));
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    private void increaseTime() {
        if(time >= 24* 60){
            time = -8 * 60;
        }
        time++;
        hour = OPENHOURS + (int) time / 60;
        minute = Math.abs((int) time % 60);
    }

    public static String getTime() {
        return String.format("%02d", hour) + ":" + String.format("%02d", minute) + " - ";
    }
}