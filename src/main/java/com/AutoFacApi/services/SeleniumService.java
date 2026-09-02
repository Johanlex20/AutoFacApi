package com.AutoFacApi.services;

import com.AutoFacApi.entity.Data;
import com.AutoFacApi.repository.iDataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class SeleniumService {

    private static final String URL_VENTAS = "https://siigonube.siigo.com/#/sales-management/2044";

    @Value("${siigo.email}")
    private String siigoEmail;

    @Value("${siigo.password}")
    private String siigoPassword;

    private final iDataRepository dataRepository;

    public SeleniumService(iDataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    // ===========================================================
    // FLUJO MANUAL (local, con ventana visible) — se mantiene igual
    // ===========================================================
    public Data capturarFactura() {

        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMinutes(15));

        try {
            login(driver, wait);
            wait.until(ExpectedConditions.urlContains("/invoice/"));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(text(),'No. FE')]")));

            return extraerFactura(driver, wait);

        } finally {
            // driver.quit(); // Comenta mientras pruebas
        }
    }

    // ===========================================================
    // FLUJO AUTOMÁTICO (headless, para correr en el servidor)
    // ===========================================================
    public List<Data> sincronizarFacturas() {

        List<Data> capturadas = new ArrayList<>();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMinutes(2));

        try {
            login(driver, wait);
            irAListaDeVentas(driver, wait);

            List<String> numerosNuevos = obtenerNumerosNuevos(driver);
            System.out.println("Facturas nuevas detectadas: " + numerosNuevos.size());

            for (String numero : numerosNuevos) {
                try {
                    Data data = capturarFacturaPorNumero(driver, wait, numero);
                    if (data != null) {
                        dataRepository.save(data);
                        capturadas.add(data);
                        System.out.println("Capturada: " + numero);
                    }
                } catch (Exception e) {
                    System.out.println("Error capturando " + numero + ": " + e.getMessage());
                }

                irAListaDeVentas(driver, wait);
            }

        } finally {
            driver.quit();
        }

        return capturadas;
    }

    private void irAListaDeVentas(WebDriver driver, WebDriverWait wait) {
        driver.get(URL_VENTAS);
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[data-testid='sales-manager-action-button-documentview']")));
    }

    private List<String> obtenerNumerosNuevos(WebDriver driver) {

        List<String> nuevos = new ArrayList<>();
        List<WebElement> filas = driver.findElements(By.cssSelector("div[role='row']"));

        for (WebElement fila : filas) {
            String numero = extraerNumeroDeFila(fila);
            if (numero == null) {
                continue;
            }

            if (dataRepository.existsByNumeroFactura(numero)) {
                // La lista viene ordenada de más reciente a más antigua:
                // si esta ya la teníamos, las siguientes también deberían estarlo.
                break;
            }

            nuevos.add(numero);
        }

        return nuevos;
    }

    private String extraerNumeroDeFila(WebElement fila) {
        try {
            WebElement celda = fila.findElement(By.cssSelector("[col-id='voucher']"));
            String texto = celda.getText();
            return texto.split("\n")[0].trim();
        } catch (Exception e) {
            return null;
        }
    }

    private Data capturarFacturaPorNumero(WebDriver driver, WebDriverWait wait, String numero) {

        List<WebElement> filas = driver.findElements(By.cssSelector("div[role='row']"));

        for (WebElement fila : filas) {
            if (numero.equals(extraerNumeroDeFila(fila))) {
                WebElement boton = fila.findElement(
                        By.cssSelector("[data-testid='sales-manager-action-button-documentview']"));
                boton.click();

                wait.until(ExpectedConditions.urlContains("/invoice/"));
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//*[contains(text(),'No. FE')]")));

                return extraerFactura(driver, wait);
            }
        }

        return null;
    }

    // ===========================================================
    // EXTRACCIÓN (sin cambios — ya funcionaba)
    // ===========================================================
    private Data extraerFactura(WebDriver driver, WebDriverWait wait) {

        Data data = new Data();

        String numero = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[contains(text(),'No. FE')]")))
                .getText();
        data.setNumeroFactura(numero.replace("No.", "").trim());

        String cliente = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//td[contains(text(),'Señores')]/following-sibling::td[1]")))
                .getText();
        data.setCliente(cliente);

        String nit = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//td[contains(text(),'NIT')]/following-sibling::td[1]")))
                .getText();
        if (nit.contains("Teléfono")) {
            nit = nit.split("Teléfono")[0].trim();
        }
        data.setNit(nit);

        String fecha = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//td[contains(text(),'Generación')]/following-sibling::td[1]")))
                .getText();
        if (fecha.contains(",")) {
            fecha = fecha.split(",")[0].trim();
        }
        data.setFechaGeneracion(fecha);

        String vencimiento = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//td[contains(text(),'Vencimiento')]/following-sibling::td[1]")))
                .getText();
        if (vencimiento.contains(",")) {
            vencimiento = vencimiento.split(",")[0].trim();
        }
        data.setFechaVencimiento(vencimiento);

        String total = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//td[contains(text(),'Total a Pagar')]/following-sibling::td[1]")))
                .getText();
        data.setValorTotal(total);

        String medioPago = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//strong[contains(text(),'Medio de pago')]/ancestor::tr/following-sibling::tr[1]/td")))
                .getText().trim();
        data.setMedioPago(medioPago);

        String formaPago = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//strong[contains(text(),'Forma de pago')]/ancestor::tr/following-sibling::tr[1]/td")))
                .getText().trim();
        data.setFormaPago(formaPago);

        return data;
    }

    // ===========================================================
    // LOGIN (sin cambios)
    // ===========================================================
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
    }
}
