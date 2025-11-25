public class FileSystem {
    private DirectoryEntry root;
    private Disk disk;

    public FileSystem(int blocks){
        root=new DirectoryEntry("");
        disk=new Disk(blocks);
    }

    public DirectoryEntry getRoot(){return root;}
    public Disk getDisk(){return disk;}

    public FileEntry findPath(String path){
        if(path.equals("/")||path.length()==0) return root;
        String[] parts=path.split("/");
        DirectoryEntry current=root;
        for(int i=0;i<parts.length;i++){
            if(parts[i]==null||parts[i].length()==0) continue;
            SimpleList<FileEntry> list=current.getChildren();
            boolean found=false;
            for(int j=0;j<list.size();j++){
                FileEntry fe=list.get(j);
                if(fe.getName().equals(parts[i])){
                    if(i==parts.length-1) return fe;
                    if(fe.isDirectory()){
                        current=(DirectoryEntry)fe;
                        found=true;break;
                    }
                }
            }
            if(!found) return null;
        }
        return current;
    }

    public boolean createDirectory(String parentPath,String name){
        FileEntry fe=findPath(parentPath);
        if(fe!=null && fe.isDirectory()){
            DirectoryEntry dir=(DirectoryEntry)fe;
            dir.addChild(new DirectoryEntry(name));
            return true;
        }
        return false;
    }

    public FileData createFile(String parentPath,String name,int blocks){
        FileEntry fe=findPath(parentPath);
        if(fe!=null && fe.isDirectory()){
            int start=disk.allocate(name,blocks);
            if(start==-1) return null;
            FileData file=new FileData(name,blocks);
            file.setStartBlock(start);
            ((DirectoryEntry)fe).addChild(file);
            return file;
        }
        return null;
    }

    public boolean deletePath(String path){
        FileEntry fe=findPath(path);
        if(fe==null||fe==root) return false;
        DirectoryEntry parent=fe.getParent();
        if(fe.isDirectory()){
            DirectoryEntry dir=(DirectoryEntry)fe;
            SimpleList<FileEntry> list=dir.getChildren();
            int total=list.size();
            Object[] arr=list.toArray();
            for(int i=0;i<total;i++){
                FileEntry child=(FileEntry)arr[i];
                deletePath(child.getPath());
            }
        }else{
            FileData fd=(FileData)fe;
            disk.freeBlocks(fd.getStartBlock());
        }
        parent.removeChild(fe);
        return true;
    }

    public boolean rename(String path,String newName){
        FileEntry fe=findPath(path);
        if(fe==null||fe==root) return false;
        fe.setName(newName);
        return true;
    }

    public void collectFiles(SimpleList<FileData> list){
        collectFilesRecursive(root,list);
    }
    private void collectFilesRecursive(DirectoryEntry dir,SimpleList<FileData> list){
        SimpleList<FileEntry> children=dir.getChildren();
        for(int i=0;i<children.size();i++){
            FileEntry fe=children.get(i);
            if(fe.isDirectory()) collectFilesRecursive((DirectoryEntry)fe,list);
            else list.add((FileData)fe);
        }
    }
}
