public class Scheduler {
    public static final int FIFO=0;
    public static final int SSTF=1;
    public static final int SCAN=2;
    public static final int CSCAN=3;

    private int policy=FIFO;
    private int direction=1;
    private Disk disk;
    private SimpleList<DiskRequest> queue=new SimpleList<>();

    public Scheduler(Disk disk){
        this.disk=disk;
    }

    public void setDisk(Disk disk){
        this.disk=disk;
    }

    public void setPolicy(int p){this.policy=p;}
    public int getPolicy(){return policy;}

    public void addRequest(DiskRequest req){
        queue.add(req);
    }

    public SimpleList<DiskRequest> flushOrder(){
        SimpleList<DiskRequest> ordered=new SimpleList<>();
        if(policy==FIFO){
            for(int i=0;i<queue.size();i++) ordered.add(queue.get(i));
        }else if(policy==SSTF){
            boolean[] used=new boolean[queue.size()];
            int head=disk.getHead();
            for(int k=0;k<queue.size();k++){
                int best=-1;int bestDist=Integer.MAX_VALUE;
                for(int i=0;i<queue.size();i++){
                    if(used[i]) continue;
                    int dist=Math.abs(queue.get(i).getTargetBlock()-head);
                    if(dist<bestDist){best=i;bestDist=dist;}
                }
                used[best]=true;
                DiskRequest r=queue.get(best);
                ordered.add(r);
                head=r.getTargetBlock();
            }
            disk.setHead(head);
        }else if(policy==SCAN || policy==CSCAN){
            int head=disk.getHead();
            int max=disk.size()-1;
            DiskRequest[] arr=new DiskRequest[queue.size()];
            for(int i=0;i<queue.size();i++) arr[i]=queue.get(i);
            // simple bubble sort by target
            for(int i=0;i<arr.length;i++){
                for(int j=0;j<arr.length-1;j++){
                    if(arr[j].getTargetBlock()>arr[j+1].getTargetBlock()){
                        DiskRequest tmp=arr[j];arr[j]=arr[j+1];arr[j+1]=tmp;
                    }
                }
            }
            if(policy==SCAN){
                if(direction>0){
                    for(int i=0;i<arr.length;i++) if(arr[i].getTargetBlock()>=head) ordered.add(arr[i]);
                    for(int i=arr.length-1;i>=0;i--) if(arr[i].getTargetBlock()<head) ordered.add(arr[i]);
                }else{
                    for(int i=arr.length-1;i>=0;i--) if(arr[i].getTargetBlock()<=head) ordered.add(arr[i]);
                    for(int i=0;i<arr.length;i++) if(arr[i].getTargetBlock()>head) ordered.add(arr[i]);
                }
                if(ordered.size()>0) disk.setHead(((DiskRequest)ordered.get(ordered.size()-1)).getTargetBlock());
                direction*=-1;
            }else{
                for(int i=0;i<arr.length;i++) if(arr[i].getTargetBlock()>=head) ordered.add(arr[i]);
                for(int i=0;i<arr.length;i++) if(arr[i].getTargetBlock()<head) ordered.add(arr[i]);
                if(ordered.size()>0) disk.setHead(((DiskRequest)ordered.get(ordered.size()-1)).getTargetBlock());
                disk.setHead(max);
            }
        }
        queue.clear();
        return ordered;
    }
}
