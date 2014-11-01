/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JD2DPCRTOptions.java
 *
 * Created on 02-sep-2010, 13:56:18
 */

package jdicom2dose;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.io.File;
import java.awt.geom.*;

/**
 *
 * @author u16066
 */
public class JD2DMatriXXFFFMaker extends javax.swing.JDialog {

    private JD2DSettings set;
    private JDosePlane dpMain;
    private int[] iBeamIndexInSettings = new int[20];
    private String sParentPath = "";
    private String sX1Y1Path = "";
    private String sX2Y1Path = "";
    private String sX1Y2Path = "";
    private String sX2Y2Path = "";
    private String sFFFPath = "";

    /** Creates new form JD2DPCRTOptions */
    public JD2DMatriXXFFFMaker(JD2DSettings s) {
        initComponents();
        set=s;
        dpMain = new JDosePlane(JDicom2DoseApp.getApplication().getMainFrame(),set);
        sParentPath=set.getWorkPath();
        this.fillCombo();
        this.refreshLabels();
    }

    private void fillCombo(){
        this.cbFields.removeAllItems();
        int j=0;
        for (int i=0;i<20;i++) {
            if (set.isBeamOn(i)){
                this.iBeamIndexInSettings[j]=i;
                this.cbFields.addItem(set.getBeamName(i));
                j++;
            }
        }
    }

    private void refreshLabels() {
        this.lvlX1Y1.setText("X1Y1: " + this.sX1Y1Path);
        this.lvlX1Y2.setText("X1Y2: " + this.sX1Y2Path);
        this.lvlX2Y1.setText("X2Y1: " + this.sX2Y1Path);
        this.lvlX2Y2.setText("X2Y2: " + this.sX2Y2Path);
        this.lvlFFF.setText(this.sFFFPath);
        if (this.sFFFPath.length()==0){
            this.butAsign.setEnabled(false);
            this.cbFields.setEnabled(false);
        }
        else {
            this.butAsign.setEnabled(true);
            this.cbFields.setEnabled(true);
        }
    }

    private int iShowBrowser(StringBuffer sFile){
        File fl= new File("");
        JFileChooser cho = new JFileChooser();
        cho.setMultiSelectionEnabled(false);
        cho.setFileSelectionMode(JFileChooser.FILES_ONLY);
        JD2DFiltro opgFilter = new JD2DFiltro(".opg","Ficheros OPG");
        cho.addChoosableFileFilter(opgFilter);
        File flPath = new File(this.sParentPath);
        if (flPath.exists()) {
            cho.setCurrentDirectory(flPath);
        }
        //JFrame mainFrame = this. .getApplication().getMainFrame();
        int iRes = cho.showOpenDialog(this);
        if (iRes == JFileChooser.APPROVE_OPTION) {
            fl=cho.getSelectedFile();
            sFile.append(fl.getAbsolutePath());
            this.sParentPath=fl.getParent();
        }
        return iRes;
    }

    private void loadX1Y1File() {
        StringBuffer sFile = new StringBuffer("");
        if (this.iShowBrowser(sFile) == JFileChooser.APPROVE_OPTION) {
            this.sX1Y1Path=sFile.toString();
            //JOptionPane.showMessageDialog(rootPane,"::sX1Y1Path::"+this.sX1Y1Path +"\n::sParentPath::"+ this.sParentPath);
        }
        else {
            this.sX1Y1Path="";
        }
        this.sFFFPath="";
        this.refreshLabels();
    }

    private void loadX1Y2File() {
        StringBuffer sFile = new StringBuffer("");
        if (this.iShowBrowser(sFile) == JFileChooser.APPROVE_OPTION) {
            this.sX1Y2Path=sFile.toString();
        }
        else {
            this.sX1Y2Path="";
        }
        this.sFFFPath="";
        this.refreshLabels();
    }
    private void loadX2Y1File() {
        StringBuffer sFile = new StringBuffer("");
        if (this.iShowBrowser(sFile) == JFileChooser.APPROVE_OPTION) {
            this.sX2Y1Path=sFile.toString();
        }
        else {
            this.sX2Y1Path="";
        }
        this.sFFFPath="";
        this.refreshLabels();
    }
    private void loadX2Y2File() {
        StringBuffer sFile = new StringBuffer("");
        if (this.iShowBrowser(sFile) == JFileChooser.APPROVE_OPTION) {
            this.sX2Y2Path=sFile.toString();
        }
        else {
            this.sX2Y2Path="";
        }
        this.sFFFPath="";
        this.refreshLabels();
    }

    private int iAsign() {
        int iRes=0;
        int iSel=this.cbFields.getSelectedIndex();
        int iField=0;
        if (iSel>=0) {
            iField=this.iBeamIndexInSettings[iSel];
        }
        else{
            JOptionPane.showMessageDialog(rootPane,"No se seleccionó ningún haz.");
            return -666;
        }
        String sNewPath=set.getConfigPath()+"\\"+this.set.getBeamName(iField)+"_FFF_conf.opg";
        StringBuffer sRes = new StringBuffer("");
        File fl = new File(this.sFFFPath);
        if (fl.exists()){
            Point2D.Double dAvg = new Point2D.Double();
            this.dpMain.clearObject();
            iRes=this.dpMain.iLoadOPGDoseFile(sFFFPath, sRes);
            if (iRes != 0) {
                JOptionPane.showMessageDialog(rootPane,"Ocurrió un error: " + sRes);
                return iRes;
            }
            this.dpMain.traslate2SSD();
            dAvg=this.dpMain.averageInROI(JD2DCtes.NORM_X1, JD2DCtes.NORM_X2, JD2DCtes.NORM_Y1, JD2DCtes.NORM_Y2);
            if (dAvg.x!=0.0) {
                this.dpMain.applyFactorInPlane(1/dAvg.x);
            }
            else {
                this.dpMain.applyFactorInPlane(0);
            }
            this.dpMain.setDataSource(JD2DCtes.DATA_OPG);
            this.dpMain.setImageName(this.set.getBeamName(iField)+"_FFF_conf");
            this.dpMain.setDcmName(this.set.getBeamName(iField)+"_FFF_conf.opg");
            iRes=this.dpMain.iOutputOPGFile(sNewPath, sRes, true, JD2DCtes.NUM_PIX_FFF, JD2DCtes.NUM_PIX_FFF);
            if (iRes != 0) {
                JOptionPane.showMessageDialog(rootPane,"Ocurrió un error: " + sRes);
                return iRes;
            }
            this.set.setBeamFFFile(iField, sNewPath);
            this.set.storeXML();
        }
        else {
            JOptionPane.showMessageDialog(rootPane,"El fichero "+this.sFFFPath+"no existe????");
        }
        return iRes;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        butAsign = new javax.swing.JButton();
        butExit = new javax.swing.JButton();
        butProcess = new javax.swing.JButton();
        butX1Y1 = new javax.swing.JButton();
        butX1Y2 = new javax.swing.JButton();
        butX2Y1 = new javax.swing.JButton();
        butX2Y2 = new javax.swing.JButton();
        lvlX1Y1 = new javax.swing.JLabel();
        lvlX1Y2 = new javax.swing.JLabel();
        lvlX2Y1 = new javax.swing.JLabel();
        lvlX2Y2 = new javax.swing.JLabel();
        lvlFFF = new javax.swing.JLabel();
        cbFields = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(jdicom2dose.JDicom2DoseApp.class).getContext().getResourceMap(JD2DMatriXXFFFMaker.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setIconImages(null);
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setName("Form"); // NOI18N
        setResizable(false);

        butAsign.setText(resourceMap.getString("butAsign.text")); // NOI18N
        butAsign.setName("butAsign"); // NOI18N
        butAsign.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butAsignActionPerformed(evt);
            }
        });

        butExit.setFont(resourceMap.getFont("butExit.font")); // NOI18N
        butExit.setText(resourceMap.getString("butExit.text")); // NOI18N
        butExit.setName("butExit"); // NOI18N
        butExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butExitActionPerformed(evt);
            }
        });

        butProcess.setText(resourceMap.getString("butProcess.text")); // NOI18N
        butProcess.setName("butProcess"); // NOI18N
        butProcess.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butProcessActionPerformed(evt);
            }
        });

        butX1Y1.setIcon(resourceMap.getIcon("butX1Y1.icon")); // NOI18N
        butX1Y1.setText(resourceMap.getString("butX1Y1.text")); // NOI18N
        butX1Y1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        butX1Y1.setName("butX1Y1"); // NOI18N
        butX1Y1.setPressedIcon(resourceMap.getIcon("butX1Y1.pressedIcon")); // NOI18N
        butX1Y1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butX1Y1ActionPerformed(evt);
            }
        });

        butX1Y2.setIcon(resourceMap.getIcon("butX1Y2.icon")); // NOI18N
        butX1Y2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        butX1Y2.setName("butX1Y2"); // NOI18N
        butX1Y2.setPressedIcon(resourceMap.getIcon("butX1Y2.pressedIcon")); // NOI18N
        butX1Y2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butX1Y2ActionPerformed(evt);
            }
        });

        butX2Y1.setIcon(resourceMap.getIcon("butX2Y1.icon")); // NOI18N
        butX2Y1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        butX2Y1.setName("butX2Y1"); // NOI18N
        butX2Y1.setPressedIcon(resourceMap.getIcon("butX2Y1.pressedIcon")); // NOI18N
        butX2Y1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butX2Y1ActionPerformed(evt);
            }
        });

        butX2Y2.setIcon(resourceMap.getIcon("butX2Y2.icon")); // NOI18N
        butX2Y2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, resourceMap.getColor("butX2Y2.border.highlightOuterColor"), resourceMap.getColor("butX2Y2.border.highlightInnerColor"), resourceMap.getColor("butX2Y2.border.shadowOuterColor"), resourceMap.getColor("butX2Y2.border.shadowInnerColor"))); // NOI18N
        butX2Y2.setName("butX2Y2"); // NOI18N
        butX2Y2.setPressedIcon(resourceMap.getIcon("butX2Y2.pressedIcon")); // NOI18N
        butX2Y2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butX2Y2ActionPerformed(evt);
            }
        });

        lvlX1Y1.setFont(resourceMap.getFont("lvlX2Y1.font")); // NOI18N
        lvlX1Y1.setText(resourceMap.getString("lvlX1Y1.text")); // NOI18N
        lvlX1Y1.setName("lvlX1Y1"); // NOI18N
        lvlX1Y1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lvlX1Y1MouseClicked(evt);
            }
        });

        lvlX1Y2.setFont(resourceMap.getFont("lvlX1Y2.font")); // NOI18N
        lvlX1Y2.setText(resourceMap.getString("lvlX1Y2.text")); // NOI18N
        lvlX1Y2.setName("lvlX1Y2"); // NOI18N
        lvlX1Y2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lvlX1Y2MouseClicked(evt);
            }
        });

        lvlX2Y1.setFont(resourceMap.getFont("lvlX2Y1.font")); // NOI18N
        lvlX2Y1.setText(resourceMap.getString("lvlX2Y1.text")); // NOI18N
        lvlX2Y1.setName("lvlX2Y1"); // NOI18N
        lvlX2Y1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lvlX2Y1MouseClicked(evt);
            }
        });

        lvlX2Y2.setFont(resourceMap.getFont("lvlX2Y1.font")); // NOI18N
        lvlX2Y2.setText(resourceMap.getString("lvlX2Y2.text")); // NOI18N
        lvlX2Y2.setName("lvlX2Y2"); // NOI18N
        lvlX2Y2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lvlX2Y2MouseClicked(evt);
            }
        });

        lvlFFF.setFont(resourceMap.getFont("lvlFFF.font")); // NOI18N
        lvlFFF.setForeground(resourceMap.getColor("lvlFFF.foreground")); // NOI18N
        lvlFFF.setText(resourceMap.getString("lvlFFF.text")); // NOI18N
        lvlFFF.setName("lvlFFF"); // NOI18N
        lvlFFF.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lvlFFFMouseClicked(evt);
            }
        });

        cbFields.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbFields.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        cbFields.setName("cbFields"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(butX1Y1)
                    .addComponent(lvlX1Y1)
                    .addComponent(butX2Y1)
                    .addComponent(lvlX2Y1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(butProcess, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(butAsign, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lvlFFF)
                            .addComponent(cbFields, javax.swing.GroupLayout.PREFERRED_SIZE, 413, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(44, 44, 44)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lvlX1Y2)
                    .addComponent(butX1Y2)
                    .addComponent(lvlX2Y2)
                    .addComponent(butX2Y2)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 279, Short.MAX_VALUE)
                        .addComponent(butExit, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(butX1Y1)
                    .addComponent(butX1Y2))
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lvlX1Y1)
                    .addComponent(lvlX1Y2))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(butX2Y1)
                    .addComponent(butX2Y2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(lvlX2Y1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(butProcess, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lvlFFF))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(butAsign, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbFields, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(lvlX2Y2, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(butExit, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        butAsign.getAccessibleContext().setAccessibleName(resourceMap.getString("butAccept.AccessibleContext.accessibleName")); // NOI18N

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void butExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butExitActionPerformed
        this.dispose();
    }//GEN-LAST:event_butExitActionPerformed

    private void butAsignActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butAsignActionPerformed
        int iRes=this.iAsign();

    }//GEN-LAST:event_butAsignActionPerformed

    private void butProcessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butProcessActionPerformed
        StringBuffer sResult=new StringBuffer("");
        int iRes=0;
        dpMain.setDataSource(JD2DCtes.DATA_FFF);
        iRes=dpMain.iLoadOPGMatriXXFFF(this.sX1Y1Path, sResult, JD2DCtes.iX1Y1);
        if (iRes==0) {
            iRes=dpMain.iLoadOPGMatriXXFFF(this.sX2Y1Path, sResult, JD2DCtes.iX2Y1);
        }
        if (iRes==0) {
            iRes=dpMain.iLoadOPGMatriXXFFF(this.sX1Y2Path, sResult, JD2DCtes.iX1Y2);
        }
        if (iRes == 0) {
            iRes=dpMain.iLoadOPGMatriXXFFF(this.sX2Y2Path, sResult, JD2DCtes.iX2Y2);
        }
        if (iRes != 0) {
            JOptionPane.showMessageDialog(rootPane,"Ocurrió un error: " + sResult);
            return;
        }
        JFileChooser cho = new JFileChooser();
        cho.setDialogType(JFileChooser.SAVE_DIALOG);
        cho.setMultiSelectionEnabled(false);
        File flPath = new File(this.sParentPath);
        if (flPath.exists()) {
            cho.setCurrentDirectory(flPath);
        }
        JFrame mainFrame = JDicom2DoseApp.getApplication().getMainFrame();
        iRes = cho.showOpenDialog(mainFrame);
        if (iRes == JFileChooser.APPROVE_OPTION) {
            dpMain.setImageName(cho.getSelectedFile().getName());
            dpMain.setDcmName(cho.getSelectedFile().getName());
            iRes=dpMain.iOutputOPGFile(cho.getSelectedFile().getAbsolutePath(), sResult);
            if (iRes != 0) {
                JOptionPane.showMessageDialog(rootPane,"Ocurrió un error: " + sResult);
            }
            else {
                this.sFFFPath=cho.getSelectedFile().getAbsolutePath();
                JOptionPane.showMessageDialog(rootPane,"El fichero "+cho.getSelectedFile().getAbsolutePath()+" se generó correctamente.");
                this.refreshLabels();
            }
        }
        else {
            JOptionPane.showMessageDialog(rootPane,"No se seleccionó un nombre de fichero");
        }
    }//GEN-LAST:event_butProcessActionPerformed

    private void butX1Y1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butX1Y1ActionPerformed
        this.loadX1Y1File();
    }//GEN-LAST:event_butX1Y1ActionPerformed

    private void butX1Y2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butX1Y2ActionPerformed
        this.loadX1Y2File();
    }//GEN-LAST:event_butX1Y2ActionPerformed

    private void butX2Y1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butX2Y1ActionPerformed
        this.loadX2Y1File();
    }//GEN-LAST:event_butX2Y1ActionPerformed

    private void butX2Y2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butX2Y2ActionPerformed
        this.loadX2Y2File();
    }//GEN-LAST:event_butX2Y2ActionPerformed

    private void lvlX1Y1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lvlX1Y1MouseClicked
        this.loadX1Y1File();
    }//GEN-LAST:event_lvlX1Y1MouseClicked

    private void lvlX1Y2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lvlX1Y2MouseClicked
        this.loadX1Y2File();
    }//GEN-LAST:event_lvlX1Y2MouseClicked

    private void lvlX2Y1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lvlX2Y1MouseClicked
        this.loadX2Y1File();
    }//GEN-LAST:event_lvlX2Y1MouseClicked

    private void lvlX2Y2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lvlX2Y2MouseClicked
        this.loadX2Y2File();
    }//GEN-LAST:event_lvlX2Y2MouseClicked

    private void lvlFFFMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lvlFFFMouseClicked
        
    }//GEN-LAST:event_lvlFFFMouseClicked

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butAsign;
    private javax.swing.JButton butExit;
    private javax.swing.JButton butProcess;
    private javax.swing.JButton butX1Y1;
    private javax.swing.JButton butX1Y2;
    private javax.swing.JButton butX2Y1;
    private javax.swing.JButton butX2Y2;
    private javax.swing.JComboBox cbFields;
    private javax.swing.JLabel lvlFFF;
    private javax.swing.JLabel lvlX1Y1;
    private javax.swing.JLabel lvlX1Y2;
    private javax.swing.JLabel lvlX2Y1;
    private javax.swing.JLabel lvlX2Y2;
    // End of variables declaration//GEN-END:variables

}
