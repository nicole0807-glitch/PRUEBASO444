import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SaveLoad {
    private SaveLoad(){}

    public static void save(FileSystem fs,String file){
        try(BufferedWriter bw=new BufferedWriter(new FileWriter(file))){
            SimpleList<FileData> files=new SimpleList<>();
            fs.collectFiles(files);
            for(int i=0;i<files.size();i++){
                FileData fd=files.get(i);
                bw.write(fd.getPath()+";"+fd.getBlocks());
                bw.newLine();
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    public static void load(FileSystem fs,String file){
        File f=new File(file);
        if(!f.exists()) return;
        try(BufferedReader br=new BufferedReader(new FileReader(f))){
            String line;
            while((line=br.readLine())!=null){
                String[] parts=line.split(";");
                if(parts.length>=2){
                    String path=parts[0];
                    int blocks=Integer.parseInt(parts[1]);
                    int idx=path.lastIndexOf('/');
                    String parent=path.substring(0,idx);
                    if(parent.length()==0) parent="/";
                    String name=path.substring(idx+1);
                    ensurePath(fs,parent);
                    fs.createFile(parent,name,blocks);
                }
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private static void ensurePath(FileSystem fs,String path){
        if(path.equals("/")) return;
        String[] parts=path.split("/");
        String current="/";
        for(int i=0;i<parts.length;i++){
            if(parts[i]==null||parts[i].length()==0) continue;
            FileEntry fe=fs.findPath(current+parts[i]);
            if(fe==null){
                fs.createDirectory(current,parts[i]);
            }
            current=current+parts[i]+"/";
        }
    }
}
