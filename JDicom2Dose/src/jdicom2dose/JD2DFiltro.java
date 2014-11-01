/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jdicom2dose;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * Clase genérica que permite añadir filtros al navegador para localizar los ficheros a procesar.
 * Un filtro por cada tipo de fichero. Siempre muestra los directorios.
 * @author Oscar Ripol
 */
public class JD2DFiltro extends FileFilter {
        private String sExt;
        private String sDesc;

        /**
         * Constructor del filtro, requiere la mácara y la descripción de la misma.
         * @param sMask = Máscara de búsqueda. No poner el asterisco. Ej '.txt'. 
         * @param sDesMask = Descripción de los ficheros que de van a filtrar. Ej 'Ficheros de texto (*.txt)'
         */
        JD2DFiltro(String sMask, String sDesMask) {
                this.sExt = sMask;
                this.sDesc = sDesMask;
        }

        /**
         * Implementa el método accept de la interfaz para decidir cuales ficheros mostrar y cuales no.
         * @param file - Fichero a mostrar
         * @return Verdadero o falso - Se muestra, no se muestra.
         */
        public boolean accept(File file) {
                return file.isDirectory() || file.getName().toLowerCase().endsWith(sExt);
        }

        /**
         * Implementa el método getDescription de la interfaz para mostrar la descripción del fistro.
         * @return - String con la descripción.
         */
        public String getDescription() {
            return "(*" + sExt + ") - " + sDesc;
        }
}