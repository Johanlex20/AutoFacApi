package com.AutoFacApi.services;

import com.AutoFacApi.entity.Data;
import org.springframework.beans.factory.annotation.Value;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class SeleniumService {

    @Value("${siigo.email}")
    private String siigoEmail;

    @Value("${siigo.password}")
    private String siigoPassword;

    public Data capturarFactura() {

        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMinutes(15));

        try {

            login(driver, wait);
            System.out.println("paso el login");

            // 2. Esperar que llegue a factura
            wait.until(ExpectedConditions.urlContains("/invoice/"));

            System.out.println("URL factura detectada");

            // 3. Esperar que cargue contenido
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(text(),'No. FE')]")));

            System.out.println("Extrayendo Datos Factura...");

            System.out.println("URL actual: " + driver.getCurrentUrl());

            // 3. EXTRAER DATOS
            Data data = extraerFactura(driver, wait);

            System.out.println("Factura capturada: " + data.getNumeroFactura());

            return data;

        } finally {
            // driver.quit(); // Comenta mientras pruebas
        }

    }

    private Data extraerFactura(WebDriver driver, WebDriverWait wait) {

        Data data = new Data();

        // =====================
        // NUMERO FACTURA
        // =====================
        String numero = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[contains(text(),'No. FE')]")))
                .getText();

        data.setNumeroFactura(numero.replace("No.", "").trim());

        System.out.println("No" + numero);

        // =====================
        // BLOQUE CLIENTE
        // =====================
        WebElement clienteTable = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//table[contains(.,'Señores')]")));

        String tablaTexto = clienteTable.getText();
        System.out.println("TABLA CLIENTE:");
        System.out.println(tablaTexto);

        // =====================
        // PARSEO MANUAL
        // =====================

        // =====================
        // CLIENTE
        // =====================
        String cliente = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//td[contains(text(),'Señores')]/following-sibling::td[1]")))
                .getText();

        data.setCliente(cliente);
        System.out.println("cliente" + cliente);

        // =====================
        // NIT
        // =====================
        String nit = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//td[contains(text(),'NIT')]/following-sibling::td[1]")))
                .getText();

        if (nit.contains("Teléfono")) {
            nit = nit.split("Teléfono")[0].trim();
        }

        data.setNit(nit);
        System.out.println("nit = " + nit);

        // =====================
        // FECHA GENERACIÓN
        // =====================
        String fecha = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//td[contains(text(),'Generación')]/following-sibling::td[1]")))
                .getText();

        if (fecha.contains(",")) {
            fecha = fecha.split(",")[0].trim();
        }

        data.setFechaGeneracion(fecha);
        System.out.println("fecha = " + fecha);

        // =====================
        // FECHA VENCIMIENTO
        // =====================
        String vencimiento = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//td[contains(text(),'Vencimiento')]/following-sibling::td[1]")))
                .getText();

        if (vencimiento.contains(",")) {
            vencimiento = vencimiento.split(",")[0].trim();
        }

        data.setFechaVencimiento(vencimiento);
        System.out.println("fecha Vencimiento = " + vencimiento);

        // =====================
        // TOTAL A PAGAR
        // =====================
        String total = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//td[contains(text(),'Total a Pagar')]/following-sibling::td[1]")))
                .getText();

        data.setValorTotal(total);
        System.out.println("total = " + total);

        // =====================
        // METODO PAGO
        // =====================
        String medioPago = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath(
                                "//strong[contains(text(),'Medio de pago')]/ancestor::tr/following-sibling::tr[1]/td")))
                .getText().trim();

        data.setMedioPago(medioPago);

        System.out.println("Medio de pago = " + medioPago);

        // =====================
        // FORMA DE PAGO
        // =====================
        String formaPago = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath(
                                "//strong[contains(text(),'Forma de pago')]/ancestor::tr/following-sibling::tr[1]/td")))
                .getText().trim();

        data.setFormaPago(formaPago);

        System.out.println("Forma de pago = " + formaPago);

        return data;
    }

    private String extraerLinea(String texto, String clave) {

        for (String linea : texto.split("\n")) {

            if (linea.contains(clave)) {

                return linea.replace(clave, "")
                        .replace(":", "")
                        .trim();
            }
        }

        return "";
    }

    private void login(WebDriver driver, WebDriverWait wait) {

        driver.get("https://siigonube.siigo.com");

        WebElement email = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("siigoSignInName")));

        email.sendKeys(siigoEmail);

        WebElement pass = driver.findElement(By.id("siigoPassword"));
        pass.sendKeys(siigoPassword);

        driver.findElement(By.id("siigoNext")).click();

        wait.until(ExpectedConditions.urlContains("dashboard"));

        System.out.println("Login OK");
    }

}