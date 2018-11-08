public class Clock{
    private double time = 0;
    private int hour = 0;
    private int minute = 0;

    public Clock(){
        Thread ClockIncrement = new Thread(new ClockIncrementer(this));
        ClockIncrement.start();
    }

    public synchronized void increaseTime(){
        time++;
        hour = (int)time/60;
        minute = (int)time%60;
    }

    public synchronized String getTime() {
        return String.format("%02d", hour) + ":" + String.format("%02d", minute);
    }

}
