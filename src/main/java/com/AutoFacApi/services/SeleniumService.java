package com.AutoFacApi.services;

import com.AutoFacApi.entity.Data;
import com.AutoFacApi.repository.iDataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SeleniumService {

    private static final String URL_VENTAS = "https://siigonube.siigo.com/#/sales-management/2044";

    @Value("${siigo.email}")
    private String siigoEmail;

    @Value("${siigo.password}")
    private String siigoPassword;

    @Value("${siigo.chrome.profile-dir}")
    private String chromeProfileDir;

    private final iDataRepository dataRepository;

    public SeleniumService(iDataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    // ===========================================================
    // Perfil de Chrome compartido y persistente entre corridas.
    // Necesario para que Siigo "recuerde" el dispositivo tras la
    // primera verificación por SMS y no la vuelva a pedir cada vez.
    // ===========================================================
    private ChromeOptions crearOpciones(boolean headless) {
        limpiarCandadosDePerfil();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--user-data-dir=" + Paths.get(chromeProfileDir).toAbsolutePath());
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
        }

        String chromeBin = System.getenv("CHROME_BIN");
        if (chromeBin != null && !chromeBin.isBlank()) {
            options.setBinary(chromeBin);
        }

        String chromeDriverBin = System.getenv("CHROMEDRIVER_BIN");
        if (chromeDriverBin != null && !chromeDriverBin.isBlank()) {
            System.setProperty("webdriver.chrome.driver", chromeDriverBin);
        }

        return options;
    }

    // Si un corte abrupto (reinicio del contenedor, restart de devtools, etc.) dejo un
    // candado del perfil sin liberar, la siguiente corrida se quedaria bloqueada para
    // siempre creyendo que otro Chrome sigue abierto. Como aqui solo corre un Chrome a
    // la vez, es seguro limpiar estos archivos antes de cada nueva sesion.
    private void limpiarCandadosDePerfil() {
        Path carpeta = Paths.get(chromeProfileDir).toAbsolutePath();
        for (String nombre : new String[]{"SingletonLock", "SingletonCookie", "SingletonSocket"}) {
            try {
                Files.deleteIfExists(carpeta.resolve(nombre));
            } catch (Exception e) {
                System.out.println("No se pudo limpiar " + nombre + ": " + e.getMessage());
            }
        }
    }

    // ===========================================================
    // FLUJO MANUAL (local, con ventana visible) — se mantiene igual
    // ===========================================================
    public Data capturarFactura() {

        WebDriver driver = new ChromeDriver(crearOpciones(false));
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMinutes(15));

        try {
            login(driver, wait);
            wait.until(ExpectedConditions.urlContains("/invoice/"));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(text(),'No. FE')]")));

            return extraerFactura(driver, wait);

        } finally {
            driver.quit();
        }
    }

    // ===========================================================
    // FLUJO AUTOMÁTICO (headless, para correr en el servidor)
    // ===========================================================
    public List<Data> sincronizarFacturas() {

        List<Data> capturadas = new ArrayList<>();

        WebDriver driver = new ChromeDriver(crearOpciones(true));
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMinutes(2));

        try {
            try {
                login(driver, wait);
                irAListaDeVentas(driver, wait);
            } catch (Exception e) {
                guardarDiagnostico(driver);
                throw e;
            }

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

    private void guardarDiagnostico(WebDriver driver) {
        try {
            System.out.println("DIAGNOSTICO - URL actual: " + driver.getCurrentUrl());

            File captura = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path destino = Paths.get(System.getProperty("java.io.tmpdir"), "siigo-sync-error.png");
            Files.copy(captura.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("DIAGNOSTICO - Screenshot guardado en: " + destino);

            Object testids = ((JavascriptExecutor) driver).executeScript(
                    "return Array.from(document.querySelectorAll('[data-testid]'))"
                            + ".map(el => el.getAttribute('data-testid'));");
            System.out.println("DIAGNOSTICO - data-testid presentes en la pagina: " + testids);
        } catch (Exception e) {
            System.out.println("DIAGNOSTICO - No se pudo capturar el diagnostico: " + e.getMessage());
        }
    }

    private void irAListaDeVentas(WebDriver driver, WebDriverWait wait) {
        driver.get(URL_VENTAS);
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[data-testid='sales-manager-action-documentview']")));
    }

    private List<String> obtenerNumerosNuevos(WebDriver driver) {

        List<String> nuevos = new ArrayList<>();
        List<WebElement> filas = driver.findElements(By.cssSelector("div.ag-row[role='row']"));

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

        List<WebElement> filas = driver.findElements(By.cssSelector("div.ag-row[role='row']"));

        for (WebElement fila : filas) {
            if (numero.equals(extraerNumeroDeFila(fila))) {
                // La columna de "Acciones" es una columna fija de AG Grid que se renderiza
                // en un contenedor aparte, alineada solo por row-id — no es descendiente
                // del mismo div de la fila, así que hay que buscarla en toda la página.
                String rowId = fila.getAttribute("row-id");
                WebElement boton = driver.findElement(By.cssSelector(
                        "div.ag-row[role='row'][row-id='" + rowId + "'] "
                                + "[data-testid='sales-manager-action-documentview']"));

                // "Ver documento" abre la factura en una pestaña nueva (igual que el link
                // del numero de factura, target="_blank") — hay que saltar a esa pestaña.
                String ventanaLista = driver.getWindowHandle();
                Set<String> ventanasAntes = driver.getWindowHandles();

                // Filas cerca del final de la ventana visible quedan tapadas por la barra
                // de paginacion fija — se hace scroll y clic por JS para evitar el error
                // "element click intercepted" del clic nativo.
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block: 'center'}); arguments[0].click();", boton);

                wait.until(d -> d.getWindowHandles().size() > ventanasAntes.size());
                Set<String> ventanasNuevas = new HashSet<>(driver.getWindowHandles());
                ventanasNuevas.removeAll(ventanasAntes);
                driver.switchTo().window(ventanasNuevas.iterator().next());

                wait.until(ExpectedConditions.urlContains("/invoice/"));
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//*[contains(text(),'No. FE')]")));

                Data data = extraerFactura(driver, wait);

                driver.close();
                driver.switchTo().window(ventanaLista);

                return data;
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
    // LOGIN — con el perfil persistente, Siigo puede reconocer el
    // dispositivo y saltarse directo al dashboard sin mostrar el
    // formulario de usuario/clave. Si eso pasa, no hay nada que llenar.
    // ===========================================================
    private void login(WebDriver driver, WebDriverWait wait) {

        driver.get("https://siigonube.siigo.com");

        boolean formularioVisible;
        try {
            WebDriverWait waitCorto = new WebDriverWait(driver, Duration.ofSeconds(15));
            waitCorto.until(ExpectedConditions.visibilityOfElementLocated(By.id("siigoSignInName")));
            formularioVisible = true;
        } catch (org.openqa.selenium.TimeoutException e) {
            formularioVisible = false;
        }

        if (formularioVisible) {
            WebElement email = driver.findElement(By.id("siigoSignInName"));
            email.sendKeys(siigoEmail);

            WebElement pass = driver.findElement(By.id("siigoPassword"));
            pass.sendKeys(siigoPassword);

            driver.findElement(By.id("siigoNext")).click();
        }

        wait.until(ExpectedConditions.urlContains("dashboard"));
    }
}
