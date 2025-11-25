public class DirectoryEntry extends FileEntry {
    private SimpleList<FileEntry> children=new SimpleList<>();
    public DirectoryEntry(String name){super(name);}
    public void addChild(FileEntry child){
        children.add(child);
        child.setParent(this);
    }
    public void removeChild(FileEntry child){
        children.remove(child);
    }
    public SimpleList<FileEntry> getChildren(){return children;}
    @Override
    public boolean isDirectory(){return true;}
}
