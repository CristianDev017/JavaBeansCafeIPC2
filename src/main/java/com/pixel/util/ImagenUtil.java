package com.pixel.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class ImagenUtil {

    private static final String CARPETA_IMAGENES = "img";

    // Copia la imagen seleccionada a la carpeta img/ con un nombre único,
    // y devuelve el nombre de archivo generado (esto es lo que se guarda en la BD).
    public static String guardarImagen(File origen) throws IOException {
        Path carpetaDestino = Paths.get(CARPETA_IMAGENES);
        if (!Files.exists(carpetaDestino)) {
            Files.createDirectories(carpetaDestino);
        }

        String extension = origen.getName().substring(origen.getName().lastIndexOf('.'));
        String nombreNuevo = UUID.randomUUID() + extension;

        Path destino = carpetaDestino.resolve(nombreNuevo);
        Files.copy(origen.toPath(), destino);

        return nombreNuevo;
    }

    public static String rutaCompleta(String nombreArchivo) {
        return CARPETA_IMAGENES + File.separator + nombreArchivo;
    }
}