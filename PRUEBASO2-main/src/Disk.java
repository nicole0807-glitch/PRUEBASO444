public class Disk {
    private DiskBlock[] blocks;
    private int head=0;

    public Disk(int total){
        blocks=new DiskBlock[total];
        for(int i=0;i<total;i++){blocks[i]=new DiskBlock(i);}
    }

    public int size(){return blocks.length;}
    public int getHead(){return head;}
    public void setHead(int h){head=h;}

    public DiskBlock getBlock(int i){return blocks[i];}

    public int peekFirstFree(){
        for(int i=0;i<blocks.length;i++){
            if(blocks[i].free) return i;
        }
        return -1;
    }

    public int allocate(String fileName,int count){
        int[] selected=new int[count];
        int found=0;
        for(int i=0;i<blocks.length && found<count;i++){
            if(blocks[i].free){selected[found]=i;found++;}
        }
        if(found<count) return -1;
        for(int i=0;i<count;i++){
            int idx=selected[i];
            blocks[idx].free=false;
            blocks[idx].fileName=fileName;
            blocks[idx].next=(i==count-1)?-1:selected[i+1];
        }
        return selected[0];
    }

    public void freeBlocks(int start){
        int current=start;
        while(current!=-1 && current<blocks.length){
            DiskBlock b=blocks[current];
            int nxt=b.next;
            b.free=true;b.fileName="";b.next=-1;
            current=nxt;
        }
    }

    public int countFree(){
        int free=0;
        for(int i=0;i<blocks.length;i++){
            if(blocks[i].free) free++;
        }
        return free;
    }
}
