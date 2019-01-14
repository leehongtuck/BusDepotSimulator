import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        Scanner sc = new Scanner(System.in);

        System.out.println("Bus Depot Simulator v0.1\n");
        System.out.print("How many buses do you want: ");
        int numBus = sc.nextInt();

        System.out.print("How many cleaners do you want: ");
        int numClean = sc.nextInt();

        System.out.print("What is the cleaning duration you want (minutes): ");
        int durClean = sc.nextInt();

        System.out.print("How many mechanics do you want: ");
        int numMech = sc.nextInt();

        System.out.print("What is the service duration you want (minutes): ");
        int durMech = sc.nextInt();

        System.out.print("How many waiting space do you want: ");
        int waitSpace = sc.nextInt();

        Depot d = new Depot(waitSpace, durClean, durMech);

        for(int i = 0; i<numClean; i++){
            new Thread(new Cleaner(i, d)).start();
        }

        for(int i = 0; i<numMech; i++){
            new Thread(new Mechanic(i, d)).start();
        }

        Thread t[] = new Thread[numBus];
        for(int i = 0; i<numBus; i++){
            t[i] = new Thread(new Bus(i, d));
            t[i].start();
        }

//        d.threadR.join();
//        int count = 0;
//        for(Thread th: t){
//
//            if(th.isAlive()){
//                System.out.println("Bus " + count + " still alive gg.");
//            }
//            count++;
//        }

//        Thread t = new Thread(new MiniBus(5,d));
//        t.start();

    }
}
