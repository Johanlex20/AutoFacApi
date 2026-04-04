package com.AutoFacApi.services;
import com.AutoFacApi.entity.Data;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

@Service
public class ExcelService {

    private final String RUTA = "C:/selenium/cuentas_prueba.xlsx";

    public void guardar(Data data) throws Exception {

        File file = new File(RUTA);

        Workbook workbook;
        Sheet sheet;

        // 1. Crear o abrir Excel
        if (!file.exists() || file.length() == 0) {

            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Facturas");

            // HEADER
            Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("Numero Factura");
            header.createCell(1).setCellValue("Cliente");
            header.createCell(2).setCellValue("Nit");
            header.createCell(3).setCellValue("Fecha");
            header.createCell(4).setCellValue("Vence");
            header.createCell(5).setCellValue("Total");
            header.createCell(6).setCellValue("Forma Pago");
            header.createCell(7).setCellValue("Medio Pago");



        } else {

            try (FileInputStream fis = new FileInputStream(file)) {
                workbook = WorkbookFactory.create(fis);
                sheet = workbook.getSheetAt(0);
            }
        }

        /// NUEVA FILA
        int lastRow = sheet.getLastRowNum() + 1;

        Row row = sheet.createRow(lastRow);

        // DATOS
        row.createCell(0).setCellValue(data.getNumeroFactura());
        row.createCell(1).setCellValue(data.getCliente());
        row.createCell(2).setCellValue(data.getNit());
        row.createCell(3).setCellValue(data.getFechaGeneracion());
        row.createCell(4).setCellValue(data.getFechaVencimiento());
        row.createCell(5).setCellValue(data.getValorTotal());
        row.createCell(6).setCellValue(data.getFormaPago());
        row.createCell(7).setCellValue(data.getMedioPago());


        // 4. Guardar archivo correctamente
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }

        workbook.close();
    }
}

