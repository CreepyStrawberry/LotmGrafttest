package Dipansu.net.untitled1;

public enum GraftMode{
    DAMAGE(40, "Damage Graft"),
    SWAP(25,"Positon Swap"),
    DEATH(60,"Death Graft");

    public final double cost;
    public final String display;
    GraftMode(double cost, String display) {
        this.cost = cost;
        this.display = display;
    }
}
