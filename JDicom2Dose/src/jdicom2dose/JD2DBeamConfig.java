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

import javax.swing.JOptionPane;
import java.text.DecimalFormat;
import javax.swing.JFileChooser;
import java.io.File;
import java.awt.geom.*;

/**
 *
 * @author u16066
 */
public class JD2DBeamConfig extends javax.swing.JDialog {

    private static final int MODE_MODIFY=0;
    private static final int MODE_NEWITEM=1;

    private DecimalFormat df4Dec = new DecimalFormat("####0.0000");


    private JD2DSettings set;
    private int[] iBeamIndexInSettings = new int[20];
    private int iBeamIndexAux = -1;
    private int iMode = this.MODE_MODIFY;
    private String sBeamNameAux = "";
    private double dCoefCalAux = 0.0;
    private double dILagA1Aux = 0.0;
    private double dILagA2Aux = 0.0;
    private double dILagA3Aux = 0.0;
    private double dILagk1Aux = 0.0;
    private double dILagk2Aux = 0.0;
    private double dILagk3Aux = 0.0;
    private String sFloodFileFieldAux = "";

    /** Creates new form JD2DPCRTOptions */
    public JD2DBeamConfig(JD2DSettings s) {
        initComponents();
        set=s;
        iMode = this.MODE_MODIFY;
        this.checkControls();
        this.fillCombo();
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

    private void checkControls(){
        if (this.iMode==this.MODE_MODIFY){
            this.txtA1.setEnabled(true);
            this.txtA2.setEnabled(true);
            this.txtA3.setEnabled(true);
            this.txtk1.setEnabled(true);
            this.txtk2.setEnabled(true);
            this.txtk3.setEnabled(true);
            this.txtDosCalCoef.setEnabled(true);
            this.txtFFFilePath.setEnabled(true);
            this.txtNewItemName.setEnabled(false);
            this.txtNewItemName.setText("");
            this.butCancel.setEnabled(true);
            this.butExit.setEnabled(true);
            this.butFindFFFile.setEnabled(true);
            this.butSave.setEnabled(true);
            this.butDelete.setEnabled(true);
            this.butNewItem.setEnabled(true);
            this.butOKNewItemName.setEnabled(false);
            this.cbFields.setEnabled(true);
        }
        else if (this.iMode==this.MODE_NEWITEM){
            this.txtA1.setEnabled(false);
            this.txtA2.setEnabled(false);
            this.txtA3.setEnabled(false);
            this.txtk1.setEnabled(false);
            this.txtk2.setEnabled(false);
            this.txtk3.setEnabled(false);
            this.txtDosCalCoef.setEnabled(false);
            this.txtFFFilePath.setEnabled(false);
            this.txtNewItemName.setEnabled(true);
            this.butCancel.setEnabled(true);
            this.butExit.setEnabled(true);
            this.butFindFFFile.setEnabled(false);
            this.butSave.setEnabled(false);
            this.butDelete.setEnabled(false);
            this.butNewItem.setEnabled(false);
            this.butOKNewItemName.setEnabled(true);
            this.cbFields.setEnabled(false);
        }
    }

    private void cleanControls(){
        this.txtA1.setText("");
        this.txtA2.setText("");
        this.txtA3.setText("");
        this.txtk1.setText("");
        this.txtk2.setText("");
        this.txtk3.setText("");
        this.txtDosCalCoef.setText("");
        this.txtFFFilePath.setText("");
        this.txtNewItemName.setText("");
    }

    private void iniVars(){
        this.sBeamNameAux = "";
        this.dCoefCalAux = 0.0;
        this.dILagA1Aux = 0.0;
        this.dILagA2Aux = 0.0;
        this.dILagA3Aux = 0.0;
        this.dILagk1Aux = 0.0;
        this.dILagk2Aux = 0.0;
        this.dILagk3Aux = 0.0;
        this.sFloodFileFieldAux = "none";
        this.iBeamIndexAux=-1;
    }
    private void fillControls(){
        this.txtA1.setText(df4Dec.format(this.dILagA1Aux));
        this.txtA2.setText(df4Dec.format(this.dILagA2Aux));
        this.txtA3.setText(df4Dec.format(this.dILagA3Aux));
        this.txtk1.setText(df4Dec.format(this.dILagk1Aux));
        this.txtk2.setText(df4Dec.format(this.dILagk2Aux));
        this.txtk3.setText(df4Dec.format(this.dILagk3Aux));
        this.txtDosCalCoef.setText(df4Dec.format(this.dCoefCalAux));
        this.txtFFFilePath.setText(this.sFloodFileFieldAux);
        this.txtNewItemName.setText("");
    }

    private void sendFromControls2Var(){
        this.dILagA1Aux=Double.parseDouble(this.txtA1.getText().replace(',', '.'));
        this.dILagA2Aux=Double.parseDouble(this.txtA2.getText().replace(',', '.'));
        this.dILagA3Aux=Double.parseDouble(this.txtA3.getText().replace(',', '.'));
        this.dILagk1Aux=Double.parseDouble(this.txtk1.getText().replace(',', '.'));
        this.dILagk2Aux=Double.parseDouble(this.txtk2.getText().replace(',', '.'));
        this.dILagk3Aux=Double.parseDouble(this.txtk3.getText().replace(',', '.'));
        this.dCoefCalAux=Double.parseDouble(this.txtDosCalCoef.getText().replace(',', '.'));
        this.sFloodFileFieldAux=this.txtFFFilePath.getText();
        this.sBeamNameAux=this.cbFields.getSelectedItem().toString();
    }

    private void sendFromVar2Config(){
        int ii=-1;
        for (int i=0;i<20;i++){
            if (!set.isBeamOn(i)){
                ii=i;
                break;
            }
        }
        this.sendFromVar2Config(ii);
    }

    private void sendFromVar2Config(int i) {
        set.setCoefCal(i,this.dCoefCalAux);
        set.setILagA1(i,this.dILagA1Aux);
        set.setILagA2(i,this.dILagA2Aux);
        set.setILagA3(i,this.dILagA3Aux);
        set.setILagk1(i,this.dILagk1Aux);
        set.setILagk2(i,this.dILagk2Aux);
        set.setILagk3(i,this.dILagk3Aux);
        set.setBeamFFFile(i,this.sFloodFileFieldAux);
        set.setBeamName(i, sBeamNameAux);
        set.setBeamOn(i, true);
    }

    private void getFromConfig(int i){
        this.dCoefCalAux=set.getCoefCal(i);
        this.dILagA1Aux=set.getILagA1(i);
        this.dILagA2Aux=set.getILagA2(i);
        this.dILagA3Aux=set.getILagA3(i);
        this.dILagk1Aux=set.getILagk1(i);
        this.dILagk2Aux=set.getILagk2(i);
        this.dILagk3Aux=set.getILagk3(i);
        this.sFloodFileFieldAux=set.getBeamFFFile(i);
        this.iBeamIndexAux=i;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtDosCalCoef = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        butCancel = new javax.swing.JButton();
        butNewItem = new javax.swing.JButton();
        butSave = new javax.swing.JButton();
        cbFields = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtFFFilePath = new javax.swing.JTextField();
        butFindFFFile = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtA1 = new javax.swing.JTextField();
        txtk1 = new javax.swing.JTextField();
        txtA2 = new javax.swing.JTextField();
        txtk2 = new javax.swing.JTextField();
        txtA3 = new javax.swing.JTextField();
        txtk3 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        butExit = new javax.swing.JButton();
        txtNewItemName = new javax.swing.JTextField();
        butDelete = new javax.swing.JButton();
        butOKNewItemName = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(jdicom2dose.JDicom2DoseApp.class).getContext().getResourceMap(JD2DBeamConfig.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setIconImages(null);
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setName("Form"); // NOI18N
        setResizable(false);

        txtDosCalCoef.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtDosCalCoef.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        txtDosCalCoef.setName("txtDoseCalCoef"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        butCancel.setFont(resourceMap.getFont("butCancel.font")); // NOI18N
        butCancel.setText(resourceMap.getString("butCancel.text")); // NOI18N
        butCancel.setName("butCancel"); // NOI18N
        butCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butCancelActionPerformed(evt);
            }
        });

        butNewItem.setFont(resourceMap.getFont("butNewItem.font")); // NOI18N
        butNewItem.setText(resourceMap.getString("butNewItem.text")); // NOI18N
        butNewItem.setName("butNewItem"); // NOI18N
        butNewItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butNewItemActionPerformed(evt);
            }
        });

        butSave.setFont(resourceMap.getFont("butCancel.font")); // NOI18N
        butSave.setText(resourceMap.getString("butSave.text")); // NOI18N
        butSave.setName("butSave"); // NOI18N
        butSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butSaveActionPerformed(evt);
            }
        });

        cbFields.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbFields.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        cbFields.setName("cbFields"); // NOI18N
        cbFields.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbFieldsActionPerformed(evt);
            }
        });

        jLabel2.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("jLabel3.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        txtFFFilePath.setEditable(false);
        txtFFFilePath.setFont(resourceMap.getFont("txtFloodFilePath.font")); // NOI18N
        txtFFFilePath.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtFFFilePath.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        txtFFFilePath.setName("txtFloodFilePath"); // NOI18N

        butFindFFFile.setFont(resourceMap.getFont("butFindFFFile.font")); // NOI18N
        butFindFFFile.setText(resourceMap.getString("butFindFFFile.text")); // NOI18N
        butFindFFFile.setName("butFindFFFile"); // NOI18N
        butFindFFFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butFindFFFileActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("jPanel1.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel1.border.titleFont"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jLabel4.setFont(resourceMap.getFont("lvlA1.font")); // NOI18N
        jLabel4.setText(resourceMap.getString("lvlA1.text")); // NOI18N
        jLabel4.setName("lvlA1"); // NOI18N

        jLabel5.setFont(resourceMap.getFont("lvlk1.font")); // NOI18N
        jLabel5.setText(resourceMap.getString("lvlk1.text")); // NOI18N
        jLabel5.setName("lvlk1"); // NOI18N

        jLabel6.setFont(resourceMap.getFont("lvlA2.font")); // NOI18N
        jLabel6.setText(resourceMap.getString("lvlA2.text")); // NOI18N
        jLabel6.setName("lvlA2"); // NOI18N

        jLabel7.setFont(resourceMap.getFont("lvlk2.font")); // NOI18N
        jLabel7.setText(resourceMap.getString("lvlk2.text")); // NOI18N
        jLabel7.setName("lvlk2"); // NOI18N

        jLabel8.setFont(resourceMap.getFont("lvlk2.font")); // NOI18N
        jLabel8.setText(resourceMap.getString("lvlA3.text")); // NOI18N
        jLabel8.setName("lvlA3"); // NOI18N

        jLabel9.setFont(resourceMap.getFont("lvlk2.font")); // NOI18N
        jLabel9.setText(resourceMap.getString("lvlk3.text")); // NOI18N
        jLabel9.setName("lvlk3"); // NOI18N

        txtA1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtA1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        txtA1.setName("txtA1"); // NOI18N

        txtk1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtk1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        txtk1.setName("txtk1"); // NOI18N

        txtA2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtA2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        txtA2.setName("txtA2"); // NOI18N

        txtk2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtk2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        txtk2.setName("txtk2"); // NOI18N

        txtA3.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtA3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        txtA3.setName("txtA3"); // NOI18N

        txtk3.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtk3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        txtk3.setName("txtk3"); // NOI18N

        jLabel10.setFont(resourceMap.getFont("jLabel10.font")); // NOI18N
        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        jLabel11.setFont(resourceMap.getFont("jLabel10.font")); // NOI18N
        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        jLabel12.setFont(resourceMap.getFont("jLabel10.font")); // NOI18N
        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtA1, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtk1, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 71, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addComponent(txtA2, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addComponent(txtk2, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addGap(72, 72, 72)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(18, 18, 18)
                        .addComponent(txtA3, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(18, 18, 18)
                        .addComponent(txtk3, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addGap(81, 81, 81))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtA3, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtk3, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9)
                            .addComponent(jLabel11)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(txtA2, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(txtk2, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtA1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtk1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        butExit.setFont(resourceMap.getFont("butExit.font")); // NOI18N
        butExit.setText(resourceMap.getString("butExit.text")); // NOI18N
        butExit.setName("butExit"); // NOI18N
        butExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butExitActionPerformed(evt);
            }
        });

        txtNewItemName.setText(resourceMap.getString("txtNewItemName.text")); // NOI18N
        txtNewItemName.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        txtNewItemName.setName("txtNewItemName"); // NOI18N
        txtNewItemName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtNewItemNameFocusLost(evt);
            }
        });

        butDelete.setFont(resourceMap.getFont("butDelete.font")); // NOI18N
        butDelete.setText(resourceMap.getString("butDelete.text")); // NOI18N
        butDelete.setName("butDelete"); // NOI18N
        butDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butDeleteActionPerformed(evt);
            }
        });

        butOKNewItemName.setFont(resourceMap.getFont("butOKNewItemName.font")); // NOI18N
        butOKNewItemName.setText(resourceMap.getString("butOKNewItemName.text")); // NOI18N
        butOKNewItemName.setName("butOKNewItemName"); // NOI18N
        butOKNewItemName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butOKNewItemNameActionPerformed(evt);
            }
        });

        jLabel13.setFont(resourceMap.getFont("jLabel10.font")); // NOI18N
        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(157, 157, 157)
                        .addComponent(butDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(butSave, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(butCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(butExit, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cbFields, 0, 370, Short.MAX_VALUE))
                            .addComponent(txtNewItemName, javax.swing.GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(1, 1, 1)
                                .addComponent(txtFFFilePath, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(butOKNewItemName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(butFindFFFile, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                            .addComponent(butNewItem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(13, 13, 13)
                        .addComponent(txtDosCalCoef, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(butNewItem)
                    .addComponent(cbFields, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNewItemName, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(butOKNewItemName))
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDosCalCoef, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtFFFilePath, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                    .addComponent(butFindFFFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(butExit, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(butCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(butSave, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(butDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void butNewItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butNewItemActionPerformed
        iMode = this.MODE_NEWITEM;
        this.checkControls();
        this.iBeamIndexAux=-1;
        this.txtNewItemName.setText("Nuevo campo");
        this.txtNewItemName.setSelectionStart(0);
        this.txtNewItemName.setSelectionEnd("Nuevo campo".length());
        this.txtNewItemName.requestFocusInWindow();
    }//GEN-LAST:event_butNewItemActionPerformed

    private void butCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butCancelActionPerformed
        this.cbFields.setSelectedItem(null);
    }//GEN-LAST:event_butCancelActionPerformed

    private void butSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butSaveActionPerformed
        this.sendFromControls2Var();
        if (this.iBeamIndexAux==-1) {
            this.sendFromVar2Config();
        }
        else {
            this.sendFromVar2Config(this.iBeamIndexAux);
        }
        this.fillCombo();
    }//GEN-LAST:event_butSaveActionPerformed

    private void butFindFFFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butFindFFFileActionPerformed
        JFileChooser cho = new JFileChooser();
        cho.setMultiSelectionEnabled(false);
        File flPath = new File(set.getWorkPath());
        if (flPath.exists()) {
            cho.setCurrentDirectory(new File(set.getWorkPath()));
        }
        JD2DFiltro opgFilter = new JD2DFiltro(".opg","Ficheros OPG");
        cho.addChoosableFileFilter(opgFilter);
        int iRes = cho.showOpenDialog(this);
        if (iRes == JFileChooser.APPROVE_OPTION) {
            Point2D.Double dAvg= new Point2D.Double();
            String sNewPath=set.getConfigPath()+"\\"+this.cbFields.getSelectedItem().toString()+"_FFF_conf.opg";
            StringBuffer sRes = new StringBuffer("");
            //File fl = new File(this.sFFFPath);
            JDosePlane dp=new JDosePlane(JDicom2DoseApp.getApplication().getMainFrame(),set);
            iRes=dp.iLoadOPGDoseFile(cho.getSelectedFile().getAbsolutePath(), sRes);
            if (iRes != 0) {
                JOptionPane.showMessageDialog(rootPane,"Ocurrió un error: " + sRes);
                return;
            }
            dp.traslate2SSD();
            dAvg=dp.averageInROI(JD2DCtes.NORM_X1, JD2DCtes.NORM_X2, JD2DCtes.NORM_Y1, JD2DCtes.NORM_Y2);
            if (dAvg.x!=0.0) {
                dp.applyFactorInPlane(100/dAvg.x);
            }
            else {
                dp.applyFactorInPlane(0);
            }
            dp.setDataSource(JD2DCtes.DATA_OPG);
            dp.setImageName(this.cbFields.getSelectedItem().toString()+"_FFF_conf");
            dp.setDcmName(this.cbFields.getSelectedItem().toString()+"_FFF_conf.opg");
            iRes=dp.iOutputOPGFile(sNewPath, sRes, true, JD2DCtes.NUM_PIX_FFF, JD2DCtes.NUM_PIX_FFF);
            if (iRes != 0) {
                JOptionPane.showMessageDialog(rootPane,"Ocurrió un error: " + sRes);
                return;
            }
            this.txtFFFilePath.setText(sNewPath);
        }
    }//GEN-LAST:event_butFindFFFileActionPerformed

    private void butExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butExitActionPerformed
        set.storeXML();
        this.dispose();
    }//GEN-LAST:event_butExitActionPerformed

    private void txtNewItemNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNewItemNameFocusLost
  
    }//GEN-LAST:event_txtNewItemNameFocusLost

    private void butDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butDeleteActionPerformed
        int i = this.iBeamIndexAux;
        this.set.setBeamOn(i,false);
        this.fillCombo();
    }//GEN-LAST:event_butDeleteActionPerformed

    private void butOKNewItemNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butOKNewItemNameActionPerformed
        if (this.txtNewItemName.getText().length() > 0) {
            this.iMode = this.MODE_NEWITEM;
            int i=0;
            boolean bExist=false;
            for (i=0;i<this.cbFields.getItemCount();i++){
                if (this.cbFields.getItemAt(i).toString().equals(this.txtNewItemName.getText())) {
                    bExist=true;
                }
            }
            if (bExist) {
                JOptionPane.showMessageDialog(this.rootPane, "Ya existe un campo con este nombre...");
                this.iMode = this.MODE_NEWITEM;
                this.checkControls();
                this.txtNewItemName.setText("Nuevo campo");
                this.txtNewItemName.setSelectionStart(0);
                this.txtNewItemName.setSelectionEnd("Nuevo campo".length());
                this.txtNewItemName.requestFocusInWindow();
            }
            else {
                this.iniVars();
                this.sBeamNameAux=this.txtNewItemName.getText();
                this.cbFields.addItem(this.sBeamNameAux);
                for (i=0;i<20;i++) {
                    if (! set.isBeamOn(i)){
                        this.iBeamIndexAux=i;
                        break;
                    }
                }
                int iNewItem = this.cbFields.getItemCount()-1;
                this.iBeamIndexInSettings[iNewItem]=this.iBeamIndexAux;
                this.iMode = this.MODE_MODIFY;
                this.checkControls();
                this.cbFields.setSelectedIndex(iNewItem);
            }
        }
        else {
            this.iBeamIndexAux=-1;
            this.iMode = this.MODE_MODIFY;
            this.checkControls();
            this.cbFields.setSelectedItem(null);
        }
        
    }//GEN-LAST:event_butOKNewItemNameActionPerformed

    private void cbFieldsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbFieldsActionPerformed
        int iIndex=this.cbFields.getSelectedIndex();
        if (iIndex==-1){
            this.cleanControls();
        }
        else {
            if (this.iMode==this.MODE_MODIFY) {
                this.getFromConfig(this.iBeamIndexInSettings[iIndex]);
            }
            this.fillControls();
        }
    }//GEN-LAST:event_cbFieldsActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butDelete;
    private javax.swing.JButton butExit;
    private javax.swing.JButton butFindFFFile;
    private javax.swing.JButton butNewItem;
    private javax.swing.JButton butOKNewItemName;
    private javax.swing.JButton butSave;
    private javax.swing.JComboBox cbFields;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField txtA1;
    private javax.swing.JTextField txtA2;
    private javax.swing.JTextField txtA3;
    private javax.swing.JTextField txtDosCalCoef;
    private javax.swing.JTextField txtFFFilePath;
    private javax.swing.JTextField txtNewItemName;
    private javax.swing.JTextField txtk1;
    private javax.swing.JTextField txtk2;
    private javax.swing.JTextField txtk3;
    // End of variables declaration//GEN-END:variables

}
