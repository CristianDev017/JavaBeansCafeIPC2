package com.pixel.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class HtmlExportUtil {

    // Guarda el HTML directamente en la carpeta Descargas del usuario, sin preguntar nada
    // y sin abrir el navegador automáticamente.
    public static boolean exportarDirecto(String htmlContenido, String nombreArchivo) {
        String carpetaDescargas = System.getProperty("user.home") + File.separator + "Downloads";
        File carpeta = new File(carpetaDescargas);

        if (!carpeta.exists()) {
            carpetaDescargas = System.getProperty("user.home") + File.separator + "Desktop";
            carpeta = new File(carpetaDescargas);
        }
        if (!carpeta.exists()) {
            carpetaDescargas = System.getProperty("user.home");
        }

        File archivo = new File(carpetaDescargas, nombreArchivo);

        try (FileWriter writer = new FileWriter(archivo)) {
            writer.write(htmlContenido);
            return true;
        } catch (IOException e) {
            System.out.println("Error al guardar el HTML: " + e.getMessage());
            return false;
        }
    }
}