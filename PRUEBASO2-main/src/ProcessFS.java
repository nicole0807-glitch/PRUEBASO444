public class ProcessFS {
    private static int counter=1;
    private int pid;
    private String user;
    private String operation;
    private String target;
    private String state;

    public ProcessFS(String user,String operation,String target){
        this.pid=counter++;
        this.user=user;
        this.operation=operation;
        this.target=target;
        this.state="nuevo";
    }
    public int getPid(){return pid;}
    public String getUser(){return user;}
    public String getOperation(){return operation;}
    public String getTarget(){return target;}
    public String getState(){return state;}
    public void setState(String s){this.state=s;}
    @Override
    public String toString(){
        return "P"+pid+" ["+state+"] "+operation+" -> "+target;
    }
}
