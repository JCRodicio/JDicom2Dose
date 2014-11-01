/*
 * Esta clase contiene las constantes y la funciones principales que tendremos
 * que emplear repetidamente en el desarrollo.
 * Es una clase con atributos y métodos estáticos, no es necesario instanciarla.
 */

package jdicom2dose;

import java.text.DecimalFormat;
import java.io.File;

/**
 * Clase con las constantes y funciones estandar (métodos estáticos).
 * @author Oscar Ripol
 */
public class JD2DCtes {

    /* Constantes para gestionar la opción de entrada de datos. */
    public static final int INPOP_DICOM=0;
    public static final int INPOP_OPG=1;
    public static final int INPOP_SEGMENTS=2;
    public static final int INPOP_DICOM_SIN_TRANSF=3;
    public static final int INPOP_ROI_SIN_TRANSF=4;
    public static final int INPOP_DICOMRTDOSE_PCRT=5;

    /* Constantes para la escritura y lectura de ficheros con formato OPG. */
    public static final String SEPARATOR = ";"; /* Separador de campos para ficheros ASCII. */
    //public static final String RET = "\n"; /* Separador de lineas para ficheros ASCII. */
    public static final String RET = System.getProperty("line.separator"); /* Separador de lineas para ficheros ASCII. */
    public static final String ET_FILE_INI= "<opimrtascii>";
    public static final String ET_FILE_END= "</opimrtascii>";
    public static final String ET_HEADER_INI= "<asciiheader>";
    public static final String ET_HEADER_END= "</asciiheader>";
    public static final String ET_DM_INI= "<dicomManager>";
    public static final String ET_DM_END= "</dicomManager>";
    public static final String ET_BODY_INI= "<asciibody>";
    public static final String ET_BODY_END= "</asciibody>";
    public static final String ET_IMG_NAME="Image Name:";
    public static final String ET_SEP="Separator:";
    public static final String ET_DOSEFACTOR="Data Factor:";
    public static final String ET_NROWS="No. of Rows:";
    public static final String ET_NCOLS="No. of Columns:";
    public static final String ET_SSD="SSD:";
    public static final String ET_SID="SID:";
    public static final String ET_SZ_CR="Field Size Cr:";
    public static final String ET_SZ_IN="Field Size In:";
    public static final String ET_DOSE_UDS="Data Unit:";
    public static final String ET_NBODIES="Number of Bodies:";
    public static final String ET_NOTE="Operators Note:";
    public static final String ET_AQTIME="AqTime:";
    public static final String ET_UM="UM:";
    public static final String ET_NSUBFRAMES="Subframes:";
    public static final String ET_FILES="Ficheros Sumados:";
    public static final String ET_PLANE_POS="Plane Position:";
    public static final String ET_X="X[";
    public static final String ET_Y="Y[";

    /**
     * Public sEtFileIni As String = "<opimrtascii>"
    Public sEtHeaderIni As String = "<asciiheader>"
    Public sEtVer As String = "File Version: 	 3"
    Public sEtSeparator As String = "Separator:"

    Public sEtFileName As String = "File Name:"
    Public sEtXRay As String = "Radiation Type: 	 X RAY"
    Public sEtSSD As String = "SSD:"
    Public sEtSID As String = "SID: 	 100.0 cm"
    Public sEtSzCr As String = "Field Size Cr:"
    Public sEtSzIn As String = "Field Size In:"
    Public sEtType As String = "Data Type: 	 Rel. Dose"
    Public sEtType2 As String = "Data Type:"
    Public sEtFactor As String = "Data Factor:"
    Public sEtUnit As String = "Data Unit:"
    Public sEtLUnit As String = "Length Unit:   cm"
    Public sEtPlane As String = "Plane: 	 XY"
    Public sEtNCols As String = "No. of Columns:"
    Public sEtNRows As String = "No. of Rows:"
    Public sEtNumOfBodies As String = "Number of Bodies:"
    Public sEtNote As String = "Operators Note:"
    Public sEtHeaderFin As String = "</asciiheader>"
    Public sEtDMIni As String = "<dicomManager>"
    Public sEtAqTime As String = "AqTime:"
    Public sEtUM As String = "UM:"
    Public sEtSubframes As String = "Subframes:"
    Public sEtFicheros As String = "Ficheros Sumados:"
    Public sEtDMFin As String = "</dicomManager>"
    Public sEtBodyIni As String = "<asciibody>"
    Public sEtPlanePos2 As String = "Plane Position:"
    Public sEtPlanePos As String = "Plane Position:     0.0 cm"
    Public sEtX As String = "X["
    Public sEtY As String = "Y["
    Public sEtBodyFin As String = "</asciibody>"
    Public sEtFileFin As String = "</opimrtascii>"

     */

    /* Constantes con las etiquetas de interés en los ficheros DICOM. */

    public static final int iMU_NUMBER = 0x30020032; /* Etiqueta en la que se guarda el número de UM. */
    public static final int iRT_IMAGE_SID = 0x30020026; /* Etiqueta en la que se guarda el SID. */
    public static final int iNUMBER_OF_SUBFRAMES = 0x00391577; /* Etiqueta en la que se guarda el Número de Subframes. */


    /* Constantes con para generar el fichero FFF. */
    public static final int iX1Y1 = 11;
    public static final int iX1Y2 = 12;
    public static final int iX2Y1 = 21;
    public static final int iX2Y2 = 22;

    /* Constantes con las el proceso sabrá que es lo que está cargando. */
    public static final int DATA_NOTHING = -1; /* Indica que no hay datos cargados. */
    public static final int DATA_PCRT = 23; /* Indica que se está procesando un fichero de PCRT. */
    public static final int DATA_OPG = 96; /* Indica que se está procesando un fichero ASCII_OPG. */
    public static final int DATA_FFF = 57; /* Indica que se está procesando un fichero ensamblado con FFF. */
    public static final int DATA_DOSE = 56; /* Indica que se está procesando un fichero con información de dosis. */

    public static final double PIXEL_RESOLUTION_EPID = 0.4; /* Resolución del Epid en milimetros */
    public static final double SUBFRAMES_SEGUNDO = 3.5;

    /* Constantes para gestionar la configuración de la aplicación. */
    public static final int NUMBER_OF_FIELDS = 9;
    public static final int DESC_FIELD = 0;
    public static final int COEF_FIELD = 1;
    public static final int ILOGA1_FIELD = 2;
    public static final int ILOGA2_FIELD = 3;
    public static final int ILOGA3_FIELD = 4;
    public static final int ILOGK1_FIELD = 5;
    public static final int ILOGK2_FIELD = 6;
    public static final int ILOGK3_FIELD = 7;
    public static final int FFFILE_FIELD = 8;
    public static final int XXXX_FIELD = -1;

    public static final int NUM_PIX_FFF = 101;
    public static final double STANDARD_SID = 1000.0; // En milimetros.

    public static final double NORM_X1 = -2; // En milimetros.
    public static final double NORM_X2 = 2;  // En milimetros.
    public static final double NORM_Y1 = -2; // En milimetros.
    public static final double NORM_Y2 = 2;  // En milimetros.

    /**
     * Redondea un double al número de decimales requerido. El separador decimal es el punto.
     * @param dD - Número a formatear.
     * @param iD - Número de decimales.
     * @return Double con el número redondeado.
     */
    public static double dRound(double dD, int iD) {
        double dMult = Math.pow(10, iD);
        return (Math.round(dMult*(dD))/dMult);
    }

    /**
     * Formatea un double al número de decimales requerido. El separador decimal es el punto.
     * @param dD - Número a formatear.
     * @param iD - Número de decimales.
     * @return String con el número formateado.
     */
    public static String sFormat(double dD, int iD) {
        String sMask = "";
        if (iD>0){
            for (int i=0; i<10; i++) {
                if (i==iD){
                    sMask="."+sMask;
                }
                else {
                    sMask="#"+sMask;
                }
            }
        }
        DecimalFormat format = new DecimalFormat(sMask);
        return format.format(dD);

    }

    /**
     * Redondea y formatea un double al número de decimales requerido. El separador decimal es el punto.
     * @param dD - Número a formatear.
     * @param iD - Número de decimales.
     * @return - String con el número formateado.
     */
    public static String sRoundAndFormat(double dD, int iD){
        return sFormat(dRound(dD,iD),iD).replace(",", ".");
    }

    public static String sSearchFileName(String sNameWithoutExt, String sExt) {
        String sNameFound=sNameWithoutExt+sExt;
        int i =0;
        File newFile= new File(sNameFound);
        while (newFile.exists()) {
            if (newFile.exists()) {
                i++;
                sNameFound=sNameWithoutExt+"_"+i+sExt;
                newFile= new File(sNameFound);
            }
            else {
                i=102;
            }
        }
        return sNameFound;
    }

    public static int iSearchInArray(double dTo, double dArray[], int iDim) {
        int iFin=iDim-1;
        int iIni=0;
        int iMid=0;
        if (dTo<dArray[iIni]) {
            iFin=0;
        }
        else if (dTo>dArray[iFin]){
            iFin=-1;
        }
        else {
            //iMid=(iDim)/2;
            while (iFin-iIni>1) {
                iMid=iIni+((iFin-iIni)/2);
                if (dTo>dArray[iMid]) {
                    iIni=iMid;
                }
                else {
                    iFin=iMid;
                }
            }
        }
        return iFin;
    }

    public static double dInterpBilineal(double dX1, double dX2,double dY1, double dY2,
                                         double df11, double df12,  double df21, double df22,
                                         double dX, double dY){
        double dResult=0.0;
        if (df11>0){
            dResult=0.0;
        }
        if ((dX2 == dX1) && (dY1 == dY2)){
            dResult = df11;
        }
        else if(dX2 == dX1) {
            dResult = ((df12 - df11) *  (dY - dY1)) / ((dY2 - dY1)) + df11;
        }
        else if(dY2 == dY1) {
            dResult = ((df21 - df11) *  (dX - dX1)) / ((dX2 - dX1)) + df11;
        }
        else {
            dResult += ((df11 * (dX2 - dX) * (dY2 - dY)) / ((dX2 - dX1) * (dY2 - dY1)));
            dResult += + ((df21 * (dX - dX1) * (dY2 - dY)) / ((dX2 - dX1) * (dY2 - dY1)));
            dResult += + ((df12 * (dX2 - dX) * (dY - dY1)) / ((dX2 - dX1) * (dY2 - dY1)));
            dResult += + ((df22 * (dX - dX1) * (dY - dY1)) / ((dX2 - dX1) * (dY2 - dY1)));
        }
        return dResult;
    }

    public static double dInterpData(int iDimX, int iDimY, double dTX, double dTY,
                                     double dX[], double dY[], double dData[][]) {
        double dResult=0.0;
        double dX1=0.0;
        double dX2=0.0;
        double dY1=0.0;
        double dY2=0.0;
        double df11=0.0;
        double df12=0.0;
        double df21=0.0;
        double df22=0.0;
        int iX1=0;
        int iX2=iSearchInArray(dTX,dX,iDimX);
        if (iX2==0) {
            iX1=0;
        }
        else if(iX2==-1) {
            iX2=iDimX-1;
            iX1=iX2;
        }
        else {
            iX1=iX2-1;
        }
        int iY1=0;
        int iY2=iSearchInArray(dTY,dY,iDimY);
        if (iY2==0) {
            iY1=0;
        }
        else if(iY2==-1) {
            iY2=iDimY-1;
            iY1=iY2;
        }
        else {
            iY1=iY2-1;
        }
        if (dTX<dX[iX1]) {
            dTX=dX[iX1];
        }
        if (dTY<dY[iY1]) {
            dTY=dY[iY1];
        }
        if (dTX>dX[iX2]) {
            dTX=dX[iX2];
        }
        if (dTY>dY[iY2]) {
            dTY=dY[iY2];
        }

        dResult=dInterpBilineal(dX[iX1],dX[iX2],dY[iY1],dY[iY2],
                                dData[iX1][iY1],dData[iX1][iY2],dData[iX2][iY1],dData[iX2][iY2],
                                dTX,dTY);
        return dResult;
    }
}
