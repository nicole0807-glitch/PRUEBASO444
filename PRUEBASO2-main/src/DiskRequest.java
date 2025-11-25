public class DiskRequest {
    private ProcessFS process;
    private int targetBlock;

    public DiskRequest(ProcessFS p,int target){
        this.process=p;this.targetBlock=target;
    }
    public ProcessFS getProcess(){return process;}
    public int getTargetBlock(){return targetBlock;}
}
