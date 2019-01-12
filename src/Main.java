public class Main {
    public static void main(String[] args) throws InterruptedException {
        Depot d = new Depot();

        for(int i = 0; i<50; i++){
            new Thread(new Cleaner(i, d)).start();
            new Thread(new Mechanic(i, d)).start();

        }
        for(int i = 0; i<100; i++){
                new Thread(new Bus(i, d)).start();
        }

//        Thread t = new Thread(new MiniBus(5,d));
//        t.start();

    }
}
