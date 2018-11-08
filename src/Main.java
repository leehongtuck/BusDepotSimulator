public class Main {
    public static void main(String[] args) {
        Depot d = new Depot();
        Thread threads[] = new Thread[20];
        for(int i = 0; i<20; i++){
            threads[i] = new Thread(new Bus(i, d));
            threads[i].start();
        }

    }
}
