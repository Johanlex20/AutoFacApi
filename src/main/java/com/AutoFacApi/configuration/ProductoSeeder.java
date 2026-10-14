package com.AutoFacApi.configuration;

import com.AutoFacApi.entity.Producto;
import com.AutoFacApi.repository.iProductoRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Siembra el catalogo inicial de productos con los datos reales de
 * "LISTA DE PRECIOS.docx" (BlueChemical). Solo corre si la tabla esta
 * vacia, para no pisar precios que se hayan actualizado despues.
 *
 * Cada presentacion es un producto propio (segun su etiqueta): garrafa de 20 L
 * y galon de 4 L. Los precios de galon no estan en la lista, quedan en 0 y la
 * landing muestra "Consultar precio" hasta que se editen desde el dashboard.
 */
@Component
public class ProductoSeeder implements CommandLineRunner {

    private static final String GARRAFA = "Garrafa 20 L";
    private static final String GALON = "Galón 4 L";
    private static final long SIN_PRECIO = 0L;

    private static final String IMG_DIR = "assets/img/productos/";

    // Sin foto propia todavia (CLEAN BLUE, DESBLUE ORG, SILICON BLUE)
    private static final String ENVASE_20L = "assets/img/envase-20l.jpg";
    private static final String ENVASE_4L = "assets/img/envase-4l.webp";

    private final iProductoRepository productoRepository;

    public ProductoSeeder(iProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) {
        if (productoRepository.count() > 0) {
            return;
        }

        String d20 = "Removedor - Desincrustante alcalino, elimina grasa quemada, sellador, sarro y hongo.";
        guardar("D-20", "Desincrustantes", d20, GARRAFA, 150000L, IMG_DIR + "d-20.jpg");
        guardar("D-20", "Desincrustantes", d20, GALON, SIN_PRECIO, IMG_DIR + "d-20-4l.jpg");

        String deterblue = "Detergente neutro industrial multiusos para limpieza general, superficies y cristalería.";
        guardar("DETERBLUE", "Detergentes", deterblue, GARRAFA, 130000L, IMG_DIR + "deterblue.jpg");
        guardar("DETERBLUE", "Detergentes", deterblue, GALON, SIN_PRECIO, IMG_DIR + "deterblue-4l.jpg");

        // POWER BLUE: sin foto de galon por ahora
        guardar("POWER BLUE", "Desengrasantes",
                "Desengrasante industrial alcalino para superficies altas en grasa.",
                GARRAFA, 155000L, IMG_DIR + "power-blue.jpg");

        String dermi = "Jabón de manos antibacterial a base de amonios cuaternarios.";
        guardar("DERMI BLUE", "Higiene de manos", dermi, GARRAFA, 130000L, IMG_DIR + "dermi-blue.jpg");
        guardar("DERMI BLUE", "Higiene de manos", dermi, GALON, SIN_PRECIO, IMG_DIR + "dermi-blue-4l.jpg");

        String gel = "Gel antibacterial para manos.";
        guardar("BLUEGEL", "Higiene de manos", gel, GARRAFA, 275000L, IMG_DIR + "bluegel.jpg");
        guardar("BLUEGEL", "Higiene de manos", gel, GALON, SIN_PRECIO, IMG_DIR + "bluegel-4l.jpg");

        guardar("CLEAN BLUE", "Aseo general",
                "Limpiador desinfectante para pisos, aroma cítrico, floral, lavanda, pino o talco.",
                GARRAFA, 120000L, ENVASE_20L);

        String bl50 = "Limpiavidrios.";
        guardar("BL-50", "Aseo general", bl50, GARRAFA, 110000L, IMG_DIR + "bl-50.jpg");
        guardar("BL-50", "Aseo general", bl50, GALON, SIN_PRECIO, IMG_DIR + "bl-50-4l.jpg");

        guardar("SILICON BLUE", "Aseo general",
                "Silicona polimérica autobrillante.",
                "Galón 3.75 L", 75000L, ENVASE_4L);

        String bl22 = "Ambientador aromatizante.";
        guardar("BL-22", "Aseo general", bl22, GARRAFA, 110000L, IMG_DIR + "bl-22.jpg");
        guardar("BL-22", "Aseo general", bl22, GALON, SIN_PRECIO, IMG_DIR + "bl-22-4l.jpg");

        guardar("DESBLUE ORG", "Desinfectantes",
                "Desinfectante para alimentos, superficies y ambientes a base de ácidos orgánicos.",
                GARRAFA, 250000L, ENVASE_20L);

        String cuat = "Desinfectante para superficies y ambientes a base de amonios cuaternarios.";
        guardar("DESBLUECUAT", "Desinfectantes", cuat, GARRAFA, 250000L, IMG_DIR + "desbluecuat.jpg");
        guardar("DESBLUECUAT", "Desinfectantes", cuat, GALON, SIN_PRECIO, IMG_DIR + "desbluecuat-4l.jpg");

        // ALCOHOL 70%: sin foto de galon por ahora
        guardar("ALCOHOL 70%", "Desinfectantes",
                "Desinfectante glicerinado.",
                GARRAFA, 130000L, IMG_DIR + "alcohol-70.jpg");

        String bl40 = "Hipoclorito de sodio 5.2% — blanqueador desinfectante.";
        guardar("BL-40", "Desinfectantes", bl40, GARRAFA, 55000L, IMG_DIR + "bl-40.jpg");
        guardar("BL-40", "Desinfectantes", bl40, GALON, SIN_PRECIO, IMG_DIR + "bl-40-4l.jpg");
    }

    private void guardar(String nombre, String categoria, String descripcion,
                         String presentacion, Long precio, String imagenUrl) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setCategoria(categoria);
        p.setDescripcion(descripcion);
        p.setPresentacion(presentacion);
        p.setPrecio(precio);
        p.setImagenUrl(imagenUrl);
        productoRepository.save(p);
    }
}
