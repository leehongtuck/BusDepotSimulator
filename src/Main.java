public class Main {
    public static void main(String[] args) throws InterruptedException {
        Depot d = new Depot();

        for(int i = 0; i<5; i++){
            new Thread(new Cleaner(i, d)).start();
            new Thread(new Mechanic(i, d)).start();
            new Thread(new Bus(i, d)).start();
        }
//        for(int i = 0; i<20; i++){
//            threads[i] = new Thread(new Bus(i, d));
//        threads[i].start();
//        Thread.sleep(100);
//    }

//        Thread t = new Thread(new MiniBus(5,d));
//        t.start();

    }
}
