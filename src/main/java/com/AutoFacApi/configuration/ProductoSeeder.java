package com.AutoFacApi.configuration;

import com.AutoFacApi.entity.Producto;
import com.AutoFacApi.repository.iProductoRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Siembra el catalogo inicial de productos con los datos reales de
 * "LISTA DE PRECIOS.docx" (BlueChemical). Solo corre si la tabla esta
 * vacia, para no pisar precios que se hayan actualizado despues.
 */
@Component
public class ProductoSeeder implements CommandLineRunner {

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

        productoRepository.save(producto("D-20", "Desincrustantes",
                "Removedor - Desincrustante alcalino, elimina grasa quemada, sellador, sarro y hongo.",
                "20 L", 150000L, ENVASE_20L));

        productoRepository.save(producto("DETERBLUE", "Detergentes",
                "Detergente neutro industrial multiusos para limpieza general, superficies y cristalería.",
                "20 L", 130000L, ENVASE_20L));

        productoRepository.save(producto("POWER BLUE", "Desengrasantes",
                "Desengrasante industrial alcalino para superficies altas en grasa.",
                "20 L", 155000L, ENVASE_20L));

        productoRepository.save(producto("DERMI BLUE", "Higiene de manos",
                "Jabón de manos antibacterial a base de amonios cuaternarios.",
                "20 L", 130000L, ENVASE_20L));

        productoRepository.save(producto("BLUE GEL", "Higiene de manos",
                "Gel antibacterial para manos.",
                "20 L", 275000L, ENVASE_20L));

        productoRepository.save(producto("CLEAN BLUE", "Aseo general",
                "Limpiador desinfectante para pisos, aroma cítrico, floral, lavanda, pino o talco.",
                "20 L", 120000L, ENVASE_20L));

        productoRepository.save(producto("BLUE-50", "Aseo general",
                "Limpiavidrios.",
                "20 L", 110000L, ENVASE_20L));

        productoRepository.save(producto("SILICON BLUE", "Aseo general",
                "Silicona polimérica autobrillante.",
                "3.75 L", 75000L, ENVASE_4L));

        productoRepository.save(producto("BLUE-22", "Aseo general",
                "Ambientador aromatizante.",
                "20 L", 110000L, ENVASE_20L));

        productoRepository.save(producto("DESBLUE ORG", "Desinfectantes",
                "Desinfectante para alimentos, superficies y ambientes a base de ácidos orgánicos.",
                "20 L", 250000L, ENVASE_20L));

        productoRepository.save(producto("DESBLUE CUAT", "Desinfectantes",
                "Desinfectante para superficies y ambientes a base de amonios cuaternarios.",
                "20 L", 250000L, ENVASE_20L));

        productoRepository.save(producto("ALCOHOL AL 70%", "Desinfectantes",
                "Desinfectante glicerinado.",
                "20 L", 130000L, ENVASE_20L));

        productoRepository.save(producto("BL-40", "Desinfectantes",
                "Hipoclorito de sodio 5.2% — blanqueador desinfectante.",
                "20 L", 55000L, ENVASE_20L));
    }

    private Producto producto(String nombre, String categoria, String descripcion,
                               String presentacion, Long precio, String imagenUrl) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setCategoria(categoria);
        p.setDescripcion(descripcion);
        p.setPresentacion(presentacion);
        p.setPrecio(precio);
        p.setImagenUrl(imagenUrl);
        return p;
    }
}
