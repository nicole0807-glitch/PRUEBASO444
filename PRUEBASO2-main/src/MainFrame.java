import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class MainFrame extends JFrame {
    private FileSystem fs;
    private Scheduler scheduler;
    private AssignmentTableModel tableModel=new AssignmentTableModel();
    private JTree tree;
    private JPanel diskPanel;
    private JTable table;
    private DefaultListModel<String> processModel=new DefaultListModel<>();
    private JComboBox<String> modeCombo;
    private JComboBox<String> policyCombo;
    private JLabel diskInfoLabel;
    private String currentUser="admin";
    private SimpleList<ProcessFS> processes=new SimpleList<>();

    public MainFrame(){
        super("Simulador de Sistema de Archivos");
        fs=new FileSystem(64);
        scheduler=new Scheduler(fs.getDisk());
        SaveLoad.load(fs,"estado.txt");
        buildUI();
        refreshAll();
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                SaveLoad.save(fs,"estado.txt");
                System.exit(0);
            }
        });
    }

    private void buildUI(){
        setSize(900,700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        tree=new JTree();
        JScrollPane treeScroll=new JScrollPane(tree);
        treeScroll.setPreferredSize(new Dimension(250,400));

        diskPanel=new JPanel();
        diskPanel.setLayout(new GridLayout(8,8,2,2));
        diskInfoLabel=new JLabel();
        JPanel diskContainer=new JPanel(new BorderLayout());
        diskContainer.add(diskInfoLabel,BorderLayout.NORTH);
        diskContainer.add(diskPanel,BorderLayout.CENTER);
        JScrollPane diskScroll=new JScrollPane(diskContainer);

        table=new JTable(tableModel);
        JScrollPane tableScroll=new JScrollPane(table);

        JList<String> processList=new JList<>(processModel);
        JScrollPane procScroll=new JScrollPane(processList);
        procScroll.setPreferredSize(new Dimension(250,150));

        JPanel left=new JPanel(new BorderLayout());
        left.add(treeScroll,BorderLayout.CENTER);
        left.add(procScroll,BorderLayout.SOUTH);

        JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,left,diskScroll);
        split.setDividerLocation(300);
        add(split,BorderLayout.CENTER);

        JPanel south=new JPanel(new BorderLayout());
        south.add(tableScroll,BorderLayout.CENTER);
        south.add(buildControls(),BorderLayout.SOUTH);
        add(south,BorderLayout.SOUTH);
    }

    private JPanel buildControls(){
        JPanel p=new JPanel();
        JButton btnDir=new JButton("Crear directorio");
        JButton btnFile=new JButton("Crear archivo");
        JButton btnRen=new JButton("Renombrar");
        JButton btnDel=new JButton("Eliminar");
        JButton btnProc=new JButton("Simular cola");
        JButton btnSave=new JButton("Guardar");
        JButton btnLoad=new JButton("Cargar");
        modeCombo=new JComboBox<>(new String[]{"Administrador","Usuario"});
        policyCombo=new JComboBox<>(new String[]{"FIFO","SSTF","SCAN","C-SCAN"});

        btnDir.addActionListener(e->createDirectoryAction());
        btnFile.addActionListener(e->createFileAction());
        btnRen.addActionListener(e->renameAction());
        btnDel.addActionListener(e->deleteAction());
        btnProc.addActionListener(e->processQueue());
        btnSave.addActionListener(e->saveAction());
        btnLoad.addActionListener(e->loadAction());
        policyCombo.addActionListener(e->scheduler.setPolicy(policyCombo.getSelectedIndex()));
        modeCombo.addActionListener(e->{
            currentUser=modeCombo.getSelectedIndex()==0?"admin":"user";
            refreshAll();
        });

        p.add(new JLabel("Modo:"));p.add(modeCombo);
        p.add(new JLabel("Planificador:"));p.add(policyCombo);
        p.add(btnDir);p.add(btnFile);p.add(btnRen);p.add(btnDel);p.add(btnProc);p.add(btnSave);p.add(btnLoad);
        return p;
    }

    private void addProcess(ProcessFS p){
        processes.add(p);
        updateProcessList();
    }

    private void createDirectoryAction(){
        if(!isAdmin()) return;
        String parent=getSelectedPath();
        if(parent==null) parent="/";
        String name=JOptionPane.showInputDialog(this,"Nombre del directorio:");
        if(name!=null && name.length()>0){
            final String finalParent=parent;
            final String finalName=name;
            ProcessFS proc=new ProcessFS(currentUser,"crear dir",parent+"/"+name);
            scheduler.addRequest(new DiskRequest(proc,fs.getDisk().getHead()));
            addProcess(proc);
            executeProcess(proc,()->fs.createDirectory(finalParent,finalName));
        }
    }

    private void createFileAction(){
        String parent=getSelectedPath();
        if(parent==null) parent="/";
        String name=JOptionPane.showInputDialog(this,"Nombre del archivo:");
        String sblocks=JOptionPane.showInputDialog(this,"Tamaño en bloques:");
        if(name!=null && sblocks!=null){
            int b=Integer.parseInt(sblocks);
            final String finalParent=parent;
            final String finalName=name;
            final int finalBlocks=b;
            ProcessFS proc=new ProcessFS(currentUser,"crear archivo",parent+"/"+name);
            int target=fs.getDisk().peekFirstFree();
            if(target<0) target=0;
            scheduler.addRequest(new DiskRequest(proc,target));
            addProcess(proc);
            executeProcess(proc,()->{
                FileData created=fs.createFile(finalParent,finalName,finalBlocks);
                if(created==null) JOptionPane.showMessageDialog(this,"Sin espacio disponible");
            });
        }
    }

    private void renameAction(){
        if(!isAdmin()) return;
        String path=getSelectedPath();
        if(path==null||"/".equals(path)) return;
        String newName=JOptionPane.showInputDialog(this,"Nuevo nombre:");
        if(newName!=null){
            ProcessFS proc=new ProcessFS(currentUser,"renombrar",path);
            scheduler.addRequest(new DiskRequest(proc,fs.getDisk().getHead()));
            addProcess(proc);
            executeProcess(proc,()->fs.rename(path,newName));
        }
    }

    private void deleteAction(){
        if(!isAdmin()) return;
        String path=getSelectedPath();
        if(path==null||"/".equals(path)) return;
        ProcessFS proc=new ProcessFS(currentUser,"eliminar",path);
        int target=0;
        FileEntry fe=fs.findPath(path);
        if(fe!=null && !fe.isDirectory()) target=((FileData)fe).getStartBlock();
        scheduler.addRequest(new DiskRequest(proc,target));
        addProcess(proc);
        executeProcess(proc,()->fs.deletePath(path));
    }

    private void saveAction(){
        SaveLoad.save(fs,"estado.txt");
        JOptionPane.showMessageDialog(this,"Estado guardado correctamente");
    }

    private void loadAction(){
        resetFileSystem();
        SaveLoad.load(fs,"estado.txt");
        refreshAll();
        JOptionPane.showMessageDialog(this,"Estado cargado correctamente");
    }

    private void processQueue(){
        SimpleList<DiskRequest> ordered=scheduler.flushOrder();
        for(int i=0;i<ordered.size();i++){
            DiskRequest req=ordered.get(i);
            ProcessFS p=req.getProcess();
            p.setState("ejecutando");
            updateProcessList();
            p.setState("terminado");
        }
        updateProcessList();
        refreshAll();
    }

    private void executeProcess(ProcessFS proc,Runnable action){
        proc.setState("listo");
        updateProcessList();
        action.run();
        proc.setState("terminado");
        updateProcessList();
        refreshAll();
    }

    private String getSelectedPath(){
        Object sel=tree.getLastSelectedPathComponent();
        if(sel==null) return getSelectedPathFromTable();
        DefaultMutableTreeNode node=(DefaultMutableTreeNode)sel;
        Object obj=node.getUserObject();
        if(obj instanceof FileEntry){
            return ((FileEntry)obj).getPath();
        }
        return null;
    }

    private String getSelectedPathFromTable(){
        int row=table.getSelectedRow();
        if(row<0) return null;
        FileData fd=tableModel.getFileAt(row);
        if(fd!=null) return fd.getPath();
        return null;
    }

    private void resetFileSystem(){
        int totalBlocks=fs.getDisk().size();
        fs=new FileSystem(totalBlocks);
        scheduler.setDisk(fs.getDisk());
        processes=new SimpleList<>();
        updateProcessList();
    }

    private boolean isAdmin(){return "admin".equals(currentUser);}

    private void refreshAll(){
        refreshTree();
        refreshDiskPanel();
        refreshTable();
    }

    private void refreshTree(){
        DefaultMutableTreeNode rootNode=buildTree(fs.getRoot());
        DefaultTreeModel model=new DefaultTreeModel(rootNode);
        tree.setModel(model);
        for(int i=0;i<tree.getRowCount();i++) tree.expandRow(i);
    }

    private DefaultMutableTreeNode buildTree(FileEntry fe){
        DefaultMutableTreeNode node=new DefaultMutableTreeNode(fe);
        if(fe.isDirectory()){
            DirectoryEntry dir=(DirectoryEntry)fe;
            SimpleList<FileEntry> children=dir.getChildren();
            for(int i=0;i<children.size();i++){
                node.add(buildTree(children.get(i)));
            }
        }
        return node;
    }

    private void refreshDiskPanel(){
        diskPanel.removeAll();
        Disk d=fs.getDisk();
        int free=d.countFree();
        int total=d.size();
        diskInfoLabel.setText("Bloques libres: "+free+" / "+total+(free==0?" (Memoria llena)":""));
        for(int i=0;i<d.size();i++){
            DiskBlock b=d.getBlock(i);
            JLabel lbl=new JLabel(String.valueOf(i),SwingConstants.CENTER);
            lbl.setOpaque(true);
            if(b.free){
                lbl.setBackground(Color.WHITE);
                lbl.setForeground(Color.BLACK);
            }else{
                lbl.setBackground(Color.LIGHT_GRAY);
                lbl.setForeground(Color.BLACK);
                lbl.setToolTipText(b.fileName+" -> "+b.next);
            }
            lbl.setPreferredSize(new Dimension(30,30));
            diskPanel.add(lbl);
        }
        diskPanel.revalidate();
        diskPanel.repaint();
    }

    private void refreshTable(){
        SimpleList<FileData> files=new SimpleList<>();
        fs.collectFiles(files);
        tableModel.setData(files);
    }

    private void updateProcessList(){
        processModel.clear();
        for(int i=0;i<processes.size();i++){
            processModel.addElement(processes.get(i).toString());
        }
    }
}
