/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jdicom2dose;

import ij.plugin.*;
import ij.io.*;
import ij.ImagePlus;
import ij.io.FileSaver;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.*;
import java.util.*;
import java.awt.Frame;
import java.awt.geom.*;
import java.awt.Point;

/**
 * Clase para manipular un plano de dosis: Tiene distintos formatos de entrada y
 * salida y permite interpolar para reducir la resolución.
 * @author Oscar Ripol
 */
public class JDosePlane {
// instance constants
    public static final int PLANE_MAX_SIZE = 2000; // Num. máx de pixeles admisibles.
    private static final int NUM_PIXELS_EPID = 1024;
    
    
// instance variables
    private int iX;
    private int iY;
    private int iXmax; // tamaño del plano de dosis en X.
    private int iYmax; // Tamaño del plano de dosis en Y.
    private int iChanels=0;
    private int iSlices=0;
    private int iFrames=0;
    private int iNSubframes = 0; // Número de Subframes.
    private double dTime = 0.0; // Tiempo de irradiación (en base al número de subframes.
    private double dMU = 0.0;  // Número de unidades de monitor.
    private double dSID = 0.0;  // Distancia fuente isocentro.
    private double dSSD = 0.0;  // Distancia fuente superficie.
    private double dOriginalSSD = 0.0;  // Distancia fuente superficie.
    private double dPixWidth = 0.0; // Anchura de pixel.
    private double dPixHeight = 0.0; // Altura de pixel.
    private double dSizeWidth = 0.0; // Anchura de imagen.
    private double dSizeHeight = 0.0; // Altura de imagen.
    private double dDoseUnitsFactor = 0.0; /* Factor parea convertir el valor del pixel a dosis. */
    private double dDoseFactor = 0.0; /* Factor parea convertir el valor del pixel a dosis. */

    private String sDoseUnits = ""; /* Unidades de dosis utilizadas. */
    private String sPlanePosLine = ";";
    private String sObsLine = "";
    private String sSeparator = ";";
    private String sNSubframesIMRT = ""; // Número de Subframes en cada slice del fichero (IMRT)

    private String sDcmPath = "";
    private String sDcmName = "";
    private String sDcmParentPath = "";
    private String sImgName = "";
    private String sOrigFiles = "";

    private double dPlane[][]=new double[PLANE_MAX_SIZE][PLANE_MAX_SIZE];;
    private double dXCoor[]=new double[PLANE_MAX_SIZE];
    private double dYCoor[]=new double[PLANE_MAX_SIZE];

    private int iDataSource = JD2DCtes.DATA_NOTHING;
    private boolean isRel = false;

    private DICOM dcm = new DICOM();
    private Frame frmMain;
    private JD2DSettings set;

    public JDosePlane(Frame mainframe, JD2DSettings s){
        frmMain=mainframe;
        set=s;
        this.clearObject();
    }

    public void traslate2SSD(){
        this.traslate2SSD(false, false);
    }
    
    public void traslate2SSD(boolean isOnlyPlane){
        this.traslate2SSD(isOnlyPlane, false);
    }

    public void traslate2SSD(boolean isOnlyPlane, boolean bIsSegmentPerf) {
        if (bIsSegmentPerf) {
            this.dSSD=this.dOriginalSSD;
        }
        if (JD2DCtes.STANDARD_SID!=this.dSSD){
            if (isOnlyPlane==false) {
                this.dPixHeight=(this.dPixHeight*JD2DCtes.STANDARD_SID)/this.dSSD;
                this.dPixWidth=(this.dPixWidth*JD2DCtes.STANDARD_SID)/this.dSSD;
                for (iX=0;iX<this.iXmax;iX++){
                    this.dXCoor[iX]=(this.dXCoor[iX]*JD2DCtes.STANDARD_SID)/this.dSSD;
                }
                for (iY=0;iY<this.iYmax;iY++){
                    this.dYCoor[iY]=(this.dYCoor[iY]*JD2DCtes.STANDARD_SID)/this.dSSD;
                }
            }
            this.applyFactorInPlane(Math.pow((this.dSSD/JD2DCtes.STANDARD_SID),2));
            this.dSSD=JD2DCtes.STANDARD_SID;
        }
    }
    
    public void applyFactorInPlane(double dFactor){
        for (iX=0;iX<this.iXmax;iX++){
            for (iY=0;iY<this.iYmax;iY++){
                this.dPlane[iX][iY]=this.dPlane[iX][iY]*dFactor;
            }
        }
    }
    
    public Point2D.Double averageInROI(double dX1, double dX2, double dY1, double dY2){
        double dAvg=0.0;
        double dDesv=0.0;
        int iCont=0;
        int iXIni=JD2DCtes.iSearchInArray(dX1, this.dXCoor, this.iXmax);
        if ((iXIni<0) | (iXIni>this.iXmax-1)){
            iXIni=0;
        }
        int iXFin=JD2DCtes.iSearchInArray(dX2, this.dXCoor, this.iXmax)+1;
        if ((iXFin<0) | (iXFin>this.iXmax-1)){
            iXFin=this.iXmax;
        }
        int iYIni=JD2DCtes.iSearchInArray(dY1, this.dYCoor, this.iYmax);
        if ((iYIni<0) | (iYIni>this.iYmax-1)){
            iYIni=0;
        }
        int iYFin=JD2DCtes.iSearchInArray(dY2, this.dYCoor, this.iYmax)+1;
        if ((iYFin<0) | (iYFin>this.iYmax-1)){
            iYFin=this.iYmax;
        }
        for (iX=iXIni;iX<iXFin;iX++){
            if ((this.dXCoor[iX]>dX1) & (this.dXCoor[iX]<dX2)){
                for (iY=iYIni;iY<iYFin;iY++){
                    if ((this.dYCoor[iY]>dY1) & (this.dYCoor[iY]<dY2)){
                        dAvg=dAvg+this.dPlane[iX][iY];
                        iCont++;
                    }
                }
            }
        }
        if (iCont>0) {
            dAvg=dAvg/iCont;
        }
        else {
            dAvg=JD2DCtes.dInterpData(this.iXmax, this.iYmax, (dX1+dX2)/2, (dY1+dY2)/2,
                                      this.dXCoor, this.dYCoor, this.dPlane);
        }
        if (iCont>1) {
            for (iX=iXIni;iX<iXFin;iX++){
                if ((this.dXCoor[iX]>dX1) & (this.dXCoor[iX]<dX2)){
                    for (iY=iYIni;iY<iYFin;iY++){
                        if ((this.dYCoor[iY]>dY1) & (this.dYCoor[iY]<dY2)){
                            dDesv=dDesv+Math.pow((this.dPlane[iX][iY]-dAvg),2);
                        }
                    }
                }
            }
            dDesv=Math.sqrt(dDesv)/(iCont-1);
        }
        Point2D.Double pResp = new Point2D.Double(dAvg,dDesv);
        return pResp;
    }
    
    private double dGetILFactor(int iIndex, double dTime){
        double dResp=0.0;
        dResp = 1 - (set.getILagA1(iIndex) * Math.exp(- dTime * set.getILagk1(iIndex)));
        dResp = dResp - (set.getILagA2(iIndex) * Math.exp(- dTime * set.getILagk2(iIndex)));
        dResp = dResp - (set.getILagA3(iIndex) * Math.exp(- dTime * set.getILagk3(iIndex)));
        return dResp;
    }

    public int iTransformPlane(int iBeamIndex, StringBuffer sResult){
        int iResp=0;
        double dAux=0.0;
        double dCoefAbs = set.getCoefCal(iBeamIndex);
        double dILFactor = this.dGetILFactor(iBeamIndex, this.dTime);
        double dAuxXCoor[]=new double [200];
        double dAuxYCoor[]=new double [200];
        double dAuxPlane[][]=new double [200][200];
        Point pDim = new Point(0,0);
        double dAuxSSD=this.dLoadFFFFile(iBeamIndex, pDim, dAuxXCoor, dAuxYCoor, dAuxPlane, sResult);
        if (dAuxSSD<=0) {
            return -1;
        }
        if (dILFactor!=0){
            dILFactor=1/dILFactor;
        }
        else {
            sResult.append("El factor de Image Lag resultó nulo. Revisar la configuración del haz.");
            return -1;
        }

        if (dCoefAbs!=0){
            dCoefAbs=1/dCoefAbs;
        }
        else {
            sResult.append("El coeficiente de calibración en dosis absoluta resultó nulo. Revisar la configuración del haz.");
            return -1;
        }

        for (iX=0;iX<pDim.x;iX++){
            dAuxXCoor[iX]=dAuxXCoor[iX]*this.dSSD/dAuxSSD;
        }
        for (iY=0;iY<pDim.y;iY++){
            dAuxYCoor[iY]=dAuxYCoor[iY]*this.dSSD/dAuxSSD;
        }
        
        for (iX=0;iX<this.iXmax;iX++){
            for (iY=0;iY<this.iYmax;iY++){
                if ((iY==512) && (iX==512)) {
                    dAux=this.dPlane[iX][iY];
                }
                dAux=JD2DCtes.dInterpData(pDim.x, pDim.y, this.dXCoor[iX], this.dYCoor[iY],
                                          dAuxXCoor, dAuxYCoor, dAuxPlane)/100; // El fichero FFF está multiplicado por 100.
                this.dPlane[iX][iY] = dAux * dCoefAbs * dILFactor * this.dPlane[iX][iY];
            }
        }
        this.sObsLine="Transformado por JD2D (v1.0.1): ("+ set.sBeamToString(iBeamIndex) +"). SSD original = " + this.dOriginalSSD + ".";
        return iResp;
    }

    public double dLoadFFFFile(int iBeamIndex, Point pDim,
                            double dAuxXCoor[],double dAuxYCoor[],double dAuxPlane[][],
                            StringBuffer sResult){
        double dRet=0.0;
        int i=0;
        int j=0;
        double dFac=0.0;
        String sAuxSeparator=";";
        String sAuxPlanePosLine="";

        String sLine="";
        String sAux="";
        boolean bInHead=false;
        boolean bPostHead=false;
        boolean bInBody=false;
        boolean bPostBody=false;
        boolean bFicheroFinalizado=false;
        String sCampos[]=new String [200];
        iY=0;

        try {
            File fl = new File(set.getBeamFFFile(iBeamIndex));
            if (fl.exists()) {
                BufferedReader leedor=new BufferedReader(new FileReader(set.getBeamFFFile(iBeamIndex)));
                sLine=leedor.readLine();
                if (! sLine.startsWith(JD2DCtes.ET_FILE_INI)){
                    // El fichero no comienza con la etiqueta requerida
                    sLine=null;
                    dRet=-2;
                    sResult.append("No encontrada la etiqueta de principio de fichero: "+JD2DCtes.ET_FILE_INI);
                }
                else {
                    do  {
                        sLine=leedor.readLine();
                        if (sLine==null) {
                            // Fichero incompleto o malformado.
                            dRet=-2;
                            sResult.append("Se ternimo el fichero si encontrar la etiqueta de final de fichero: " + JD2DCtes.ET_FILE_END);
                            break;
                        }
                        sLine=sLine.trim();
                        if (sLine.startsWith(JD2DCtes.ET_FILE_END)) {
                            if ((bInHead==false) && (bPostHead==true) &&
                                (bInBody==false) && (bPostBody==true)){
                                bFicheroFinalizado=true;
                            }
                            else {
                                // Fichero incompleto o malformado.
                                dRet=-2;
                                sResult.append("Se encontro la siguiente etiqueta fuera de lugar: " + JD2DCtes.ET_FILE_END);
                                break;
                            }
                        }
                        else if (sLine.startsWith(JD2DCtes.ET_HEADER_INI)) {
                            if ((bInHead==false) && (bPostHead==false) &&
                                (bInBody==false) && (bPostBody==false)){
                                bInHead=true;
                            }
                            else {
                                // Fichero incompleto o malformado.
                                dRet=-2;
                                sResult.append("Se encontro la siguiente etiqueta fuera de lugar: " + JD2DCtes.ET_HEADER_INI);
                                break;
                            }
                        }
                        else if (sLine.startsWith(JD2DCtes.ET_HEADER_END)){
                            if ((bInHead==true) && (bPostHead==false) &&
                                 (bInBody==false) && (bPostBody==false)){
                                bPostHead=true;
                                bInHead=false;
                            }
                            else {
                                // Fichero incompleto o malformado.
                                dRet=-2;
                                sResult.append("Se encontro la siguiente etiqueta fuera de lugar: " + JD2DCtes.ET_HEADER_END);
                                break;
                            }
                        }
                        else if (sLine.startsWith(JD2DCtes.ET_BODY_INI)){
                            if ((bInHead==false) && (bPostHead==true) &&
                                 (bInBody==false) && (bPostBody==false)){
                                bInBody=true;
                            }
                            else {
                                // Fichero incompleto o malformado.
                                dRet=-2;
                                sResult.append("Se encontro la siguiente etiqueta fuera de lugar: " + JD2DCtes.ET_BODY_INI);
                                break;
                            }
                        }
                        else if (sLine.startsWith(JD2DCtes.ET_BODY_END)){
                            if ((bInHead==false) && (bPostHead==true) &&
                                 (bInBody==true) && (bPostBody==false)){
                                bInBody=false;
                                bPostBody=true;
                            }
                            else {
                                // Fichero incompleto o malformado.
                                dRet=-2;
                                sResult.append("Se encontro la siguiente etiqueta fuera de lugar: " + JD2DCtes.ET_BODY_END);
                                break;
                            }
                        }
                        else {
                            if (bInHead) {
                                if (sLine.startsWith(JD2DCtes.ET_SEP)){
                                    sAux=sLine.substring(JD2DCtes.ET_SEP.length()+1);
                                    if (sAux.trim().equals("[TAB]")) {
                                        sAuxSeparator = "\t";
                                    }
                                    else {
                                        sAuxSeparator = sAux.replace('"',' ').trim();
                                    }
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_NCOLS)) {
                                    sAux=sLine.substring(JD2DCtes.ET_NCOLS.length()).trim();
                                    pDim.x=Integer.parseInt(sAux);
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_NROWS)) {
                                    sAux=sLine.substring(JD2DCtes.ET_NROWS.length()).trim();
                                    pDim.y=Integer.parseInt(sAux);
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_SSD)) {
                                    sAux=sLine.substring(JD2DCtes.ET_SSD.length()+1);
                                    if (sAux.endsWith(" cm")) {
                                        dFac=10;
                                    }
                                    else if (sAux.endsWith(" mm")) {
                                        dFac=1;
                                    }
                                    else {
                                        dFac=0;
                                    }
                                    dRet=Double.parseDouble(sAux.substring(0,sAux.length()-3))*dFac;
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_NBODIES)) {
                                    sAux=sLine.substring(JD2DCtes.ET_NBODIES.length()-1,JD2DCtes.ET_NBODIES.length());
                                    if (sAux.equals("1")) {
                                        dRet=-2;
                                        sResult.append("Fichero con más de un body. solo se permite una imagen por archivo.");
                                        break;
                                    }
                                }
                            }
                            else if (bInBody) {
                                if (sLine.startsWith(JD2DCtes.ET_PLANE_POS)){
                                    sAuxPlanePosLine=sLine;
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_X)) {
                                    sCampos=sLine.split(sAuxSeparator);
                                    dFac=1;
                                    if (sCampos[0].contains("cm")){
                                        dFac=10;
                                    }
                                    for (iX=1;iX<sCampos.length;iX++){
                                        dAuxXCoor[iX-1]=Double.parseDouble(sCampos[iX])*dFac;
                                    }
                                }
                                else if ((! sLine.startsWith(JD2DCtes.ET_Y)) && (! (sLine.length()==0))) {
                                    if (iY<pDim.y) {
                                        sCampos=sLine.split(sAuxSeparator);
                                        dAuxYCoor[iY]=Double.parseDouble(sCampos[0])*dFac;
                                        for (iX=1;iX<sCampos.length;iX++){
                                            dAuxPlane[iX-1][iY]=Double.parseDouble(sCampos[iX]);
                                        }
                                    }
                                    iY++;
                                }

                            }
                            if ((bPostHead==true) && (bInBody==false) && (bPostBody==false)) {
                                if (dRet==0) {
                                    dRet = -2;
                                    sResult.append("No se encontro la siguiente etiqueta: " + JD2DCtes.ET_SSD);
                                    break;
                                }
                                if (sAuxSeparator.equals("")) {
                                    dRet = -2;
                                    sResult.append("No se encontro la siguiente etiqueta: " + JD2DCtes.ET_SEP);
                                    break;
                                }
                                if ((pDim.x==0) || (pDim.y==0)) {
                                    dRet = -2;
                                    sResult.append("No se encontraron las etiquetas de tamaño de la matriz de datos: " + JD2DCtes.ET_SZ_CR + " o " + JD2DCtes.ET_SZ_IN);
                                    break;
                                }
                            }
                        }
                    } while (bFicheroFinalizado==false); // end while
                    leedor.close();
                }

            }
            else{
                dRet=-1;
                sResult.append("Hubo un problema y el fichero en FFF no se encontro. Revise la configuración del haz");
            }
        }
        catch (IOException e) {
            dRet=-1;
            sResult.append("Problema inesperado."+e.toString());
        }

        return dRet;
    }

    public int iLoadOPGDoseFile(String sPath, StringBuffer sResult){
        int iRet=0;
        int i=0;
        int j=0;
        double dFac=0.0;
        String sLine="";
        String sAux="";
        boolean bInHead=false;
        boolean bPostHead=false;
        boolean bInDM=false;
        boolean bInBody=false;
        boolean bPostBody=false;
        boolean bFicheroFinalizado=false;
        String sCampos[];
        double dDataUnitsFactor=1;
        iY=0;

        try {
            File fl = new File(sPath);
            if (fl.exists()) {
                this.sDcmName=fl.getName();
                this.sDcmParentPath=fl.getParent();
                BufferedReader leedor=new BufferedReader(new FileReader(sPath));
                sLine=leedor.readLine();
                if (! sLine.startsWith(JD2DCtes.ET_FILE_INI)){
                    // El fichero no comienza con la etiqueta requerida
                    sLine=null;
                    iRet=-2;
                    sResult.append("No encontrada la etiqueta de principio de fichero: "+JD2DCtes.ET_FILE_INI);
                }
                else {
                    do  {
                        sLine=leedor.readLine();
                        if (sLine==null) {
                            // Fichero incompleto o malformado.
                            iRet=-2;
                            sResult.append("Se ternimo el fichero si encontrar la etiqueta de final de fichero: " + JD2DCtes.ET_FILE_END);
                            break;
                        }
                        sLine=sLine.trim();
                        if (sLine.startsWith(JD2DCtes.ET_FILE_END)) {
                            if ((bInDM==false) && (bInHead==false) && (bPostHead==true) &&
                                (bInBody==false) && (bPostBody==true)){
                                bFicheroFinalizado=true;
                            }
                            else {
                                // Fichero incompleto o malformado.
                                iRet=-2;
                                sResult.append("Se encontro la siguiente etiqueta fuera de lugar: " + JD2DCtes.ET_FILE_END);
                                break;
                            }
                        }
                        else if (sLine.startsWith(JD2DCtes.ET_HEADER_INI)) {
                            if ((bInDM==false) && (bInHead==false) && (bPostHead==false) &&
                                (bInBody==false) && (bPostBody==false)){
                                bInHead=true;
                            }
                            else {
                                // Fichero incompleto o malformado.
                                iRet=-2;
                                sResult.append("Se encontro la siguiente etiqueta fuera de lugar: " + JD2DCtes.ET_HEADER_INI);
                                break;
                            }
                        }
                        else if (sLine.startsWith(JD2DCtes.ET_HEADER_END)){
                            if ((bInDM==false) && (bInHead==true) && (bPostHead==false) &&
                                 (bInBody==false) && (bPostBody==false)){
                                bPostHead=true;
                                bInHead=false;
                            }
                            else {
                                // Fichero incompleto o malformado.
                                iRet=-2;
                                sResult.append("Se encontro la siguiente etiqueta fuera de lugar: " + JD2DCtes.ET_HEADER_END);
                                break;
                            }
                        }
                        else if (sLine.startsWith(JD2DCtes.ET_DM_INI)){
                            if ((bInDM==false) && (bInHead==false) && (bPostHead==true) &&
                                 (bInBody==false) && (bPostBody==false)){
                                bInDM=true;
                            }
                            else {
                                // Fichero incompleto o malformado.
                                iRet=-2;
                                sResult.append("Se encontro la siguiente etiqueta fuera de lugar: " + JD2DCtes.ET_DM_INI);
                                break;
                            }
                        }
                        else if (sLine.startsWith(JD2DCtes.ET_DM_END)){
                            if ((bInDM==true) && (bInHead==false) && (bPostHead==true) &&
                                 (bInBody==false) && (bPostBody==false)){
                                bInDM=false;
                            }
                            else {
                                // Fichero incompleto o malformado.
                                iRet=-2;
                                sResult.append("Se encontro la siguiente etiqueta fuera de lugar: " + JD2DCtes.ET_DM_END);
                                break;
                            }
                        }
                        else if (sLine.startsWith(JD2DCtes.ET_BODY_INI)){
                            if ((bInDM==false) && (bInHead==false) && (bPostHead==true) &&
                                 (bInBody==false) && (bPostBody==false)){
                                bInBody=true;
                            }
                            else {
                                // Fichero incompleto o malformado.
                                iRet=-2;
                                sResult.append("Se encontro la siguiente etiqueta fuera de lugar: " + JD2DCtes.ET_BODY_INI);
                                break;
                            }
                        }
                        else if (sLine.startsWith(JD2DCtes.ET_BODY_END)){
                            if ((bInDM==false) && (bInHead==false) && (bPostHead==true) &&
                                 (bInBody==true) && (bPostBody==false)){
                                bInBody=false;
                                bPostBody=true;
                            }
                            else {
                                // Fichero incompleto o malformado.
                                iRet=-2;
                                sResult.append("Se encontro la siguiente etiqueta fuera de lugar: " + JD2DCtes.ET_BODY_END);
                                break;
                            }
                        }
                        else {
                            if (bInHead) {
                                if (sLine.startsWith(JD2DCtes.ET_SEP)){
                                    sAux=sLine.substring(JD2DCtes.ET_SEP.length()+1);
                                    if (sAux.trim().equals("[TAB]")) {
                                        this.sSeparator = "\t";
                                    }
                                    else {
                                        this.sSeparator = sAux.replace('"',' ').trim();
                                    }
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_IMG_NAME)) {
                                    this.sImgName=sLine.substring(JD2DCtes.ET_IMG_NAME.length()+1).trim();
                                }
                                 else if (sLine.startsWith(JD2DCtes.ET_NOTE)){
                                    this.sObsLine=sLine;
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_DOSEFACTOR)) {
                                    this.dDoseFactor=Double.parseDouble(sLine.substring(JD2DCtes.ET_DOSEFACTOR.length()+1));
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_NCOLS)) {
                                    sAux=sLine.substring(JD2DCtes.ET_NCOLS.length()).trim();
                                    this.iXmax=Integer.parseInt(sAux);
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_NROWS)) {
                                    sAux=sLine.substring(JD2DCtes.ET_NROWS.length()).trim();
                                    this.iYmax=Integer.parseInt(sAux);
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_SSD)) {
                                    sAux=sLine.substring(JD2DCtes.ET_SSD.length()+1);
                                    if (sAux.endsWith(" cm")) {
                                        dFac=10;
                                    }
                                    else if (sAux.endsWith(" mm")) {
                                        dFac=1;
                                    }
                                    else {
                                        dFac=0;
                                    }
                                    this.dSSD=Double.parseDouble(sAux.substring(0,sAux.length()-3))*dFac;
                                    this.dOriginalSSD=this.dSSD;
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_SID)) {
                                    sAux=sLine.substring(JD2DCtes.ET_SID.length()+1);
                                    if (sAux.endsWith(" cm")) {
                                        dFac=10;
                                    }
                                    else if (sAux.endsWith(" mm")) {
                                        dFac=1;
                                    }
                                    else {
                                        dFac=0;
                                    }
                                    this.dSID=Double.parseDouble(sAux.substring(0,sAux.length()-3))*dFac;
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_SZ_IN)) {
                                    sAux=sLine.substring(JD2DCtes.ET_SZ_IN.length()+1);
                                    if (sAux.endsWith(" cm")) {
                                        dFac=10;
                                    }
                                    else if (sAux.endsWith(" mm")) {
                                        dFac=1;
                                    }
                                    else {
                                        dFac=0;
                                    }
                                    this.dSizeWidth=Double.parseDouble(sAux.substring(0,sAux.length()-3))*dFac;
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_SZ_CR)) {
                                    sAux=sLine.substring(JD2DCtes.ET_SZ_CR.length()+1);
                                    if (sAux.endsWith(" cm")) {
                                        dFac=10;
                                    }
                                    else if (sAux.endsWith(" mm")) {
                                        dFac=1;
                                    }
                                    else {
                                        dFac=0;
                                    }
                                    this.dSizeHeight=Double.parseDouble(sAux.substring(0,sAux.length()-3))*dFac;
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_DOSE_UDS)) {
                                    sAux=sLine.substring(JD2DCtes.ET_DOSE_UDS.length()+1).trim();
                                    if (sAux.endsWith("mGy")) {
                                        this.dDoseUnitsFactor=0.1;
                                        this.isRel=false;
                                        this.sDoseUnits="cGy";
                                    }
                                    else if (sAux.endsWith(" Gy")) {
                                        this.dDoseUnitsFactor=100;
                                        this.sDoseUnits="cGy";
                                        this.isRel=false;
                                    }
                                    else if (sAux.endsWith("cGy")) {
                                        this.dDoseUnitsFactor=1;
                                        this.sDoseUnits="cGy";
                                        this.isRel=false;
                                    }
                                    else if (sAux.endsWith("%")) {
                                        this.dDoseUnitsFactor=1;
                                        this.sDoseUnits="1 %";
                                        this.isRel=true;
                                    }
                                    else {
                                        this.dDoseUnitsFactor=1;
                                        this.sDoseUnits="??";
                                        this.isRel=true;
                                    }
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_NBODIES)) {
                                    sAux=sLine.substring(JD2DCtes.ET_NBODIES.length()-1,JD2DCtes.ET_NBODIES.length());
                                    if (sAux.equals("1")) {
                                        iRet=-2;
                                        sResult.append("Fichero con más de un body. solo se permite una imagen por archivo.");
                                        break;
                                    }
                                }
                            }
                            else if (bInDM){
                                if (sLine.startsWith(JD2DCtes.ET_UM)){
                                    this.dMU=Double.parseDouble(sLine.substring(JD2DCtes.ET_UM.length()+1));
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_NSUBFRAMES)) {
                                    this.iNSubframes=Integer.parseInt(sLine.substring(JD2DCtes.ET_NSUBFRAMES.length()+1));
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_AQTIME)) {
                                    this.dTime=Double.parseDouble(sLine.substring(JD2DCtes.ET_AQTIME.length()+1));
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_FILES)) {
                                    this.sOrigFiles=sLine.substring(JD2DCtes.ET_FILES.length()+1);
                                }
                            }
                            else if (bInBody) {
                                if (sLine.startsWith(JD2DCtes.ET_PLANE_POS)){
                                    this.sPlanePosLine=sLine;
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_X)) {
                                    sCampos=sLine.split(this.sSeparator);
                                    dFac=1;
                                    if (sCampos[0].contains("cm")){
                                        dFac=10;
                                    }
                                    for (iX=1;iX<sCampos.length;iX++){
                                        this.dXCoor[iX-1]=Double.parseDouble(sCampos[iX])*dFac;
                                    }
                                }
                                else if ((! sLine.startsWith(JD2DCtes.ET_Y)) && (! (sLine.length()==0))) {
                                    if (iY<this.iYmax) {
                                        sCampos=sLine.split(this.sSeparator);
                                        this.dYCoor[iY]=Double.parseDouble(sCampos[0])*dFac;
                                        for (iX=1;iX<sCampos.length;iX++){
                                            this.dPlane[iX-1][iY]=Double.parseDouble(sCampos[iX])*this.dDoseFactor*this.dDoseUnitsFactor;
                                        }
                                    }
                                    iY++;
                                }

                            }
                            if ((bPostHead==true) && (bInBody==false) && (bPostBody==false)) {
                                if (this.dSSD==0) {
                                    iRet = -2;
                                    sResult.append("No se encontro la siguiente etiqueta: " + JD2DCtes.ET_SSD);
                                    break;
                                }
                                if (this.sSeparator=="") {
                                    iRet = -2;
                                    sResult.append("No se encontro la siguiente etiqueta: " + JD2DCtes.ET_SEP);
                                    break;
                                }
                                if ((this.iXmax==0) || (this.iYmax==0)) {
                                    iRet = -2;
                                    sResult.append("No se encontraron las etiquetas de tamaño de la matriz de datos: " + JD2DCtes.ET_SZ_CR + " o " + JD2DCtes.ET_SZ_IN);
                                    break;
                                }
                                this.dPixHeight=this.dSizeHeight/this.iYmax;
                                this.dPixWidth=this.dSizeWidth/this.iXmax;
                            }
                        }
                    } while (bFicheroFinalizado==false); // end while
                    leedor.close();
                }
            }
            else{
                iRet=-1;
                sResult.append("Hubo un problema y el fichero en fomato OPG no se encontro.");
            }
        }
        catch (IOException e) {
            iRet=-1;
            sResult.append("Problema inesperado."+e.toString());
        }

        return iRet;
    }

    public int iLoadOPGMatriXXFFF(String sPath, StringBuffer sResult, int iQuadrant){
        int iRet=0;
        int i=0;
        int j=0;
        double dFac=0.0;
        String sAuxSeparator=";";
        double dAuxDoseFactor=0.0;
        int iAuxXmax=0;
        int iAuxYmax=0;
        double dAuxSSD=0.0;
        double dAuxSizeWidth=0.0;
        double dAuxSizeHeight=0.0;
        double dAuxDoseUnitsFactor=0.0;
        boolean isAuxRel=false;
        String sAuxDoseUnits="";
        String sAuxPlanePosLine="";
        double dAuxXCoor[]=new double [200];
        double dAuxYCoor[]=new double [200];
        double dAuxPlane[][]=new double [200][200];

        String sLine="";
        String sAux="";
        boolean bInHead=false;
        boolean bPostHead=false;
        boolean bInBody=false;
        boolean bPostBody=false;
        boolean bFicheroFinalizado=false;
        String sCampos[]=new String [200];;
        double dDataUnitsFactor=1;
        iY=0;

        try {
            File fl = new File(sPath);
            if (fl.exists()) {
                BufferedReader leedor=new BufferedReader(new FileReader(sPath));
                sLine=leedor.readLine();
                if (! sLine.startsWith(JD2DCtes.ET_FILE_INI)){
                    // El fichero no comienza con la etiqueta requerida
                    sLine=null;
                    iRet=-2;
                    sResult.append("No encontrada la etiqueta de principio de fichero: "+JD2DCtes.ET_FILE_INI);
                }
                else {
                    do  {
                        sLine=leedor.readLine();
                        if (sLine==null) {
                            // Fichero incompleto o malformado.
                            iRet=-2;
                            sResult.append("Se ternimo el fichero si encontrar la etiqueta de final de fichero: " + JD2DCtes.ET_FILE_END);
                            break;
                        }
                        sLine=sLine.trim();
                        if (sLine.startsWith(JD2DCtes.ET_FILE_END)) {
                            if ((bInHead==false) && (bPostHead==true) &&
                                (bInBody==false) && (bPostBody==true)){
                                bFicheroFinalizado=true;
                            }
                            else {
                                // Fichero incompleto o malformado.
                                iRet=-2;
                                sResult.append("Se encontro la siguiente etiqueta fuera de lugar: " + JD2DCtes.ET_FILE_END);
                                break;
                            }
                        }
                        else if (sLine.startsWith(JD2DCtes.ET_HEADER_INI)) {
                            if ((bInHead==false) && (bPostHead==false) &&
                                (bInBody==false) && (bPostBody==false)){
                                bInHead=true;
                            }
                            else {
                                // Fichero incompleto o malformado.
                                iRet=-2;
                                sResult.append("Se encontro la siguiente etiqueta fuera de lugar: " + JD2DCtes.ET_HEADER_INI);
                                break;
                            }
                        }
                        else if (sLine.startsWith(JD2DCtes.ET_HEADER_END)){
                            if ((bInHead==true) && (bPostHead==false) &&
                                 (bInBody==false) && (bPostBody==false)){
                                bPostHead=true;
                                bInHead=false;
                            }
                            else {
                                // Fichero incompleto o malformado.
                                iRet=-2;
                                sResult.append("Se encontro la siguiente etiqueta fuera de lugar: " + JD2DCtes.ET_HEADER_END);
                                break;
                            }
                        }
                        else if (sLine.startsWith(JD2DCtes.ET_BODY_INI)){
                            if ((bInHead==false) && (bPostHead==true) &&
                                 (bInBody==false) && (bPostBody==false)){
                                bInBody=true;
                            }
                            else {
                                // Fichero incompleto o malformado.
                                iRet=-2;
                                sResult.append("Se encontro la siguiente etiqueta fuera de lugar: " + JD2DCtes.ET_BODY_INI);
                                break;
                            }
                        }
                        else if (sLine.startsWith(JD2DCtes.ET_BODY_END)){
                            if ((bInHead==false) && (bPostHead==true) &&
                                 (bInBody==true) && (bPostBody==false)){
                                bInBody=false;
                                bPostBody=true;
                            }
                            else {
                                // Fichero incompleto o malformado.
                                iRet=-2;
                                sResult.append("Se encontro la siguiente etiqueta fuera de lugar: " + JD2DCtes.ET_BODY_END);
                                break;
                            }
                        }
                        else {
                            if (bInHead) {
                                if (sLine.startsWith(JD2DCtes.ET_SEP)){
                                    sAux=sLine.substring(JD2DCtes.ET_SEP.length()+1);
                                    if (sAux.trim().equals("[TAB]")) {
                                        sAuxSeparator = "\t";
                                    }
                                    else {
                                        sAuxSeparator = sAux.replace('"',' ').trim();
                                    }
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_DOSEFACTOR)) {
                                    dAuxDoseFactor=Double.parseDouble(sLine.substring(JD2DCtes.ET_DOSEFACTOR.length()+1));
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_NCOLS)) {
                                    sAux=sLine.substring(JD2DCtes.ET_NCOLS.length()).trim();
                                    iAuxXmax=Integer.parseInt(sAux);
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_NROWS)) {
                                    sAux=sLine.substring(JD2DCtes.ET_NROWS.length()).trim();
                                    iAuxYmax=Integer.parseInt(sAux);
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_SSD)) {
                                    sAux=sLine.substring(JD2DCtes.ET_SSD.length()+1);
                                    if (sAux.endsWith(" cm")) {
                                        dFac=10;
                                    }
                                    else if (sAux.endsWith(" mm")) {
                                        dFac=1;
                                    }
                                    else {
                                        dFac=0;
                                    }
                                    dAuxSSD=Double.parseDouble(sAux.substring(0,sAux.length()-3))*dFac;
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_SID)) {
                                    sAux=sLine.substring(JD2DCtes.ET_SID.length()+1);
                                    if (sAux.endsWith(" cm")) {
                                        dFac=10;
                                    }
                                    else if (sAux.endsWith(" mm")) {
                                        dFac=1;
                                    }
                                    else {
                                        dFac=0;
                                    }
                                    this.dSID=Double.parseDouble(sAux.substring(0,sAux.length()-3))*dFac;
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_SZ_IN)) {
                                    sAux=sLine.substring(JD2DCtes.ET_SZ_IN.length()+1);
                                    if (sAux.endsWith(" cm")) {
                                        dFac=10;
                                    }
                                    else if (sAux.endsWith(" mm")) {
                                        dFac=1;
                                    }
                                    else {
                                        dFac=0;
                                    }
                                    dAuxSizeWidth=Double.parseDouble(sAux.substring(0,sAux.length()-3))*dFac;
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_SZ_CR)) {
                                    sAux=sLine.substring(JD2DCtes.ET_SZ_CR.length()+1);
                                    if (sAux.endsWith(" cm")) {
                                        dFac=10;
                                    }
                                    else if (sAux.endsWith(" mm")) {
                                        dFac=1;
                                    }
                                    else {
                                        dFac=0;
                                    }
                                    dAuxSizeHeight=Double.parseDouble(sAux.substring(0,sAux.length()-3))*dFac;
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_DOSE_UDS)) {
                                    sAux=sLine.substring(JD2DCtes.ET_DOSE_UDS.length()+1).trim();
                                    if (sAux.endsWith("mGy")) {
                                        dAuxDoseUnitsFactor=0.1;
                                        isAuxRel=false;
                                        sAuxDoseUnits="cGy";
                                    }
                                    else if (sAux.endsWith(" Gy")) {
                                        dAuxDoseUnitsFactor=100;
                                        sAuxDoseUnits="cGy";
                                        isAuxRel=false;
                                    }
                                    else if (sAux.endsWith("%")) {
                                        dAuxDoseUnitsFactor=1;
                                        sAuxDoseUnits="1 %";
                                        isAuxRel=true;
                                    }
                                    else {
                                        dAuxDoseUnitsFactor=1;
                                        sAuxDoseUnits="??";
                                        isAuxRel=true;
                                    }
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_NBODIES)) {
                                    sAux=sLine.substring(JD2DCtes.ET_NBODIES.length()-1,JD2DCtes.ET_NBODIES.length());
                                    if (sAux.equals("1")) {
                                        iRet=-2;
                                        sResult.append("Fichero con más de un body. solo se permite una imagen por archivo.");
                                        break;
                                    }
                                }
                            }
                            else if (bInBody) {
                                if (sLine.startsWith(JD2DCtes.ET_PLANE_POS)){
                                    sAuxPlanePosLine=sLine;
                                }
                                else if (sLine.startsWith(JD2DCtes.ET_X)) {
                                    sCampos=sLine.split(sAuxSeparator);
                                    dFac=1;
                                    if (sCampos[0].contains("cm")){
                                        dFac=10;
                                    }
                                    for (iX=1;iX<sCampos.length;iX++){
                                        dAuxXCoor[iX-1]=Double.parseDouble(sCampos[iX])*dFac;
                                    }
                                }
                                else if ((! sLine.startsWith(JD2DCtes.ET_Y)) && (! (sLine.length()==0))) {
                                    if (iY<iAuxYmax) {
                                        sCampos=sLine.split(sAuxSeparator);
                                        dAuxYCoor[iY]=Double.parseDouble(sCampos[0])*dFac;
                                        for (iX=1;iX<sCampos.length;iX++){
                                            dAuxPlane[iX-1][iY]=Double.parseDouble(sCampos[iX])*dAuxDoseFactor*dAuxDoseUnitsFactor;
                                        }
                                    }
                                    iY++;
                                }

                            }
                            if ((bPostHead==true) && (bInBody==false) && (bPostBody==false)) {
                                if (dAuxSSD==0) {
                                    iRet = -2;
                                    sResult.append("No se encontro la siguiente etiqueta: " + JD2DCtes.ET_SSD);
                                    break;
                                }
                                if (sAuxSeparator=="") {
                                    iRet = -2;
                                    sResult.append("No se encontro la siguiente etiqueta: " + JD2DCtes.ET_SEP);
                                    break;
                                }
                                if ((iAuxXmax==0) || (iAuxYmax==0)) {
                                    iRet = -2;
                                    sResult.append("No se encontraron las etiquetas de tamaño de la matriz de datos: " + JD2DCtes.ET_SZ_CR + " o " + JD2DCtes.ET_SZ_IN);
                                    break;
                                }
                            }
                        }
                    } while (bFicheroFinalizado==false); // end while
                    leedor.close();
                    if (iQuadrant==JD2DCtes.iX1Y1){
                        for (iX=0;iX<iAuxXmax;iX++){
                            this.dXCoor[iX]=dAuxXCoor[iX] -100;
                            for (iY=0;iY<iAuxYmax;iY++){
                                this.dPlane[iX][iY]=dAuxPlane[iX][iY];
                            }
                        }
                        for (iY=0;iY<iAuxYmax;iY++){
                            this.dYCoor[iY]=dAuxYCoor[iY]-100;
                        }
                        this.iXmax=iAuxXmax;
                        this.iYmax=iAuxYmax;
                        this.dSSD=dAuxSSD;
                        this.dOriginalSSD=this.dSSD;
                    }
                    else if(iQuadrant == JD2DCtes.iX2Y1){
                        if (this.dSSD!=dAuxSSD){
                            iRet=-1;
                            sResult.append("Los ficheros X1Y1 y X2Y1 tienen distinta SSD.");
                            return iRet;
                        }
                        int iIniXAux=0;
                        int iIniX=0;
                        for (iX=0;iX<iAuxXmax;iX++){
                            if (dAuxXCoor[iX]> -100){
                                iIniXAux=iX;
                                break;
                            }
                        }
                        for (iX=0;iX<this.iXmax;iX++){
                            if (dXCoor[iX]> 0){
                                iIniX=iX;
                                break;
                            }
                        }
                        for (iX=0;iX<iAuxXmax-iIniXAux;iX++){
                            this.dXCoor[iX+iIniX]=dAuxXCoor[iX+iIniXAux]+100;
                            for (iY=0;iY<iAuxYmax;iY++){
                                this.dPlane[iX+iIniX][iY]=dAuxPlane[iX+iIniXAux][iY];
                            }
                        }
                        this.iXmax=iAuxXmax+iIniX-iIniXAux;
                    }
                    else if(iQuadrant == JD2DCtes.iX1Y2){
                        if (this.dSSD!=dAuxSSD){
                            iRet=-1;
                            sResult.append("Los ficheros X1Y1 y X1Y2 tienen distinta SSD.");
                            return iRet;
                        }
                        int iIniYAux=0;
                        int iIniY=0;
                        for (iX=0;iX<iAuxXmax;iX++){
                            dAuxXCoor[iX]=dAuxXCoor[iX]+100;
                        }
                        for (iY=0;iY<iAuxYmax;iY++){
                            dAuxYCoor[iY]=(-1*dAuxYCoor[iY])+100;
                        }
                        for (iY=iAuxYmax-1;iY>=0;iY--){
                            if (dAuxYCoor[iY]> 0){
                                iIniYAux=iY;
                                break;
                            }
                        }
                        for (iY=0;iY<this.iYmax;iY++){
                            if (dYCoor[iY]> 0){
                                iIniY=iY;
                                break;
                            }
                        }
                        for (iY=iIniY; iY<=iIniY + iIniYAux; iY++){
                            this.dYCoor[iY]=dAuxYCoor[iIniY + iIniYAux-iY];
                        }
                        for (iX=0;iX<iAuxXmax;iX++){
                            for (iY=iIniY;iY<=iIniY+iIniYAux;iY++){
                                this.dPlane[iX][iY]=dAuxPlane[iAuxXmax-1-iX][iIniYAux-iY+iIniY];
                            }
                        }
                        this.iYmax=iIniY + iIniYAux + 1;
                    }
                    else if(iQuadrant == JD2DCtes.iX2Y2){
                        if (this.dSSD!=dAuxSSD){
                            iRet=-1;
                            sResult.append("Los ficheros X1Y1 y X2Y2 tienen distinta SSD.");
                            return iRet;
                        }
                        int iIniX=0;
                        int iIniYAux=0;
                        int iIniXAux=0;
                        int iIniY=0;
                        for (iX=0;iX<iAuxXmax;iX++){
                            dAuxXCoor[iX]=(-1*dAuxXCoor[iX])+100;
                        }
                        for (iY=0;iY<iAuxYmax;iY++){
                            dAuxYCoor[iY]=(-1*dAuxYCoor[iY])+100;
                        }
                        for (iX=iAuxXmax-1;iX>=0;iX--){
                            if (dAuxXCoor[iX]> 0){
                                iIniXAux=iX;
                                break;
                            }
                        }
                        for (iY=iAuxYmax-1;iY>=0;iY--){
                            if (dAuxYCoor[iY]> 0){
                                iIniYAux=iY;
                                break;
                            }
                        }
                        for (iX=0;iX<this.iXmax;iX++){
                            if (dXCoor[iX]> 0){
                                iIniX=iX;
                                break;
                            }
                        }
                        for (iY=0;iY<this.iYmax;iY++){
                            if (dYCoor[iY]> 0){
                                iIniY=iY;
                                break;
                            }
                        }
                        for (iX=iIniX;iX<=iIniX+iIniXAux;iX++){
                            for (iY=iIniY;iY<=iIniY+iIniYAux;iY++){
                                this.dPlane[iX][iY]=dAuxPlane[iIniXAux+iIniX-iX][iIniYAux+iIniY-iY];
                            }
                        }
                        this.isRel=true;
                        this.dDoseFactor=1.0;
                        this.sDoseUnits="cGy";
                        this.dPixHeight=(this.dXCoor[this.iXmax-1]-this.dXCoor[0])/this.iXmax;
                        this.dPixWidth=(this.dYCoor[this.iYmax-1]-this.dYCoor[0])/this.iYmax;
                    }
                }

            }
            else{
                iRet=-1;
                sResult.append("Hubo un problema y el fichero en fomato OPG no se encontro.");
            }
        }
        catch (IOException e) {
            iRet=-1;
            sResult.append("Problema inesperado."+e.toString());
        }

        return iRet;
    }

    public int iLoadDICOMDoseFile(String sPath, StringBuffer sResult){
        return iLoadDICOMDoseFile(sPath, sResult, false);
    }

    public int iLoadDICOMDoseFile(String sPath, StringBuffer sResult, boolean isIMRT) {
        // initialise instance variables
        String sLine = "";
        int dimensiones[] = new int[5];
        double dAux=0;
        int pixel[] = new int[4];
        iX = 0;
        iY = 0;
        for (int i=0;i<5;i++) dimensiones[i]=0;
        this.sDcmPath = sPath;
        dcm.open(sPath);
        if (dcm.getWidth()==0) {
            sLine="Error de apertura: '" + sPath + "'\n"; sResult.append(sLine);
            return -1;
        }
        else {
            sLine="Procesando: '" + sPath + "'...\n"; sResult.append(sLine);
        }
        File fl=new File(sPath);
        this.sDcmName=fl.getName();
        this.sDcmParentPath=fl.getParent();
        this.sImgName=dcm.getTitle();
        dimensiones=dcm.getDimensions();
        
        JD2DDicomDecoder dd = new JD2DDicomDecoder(fl.getParent()+"\\",fl.getName());
        FileInfo fi = null;
        try {
            fi = dd.getFileInfo();
            if (this.iDataSource==JD2DCtes.DATA_PCRT){
                this.dPixWidth=fi.pixelWidth;
                this.dPixHeight=fi.pixelHeight;
            }
            else {
                this.dPixWidth=JD2DCtes.PIXEL_RESOLUTION_EPID;
                this.dPixHeight=JD2DCtes.PIXEL_RESOLUTION_EPID;
            }
            this.dSizeHeight=this.dPixHeight+this.iYmax;
            this.dSizeWidth=this.dPixWidth+this.iXmax;

        }
        catch (IOException e) {
            String msg = e.getMessage();
            if (msg.indexOf("EOF")<0) {
            }
            else if (!dd.dicmFound()) {
                msg = "This does not appear to be a valid\n"
                + "DICOM file. It does not have the\n"
                + "characters 'DICM' at offset 128.";
           }
            else {
                msg="(Unexpected Error) - " + msg;
            }
            sLine="Error : '" + msg + "'\n"; sResult.append(sLine);
            return -1;
        }
        if (this.iDataSource==JD2DCtes.DATA_PCRT){
            this.dSSD=0.0;
            this.dOriginalSSD=0.0;
            this.dMU=0.0;
        }
        else {
            this.dSSD = dd.dSID;
            this.dOriginalSSD=this.dSSD;
            this.dMU=dd.dUM;
        }
        this.sDoseUnits=dd.sDoseType;
        this.isRel=false;
        this.dDoseFactor=dd.dDoseFactor;
        if (this.dDoseFactor==-1){
            this.sDoseUnits="cGy";
            this.dDoseFactor=1;
        }

        if (dd.dSubframes > 0) {
            dAux=dd.dSubframes;
        }
        else {
            dAux=0;
            this.sNSubframesIMRT=dd.sSubframes;
            StringTokenizer st = new StringTokenizer(dd.sSubframes,"\\");
            Double d;
            while (st.hasMoreTokens()) {
                try {d = new Double(st.nextToken());}
                catch (NumberFormatException e) {d = null;}
                if (d!=null) {
                    dAux=dAux+d.doubleValue();
                }
           }
        }

        if (dimensiones[3] > 1) {
            sLine="CUIDADO: El fichero tiene " + dimensiones[3] + " slices!\n"; sResult.append(sLine);
        }
        this.iChanels=dimensiones[2];
        this.iSlices=dimensiones[3];
        this.iFrames=dimensiones[4];
        this.iNSubframes = (int) dAux;
        this.dSID=1000;
        this.dTime=this.iNSubframes/JD2DCtes.SUBFRAMES_SEGUNDO;
        this.dTime=JD2DCtes.dRound(this.dTime,2);
        sLine="\tChanels: "+this.iChanels+"\tSlices: "+this.iSlices+"\tFrames: "+iFrames+"\tSubframes: "+this.iNSubframes+"\n";
        sResult.append(sLine);
        sLine="\tHeight: "+dimensiones[1]+"\tWidth:"+dimensiones[1]+" \tSSD: "+this.dSSD+"\tUM: "+this.dMU+"\n";
        sResult.append(sLine);

        dcm.show("PROCESANDO LA IMAGEN, POR FAVOR, ESPERE...");
        //dcm.getFileInfo().
        //this.dPixHeight=dcm.pixelHeight;
        //this.dPixWidth=dcm.pixelWidth;
        this.iXmax = dcm.getHeight();
        this.iYmax = dcm.getWidth();
        //sLine="\t- DCM - \tHeight: "+iXmax+"\tWidth:"+iYmax+"\n"; sResult.append(sLine);
        for (iY=0;iY<iYmax;iY++) {
               this.dYCoor[iY]=this.dPixWidth*(iY-(((double)iYmax)/2));
        }
        for (iX=iXmax-1;iX>=0;iX--) {
            this.dXCoor[iX]=this.dPixHeight*(iX-(((double)iXmax)/2));
            for (iY=0;iY<iYmax;iY++) {
                pixel=dcm.getPixel(iY,iX);
//// TODO               if ((iY==512) && (iX==512)) {
////                        dAux=pixel[0];
////                        dAux=pixel[0]*this.iNSubframes;
////                }
                this.dPlane[iX][iY]=pixel[0]*this.iNSubframes;
            }
        }
        if (isIMRT==false) {
            dcm.close();
        }
        return 0;
    }

    public int iLoadSliceDICOMDoseFile(int iSlice) {
        double dAux=0;
        int pixel[] = new int[4];
        iX = 0;
        iY = 0;

        String sHarl = this.sNSubframesIMRT.replace("\\","#");
        String sAux [] = sHarl.split("#");
        Double d;

        try {
            d = Double.parseDouble(sAux[iSlice-1]);
        }
        catch (NumberFormatException e) {
            d = null;
        }
        if (d!=null) {
                dAux=dAux+d.doubleValue();
        }
        
        this.iNSubframes = (int) dAux;
        this.dTime=this.iNSubframes/JD2DCtes.SUBFRAMES_SEGUNDO;
        this.dTime=JD2DCtes.dRound(this.dTime,2);

        dcm.setSlice(iSlice + 1);
        for (iX=iXmax-1;iX>=0;iX--) {
            for (iY=0;iY<iYmax;iY++) {
////TODO                if ((iY==512) && (iX==512)) {
////                        dAux=pixel[0];
////                        dAux=pixel[0]*this.iNSubframes;
////                }
                pixel=dcm.getPixel(iY,iX);
                this.dPlane[iX][iY]=pixel[0]*this.iNSubframes;
            }
        }
        if (iSlice==(this.iSlices-1)) {
            dcm.close();
        }
        return 0;
    }

    public int iOutputIMGFile(String sPath, StringBuffer sResult){
        return this.iOutputOPGFile(sPath, sResult, false, 0 , 0);
    }

    public int iOutputIMGFile(String sPath, StringBuffer sResult,
                                boolean isGoinToBeInterpolated, int iXNumPix, int iYNumPix){
        int iRes=0;
        int iAux=0;
        double dAux=0.0;
        Color col;
        BufferedImage buff = new BufferedImage(this.iXmax, this.iYmax, BufferedImage.TYPE_INT_ARGB);
        for (iX=0;iX<iXmax;iX++) {
            for (iY=0;iY<iYmax;iY++) {
                if (dAux<this.dPlane[iX][iY]) {
                    dAux = this.dPlane[iX][iY];
                }
            }
        }
        for (iX=0;iX<iXmax;iX++) {
            for (iY=0;iY<iYmax;iY++) {
                iAux= (int) ((255*this.dPlane[iX][iY])/dAux);
                col = new Color(iAux,iAux,iAux);
                buff.setRGB(iY, iX, col.getRGB());
            }
        }
        ImagePlus ip = new ImagePlus(this.sImgName, buff); //TODO Hay que inicializarlo todo.
        FileSaver fs = new FileSaver(ip);
        sPath=sPath + "_Fact_cGy_" + JD2DCtes.dRound(dAux/255,0) + ".tiff";
        if (fs.saveAsTiff(sPath)==false) {
            iRes = -1;
            sResult.append("Error al guardar el archivo TIFF: " + sPath);
        }
        return iRes;
    }

    public int iOutputOPGFile(String sPath, StringBuffer sResult){
        return this.iOutputOPGFile(sPath, sResult, false, 0 , 0);
    }

    public int iOutputOPGFile(String sPath, StringBuffer sResult,
                                boolean isGoinToBeInterpolated, int iXNumPix, int iYNumPix){
    int iRes=0;
    String sLine="";
    int iXmaxAux=0;
    double dTime=0.0;
    double dAuxPixHeight=1.0;
    double dXCoorAux[]=new double[PLANE_MAX_SIZE];

    int iYmaxAux=0;
    double dAuxPixWidth=1.0;
    double dYCoorAux[]=new double[PLANE_MAX_SIZE];

    if (isGoinToBeInterpolated) {
        iXmaxAux=iXNumPix;
        double dd=0.0;
        dAuxPixHeight=(this.iXmax*this.dPixHeight)/iXNumPix;
        for (iX=iXNumPix-1;iX>=0;iX--) {
            dd=((double)iXNumPix)/2;
            dd=iX-(((double)iXNumPix)/2);
             dd=dAuxPixHeight*(iX-(((double)iXNumPix)/2));
            dXCoorAux[iX]=dAuxPixHeight*(iX-(((double)iXNumPix)/2)+0.5);
        }
        iYmaxAux=iYNumPix;
        dAuxPixWidth=(this.iYmax*this.dPixWidth)/iYNumPix;
        for (iY=0;iY<iYNumPix;iY++) {
            dd=((double)iYNumPix)/2;
            dd=iY-(((double)iYNumPix)/2);
            dd=dAuxPixWidth*(iY-(((double)iYNumPix)/2));
            dYCoorAux[iY]=dAuxPixWidth*(iY-(((double)iYNumPix)/2)+0.5);
        }
    }
    else {
        iXmaxAux=this.iXmax;
        dAuxPixHeight=this.dPixHeight;
        dXCoorAux=this.dXCoor;
        iYmaxAux=this.iYmax;
        dAuxPixWidth=this.dPixWidth;
        dYCoorAux=this.dYCoor;
    }

    try {
            BufferedWriter condemor = new BufferedWriter(new FileWriter(sPath));
            String sSep = JD2DCtes.SEPARATOR;
            //StringBuilder sw = new StringBuilder();
            sLine=sLine + "<opimrtascii>" + JD2DCtes.RET;
            sLine=sLine + "<asciiheader>" + JD2DCtes.RET;
            sLine=sLine + "File Version: \t 3" + JD2DCtes.RET;
            sLine=sLine + "Separator: \t \""+sSep+"\"" + JD2DCtes.RET;
            sLine=sLine + "File Name: \t "+this.sDcmName + JD2DCtes.RET;
            sLine=sLine + "Image Name: \t "+this.sImgName + JD2DCtes.RET;
            sLine=sLine + "Radiation Type: \t X RAY" + JD2DCtes.RET;
            sLine=sLine + "SSD: \t " + JD2DCtes.sRoundAndFormat(this.dSSD,1) + " mm" + JD2DCtes.RET;
            sLine=sLine + "SID: \t " + JD2DCtes.sRoundAndFormat(this.dSID,1) + " mm" + JD2DCtes.RET;
            sLine=sLine + "Field Size Cr: \t "+JD2DCtes.sRoundAndFormat(iXmaxAux*dAuxPixHeight,2)+" mm" + JD2DCtes.RET;
            sLine=sLine + "Field Size In: \t "+JD2DCtes.sRoundAndFormat(iYmaxAux*dAuxPixWidth,2)+" mm" + JD2DCtes.RET;
            if (this.isRel){
                 sLine=sLine + "Data Type: \t Relative" + JD2DCtes.RET;
            }
            else{
                 sLine=sLine + "Data Type: \t Absolute" + JD2DCtes.RET;
            }
            sLine=sLine + "Data Factor: \t "+this.dDoseFactor + JD2DCtes.RET;
            sLine=sLine + "Data Unit: \t "+this.sDoseUnits + JD2DCtes.RET;
            sLine=sLine + "Length Unit: \t mm" + JD2DCtes.RET;
            sLine=sLine + "Plane: \t XY" + JD2DCtes.RET;
            sLine=sLine + "No. of Columns: \t "+iXmaxAux + JD2DCtes.RET;
            sLine=sLine + "No. of Rows: \t "+iYmaxAux + JD2DCtes.RET;
            sLine=sLine + "Number of Bodies: \t 1" + JD2DCtes.RET;
            if (this.iDataSource==JD2DCtes.DATA_PCRT) {
                sLine=sLine + "Operators Note: \t PLANO DE DOSIS DE PCRT" + JD2DCtes.RET;
            }
            else if (this.iDataSource==JD2DCtes.DATA_OPG) {
                sLine=sLine + "Operators Note: \t " + this.sObsLine + JD2DCtes.RET;
            }
            else if (this.iDataSource==JD2DCtes.DATA_DOSE) {
                sLine=sLine + "Operators Note: \t " + this.sObsLine + JD2DCtes.RET;
            }
            else if (this.iDataSource==JD2DCtes.DATA_FFF) {
                sLine=sLine + "Operators Note: \t PLANO DE DOSIS DE FFF ENSAMBLADO CON ORIGEN MatriXX" + JD2DCtes.RET;
            }
            else {
                sLine=sLine + "Operators Note: \t Origen desconocido." + JD2DCtes.RET;
            }
            sLine=sLine + "</asciiheader>" + JD2DCtes.RET;

            if (this.iDataSource==JD2DCtes.DATA_DOSE) {
                sLine=sLine + JD2DCtes.ET_DM_INI + JD2DCtes.RET;
                dTime=this.iNSubframes/JD2DCtes.SUBFRAMES_SEGUNDO;
                sLine=sLine + "AqTime: \t" + JD2DCtes.sRoundAndFormat(dTime,3) + JD2DCtes.RET;
                sLine=sLine + "UM: \t" + JD2DCtes.sRoundAndFormat(this.dMU,2) + JD2DCtes.RET;
                sLine=sLine + "Subframes: \t" + this.iNSubframes + JD2DCtes.RET;
                sLine=sLine + JD2DCtes.ET_DM_END + JD2DCtes.RET;
            }

            sLine=sLine + "<asciibody>" + JD2DCtes.RET;
            if (this.iDataSource==JD2DCtes.DATA_OPG) {
                sLine=sLine + this.sPlanePosLine + JD2DCtes.RET;
            }
            else {
                sLine=sLine + "Plane Position: \t 0.0 mm" + JD2DCtes.RET + JD2DCtes.RET;
            }
            sLine=sLine + "X[mm]\t";
            double dAux=0.0;
            for (iY=0;iY<iYmaxAux;iY++) {
               //dAux=this.dPixWidth*(iY-(((double)iYmax)/2));
               //sLine=sLine+JD2DCtes.sRoundAndFormat(this.dPixWidth*(iY-(((double)iYmax)/2)),2);
               sLine=sLine+sSep+JD2DCtes.sRoundAndFormat(dYCoorAux[iY],2);
            }
            sLine = sLine + JD2DCtes.RET;
            condemor.write(sLine);
            sLine="";
            sLine=sLine + "Y[mm]" + JD2DCtes.RET;
            for (iX=0;iX<iXmaxAux;iX++) {
                //sLine=sLine+JD2DCtes.sRoundAndFormat(this.dPixHeight*((((double)iXmax)/2)-iX),2);
                sLine=sLine+JD2DCtes.sRoundAndFormat(dXCoorAux[iX],2);
                for (iY=0;iY<iYmaxAux;iY++) {
////TODO                    if ((iY==250) && (iX==250)) {
////                        dAux=this.dPlane[iX][iY];
////                    }
                    if (isGoinToBeInterpolated) {
                        //TODO
                        dAux=JD2DCtes.dInterpData(iXmax, iYmax,dXCoorAux[iX], dYCoorAux[iY], dXCoor, dYCoor, dPlane);
                        sLine=sLine+sSep+JD2DCtes.sRoundAndFormat(dAux,3);
                    }
                    else {
                        sLine=sLine+sSep+JD2DCtes.sRoundAndFormat(this.dPlane[iX][iY],3);
                    }
                }
                sLine=sLine + JD2DCtes.RET;
                if (sLine.length()>30000) {
                    condemor.write(sLine);
                    sLine="";
                }
            }
            sLine=sLine + "</asciibody>" + JD2DCtes.RET;
            sLine=sLine + "</opimrtascii>" + JD2DCtes.RET;
            condemor.write(sLine);
            sLine="";
            condemor.close();
            sLine="Tratado el fichero: '" +this.sDcmName + "'";
            sResult.append(sLine);
            }
        catch (IOException e)
        {
            sLine="Ocurrio una excepción al procesar el fichero: '" + this.sDcmName + "'";
            sResult.append(sLine);
            sLine="Detalles: '" + e.toString() + "'";
            sResult.append(sLine);
        }

    return iRes;
    }

    /*
     * Devuelve el path absoluto del fichero original.
     */
    public String getFilePath(){
        return this.sDcmPath;
    }

    /*
     * Devuelve el path absoluto del fichero original.
     */
    public String getParentPath(){
        return this.sDcmParentPath;
    }

    /*
     * Devuelve el nombre de la imagen.
     */
    public String getImageName(){
        return this.sImgName;
    }

    public void setUM(double dNum){
        this.dMU=dNum;
    }

    public double getUM(){
        return this.dMU;
    }
    
    public void setNumSubframes(int iNum){
        this.iNSubframes=iNum;
    }

    public int getNumSubframes(){
        return this.iNSubframes;
    }

    public void setNumSlices(int iNum){
        this.iSlices=iNum;
    }

    public int getNumSlices(){
        return this.iSlices;
    }

    public void setImageName(String sName){
        this.sImgName=sName;
    }

    public void setDcmName(String sName){
        this.sDcmName=sName;
    }

    public int getDataSource(){
        return this.iDataSource;
    }

    public void setDataSource(int iSD){
        this.iDataSource=iSD;
    }

    public void clearObject(){
        iDataSource = JD2DCtes.DATA_NOTHING;
        iXmax=0;
        iYmax=0;
        iChanels=0;
        iFrames=0;
        iSlices =0;
        dTime = 0.0;
        sDoseUnits = "";
        dDoseFactor = 0.0;
        sDcmPath = "";
        sDcmName = "";
        sDcmParentPath = "";
        sImgName = "";
        dMU = 0.0;
        dSID = 0.0;
        dSSD = 0.0;
        dOriginalSSD = 0.0;
        dPixWidth = 0.0;
        dPixHeight = 0.0;
        iNSubframes = 0;
        this.clearPlane();
        this.clearCoords();
    }

    private void clearPlane(){
        for (iX=0;iX<PLANE_MAX_SIZE;iX++){
            for (iY=0;iY<PLANE_MAX_SIZE;iY++){
                dPlane[iX][iY]=0.0;
            }
        }
    }

    private void clearCoords(){
        for (iX=0;iX<PLANE_MAX_SIZE;iX++){
            dXCoor[iX]=0.0;
        }
        for (iY=0;iY<PLANE_MAX_SIZE;iY++){
            dYCoor[iY]=0.0;
        }
    }

}
