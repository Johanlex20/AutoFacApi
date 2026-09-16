package com.AutoFacApi.services;

import com.AutoFacApi.entity.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SiigoSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(SiigoSyncScheduler.class);

    private final SeleniumService seleniumService;

    public SiigoSyncScheduler(SeleniumService seleniumService) {
        this.seleniumService = seleniumService;
    }

    @Scheduled(cron = "${siigo.sync.cron}", zone = "${siigo.sync.zone}")
    public void sincronizar() {
        log.info("Iniciando sincronización automática de facturas Siigo...");
        try {
            List<Data> capturadas = seleniumService.sincronizarFacturas();
            log.info("Sincronización automática completada: {} factura(s) nueva(s) capturada(s).", capturadas.size());
        } catch (Exception e) {
            log.error("Error durante la sincronización automática de facturas Siigo", e);
        }
    }
}
