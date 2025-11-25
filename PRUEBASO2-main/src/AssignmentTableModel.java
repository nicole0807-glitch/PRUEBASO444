import javax.swing.table.AbstractTableModel;

public class AssignmentTableModel extends AbstractTableModel {
    private SimpleList<FileData> data=new SimpleList<>();

    public void setData(SimpleList<FileData> list){
        data=list;
        fireTableDataChanged();
    }

    public FileData getFileAt(int row){
        if(row<0||row>=data.size()) return null;
        return data.get(row);
    }

    @Override
    public int getRowCount(){return data.size();}

    @Override
    public int getColumnCount(){return 4;}

    @Override
    public Object getValueAt(int row,int col){
        FileData fd=data.get(row);
        if(fd==null) return "";
        switch(col){
            case 0:return fd.getName();
            case 1:return fd.getBlocks();
            case 2:return fd.getStartBlock();
            case 3:return "#"+Integer.toHexString(fd.getColor().getRGB()).substring(2);
            default:return "";
        }
    }

    @Override
    public String getColumnName(int col){
        switch(col){
            case 0:return "Archivo";
            case 1:return "Bloques";
            case 2:return "Primer bloque";
            case 3:return "Color";
        }
        return super.getColumnName(col);
    }
}
