/*
 * JDicom2DoseView.java
 */

package jdicom2dose;

import java.awt.Toolkit;
import java.awt.Image;
import java.text.DecimalFormat;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import java.io.File;
import java.awt.geom.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


/**
 * La ventana principal de la aplicación.
 */
public class JDicom2DoseView extends FrameView {

    private JD2DSettings set;
    private int[] iBeamIndexInCombo = new int[20];
    private File[] choosedFiles;
    private int iSelectInputOption=0;
    private DecimalFormat df4Dec = new DecimalFormat("####0.0000");
    private DecimalFormat df2Dec = new DecimalFormat("####0.00");
    private boolean bThereAreSelectedFiles = false;

    public JDicom2DoseView(SingleFrameApplication app) {
        super(app);

        // change default icon
        Toolkit kit = Toolkit.getDefaultToolkit();
        //Image frameIcon = kit.getImage("F:\\JDicom2Dose\\src\\jdicom2dose\\resources\\caracol.png");
        //Image frameIcon = kit.getImage("src\\jdicom2dose\\resources\\caracol.png");
        Image frameIcon = kit.getImage("src\\jdicom2dose\\resources\\JD2D.png");
        getFrame().setIconImage(frameIcon);
        // PATH ..\resources\caracol.png;

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
        this.txtResult.setEditable(false);
        this.set=new JD2DSettings();
        this.fillWithSettings();
        this.enableInterControls();
        this.enableOutPutControls();

    }

    private void selectImputOptionChanged () {
        this.iSelectInputOption=this.jComboBox1.getSelectedIndex();
        this.enableControls();
    }

    private void enableInterControls () {
        this.lvlPixXPix.setEnabled(this.chkIsInterpolating.isSelected());
        this.lvlPixels.setEnabled(this.chkIsInterpolating.isSelected());
        this.txtIntX.setEnabled(this.chkIsInterpolating.isSelected());
        this.txtIntY.setEnabled(this.chkIsInterpolating.isSelected());
    }

    private void enableOutPutControls () {
        boolean isAnyOutput = ((this.chkIMGOutput.isSelected())||(this.chkOPGOutput.isSelected()));
        this.butLetsGo.setEnabled(isAnyOutput & this.bThereAreSelectedFiles);
    }

    private void enableControls () {
        int i=0;
        int iCase=this.iSelectInputOption;
        boolean bCorr = true;
        boolean bInter = true;
        boolean bOutput = true;
        switch(iCase) {
            case JD2DCtes.INPOP_DICOM:
                bCorr = true;
                bInter = true;
                bOutput = true;
                break;
            case JD2DCtes.INPOP_OPG:
                bCorr = true;
                bInter = true;
                bOutput = true;
                break;
            case JD2DCtes.INPOP_SEGMENTS:
                bCorr = true;
                bInter = true;
                bOutput = true;
                break;
            case JD2DCtes.INPOP_DICOM_SIN_TRANSF:
                bCorr = false;
                bInter = true;
                bOutput = true;
                break;
            case JD2DCtes.INPOP_DICOMRTDOSE_PCRT:
                bCorr = false;
                bInter = true;
                bOutput = true;
                this.txtIntX.setText(String.valueOf(set.getNumPixPCRT()));
                this.txtIntY.setText(String.valueOf(set.getNumPixPCRT()));
                this.chkIsInterpolating.setSelected(set.isInterpPCRTFiles());
                break;
            case JD2DCtes.INPOP_ROI_SIN_TRANSF:
                bCorr = false;
                bInter = false;
                bOutput = false;
                break;
            default:
                bCorr = false;
                bInter = false;
                bOutput = false;
                break;
        }
        this.enableInterControls();
        this.enableOutPutControls();
        this.cbBeamConfig.setEnabled(bCorr);
        this.lvlBeams.setEnabled(bCorr);
        for (i=0;i<this.pnlCorrections.getComponentCount();i++){
            this.pnlCorrections.getComponent(i).setEnabled(bCorr);
        }
        for (i=0;i<this.pnlInterpol.getComponentCount();i++){
            this.pnlInterpol.getComponent(i).setEnabled(bInter);
        }
        for (i=0;i<this.pnlOutput.getComponentCount();i++){
            this.pnlOutput.getComponent(i).setEnabled(bOutput);
        }
        
    }

    private void fillWithSettings() {
        this.txtIntX.setText(String.valueOf(set.getNumPixIntDefX()));
        this.txtIntY.setText(String.valueOf(set.getNumPixIntDefY()));
        this.chkIMGOutput.setSelected(set.isGenerateTIFFile());
        this.chkIsInterpolating.setSelected(set.isInterpolateFiles());
        this.chkOPGOutput.setSelected(set.isGenerateASCIIFile());
        this.chkToIsocenter.setSelected(set.isTrasl2Isocenter());
        this.fillComboBeamSettings();
    }

    private void fillComboBeamSettings() {
        this.cbBeamConfig.removeAllItems();
        int j=0;
        for (int i=0;i<20;i++) {
            if (set.isBeamOn(i)){
                this.iBeamIndexInCombo[j]=i;
                this.cbBeamConfig.addItem(set.getBeamName(i));
                j++;
            }
        }
    }

    private void beamSettingsChanged () {
        String sAux="";
        if (this.cbBeamConfig.getSelectedIndex()>-1) {
            int iIndex = this.iBeamIndexInCombo[this.cbBeamConfig.getSelectedIndex()];
            this.txtFFFPath.setText(set.getBeamFFFile(iIndex));
            sAux=" Cf = " + this.df2Dec.format(set.getCoefCal(iIndex)) + " 1/cGy";
            this.lvlDoseCoef.setText(sAux);
            sAux=" A1 = " + this.df4Dec.format(set.getILagA1(iIndex)) + " ";
            this.lvlA1.setText(sAux);
            sAux=" A2 = " + this.df4Dec.format(set.getILagA2(iIndex)) + " ";
            this.lvlA2.setText(sAux);
            sAux=" A3 = " + this.df4Dec.format(set.getILagA3(iIndex)) + " ";
            this.lvlA3.setText(sAux);
            sAux=" k1 = " + this.df4Dec.format(set.getILagk1(iIndex)) + " ";
            this.lvlk1.setText(sAux);
            sAux=" k2 = " + this.df4Dec.format(set.getILagk2(iIndex)) + " ";
            this.lvlk2.setText(sAux);
            sAux=" k3 = " + this.df4Dec.format(set.getILagk3(iIndex)) + " ";
            this.lvlk3.setText(sAux);
        }
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = JDicom2DoseApp.getApplication().getMainFrame();
            aboutBox = new JDicom2DoseAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        JDicom2DoseApp.getApplication().show(aboutBox);
    }

    @Action
    public void showPCRTConfig() {
        if (pcrtBox == null) {
            JFrame mainFrame = JDicom2DoseApp.getApplication().getMainFrame();
            pcrtBox = new JD2DPCRTOptions(set);
            pcrtBox.setLocationRelativeTo(mainFrame);
        }
        JDicom2DoseApp.getApplication().show(pcrtBox);
    }

    @Action
    public void showBeamConfig() {
        if (configBeamBox == null) {
            JFrame mainFrame = JDicom2DoseApp.getApplication().getMainFrame();
            configBeamBox = new JD2DBeamConfig(set);
            configBeamBox.setLocationRelativeTo(mainFrame);
        }
        JDicom2DoseApp.getApplication().show(configBeamBox);
        this.set.storeXML();
        this.fillComboBeamSettings();
    }

    @Action
    public void showGeneralConfig() {
        if (generalConfigBox == null) {
            JFrame mainFrame = JDicom2DoseApp.getApplication().getMainFrame();
            generalConfigBox = new JD2DGeneralConfig(set);
            generalConfigBox.setLocationRelativeTo(mainFrame);
        }
        JDicom2DoseApp.getApplication().show(generalConfigBox);
        this.set.storeXML();
    }

    @Action
    public void showFFFMaker() {
        if (fffMaker == null) {
            JFrame mainFrame = JDicom2DoseApp.getApplication().getMainFrame();
            fffMaker = new JD2DMatriXXFFFMaker(set);
            fffMaker.setLocationRelativeTo(mainFrame);
        }
        JDicom2DoseApp.getApplication().show(fffMaker);
    }

    public void callPCRT2OPG() {
        int iRes=0;
        StringBuffer sbf=new StringBuffer("");
        JFrame mainFrame = JDicom2DoseApp.getApplication().getMainFrame();
        //this.txtResult.setText("");
        JDosePlane dp;
        String sFileOut="";
        for (int i=0; i<this.choosedFiles.length; i++) {
            this.txtResult.append("Procesando el fichero " + this.choosedFiles[i].getName()+" ...");
            statusMessageLabel.setText("Procesando el fichero " + this.choosedFiles[i].getName()+" ...");
            progressBar.setString(this.choosedFiles[i].getName());
            dp=new JDosePlane(mainFrame,set);
            dp.setDataSource(JD2DCtes.DATA_PCRT);
            sbf.delete(0,sbf.length());
            //op.showInputDialog(null, "Mensaje","Título del Mensaje");
            iRes=dp.iLoadDICOMDoseFile(this.choosedFiles[i].getAbsolutePath(), sbf);
            //iRes = cho.showSaveDialog(mainFrame);
            //if (iRes == JFileChooser.APPROVE_OPTION) {
            if (iRes==0){
                this.txtResult.append(" PROCESADO.\n");
                if (this.chkToIsocenter.isSelected()){
                    dp.traslate2SSD();
                }
                if (set.isRenamePCRTFiles()){
                    String sNewPath = dp.getParentPath().trim()+"\\"+dp.getImageName().trim();
                    sFileOut=JD2DCtes.sSearchFileName(sNewPath, ".opg");
                }
                else {
                    sFileOut=dp.getFilePath()+".opg";
                }

                sbf.delete(0,sbf.length());
                this.txtResult.append("  Generando el fichero " + sFileOut+" ... ");
                statusMessageLabel.setText("Generando el fichero " + sFileOut+" ... ");
                if (this.chkOPGOutput.isSelected()){
                    iRes=dp.iOutputOPGFile(sFileOut, sbf, true,
                                       Integer.parseInt(this.txtIntX.getText()),
                                       Integer.parseInt(this.txtIntY.getText()));
                }
                if (this.chkIMGOutput.isSelected()){
                    iRes=dp.iOutputIMGFile(sFileOut, sbf, true,
                                       Integer.parseInt(this.txtIntX.getText()),
                                       Integer.parseInt(this.txtIntY.getText()));
                }
                if (iRes==0){
                    this.txtResult.append(" GENERADO.\n");
                }
                else {
                    this.txtResult.append(" ERROR.\n");
                    this.txtResult.append(sbf.toString());
                }
            }
            else {
                this.txtResult.append(" ERROR.\n");
                this.txtResult.append(sbf.toString());
            }
        }
        progressBar.setString(this.choosedFiles.length + " ficheros procesados.");
        statusMessageLabel.setText(this.choosedFiles.length + " ficheros procesados.");
        this.txtResult.append(this.choosedFiles.length + " ficheros procesados.\n" );
    }

    public void callDICOM2OPG(int iTransformMode) {
        int iRes=0;
        boolean willTransform=true;
        StringBuffer sbf=new StringBuffer("");
        JFrame mainFrame = JDicom2DoseApp.getApplication().getMainFrame();
        //this.txtResult.setText("");
        JDosePlane dp;
        String sFileOut="";
        String sSubfix="_corr.opg";
        if (iTransformMode==JD2DCtes.INPOP_DICOM_SIN_TRANSF) {
            willTransform=false;
            sSubfix=".opg";
        }
        for (int i=0; i<this.choosedFiles.length; i++) {
            this.txtResult.append("Procesando el fichero " + this.choosedFiles[i].getName()+" ...");
            statusMessageLabel.setText("Procesando el fichero " + this.choosedFiles[i].getName()+" ...");
            progressBar.setString(this.choosedFiles[i].getName());
            dp=new JDosePlane(mainFrame,set);
            sbf.delete(0,sbf.length());
            //op.showInputDialog(null, "Mensaje","Título del Mensaje");
            if ((iTransformMode==JD2DCtes.INPOP_DICOM) ||
                (iTransformMode==JD2DCtes.INPOP_DICOM_SIN_TRANSF)) {
                dp.setDataSource(JD2DCtes.DATA_DOSE);
                iRes=dp.iLoadDICOMDoseFile(this.choosedFiles[i].getAbsolutePath(), sbf);
            }
            else if (iTransformMode==JD2DCtes.INPOP_OPG) {
                dp.setDataSource(JD2DCtes.DATA_DOSE);
                iRes=dp.iLoadOPGDoseFile(this.choosedFiles[i].getAbsolutePath(), sbf);
            }
            else {
                this.txtResult.append("  Error en la variable iTransformMode (=" + iTransformMode + ") transformando el fichero " + sFileOut+" ... \n");
                iRes=-1;
            }
            //iRes = cho.showSaveDialog(mainFrame);
            //if (iRes == JFileChooser.APPROVE_OPTION) {
            if (iRes==0){
                this.txtResult.append(" PROCESADO.\n");
                if (this.chkToIsocenter.isSelected()){
                    dp.traslate2SSD();
                }
                if (set.isRenameFiless()){
                    String sNewPath = dp.getParentPath().trim()+"\\"+dp.getImageName().trim();
                    sFileOut=JD2DCtes.sSearchFileName(sNewPath, sSubfix);
                }
                else {
                    sFileOut=dp.getFilePath()+sSubfix;
                }

                if (willTransform) {
                    sbf.delete(0,sbf.length());
                    this.txtResult.append("  Transformando el fichero " + sFileOut+" ... ");
                    statusMessageLabel.setText("Transformando el fichero " + sFileOut+" ... ");
                    int iIndex = this.iBeamIndexInCombo[this.cbBeamConfig.getSelectedIndex()];
                    iRes=dp.iTransformPlane(iIndex, sbf);
                }

                sbf.delete(0,sbf.length());
                if (iRes==0){
                    this.txtResult.append("  Generando el fichero " + sFileOut+" ... ");
                    statusMessageLabel.setText("Generando el fichero " + sFileOut+" ... ");
                    if (this.chkOPGOutput.isSelected()){
                        iRes=dp.iOutputOPGFile(sFileOut, sbf, this.chkIsInterpolating.isSelected(),
                                           Integer.parseInt(this.txtIntX.getText()),
                                           Integer.parseInt(this.txtIntY.getText()));
                    }
                    if (this.chkIMGOutput.isSelected()){
                        iRes=dp.iOutputIMGFile(sFileOut, sbf, this.chkIsInterpolating.isSelected(),
                                           Integer.parseInt(this.txtIntX.getText()),
                                           Integer.parseInt(this.txtIntY.getText()));
                    }
                }
                if (iRes==0){
                    this.txtResult.append(" GENERADO.\n");
                }
                else {
                    this.txtResult.append(" ERROR.\n");
                    this.txtResult.append(sbf.toString());
                }
            }
            else {
                this.txtResult.append(" ERROR.\n");
                this.txtResult.append(sbf.toString());
            }
        }
        progressBar.setString(this.choosedFiles.length + " ficheros procesados.");
        statusMessageLabel.setText(this.choosedFiles.length + " ficheros procesados.");
        this.txtResult.append(this.choosedFiles.length + " ficheros procesados.\n" );
    }

    public void callDICOMSegments2OPG() {
        int iRes=0;
        int iSlice=0;
        boolean bOnlyPlane=false;
        boolean bIsSegmentPerf=false;
        StringBuffer sbf=new StringBuffer("");
        JFrame mainFrame = JDicom2DoseApp.getApplication().getMainFrame();
        JDosePlane dp;
        String sFileOut="";
        String sSubfix="";
        String sAux="";
        for (int i=0; i<this.choosedFiles.length; i++) {
            this.txtResult.append("Procesando el fichero " + this.choosedFiles[i].getName()+" ...");
            statusMessageLabel.setText("Procesando el fichero " + this.choosedFiles[i].getName()+" ...");
            progressBar.setString(this.choosedFiles[i].getName());
            dp=new JDosePlane(mainFrame,set);
            sbf.delete(0,sbf.length());
            dp.setDataSource(JD2DCtes.DATA_DOSE);
            iRes=dp.iLoadDICOMDoseFile(this.choosedFiles[i].getAbsolutePath(), sbf, true);
            if (iRes==0){
                for (iSlice=0;iSlice<dp.getNumSlices(); iSlice++){
                    this.txtResult.append(" PROCESADO.\n");
                    if (iSlice==0) {
                        bOnlyPlane=false;
                        bIsSegmentPerf=false;
                        sSubfix="_corr_tot.opg";
                        sAux="\t FICHERO TOTAL.";
                    }
                    else {
                        bOnlyPlane=true;
                        bIsSegmentPerf=true;
                        sSubfix="_corr_seg" + iSlice + ".opg";
                        sAux="\t Segmento: " + iSlice + ".";
                        iRes=dp.iLoadSliceDICOMDoseFile(iSlice);
                        if (iRes!=0){
                            this.txtResult.append(" Error en la generación del segmento " + iSlice + ".\n");
                            return;
                        }
                    }
                    if (this.chkToIsocenter.isSelected()){
                        dp.traslate2SSD(bOnlyPlane,bIsSegmentPerf);
                    }
                    if (set.isRenameFiless()){
                        String sNewPath = dp.getParentPath().trim()+"\\"+dp.getImageName().trim();
                        sFileOut=JD2DCtes.sSearchFileName(sNewPath, sSubfix);
                    }
                    else {
                        sFileOut=dp.getFilePath()+sSubfix;
                    }

                    sbf.delete(0,sbf.length());
                    this.txtResult.append("Transformando " +sAux+" ... \n");
                    statusMessageLabel.setText("Transformando el fichero " + sFileOut+sAux+" ... ");
                    int iIndex = this.iBeamIndexInCombo[this.cbBeamConfig.getSelectedIndex()];
                    iRes=dp.iTransformPlane(iIndex, sbf);

                    sbf.delete(0,sbf.length());
                    if (iRes==0){
                        this.txtResult.append("  Generando " + sAux+" ... \n");
                        statusMessageLabel.setText("Generando el fichero " + sFileOut+sAux+" ... ");
                        if (this.chkOPGOutput.isSelected()){
                            iRes=dp.iOutputOPGFile(sFileOut, sbf, true,
                                               Integer.parseInt(this.txtIntX.getText()),
                                               Integer.parseInt(this.txtIntY.getText()));
                        }
                        if (this.chkIMGOutput.isSelected()){
                            iRes=dp.iOutputIMGFile(sFileOut, sbf, true,
                                               Integer.parseInt(this.txtIntX.getText()),
                                               Integer.parseInt(this.txtIntY.getText()));
                        }
                    }
                    if (iRes==0){
                        this.txtResult.append(sFileOut +" GENERADO.\n");
                    }
                    else {
                        this.txtResult.append(sFileOut + " ERROR.\n");
                        this.txtResult.append(sbf.toString());
                    }
                }

            }
            else {
                this.txtResult.append(" ERROR.\n");
                this.txtResult.append(sbf.toString());
            }
        }
        progressBar.setString(this.choosedFiles.length + " ficheros procesados.");
        statusMessageLabel.setText(this.choosedFiles.length + " ficheros procesados.");
        this.txtResult.append(this.choosedFiles.length + " ficheros procesados.\n" );
    }

    public void callAvgInROI() {
        int iRes=0;
        StringBuffer sbf=new StringBuffer("");
        StringBuilder sbfOut=new StringBuilder("");
        sbfOut.append("FILE;UM;SUBFRAMES;TONO_GRIS;DESES_GRIS;" + JD2DCtes.RET);
        JFrame mainFrame = JDicom2DoseApp.getApplication().getMainFrame();
        //this.txtResult.setText("");
        JDosePlane dp;
        Point2D.Double pAvgDesv = new Point2D.Double();
        String sAux="";
        for (int i=0; i<this.choosedFiles.length; i++) {
            this.txtResult.append("Procesando el fichero " + this.choosedFiles[i].getName()+" ...");
            statusMessageLabel.setText("Procesando el fichero " + this.choosedFiles[i].getName()+" ...");
            progressBar.setString(this.choosedFiles[i].getName());
            dp=new JDosePlane(mainFrame,set);
            dp.setDataSource(JD2DCtes.DATA_DOSE);
            sbf.delete(0,sbf.length());
            //op.showInputDialog(null, "Mensaje","Título del Mensaje");
            iRes=dp.iLoadDICOMDoseFile(this.choosedFiles[i].getAbsolutePath(), sbf);
            //iRes = cho.showSaveDialog(mainFrame);
            //if (iRes == JFileChooser.APPROVE_OPTION) {
            if (iRes==0){
                pAvgDesv=dp.averageInROI(set.getX1ForROI(),set.getX2ForROI(),
                                         set.getY1ForROI(),set.getY2ForROI());
                sAux=dp.getImageName() + ";" + df2Dec.format(dp.getUM()) + ";" +
                     dp.getNumSubframes() + ";" + df4Dec.format(pAvgDesv.x) +
                     ";"+df4Dec.format(pAvgDesv.y) + ";" + JD2DCtes.RET;
                sbfOut.append(sAux);
                this.txtResult.append(" PROCESADO.\n");
            }
            else {
                this.txtResult.append(" ERROR.\n");
                this.txtResult.append(sbf.toString());
            }
        }
        JFileChooser cho = new JFileChooser();
        cho.setDialogType(JFileChooser.SAVE_DIALOG);
        cho.setMultiSelectionEnabled(false);
        File flPath = new File(set.getWorkPath());
        if (flPath.exists()) {
            cho.setCurrentDirectory(flPath);
        }
        iRes = cho.showOpenDialog(mainFrame);
        if (iRes == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedWriter w = new BufferedWriter(new FileWriter(cho.getSelectedFile().getAbsolutePath()));
                w.write(sbfOut.toString());
                w.close();
            }
            catch (IOException e){
                this.txtResult.append(" ERROR.\n");
                this.txtResult.append(e.toString());
            }
        }
        progressBar.setString(this.choosedFiles.length + " ficheros procesados.");
        statusMessageLabel.setText(this.choosedFiles.length + " ficheros procesados.");
        this.txtResult.append(this.choosedFiles.length + " ficheros procesados.\n" );
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        butBrowseFile = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtResult = new javax.swing.JTextArea();
        pnlOptions = new javax.swing.JPanel();
        lvlBeams = new javax.swing.JLabel();
        cbBeamConfig = new javax.swing.JComboBox();
        pnlCorrections = new javax.swing.JPanel();
        lvlDoseCoef = new javax.swing.JLabel();
        lvlA1 = new javax.swing.JLabel();
        lvlA2 = new javax.swing.JLabel();
        lvlA3 = new javax.swing.JLabel();
        lvlk1 = new javax.swing.JLabel();
        lvlk2 = new javax.swing.JLabel();
        lvlk3 = new javax.swing.JLabel();
        lvlFFFFile = new javax.swing.JLabel();
        txtFFFPath = new javax.swing.JTextField();
        pnlInterpol = new javax.swing.JPanel();
        txtIntX = new javax.swing.JTextField();
        lvlPixXPix = new javax.swing.JLabel();
        lvlPixels = new javax.swing.JLabel();
        txtIntY = new javax.swing.JTextField();
        chkIsInterpolating = new javax.swing.JCheckBox();
        chkToIsocenter = new javax.swing.JCheckBox();
        pnlOutput = new javax.swing.JPanel();
        chkOPGOutput = new javax.swing.JCheckBox();
        chkIMGOutput = new javax.swing.JCheckBox();
        butLetsGo = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        mnuConfigBeams = new javax.swing.JMenuItem();
        mnuConfigPCRT = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        mnuMakeFFFMatriXX = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setFocusable(false);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(jdicom2dose.JDicom2DoseApp.class).getContext().getResourceMap(JDicom2DoseView.class);
        mainPanel.setFont(resourceMap.getFont("mainPanel.font")); // NOI18N
        mainPanel.setName("mainPanel"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("lvlOpcion.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("lvlOpcion.text")); // NOI18N
        jLabel1.setName("lvlOpcion"); // NOI18N

        jComboBox1.setFont(resourceMap.getFont("cmbOpcion.font")); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ficheros DICOM de Therapist a planos de dosis...", "Ficheros OmniPro I'mRT (OPG sin corregir) a planos de dosis...", "Segmentos IMRT de fichero DICOM de Therapist a planos de dosis...", "Ficheros DICOM de Therapist a ficheros OmniPro I'mRT (OPG sin corregir)...", "Extraer datos de ROI de ficheros DICOM de Therapist...", "Ficheros DICOM RTDOSE de PCRT a formato OPG (OmniPro I'mRT)" }));
        jComboBox1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jComboBox1.setName("cmbOpcion"); // NOI18N
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        butBrowseFile.setFont(resourceMap.getFont("butBrowseFile.font")); // NOI18N
        butBrowseFile.setLabel(resourceMap.getString("butBrowseFile.label")); // NOI18N
        butBrowseFile.setName("butBrowseFile"); // NOI18N
        butBrowseFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butBrowseFileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(butBrowseFile, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, 0, 351, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(butBrowseFile, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        txtResult.setColumns(20);
        txtResult.setFont(resourceMap.getFont("txtResult.font")); // NOI18N
        txtResult.setRows(5);
        txtResult.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        txtResult.setName("txtResult"); // NOI18N
        jScrollPane1.setViewportView(txtResult);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                .addContainerGap())
        );

        pnlOptions.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlOptions.border.title"))); // NOI18N
        pnlOptions.setForeground(resourceMap.getColor("pnlOptions.foreground")); // NOI18N
        pnlOptions.setName("pnlOptions"); // NOI18N

        lvlBeams.setFont(resourceMap.getFont("lvlBeams.font")); // NOI18N
        lvlBeams.setText(resourceMap.getString("lvlBeams.text")); // NOI18N
        lvlBeams.setName("lvlBeams"); // NOI18N

        cbBeamConfig.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbBeamConfig.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        cbBeamConfig.setName("cbBeamConfig"); // NOI18N
        cbBeamConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbBeamConfigActionPerformed(evt);
            }
        });

        pnlCorrections.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlCorrections.border.title"))); // NOI18N
        pnlCorrections.setName("pnlCorrections"); // NOI18N

        lvlDoseCoef.setText(resourceMap.getString("lvlDoseCoef.text")); // NOI18N
        lvlDoseCoef.setName("lvlDoseCoef"); // NOI18N

        lvlA1.setText(resourceMap.getString("lvlA1.text")); // NOI18N
        lvlA1.setName("lvlA1"); // NOI18N

        lvlA2.setText(resourceMap.getString("lvlA2.text")); // NOI18N
        lvlA2.setName("lvlA2"); // NOI18N

        lvlA3.setText(resourceMap.getString("lvlA3.text")); // NOI18N
        lvlA3.setName("lvlA3"); // NOI18N

        lvlk1.setText(resourceMap.getString("lvlk1.text")); // NOI18N
        lvlk1.setName("lvlk1"); // NOI18N

        lvlk2.setText(resourceMap.getString("lvlk2.text")); // NOI18N
        lvlk2.setName("lvlk2"); // NOI18N

        lvlk3.setText(resourceMap.getString("lvlk3.text")); // NOI18N
        lvlk3.setName("lvlk3"); // NOI18N

        lvlFFFFile.setText(resourceMap.getString("lvlFFFFile.text")); // NOI18N
        lvlFFFFile.setName("lvlFFFFile"); // NOI18N

        txtFFFPath.setEditable(false);
        txtFFFPath.setFont(resourceMap.getFont("txtFFFPath.font")); // NOI18N
        txtFFFPath.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtFFFPath.setText(resourceMap.getString("txtFFFPath.text")); // NOI18N
        txtFFFPath.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        txtFFFPath.setFocusable(false);
        txtFFFPath.setName("txtFFFPath"); // NOI18N

        javax.swing.GroupLayout pnlCorrectionsLayout = new javax.swing.GroupLayout(pnlCorrections);
        pnlCorrections.setLayout(pnlCorrectionsLayout);
        pnlCorrectionsLayout.setHorizontalGroup(
            pnlCorrectionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCorrectionsLayout.createSequentialGroup()
                .addGroup(pnlCorrectionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlCorrectionsLayout.createSequentialGroup()
                        .addComponent(lvlFFFFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtFFFPath, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE))
                    .addGroup(pnlCorrectionsLayout.createSequentialGroup()
                        .addGroup(pnlCorrectionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lvlk1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lvlA1, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlCorrectionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lvlk2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lvlA2, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE))
                        .addGap(10, 10, 10)
                        .addGroup(pnlCorrectionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lvlk3, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                            .addComponent(lvlA3, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)))
                    .addComponent(lvlDoseCoef, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlCorrectionsLayout.setVerticalGroup(
            pnlCorrectionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCorrectionsLayout.createSequentialGroup()
                .addComponent(lvlDoseCoef)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlCorrectionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lvlA1)
                    .addComponent(lvlA2)
                    .addComponent(lvlA3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlCorrectionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lvlk1)
                    .addComponent(lvlk2)
                    .addComponent(lvlk3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlCorrectionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lvlFFFFile)
                    .addComponent(txtFFFPath, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        pnlInterpol.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlInterpol.border.title"))); // NOI18N
        pnlInterpol.setName("pnlInterpol"); // NOI18N

        txtIntX.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtIntX.setText(resourceMap.getString("txtIntX.text")); // NOI18N
        txtIntX.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        txtIntX.setMinimumSize(new java.awt.Dimension(50, 20));
        txtIntX.setName("txtIntX"); // NOI18N

        lvlPixXPix.setFont(resourceMap.getFont("lvlPixXPix.font")); // NOI18N
        lvlPixXPix.setText(resourceMap.getString("lvlPixXPix.text")); // NOI18N
        lvlPixXPix.setName("lvlPixXPix"); // NOI18N

        lvlPixels.setFont(resourceMap.getFont("lvlPixels.font")); // NOI18N
        lvlPixels.setText(resourceMap.getString("lvlPixels.text")); // NOI18N
        lvlPixels.setName("lvlPixels"); // NOI18N

        txtIntY.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtIntY.setText(resourceMap.getString("txtIntY.text")); // NOI18N
        txtIntY.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        txtIntY.setMinimumSize(new java.awt.Dimension(50, 20));
        txtIntY.setName("txtIntY"); // NOI18N

        chkIsInterpolating.setFont(resourceMap.getFont("chkIsInterpolating.font")); // NOI18N
        chkIsInterpolating.setSelected(true);
        chkIsInterpolating.setText(resourceMap.getString("chkIsInterpolating.text")); // NOI18N
        chkIsInterpolating.setName("chkIsInterpolating"); // NOI18N
        chkIsInterpolating.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chkIsInterpolatingStateChanged(evt);
            }
        });

        chkToIsocenter.setFont(resourceMap.getFont("chkToIsocenter.font")); // NOI18N
        chkToIsocenter.setSelected(true);
        chkToIsocenter.setText(resourceMap.getString("chkToIsocenter.text")); // NOI18N
        chkToIsocenter.setName("chkToIsocenter"); // NOI18N

        javax.swing.GroupLayout pnlInterpolLayout = new javax.swing.GroupLayout(pnlInterpol);
        pnlInterpol.setLayout(pnlInterpolLayout);
        pnlInterpolLayout.setHorizontalGroup(
            pnlInterpolLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlInterpolLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlInterpolLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlInterpolLayout.createSequentialGroup()
                        .addComponent(chkIsInterpolating)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtIntX, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lvlPixXPix)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtIntY, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lvlPixels))
                    .addGroup(pnlInterpolLayout.createSequentialGroup()
                        .addComponent(chkToIsocenter)
                        .addContainerGap(111, Short.MAX_VALUE))))
        );
        pnlInterpolLayout.setVerticalGroup(
            pnlInterpolLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlInterpolLayout.createSequentialGroup()
                .addGroup(pnlInterpolLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkIsInterpolating)
                    .addComponent(txtIntX, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lvlPixXPix)
                    .addComponent(txtIntY, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lvlPixels))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkToIsocenter)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlOutput.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlOutput.border.title"))); // NOI18N
        pnlOutput.setName("pnlOutput"); // NOI18N

        chkOPGOutput.setFont(resourceMap.getFont("chkOPGOutput.font")); // NOI18N
        chkOPGOutput.setSelected(true);
        chkOPGOutput.setText(resourceMap.getString("chkOPGOutput.text")); // NOI18N
        chkOPGOutput.setName("chkOPGOutput"); // NOI18N
        chkOPGOutput.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chkOPGOutputStateChanged(evt);
            }
        });

        chkIMGOutput.setFont(resourceMap.getFont("chkIMGOutput.font")); // NOI18N
        chkIMGOutput.setText(resourceMap.getString("chkIMGOutput.text")); // NOI18N
        chkIMGOutput.setName("chkIMGOutput"); // NOI18N
        chkIMGOutput.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chkIMGOutputStateChanged(evt);
            }
        });

        javax.swing.GroupLayout pnlOutputLayout = new javax.swing.GroupLayout(pnlOutput);
        pnlOutput.setLayout(pnlOutputLayout);
        pnlOutputLayout.setHorizontalGroup(
            pnlOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOutputLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkOPGOutput)
                    .addComponent(chkIMGOutput))
                .addContainerGap(185, Short.MAX_VALUE))
        );
        pnlOutputLayout.setVerticalGroup(
            pnlOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOutputLayout.createSequentialGroup()
                .addComponent(chkOPGOutput)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkIMGOutput)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        butLetsGo.setFont(resourceMap.getFont("butLetsGo.font")); // NOI18N
        butLetsGo.setForeground(resourceMap.getColor("butLetsGo.foreground")); // NOI18N
        butLetsGo.setText(resourceMap.getString("butLetsGo.text")); // NOI18N
        butLetsGo.setName("butLetsGo"); // NOI18N
        butLetsGo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butLetsGoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlOptionsLayout = new javax.swing.GroupLayout(pnlOptions);
        pnlOptions.setLayout(pnlOptionsLayout);
        pnlOptionsLayout.setHorizontalGroup(
            pnlOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(butLetsGo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlOutput, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlCorrections, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlOptionsLayout.createSequentialGroup()
                        .addComponent(lvlBeams)
                        .addGap(35, 35, 35)
                        .addComponent(cbBeamConfig, 0, 328, Short.MAX_VALUE))
                    .addComponent(pnlInterpol, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlOptionsLayout.setVerticalGroup(
            pnlOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOptionsLayout.createSequentialGroup()
                .addGroup(pnlOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lvlBeams)
                    .addComponent(cbBeamConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlCorrections, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlInterpol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(butLetsGo, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(140, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(154, 154, 154))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlOptions, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("configMenu.text")); // NOI18N
        fileMenu.setName("configMenu"); // NOI18N
        fileMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileMenuActionPerformed(evt);
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(jdicom2dose.JDicom2DoseApp.class).getContext().getActionMap(JDicom2DoseView.class, this);
        jMenuItem1.setAction(actionMap.get("showGeneralConfig")); // NOI18N
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        fileMenu.add(jMenuItem1);

        mnuConfigBeams.setAction(actionMap.get("showBeamConfig")); // NOI18N
        mnuConfigBeams.setText(resourceMap.getString("mnuConfigBeams.text")); // NOI18N
        mnuConfigBeams.setName("mnuConfigBeams"); // NOI18N
        fileMenu.add(mnuConfigBeams);

        mnuConfigPCRT.setAction(actionMap.get("showPCRTConfig")); // NOI18N
        mnuConfigPCRT.setText(resourceMap.getString("mnuConfigPCRT.text")); // NOI18N
        mnuConfigPCRT.setName("mnuConfigPCRT"); // NOI18N
        fileMenu.add(mnuConfigPCRT);

        menuBar.add(fileMenu);
        fileMenu.getAccessibleContext().setAccessibleName(resourceMap.getString("fileMenu.AccessibleContext.accessibleName")); // NOI18N

        jMenu1.setAction(actionMap.get("showFFFMaker")); // NOI18N
        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N

        mnuMakeFFFMatriXX.setAction(actionMap.get("showFFFMaker")); // NOI18N
        mnuMakeFFFMatriXX.setText(resourceMap.getString("mnuMakeFFFMatriXX.text")); // NOI18N
        mnuMakeFFFMatriXX.setName("mnuMakeFFFMatriXX"); // NOI18N
        jMenu1.add(mnuMakeFFFMatriXX);

        menuBar.add(jMenu1);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        helpMenu.add(exitMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N
        progressBar.setString(resourceMap.getString("progressBar.string")); // NOI18N
        progressBar.setStringPainted(true);

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 917, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 747, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        this.selectImputOptionChanged();
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void butBrowseFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butBrowseFileActionPerformed
        int iCase=this.iSelectInputOption;
        this.txtResult.setText("");
        JFrame mainFrame = JDicom2DoseApp.getApplication().getMainFrame();
        JFileChooser cho = new JFileChooser();
        cho.setMultiSelectionEnabled(true);
        File flPath = new File(set.getWorkPath());
        if (flPath.exists()) {
            cho.setCurrentDirectory(flPath);
        }
        JD2DFiltro filOPG = new JD2DFiltro(".opg","Ficheros OPG");
        JD2DFiltro filDCM = new JD2DFiltro(".dcm","Ficheros DICOM (*.dcm)");
        JD2DFiltro filIMA = new JD2DFiltro(".ima","Ficheros DICOM (*.ima)");
        switch(iCase) {
            case JD2DCtes.INPOP_DICOM:
                cho.addChoosableFileFilter(filIMA);
                break;
            case JD2DCtes.INPOP_OPG:
                cho.addChoosableFileFilter(filOPG);
                break;
            case JD2DCtes.INPOP_SEGMENTS:
                cho.addChoosableFileFilter(filIMA);
                break;
            case JD2DCtes.INPOP_DICOM_SIN_TRANSF:
                cho.addChoosableFileFilter(filIMA);
                cho.addChoosableFileFilter(filDCM);
                break;
            case JD2DCtes.INPOP_DICOMRTDOSE_PCRT:
                cho.addChoosableFileFilter(filDCM);
                break;
            case JD2DCtes.INPOP_ROI_SIN_TRANSF:
                cho.addChoosableFileFilter(filIMA);
                break;
            default:
                break;
        }
        int iRes = cho.showOpenDialog(mainFrame);
        if (iRes == JFileChooser.APPROVE_OPTION) {
            this.txtResult.setText("Selecionados "+ cho.getSelectedFiles().length+" ficheros:\n");
            for (int i=0;i<cho.getSelectedFiles().length;i++){
                this.txtResult.append("  "+ cho.getSelectedFiles()[i].getAbsolutePath()+"\n");
            }
            choosedFiles=cho.getSelectedFiles();
            this.bThereAreSelectedFiles=true;
        }
        else {
            this.bThereAreSelectedFiles=false;
            this.txtResult.setText("No se selecionó ningún fichero.\n");
        }
        this.enableOutPutControls();
    }//GEN-LAST:event_butBrowseFileActionPerformed

    private void cbBeamConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbBeamConfigActionPerformed
        this.beamSettingsChanged();
    }//GEN-LAST:event_cbBeamConfigActionPerformed

    private void fileMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileMenuActionPerformed
        this.showGeneralConfig();
    }//GEN-LAST:event_fileMenuActionPerformed

    private void chkIsInterpolatingStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chkIsInterpolatingStateChanged
        this.enableInterControls();
    }//GEN-LAST:event_chkIsInterpolatingStateChanged

    private void chkOPGOutputStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chkOPGOutputStateChanged
        this.enableOutPutControls();
    }//GEN-LAST:event_chkOPGOutputStateChanged

    private void chkIMGOutputStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chkIMGOutputStateChanged
        this.enableOutPutControls();
    }//GEN-LAST:event_chkIMGOutputStateChanged

    private void butLetsGoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butLetsGoActionPerformed
        this.butLetsGo.setEnabled(false);
        int iCase=this.iSelectInputOption;
        switch(iCase) {
            case JD2DCtes.INPOP_DICOM:
                this.callDICOM2OPG(JD2DCtes.INPOP_DICOM);
                break;
            case JD2DCtes.INPOP_OPG:
                this.callDICOM2OPG(JD2DCtes.INPOP_OPG);
                break;
            case JD2DCtes.INPOP_SEGMENTS:
                this.callDICOMSegments2OPG();
                break;
            case JD2DCtes.INPOP_DICOM_SIN_TRANSF:
                this.callDICOM2OPG(JD2DCtes.INPOP_DICOM_SIN_TRANSF);
                break;
            case JD2DCtes.INPOP_DICOMRTDOSE_PCRT:
                this.callPCRT2OPG();
                break;
            case JD2DCtes.INPOP_ROI_SIN_TRANSF:
                this.callAvgInROI();
                break;
            default:
                break;
        }
        this.butLetsGo.setEnabled(true);
    }//GEN-LAST:event_butLetsGoActionPerformed

    // Declaración de variables propias del GUI

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butBrowseFile;
    private javax.swing.JButton butLetsGo;
    private javax.swing.JComboBox cbBeamConfig;
    private javax.swing.JCheckBox chkIMGOutput;
    private javax.swing.JCheckBox chkIsInterpolating;
    private javax.swing.JCheckBox chkOPGOutput;
    private javax.swing.JCheckBox chkToIsocenter;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lvlA1;
    private javax.swing.JLabel lvlA2;
    private javax.swing.JLabel lvlA3;
    private javax.swing.JLabel lvlBeams;
    private javax.swing.JLabel lvlDoseCoef;
    private javax.swing.JLabel lvlFFFFile;
    private javax.swing.JLabel lvlPixXPix;
    private javax.swing.JLabel lvlPixels;
    private javax.swing.JLabel lvlk1;
    private javax.swing.JLabel lvlk2;
    private javax.swing.JLabel lvlk3;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem mnuConfigBeams;
    private javax.swing.JMenuItem mnuConfigPCRT;
    private javax.swing.JMenuItem mnuMakeFFFMatriXX;
    private javax.swing.JPanel pnlCorrections;
    private javax.swing.JPanel pnlInterpol;
    private javax.swing.JPanel pnlOptions;
    private javax.swing.JPanel pnlOutput;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JTextField txtFFFPath;
    private javax.swing.JTextField txtIntX;
    private javax.swing.JTextField txtIntY;
    private javax.swing.JTextArea txtResult;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
    private JD2DPCRTOptions pcrtBox;
    private JD2DBeamConfig configBeamBox;
    private JD2DGeneralConfig generalConfigBox;
    private JD2DMatriXXFFFMaker fffMaker;

}

