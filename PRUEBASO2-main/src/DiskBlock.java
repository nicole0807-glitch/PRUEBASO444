public class DiskBlock {
    public int index;
    public boolean free=true;
    public int next=-1;
    public String fileName="";
    public DiskBlock(int index){this.index=index;}
}
