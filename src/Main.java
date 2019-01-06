public class Main {
    public static void main(String[] args) throws InterruptedException {
        Depot d = new Depot();
        Thread threads[] = new Thread[20];
        Thread threadC[] = new Thread[5];
        Thread threadM[] = new Thread[5];

//        for(int i = 0; i<5; i++){
//            threadC[i] = new Thread(new Cleaner(i, d));
//            threadM[i] = new Thread(new Cleaner(i, d));
//        }
        for(int i = 0; i<20; i++){
            threads[i] = new Thread(new Bus(i, d));
            threads[i].start();
        }

//        Thread t = new Thread(new MiniBus(5,d));
//        t.start();

    }
}
