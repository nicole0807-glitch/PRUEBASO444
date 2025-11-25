import java.awt.Color;

public class FileData extends FileEntry {
    private int blocks;
    private int startBlock;
    private Color color;

    public FileData(String name,int blocks){
        super(name);
        this.blocks=blocks;
        this.startBlock=-1;
        this.color=new Color((int)(Math.random()*200+30),(int)(Math.random()*200+30),(int)(Math.random()*200+30));
    }
    public int getBlocks(){return blocks;}
    public void setBlocks(int b){this.blocks=b;}
    public int getStartBlock(){return startBlock;}
    public void setStartBlock(int s){this.startBlock=s;}
    public Color getColor(){return color;}
    @Override
    public boolean isDirectory(){return false;}
}
