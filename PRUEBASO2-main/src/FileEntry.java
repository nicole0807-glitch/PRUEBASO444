public abstract class FileEntry {
    protected String name;
    protected DirectoryEntry parent;

    public FileEntry(String name){this.name=name;}
    public String getName(){return name;}
    public void setName(String n){this.name=n;}
    public DirectoryEntry getParent(){return parent;}
    public void setParent(DirectoryEntry p){this.parent=p;}
    public String getPath(){
        if(parent==null) return "/";
        String prefix=parent.getPath();
        if("/".equals(prefix)) return "/"+name;
        return prefix+"/"+name;
    }
    public abstract boolean isDirectory();

    @Override
    public String toString(){
        if(parent==null || name.length()==0) return "/";
        return name;
    }
}
