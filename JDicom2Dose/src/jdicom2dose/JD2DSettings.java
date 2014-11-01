/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jdicom2dose;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import javax.swing.JOptionPane;

/**
 *
 * @author u16066
 */
public class JD2DSettings {

    private String sXMLPath="JD2DSettings.xml";
    private Properties prop = new Properties();

    /** Variables con la configuración de la aplicación. */
    private String sWorkPath = "C:/";
    private String sConfigPath = "C:/";
    private boolean bRenamePCRTFiles = false;
    private boolean bRenameFiles = false;
    private boolean bInterpolatePCRTFiles = false;
    private boolean bInterpolateFiles = false;
    private boolean bGenerateASCIIFile = true;
    private boolean bGenerateTIFFile = false;
    private boolean bTrasl2Isocenter = true;
    private int iNumPixPCRT = 0;
    private int iNumPixIntDefX = 0;
    private int iNumPixIntDefY = 0;
    private double dX1ForROI = -5.0;
    private double dX2ForROI = 5.0;
    private double dY1ForROI = -5.0;
    private double dY2ForROI = 5.0;


    /** Variables con la configuración de la aplicación - Configuración de haces. */
    private String [] sConfBeam = new String [20];
    private boolean [] isBeamOn = new boolean [20];
    private String [] sBeamName = new String [20];
    private double [] dCoefCal = new double [20];
    private double [] dILagA1 = new double [20];
    private double [] dILagA2 = new double [20];
    private double [] dILagA3 = new double [20];
    private double [] dILagk1 = new double [20];
    private double [] dILagk2 = new double [20];
    private double [] dILagk3 = new double [20];
    private String [] sFloodFileField = new String [20];

    private String sTag = "";
    private String sPatern = "sConfigBeam";


    public JD2DSettings() {
        try {
            if (! this.isSettingsFileOverThere()) {
                JOptionPane.showMessageDialog(null, "No se encontró el fichero de configuración, se creará a continuación.", "JD2D Settings", 1);
                this.saveNewXML();
            }
            FileInputStream fis = new FileInputStream(sXMLPath);
            prop.loadFromXML(fis);
            prop.list(System.out);
            this.loadXML();
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Problema al guardar la configuración: \n" + e.toString(), "JD2D Settings", 1);
        }
    }

    private boolean isSettingsFileOverThere(){
        File f= new File(sXMLPath);
        return f.exists();
    }

    private void initConfigData(){
        for (int i=0; i<20 ; i++) {
            this.sConfBeam[i]="";
        }
        this.parseConfigData();
    }

    private void parseConfigData(){
        for (int i=0; i<20; i++) {
            if (this.sConfBeam[i].length() > 0) {
                this.parseBeamConfig(i, this.sConfBeam[i]);
            }
            else {
                this.initBeamConfig(i);
            }
        }
    }

    void initBeamConfig(int i){
        this.isBeamOn[i] = false;
        this.sBeamName[i] = "";
        this.dCoefCal[i] = 0.0;
        this.dILagA1[i] = 0.0;
        this.dILagA2[i] = 0.0;
        this.dILagA3[i] = 0.0;
        this.dILagk1[i] = 0.0;
        this.dILagk2[i] = 0.0;
        this.dILagk3[i] = 0.0;
        this.sFloodFileField[i] = "";
    }

    void parseBeamConfig(int i, String sConf){
        if (sConf.length()>0) {
            String [] sFields = sConf.split("#");
            if (sFields.length == JD2DCtes.NUMBER_OF_FIELDS) {
                this.isBeamOn[i] = true;
                this.sBeamName[i] = sFields[JD2DCtes.DESC_FIELD];
                this.dCoefCal[i] = Double.parseDouble(sFields[JD2DCtes.COEF_FIELD]);
                this.dILagA1[i] = Double.parseDouble(sFields[JD2DCtes.ILOGA1_FIELD]);
                this.dILagA2[i] = Double.parseDouble(sFields[JD2DCtes.ILOGA2_FIELD]);
                this.dILagA3[i] = Double.parseDouble(sFields[JD2DCtes.ILOGA3_FIELD]);
                this.dILagk1[i] = Double.parseDouble(sFields[JD2DCtes.ILOGK1_FIELD]);
                this.dILagk2[i] = Double.parseDouble(sFields[JD2DCtes.ILOGK2_FIELD]);
                this.dILagk3[i] = Double.parseDouble(sFields[JD2DCtes.ILOGK3_FIELD]);
                this.sFloodFileField[i] = sFields[JD2DCtes.FFFILE_FIELD];
            }
        }
        else {
            this.initBeamConfig(i);
        }
    }

    String formatBeamConfig(int i){
        String sConf = "";
        if (this.isBeamOn[i]) {
            sConf = sConf + this.sBeamName[i] + "#";
            sConf = sConf + this.dCoefCal[i] + "#";
            sConf = sConf + this.dILagA1[i] + "#";
            sConf = sConf + this.dILagA2[i] + "#";
            sConf = sConf + this.dILagA3[i] + "#";
            sConf = sConf + this.dILagk1[i] + "#";
            sConf = sConf + this.dILagk2[i] + "#";
            sConf = sConf + this.dILagk3[i] + "#";
            sConf = sConf + this.sFloodFileField[i] + "#";
        }
        return sConf;
    }

    public String sBeamToString(int i){
        String sResp = "";
        if (this.isBeamOn[i]) {
            sResp = sResp + "BEAM= " + this.sBeamName[i] + ", ";
            sResp = sResp + "Cf= " + this.dCoefCal[i] + ", ";
            sResp = sResp + "A1= " + this.dILagA1[i] + ", ";
            sResp = sResp + "A2= " + this.dILagA2[i] + ", ";
            sResp = sResp + "A3= " + this.dILagA3[i] + ", ";
            sResp = sResp + "k1= " + this.dILagk1[i] + ", ";
            sResp = sResp + "k2= " + this.dILagk2[i] + ", ";
            sResp = sResp + "k3= " + this.dILagk3[i] + ", ";
            sResp = sResp + "FFFile= " + this.sFloodFileField[i] + ".";
        }
        return sResp;
    }

    public String[] getConfigBeams(){
        return this.sConfBeam;
    }

    public void setConfigBeams(String[] sConf){
        int j=0;
        for (int i=0; i<20; i++) {
            if (sConf[i].length() > 0) {
               this.sConfBeam[j]=sConf[i];
               j=j+1;
            }
        }
    }

    public boolean isBeamOn(int i){
        return this.isBeamOn[i];
    }
    public void setBeamOn(int i, boolean bIsON){
        this.isBeamOn[i]=bIsON;
    }

    public String getBeamName(int i){
        return this.sBeamName[i];
    }
    public void setBeamName(int i, String sName){
        this.sBeamName[i]=sName;
    }

    public double getCoefCal(int i){
        return this.dCoefCal[i];
    }
    public void setCoefCal(int i, double dD){
        this.dCoefCal[i]=dD;
    }

    public double getILagA1(int i){
        return this.dILagA1[i];
    }
    public void setILagA1(int i, double dD){
        this.dILagA1[i]=dD;
    }

    public double getILagA2(int i){
        return this.dILagA2[i];
    }
    public void setILagA2(int i, double dD){
        this.dILagA2[i]=dD;
    }

    public double getILagA3(int i){
        return this.dILagA3[i];
    }
    public void setILagA3(int i, double dD){
        this.dILagA3[i]=dD;
    }

    public double getILagk1(int i){
        return this.dILagk1[i];
    }
    public void setILagk1(int i, double dD){
        this.dILagk1[i]=dD;
    }

    public double getILagk2(int i){
        return this.dILagk2[i];
    }
    public void setILagk2(int i, double dD){
        this.dILagk2[i]=dD;
    }

    public double getILagk3(int i){
        return this.dILagk3[i];
    }
    public void setILagk3(int i, double dD){
        this.dILagk3[i]=dD;
    }

    public String getBeamFFFile(int i){
        return this.sFloodFileField[i];
    }
    public void setBeamFFFile(int i, String sFile){
        this.sFloodFileField[i]=sFile;
    }

    public int getNumPixPCRT(){
        return this.iNumPixPCRT;
    }
    public void setNumPixPCRT(int iNum){
        this.iNumPixPCRT=iNum;
    }

    public int getNumPixIntDefX(){
        return this.iNumPixIntDefX;
    }
    public void setNumPixIntDefX(int iNum){
        this.iNumPixIntDefX=iNum;
    }

    public int getNumPixIntDefY(){
        return this.iNumPixIntDefY;
    }
    public void setNumPixIntDefY(int iNum){
        this.iNumPixIntDefY=iNum;
    }

    public double getX1ForROI(){
        return this.dX1ForROI;
    }
    public void setX1ForROI(double dNum){
        this.dX1ForROI=dNum;
    }

    public double getX2ForROI(){
        return this.dX2ForROI;
    }
    public void setX2ForROI(double dNum){
        this.dX2ForROI=dNum;
    }

    public double getY1ForROI(){
        return this.dY1ForROI;
    }
    public void setY1ForROI(double dNum){
        this.dY1ForROI=dNum;
    }

    public double getY2ForROI(){
        return this.dY2ForROI;
    }
    public void setY2ForROI(double dNum){
        this.dY2ForROI=dNum;
    }

    public boolean isInterpPCRTFiles(){
        return this.bInterpolatePCRTFiles;
    }
    public void setInterpPCRTFiles(boolean booool){
        this.bInterpolatePCRTFiles=booool;
    }


    public boolean isRenameFiless(){
        return this.bRenameFiles;
    }

    public void setRenameFiles(boolean booool){
        this.bRenameFiles=booool;
    }

    public boolean isRenamePCRTFiles(){
        return this.bRenamePCRTFiles;
    }

    public void setRenamePCRTFiles(boolean booool){
        this.bRenamePCRTFiles=booool;
    }

    public boolean isInterpolateFiles(){
        return this.bInterpolateFiles;
    }
    public void setInterpolateFiles(boolean booool){
        this.bInterpolateFiles=booool;
    }
    public boolean isGenerateASCIIFile(){
        return this.bGenerateASCIIFile;
    }
    public void setGenerateASCIIFile(boolean booool){
        this.bGenerateASCIIFile=booool;
    }
    public boolean isGenerateTIFFile(){
        return this.bGenerateTIFFile;
    }
    public void setGenerateTIFFile(boolean booool){
        this.bGenerateTIFFile=booool;
    }
    public boolean isTrasl2Isocenter(){
        return this.bTrasl2Isocenter;
    }
    public void setTrasl2Isocenter(boolean booool){
        this.bTrasl2Isocenter=booool;
    }

    public String getWorkPath(){
        return this.sWorkPath;
    }
    public void setWorkPath(String sPath){
        this.sWorkPath=sPath;
    }

    public String getConfigPath(){
        return this.sConfigPath;
    }
    public void setConfigPath(String sPath){
        this.sConfigPath=sPath;
    }

    private void saveNewXML() throws IOException {
        prop.clear();
        prop.setProperty("bInterpolatePCRTFiles", String.valueOf(this.bInterpolatePCRTFiles));
        prop.setProperty("bRenamePCRTFiles", String.valueOf(this.bRenamePCRTFiles));
        prop.setProperty("bRenameFiles", String.valueOf(this.bRenameFiles));
        prop.setProperty("bGenerateASCIIFile", String.valueOf(this.bGenerateASCIIFile));
        prop.setProperty("bGenerateTIFFile", String.valueOf(this.bGenerateTIFFile));
        prop.setProperty("bInterpolateFiles", String.valueOf(this.bInterpolateFiles));
        prop.setProperty("bTrasl2Isocenter", String.valueOf(this.bTrasl2Isocenter));
        prop.setProperty("iNumPixPCRT", String.valueOf(this.iNumPixPCRT));
        prop.setProperty("iNumPixIntDefX", String.valueOf(this.iNumPixIntDefX));
        prop.setProperty("iNumPixIntDefY", String.valueOf(this.iNumPixIntDefY));
        prop.setProperty("dX1ForROI", String.valueOf(this.dX1ForROI));
        prop.setProperty("dX2ForROI", String.valueOf(this.dX2ForROI));
        prop.setProperty("dY1ForROI", String.valueOf(this.dY1ForROI));
        prop.setProperty("dY2ForROI", String.valueOf(this.dY2ForROI));
        prop.setProperty("sWorkPath", this.sWorkPath);
        prop.setProperty("sConfigPath", this.sConfigPath);
        for (int i=0; i<20; i++) {
                sTag=sPatern+i;
                prop.setProperty(sTag, this.sConfBeam[i]);
        }
        FileOutputStream fos =new FileOutputStream(this.sXMLPath);
        prop.storeToXML(fos, this.sXMLPath);
        fos.close();
    }

    public void storeXML() {
        try {
            prop.setProperty("bInterpolatePCRTFiles", String.valueOf(this.bInterpolatePCRTFiles));
            prop.setProperty("bGenerateASCIIFile", String.valueOf(this.bGenerateASCIIFile));
            prop.setProperty("bGenerateTIFFile", String.valueOf(this.bGenerateTIFFile));
            prop.setProperty("bInterpolateFiles", String.valueOf(this.bInterpolateFiles));
            prop.setProperty("bTrasl2Isocenter", String.valueOf(this.bTrasl2Isocenter));
            prop.setProperty("bRenamePCRTFiles", String.valueOf(this.bRenamePCRTFiles));
            prop.setProperty("bRenameFiles", String.valueOf(this.bRenameFiles));
            prop.setProperty("iNumPixPCRT", String.valueOf(this.iNumPixPCRT));
            prop.setProperty("iNumPixIntDefX", String.valueOf(this.iNumPixIntDefX));
            prop.setProperty("iNumPixIntDefY", String.valueOf(this.iNumPixIntDefY));
            prop.setProperty("dX1ForROI", String.valueOf(this.dX1ForROI));
            prop.setProperty("dX2ForROI", String.valueOf(this.dX2ForROI));
            prop.setProperty("dY1ForROI", String.valueOf(this.dY1ForROI));
            prop.setProperty("dY2ForROI", String.valueOf(this.dY2ForROI));
            prop.setProperty("sWorkPath", this.sWorkPath);
            prop.setProperty("sConfigPath", this.sConfigPath);
            for (int i=0; i<20; i++) {
                sTag=sPatern+i;
                this.sConfBeam[i]=this.formatBeamConfig(i);
                prop.setProperty(sTag, this.sConfBeam[i]);
            }
            FileOutputStream fos = new FileOutputStream(this.sXMLPath);
            prop.storeToXML(fos, this.sXMLPath);
            fos.close();
            this.loadXML();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Problema al guardar la configuración: \n" + ex.toString(), "JD2D Settings", 1);
        }
    }

    private void loadXML() throws IOException {
        if (prop.getProperty("bInterpolatePCRTFiles") != null) {
            this.bInterpolatePCRTFiles=Boolean.valueOf(prop.getProperty("bInterpolatePCRTFiles"));
        }
        if (prop.getProperty("bRenamePCRTFiles") != null) {
            this.bRenamePCRTFiles=Boolean.valueOf(prop.getProperty("bRenamePCRTFiles"));
        }
        if (prop.getProperty("bRenameFiles") != null) {
            this.bRenameFiles=Boolean.valueOf(prop.getProperty("bRenameFiles"));
        }
        if (prop.getProperty("bGenerateASCIIFile") != null) {
            this.bGenerateASCIIFile=Boolean.valueOf(prop.getProperty("bGenerateASCIIFile"));
        }
        if (prop.getProperty("bGenerateTIFFile") != null) {
            this.bGenerateTIFFile=Boolean.valueOf(prop.getProperty("bGenerateTIFFile"));
        }
        if (prop.getProperty("bInterpolateFiles") != null) {
            this.bInterpolateFiles=Boolean.valueOf(prop.getProperty("bInterpolateFiles"));
        }
        if (prop.getProperty("bTrasl2Isocenter") != null) {
            this.bTrasl2Isocenter=Boolean.valueOf(prop.getProperty("bTrasl2Isocenter"));
        }
        if (prop.getProperty("iNumPixPCRT") != null) {
            this.iNumPixPCRT=Integer.valueOf(prop.getProperty("iNumPixPCRT"));
        }
        if (prop.getProperty("iNumPixIntDefX") != null) {
            this.iNumPixIntDefX=Integer.valueOf(prop.getProperty("iNumPixIntDefX"));
        }
        if (prop.getProperty("iNumPixIntDefY") != null) {
            this.iNumPixIntDefY=Integer.valueOf(prop.getProperty("iNumPixIntDefY"));
        }
        if (prop.getProperty("dX1ForROI") != null) {
            this.dX1ForROI=Double.valueOf(prop.getProperty("dX1ForROI"));
        }
        if (prop.getProperty("dX2ForROI") != null) {
            this.dX2ForROI=Double.valueOf(prop.getProperty("dX2ForROI"));
        }
        if (prop.getProperty("dY1ForROI") != null) {
            this.dY1ForROI=Double.valueOf(prop.getProperty("dY1ForROI"));
        }
        if (prop.getProperty("dY2ForROI") != null) {
            this.dY2ForROI=Double.valueOf(prop.getProperty("dY2ForROI"));
        }
        this.sWorkPath=prop.getProperty("sWorkPath");
        if (this.sWorkPath == null) {
            this.sWorkPath="C:/";
        }
        this.sConfigPath=prop.getProperty("sConfigPath");
        if (this.sConfigPath == null) {
            this.sConfigPath="C:/";
        }
        for (int i=0; i<20; i++) {
            sTag=sPatern+i;
            this.sConfBeam[i]=prop.getProperty(sTag);
        }
        this.parseConfigData();
    }

}
