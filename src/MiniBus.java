public class MiniBus extends Bus {

    public MiniBus(int id, Depot depot) {
        super(id, depot);
    }

    public String toString(){
        return "Minibus "+ id;
    }
}
