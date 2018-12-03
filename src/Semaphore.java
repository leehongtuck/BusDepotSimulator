public class Semaphore {
    private int value;

    public Semaphore(int value){
        this.value = value;
    }

    public synchronized void up(){
        value++;
        notify();
    }

    public synchronized void down(){
        if(value == 0){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else {
            value--;
        }
    }
}























































