package com.group.hackathon_G5Dev.domain.service;

import com.group.hackathon_G5Dev.domain.model.CalculCarbone;
import com.group.hackathon_G5Dev.domain.model.ClasseCarbone;
import com.group.hackathon_G5Dev.domain.model.Site;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Service de génération du rapport PDF d'empreinte carbone.
 * Utilise la charte graphique EcoTrack v1.0 (palette Vert Forêt / Noir Forêt)
 * et inclut les KPIs, la répartition par poste et le benchmark sectoriel.
 */
@Service
@RequiredArgsConstructor
public class RapportPdfService {

    // ══════════════════════════════════════════════════════════════
    // Palette EcoTrack — Charte Graphique v1.0
    // ══════════════════════════════════════════════════════════════

    private static final Color VERT_FORET = new Color(45, 122, 79);
    private static final Color VERT_NUIT = new Color(26, 77, 49);
    private static final Color VERT_ECLAT = new Color(76, 175, 120);
    private static final Color BLEU_CAPGEMINI = new Color(0, 112, 173);
    private static final Color VERT_MENTHE = new Color(168, 213, 181);
    private static final Color VERT_PALE = new Color(232, 245, 227);
    private static final Color NOIR_FORET = new Color(13, 26, 18);
    private static final Color SUCCES = new Color(76, 175, 120);
    private static final Color ATTENTION = new Color(230, 168, 23);
    private static final Color ERREUR = new Color(192, 57, 43);

    private static final Color FOND_HEADER = NOIR_FORET;
    private static final Color FOND_KPI = new Color(20, 38, 25);
    private static final Color BORDURE_KPI = new Color(160, 130, 60);
    private static final Color TEXTE_CLAIR = new Color(220, 235, 225);
    private static final Color TEXTE_CAPTION = new Color(140, 165, 148);

    // ══════════════════════════════════════════════════════════════
    // Fonts
    // ══════════════════════════════════════════════════════════════

    private static final Font DISPLAY_FONT = new Font(Font.HELVETICA, 26, Font.BOLD, Color.WHITE);
    private static final Font H2_FONT = new Font(Font.HELVETICA, 14, Font.BOLD, VERT_FORET);
    private static final Font BRAND_FONT = new Font(Font.HELVETICA, 9, Font.BOLD, VERT_ECLAT);
    private static final Font HEADER_LABEL_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL, TEXTE_CAPTION);
    private static final Font BODY_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL, NOIR_FORET);
    private static final Font BODY_BOLD_FONT = new Font(Font.HELVETICA, 9, Font.BOLD, NOIR_FORET);
    private static final Font CAPTION_FONT = new Font(Font.HELVETICA, 7, Font.ITALIC, new Color(100, 120, 108));
    private static final Font TABLE_HEADER_FONT = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);
    private static final Font KPI_VALUE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD, VERT_ECLAT);
    private static final Font KPI_LABEL_FONT = new Font(Font.HELVETICA, 7, Font.BOLD, TEXTE_CAPTION);
    private static final Font KPI_UNIT_FONT = new Font(Font.HELVETICA, 7, Font.NORMAL, TEXTE_CAPTION);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Chemins des assets
    private static final String ASSET_FOND = "assets/fondcarrévert.png";
    private static final String ASSET_LOGO = "assets/logoecotrack-removebg-preview.png";
    private static final String ASSET_LOGO_FOND = "assets/logoecotrack.png";

    public byte[] genererRapport(CalculCarbone calcul) {
        Site site = calcul.getSite();
        Map<String, Double> detail = calcul.getDetailParCategorie();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            doc.open();

            // Charger les images
            Image fondMosaique = loadImage(ASSET_FOND);
            Image logoTransparent = loadImage(ASSET_LOGO);
            Image logoAvecFond = loadImage(ASSET_LOGO_FOND);

            // ══════════════════════════════════════════════════════════════
            // FOND MOSAIQUE sur toute la page (derrière le contenu)
            // ══════════════════════════════════════════════════════════════
            if (fondMosaique != null) {
                drawMosaicBackground(writer, fondMosaique);
            }

            // ══════════════════════════════════════════════════════════════
            // BANNER HEADER — 2 colonnes : texte + logo
            // ══════════════════════════════════════════════════════════════
            PdfPTable banner = new PdfPTable(2);
            banner.setWidthPercentage(110);
            banner.setSpacingAfter(20);
            banner.setWidths(new float[]{3, 1});

            // Colonne gauche — texte
            PdfPCell bannerTextCell = new PdfPCell();
            bannerTextCell.setBackgroundColor(FOND_HEADER);
            bannerTextCell.setPadding(28);
            bannerTextCell.setPaddingBottom(22);
            bannerTextCell.setBorderWidth(0);

            Paragraph brandLine = new Paragraph();
            brandLine.add(new Chunk("EcoTrack", BRAND_FONT));
            brandLine.add(new Chunk("  —  ", new Font(Font.HELVETICA, 9, Font.NORMAL, TEXTE_CAPTION)));
            brandLine.add(new Chunk("Carbon Intelligence Platform", new Font(Font.HELVETICA, 8, Font.NORMAL, TEXTE_CAPTION)));
            brandLine.setSpacingAfter(10);
            bannerTextCell.addElement(brandLine);

            Paragraph titlePara = new Paragraph("Rapport Empreinte Carbone", DISPLAY_FONT);
            bannerTextCell.addElement(titlePara);

            Paragraph siteNamePara = new Paragraph(site.getNom(),
                    new Font(Font.HELVETICA, 13, Font.NORMAL, TEXTE_CLAIR));
            siteNamePara.setSpacingBefore(6);
            bannerTextCell.addElement(siteNamePara);

            String dateStr = calcul.getDateCalcul() != null ? calcul.getDateCalcul().format(DATE_FMT) : "N/A";
            Paragraph datePara = new Paragraph("Calculé le " + dateStr, HEADER_LABEL_FONT);
            datePara.setSpacingBefore(8);
            bannerTextCell.addElement(datePara);

            LineSeparator headerSep = new LineSeparator(2f, 40, VERT_FORET, Element.ALIGN_LEFT, 0);
            Paragraph sepPara = new Paragraph();
            sepPara.add(headerSep);
            sepPara.setSpacingBefore(10);
            bannerTextCell.addElement(sepPara);

            Paragraph cobrand = new Paragraph();
            cobrand.add(new Chunk("by ", new Font(Font.HELVETICA, 7, Font.ITALIC, TEXTE_CAPTION)));
            cobrand.add(new Chunk("capgemini", new Font(Font.HELVETICA, 8, Font.BOLD, BLEU_CAPGEMINI)));
            cobrand.setSpacingBefore(6);
            bannerTextCell.addElement(cobrand);

            banner.addCell(bannerTextCell);

            // Colonne droite — logo
            PdfPCell bannerLogoCell = new PdfPCell();
            bannerLogoCell.setBackgroundColor(FOND_HEADER);
            bannerLogoCell.setBorderWidth(0);
            bannerLogoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            bannerLogoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            bannerLogoCell.setPadding(15);

            if (logoTransparent != null) {
                logoTransparent.scaleToFit(90, 90);
                Paragraph logoPara = new Paragraph();
                logoPara.setAlignment(Element.ALIGN_CENTER);
                logoPara.add(new Chunk(logoTransparent, 0, 0, true));
                bannerLogoCell.addElement(logoPara);
            }

            banner.addCell(bannerLogoCell);
            doc.add(banner);

            // ══════════════════════════════════════════════════════════════
            // INFORMATIONS DU SITE
            // ══════════════════════════════════════════════════════════════
            PdfPTable infoTable = new PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(18);
            infoTable.setWidths(new float[]{1.2f, 2f, 1.2f, 2f});

            addInfoPair(infoTable, "Adresse", site.getAdresse() != null ? site.getAdresse() : "Non renseignée");
            addInfoPair(infoTable, "Ville", site.getVille() != null ? site.getVille() : "Non renseignée");
            addInfoPair(infoTable, "Type bâtiment", site.getTypeBatiment() != null ? site.getTypeBatiment().name() : "BUREAU");
            addInfoPair(infoTable, "Superficie", String.format("%.0f m²", site.getSurfaceTotale()));
            addInfoPair(infoTable, "Employés", String.valueOf(site.getNombreEmployes()));
            addInfoPair(infoTable, "Places parking", String.valueOf(site.getTotalPlaces()));
            addInfoPair(infoTable, "Durée de vie", (site.getDureeVie() != null ? site.getDureeVie() : 50) + " ans");
            addInfoPair(infoTable, "Année réf.", String.valueOf(calcul.getAnneeReference()));
            addSection(doc, "Informations du site", infoTable);

            // ══════════════════════════════════════════════════════════════
            // KPIs
            // ══════════════════════════════════════════════════════════════
            PdfPTable kpiTable = new PdfPTable(4);
            kpiTable.setWidthPercentage(100);
            kpiTable.setSpacingAfter(12);

            addKpiCard(kpiTable, "CO2 TOTAL", formatKg(calcul.getCo2Total()), "kgCO2e / an");
            addKpiCard(kpiTable, "CO2 / M2", formatKg(calcul.getCo2ParM2()), "kgCO2e / m²");
            addKpiCard(kpiTable, "CO2 / ETP", formatKg(calcul.getCo2ParEmploye()), "kgCO2e / employé");
            addKpiCard(kpiTable, "SURFACE", String.format("%.0f", site.getSurfaceTotale()), "m²");
            addSection(doc, "Indicateurs clés", kpiTable);

            // ══════════════════════════════════════════════════════════════
            // CLASSE CARBONE
            // ══════════════════════════════════════════════════════════════
            double ecParM2 = calcul.getCo2ParM2() != null ? calcul.getCo2ParM2() : 0;
            ClasseCarbone classe = ClasseCarbone.fromCo2ParM2(ecParM2);

            PdfPTable classeTable = new PdfPTable(2);
            classeTable.setWidthPercentage(55);
            classeTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            classeTable.setSpacingAfter(20);
            classeTable.setWidths(new float[]{1, 3});
            classeTable.setKeepTogether(true);

            PdfPCell badgeCell = new PdfPCell(new Phrase(classe.name(),
                    new Font(Font.HELVETICA, 32, Font.BOLD, Color.WHITE)));
            badgeCell.setBackgroundColor(getClasseColor(classe));
            badgeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            badgeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            badgeCell.setPadding(14);
            badgeCell.setBorderWidth(0);
            classeTable.addCell(badgeCell);

            PdfPCell classeLabelCell = new PdfPCell();
            classeLabelCell.setBorderWidth(0);
            classeLabelCell.setPadding(10);
            classeLabelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            classeLabelCell.setBackgroundColor(VERT_PALE);

            classeLabelCell.addElement(new Paragraph("Classe carbone",
                    new Font(Font.HELVETICA, 8, Font.BOLD, VERT_NUIT)));
            Paragraph classeDesc = new Paragraph(classe.getLabel(),
                    new Font(Font.HELVETICA, 13, Font.BOLD, VERT_FORET));
            classeDesc.setSpacingBefore(2);
            classeLabelCell.addElement(classeDesc);
            classeLabelCell.addElement(new Paragraph(
                    String.format("Seuil : %.0f - %.0f kgCO2e/m²/an",
                            classe.getSeuilMin(),
                            classe.getSeuilMax() == Double.MAX_VALUE ? 999 : classe.getSeuilMax()),
                    CAPTION_FONT));
            classeTable.addCell(classeLabelCell);

            doc.add(classeTable);

            // ══════════════════════════════════════════════════════════════
            // RÉPARTITION DES ÉMISSIONS
            // ══════════════════════════════════════════════════════════════
            PdfPTable repartTable = new PdfPTable(3);
            repartTable.setWidthPercentage(100);
            repartTable.setSpacingAfter(8);
            repartTable.setWidths(new float[]{3, 2, 1});
            repartTable.setHeaderRows(1);

            addTableHeader(repartTable, "Poste d'émission");
            addTableHeader(repartTable, "Émissions (kgCO2e/an)", Element.ALIGN_RIGHT);
            addTableHeader(repartTable, "Part", Element.ALIGN_RIGHT);

            double total = calcul.getCo2Total() != null ? calcul.getCo2Total() : 1;
            double ecConstruction = calcul.getCo2Construction() != null ? calcul.getCo2Construction() : 0;
            int row = 0;

            addRepartRow(repartTable, "Construction (matériaux)", ecConstruction, total, false, row++);
            addRepartRow(repartTable, "     Béton", detail.getOrDefault("EC_beton", 0.0), total, true, row++);
            addRepartRow(repartTable, "     Acier", detail.getOrDefault("EC_acier", 0.0), total, true, row++);
            addRepartRow(repartTable, "     Verre", detail.getOrDefault("EC_verre", 0.0), total, true, row++);
            addRepartRow(repartTable, "     Bois", detail.getOrDefault("EC_bois", 0.0), total, true, row++);

            double ecAutres = detail.getOrDefault("EC_autres", 0.0);
            if (ecAutres != 0) {
                addRepartRow(repartTable, "     Autres", ecAutres, total, true, row++);
            }

            addRepartRow(repartTable, "Énergie", detail.getOrDefault("EC_nrj", 0.0), total, false, row++);
            addRepartRow(repartTable, "Mobilité employés", detail.getOrDefault("EC_mob", 0.0), total, false, row++);
            addRepartRow(repartTable, "Parking", detail.getOrDefault("EC_park", 0.0), total, false, row++);
            addRepartRow(repartTable, "Déchets", detail.getOrDefault("EC_dech", 0.0), total, false, row++);

            addTotalRow(repartTable, "TOTAL", calcul.getCo2Total());
            addSection(doc, "Répartition des émissions", repartTable);

            // Construction vs Exploitation
            double exploitation = calcul.getCo2Exploitation() != null ? calcul.getCo2Exploitation() : 0;

            PdfPTable splitTable = new PdfPTable(2);
            splitTable.setWidthPercentage(100);
            splitTable.setSpacingAfter(18);
            splitTable.setKeepTogether(true);

            addSplitCell(splitTable, "Construction", ecConstruction, total, VERT_NUIT);
            addSplitCell(splitTable, "Exploitation", exploitation, total, VERT_FORET);
            doc.add(splitTable);

            // ══════════════════════════════════════════════════════════════
            // BENCHMARK SECTORIEL
            // ══════════════════════════════════════════════════════════════
            PdfPTable benchTable = new PdfPTable(4);
            benchTable.setWidthPercentage(100);
            benchTable.setSpacingAfter(15);
            benchTable.setWidths(new float[]{2.5f, 2, 2, 1.5f});
            benchTable.setHeaderRows(1);

            addTableHeader(benchTable, "Indicateur");
            addTableHeader(benchTable, "Votre site", Element.ALIGN_RIGHT);
            addTableHeader(benchTable, "Moyenne secteur", Element.ALIGN_RIGHT);
            addTableHeader(benchTable, "Écart", Element.ALIGN_RIGHT);

            double superficie = site.getSurfaceTotale();
            double constructionParM2 = superficie > 0 ? ecConstruction / superficie : 0;
            double exploitationParM2 = superficie > 0 ? exploitation / superficie : 0;

            addBenchRow(benchTable, "Construction (kgCO2e/m²/an)", constructionParM2,
                    detail.getOrDefault("moy_construction", 0.0),
                    detail.getOrDefault("ecart_construction_pct", 0.0), 0);
            addBenchRow(benchTable, "Exploitation (kgCO2e/m²/an)", exploitationParM2,
                    detail.getOrDefault("moy_exploitation", 0.0),
                    detail.getOrDefault("ecart_exploitation_pct", 0.0), 1);
            addBenchRow(benchTable, "Total (kgCO2e/m²/an)", ecParM2,
                    detail.getOrDefault("moy_total", 0.0),
                    detail.getOrDefault("ecart_total_pct", 0.0), 2);

            addSection(doc, "Benchmark sectoriel", benchTable);

            // ══════════════════════════════════════════════════════════════
            // FOOTER — avec logo EcoTrack
            // ══════════════════════════════════════════════════════════════
            doc.add(new Paragraph("\n"));

            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(110);
            footerTable.setKeepTogether(true);
            footerTable.setWidths(new float[]{1, 4});

            // Logo footer
            PdfPCell footerLogoCell = new PdfPCell();
            footerLogoCell.setBackgroundColor(NOIR_FORET);
            footerLogoCell.setBorderWidth(0);
            footerLogoCell.setPadding(8);
            footerLogoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            footerLogoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            if (logoAvecFond != null) {
                logoAvecFond.scaleToFit(40, 40);
                Paragraph logoFooterPara = new Paragraph();
                logoFooterPara.setAlignment(Element.ALIGN_CENTER);
                logoFooterPara.add(new Chunk(logoAvecFond, 0, 0, true));
                footerLogoCell.addElement(logoFooterPara);
            }
            footerTable.addCell(footerLogoCell);

            // Texte footer
            PdfPCell footerTextCell = new PdfPCell();
            footerTextCell.setBackgroundColor(NOIR_FORET);
            footerTextCell.setPadding(12);
            footerTextCell.setBorderWidth(0);
            footerTextCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            Paragraph footerLine1 = new Paragraph(
                    "EcoTrack — Carbon Intelligence Platform",
                    new Font(Font.HELVETICA, 8, Font.BOLD, VERT_ECLAT));
            footerTextCell.addElement(footerLine1);

            Paragraph footerLine2 = new Paragraph(
                    "Référentiel GHG Protocol (Scope 1, 2, 3) — Base Empreinte ADEME",
                    new Font(Font.HELVETICA, 7, Font.NORMAL, TEXTE_CAPTION));
            footerLine2.setSpacingBefore(3);
            footerTextCell.addElement(footerLine2);

            Paragraph footerLine3 = new Paragraph(
                    "SUP² Vinci x Capgemini  |  Hackathon #26  |  16-17 Mars 2026",
                    new Font(Font.HELVETICA, 7, Font.NORMAL, TEXTE_CAPTION));
            footerLine3.setSpacingBefore(2);
            footerTextCell.addElement(footerLine3);

            footerTable.addCell(footerTextCell);
            doc.add(footerTable);

            doc.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la génération du rapport PDF", e);
        }

        return out.toByteArray();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Images
    // ══════════════════════════════════════════════════════════════════════

    private Image loadImage(String classpath) {
        try {
            byte[] bytes = new ClassPathResource(classpath).getInputStream().readAllBytes();
            return Image.getInstance(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private void drawMosaicBackground(PdfWriter writer, Image tile) throws DocumentException {
        PdfContentByte canvas = writer.getDirectContentUnder();
        float pageW = PageSize.A4.getWidth();
        float pageH = PageSize.A4.getHeight();

        // Taille de chaque tuile du motif
        float tileSize = 80;
        tile.scaleAbsolute(tileSize, tileSize);

        // Réduire l'opacité en utilisant un état graphique
        com.lowagie.text.pdf.PdfGState gs = new com.lowagie.text.pdf.PdfGState();
        gs.setFillOpacity(0.06f);
        gs.setStrokeOpacity(0.06f);
        canvas.setGState(gs);

        for (float x = 0; x < pageW; x += tileSize) {
            for (float y = 0; y < pageH; y += tileSize) {
                tile.setAbsolutePosition(x, y);
                canvas.addImage(tile);
            }
        }

        // Reset opacity
        com.lowagie.text.pdf.PdfGState gsReset = new com.lowagie.text.pdf.PdfGState();
        gsReset.setFillOpacity(1f);
        gsReset.setStrokeOpacity(1f);
        canvas.setGState(gsReset);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════════

    private Paragraph sectionTitle(String text) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(text, H2_FONT));
        p.setSpacingBefore(6);
        p.setSpacingAfter(2);

        Paragraph container = new Paragraph();
        container.add(p);

        LineSeparator sep = new LineSeparator(1f, 100, VERT_MENTHE, Element.ALIGN_LEFT, -4);
        container.add(sep);
        container.setSpacingAfter(24);
        return container;
    }

    private void addSection(Document doc, String title, PdfPTable content) throws DocumentException {
        PdfPTable wrapper = new PdfPTable(1);
        wrapper.setWidthPercentage(100);
        wrapper.setKeepTogether(true);
        wrapper.setSpacingAfter(8);

        PdfPCell cell = new PdfPCell();
        cell.setBorderWidth(0);
        cell.setPadding(0);
        cell.addElement(sectionTitle(title));
        cell.addElement(content);
        wrapper.addCell(cell);

        doc.add(wrapper);
    }

    private void addInfoPair(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label,
                new Font(Font.HELVETICA, 7, Font.BOLD, VERT_FORET)));
        labelCell.setBorderWidth(0);
        labelCell.setPaddingTop(3);
        labelCell.setPaddingBottom(3);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, BODY_FONT));
        valueCell.setBorderWidth(0);
        valueCell.setPaddingTop(3);
        valueCell.setPaddingBottom(3);
        table.addCell(valueCell);
    }

    private void addKpiCard(PdfPTable table, String label, String value, String unit) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(FOND_KPI);
        cell.setBorderWidth(1);
        cell.setBorderColor(BORDURE_KPI);
        cell.setPadding(10);
        cell.setNoWrap(true);

        Paragraph labelPara = new Paragraph(label, KPI_LABEL_FONT);
        cell.addElement(labelPara);

        Paragraph valuePara = new Paragraph(formatKpiValue(value), KPI_VALUE_FONT);
        valuePara.setSpacingBefore(4);
        cell.addElement(valuePara);

        Paragraph unitPara = new Paragraph(unit, KPI_UNIT_FONT);
        unitPara.setSpacingBefore(3);
        cell.addElement(unitPara);

        table.addCell(cell);
    }

    private void addTableHeader(PdfPTable table, String text) {
        addTableHeader(table, text, Element.ALIGN_LEFT);
    }

    private void addTableHeader(PdfPTable table, String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_HEADER_FONT));
        cell.setBackgroundColor(VERT_NUIT);
        cell.setPadding(7);
        cell.setBorderWidth(0);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }

    private void addRepartRow(PdfPTable table, String label, double value, double total, boolean isSubRow, int rowIndex) {
        Font labelFont = isSubRow ? CAPTION_FONT : BODY_FONT;
        Font valueFont = isSubRow ? CAPTION_FONT : BODY_FONT;
        Color bg = Color.WHITE;
        if (!isSubRow && rowIndex % 2 == 0) bg = VERT_PALE;

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorderWidth(0);
        labelCell.setPadding(5);
        labelCell.setBackgroundColor(bg);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(formatKg(value), valueFont));
        valueCell.setBorderWidth(0);
        valueCell.setPadding(5);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setBackgroundColor(bg);
        table.addCell(valueCell);

        double pct = total > 0 ? (value / total) * 100 : 0;
        PdfPCell pctCell = new PdfPCell(new Phrase(String.format("%.1f%%", pct), valueFont));
        pctCell.setBorderWidth(0);
        pctCell.setPadding(5);
        pctCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        pctCell.setBackgroundColor(bg);
        table.addCell(pctCell);
    }

    private void addTotalRow(PdfPTable table, String label, Double totalValue) {
        Font totalFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, totalFont));
        labelCell.setBackgroundColor(VERT_NUIT);
        labelCell.setPadding(7);
        labelCell.setBorderWidth(0);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(formatKg(totalValue), totalFont));
        valueCell.setBackgroundColor(VERT_NUIT);
        valueCell.setPadding(7);
        valueCell.setBorderWidth(0);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);

        PdfPCell pctCell = new PdfPCell(new Phrase("100%", totalFont));
        pctCell.setBackgroundColor(VERT_NUIT);
        pctCell.setPadding(7);
        pctCell.setBorderWidth(0);
        pctCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(pctCell);
    }

    private void addSplitCell(PdfPTable table, String label, double value, double total, Color accentColor) {
        double pct = total > 0 ? (value / total) * 100 : 0;

        PdfPCell cell = new PdfPCell();
        cell.setBorderWidth(0);
        cell.setBorderWidthLeft(4);
        cell.setBorderColorLeft(accentColor);
        cell.setPadding(10);
        cell.setBackgroundColor(VERT_PALE);

        cell.addElement(new Paragraph(label,
                new Font(Font.HELVETICA, 9, Font.BOLD, accentColor)));

        Paragraph valuePara = new Paragraph(
                formatKg(value) + " kgCO2e/an  (" + String.format("%.1f%%", pct) + ")",
                BODY_FONT);
        valuePara.setSpacingBefore(2);
        cell.addElement(valuePara);

        table.addCell(cell);
    }

    private void addBenchRow(PdfPTable table, String label, double siteValue, double benchValue, double ecartPct, int rowIndex) {
        Color bg = rowIndex % 2 == 0 ? VERT_PALE : Color.WHITE;

        PdfPCell labelCell = new PdfPCell(new Phrase(label, BODY_FONT));
        labelCell.setBorderWidth(0);
        labelCell.setPadding(6);
        labelCell.setBackgroundColor(bg);
        table.addCell(labelCell);

        PdfPCell siteCell = new PdfPCell(new Phrase(String.format("%.2f", siteValue), BODY_BOLD_FONT));
        siteCell.setBorderWidth(0);
        siteCell.setPadding(6);
        siteCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        siteCell.setBackgroundColor(bg);
        table.addCell(siteCell);

        PdfPCell benchCell = new PdfPCell(new Phrase(String.format("%.2f", benchValue), BODY_FONT));
        benchCell.setBorderWidth(0);
        benchCell.setPadding(6);
        benchCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        benchCell.setBackgroundColor(bg);
        table.addCell(benchCell);

        String ecartStr = String.format("%+.1f%%", ecartPct);
        Color ecartColor = ecartPct <= 0 ? SUCCES : ERREUR;
        PdfPCell ecartCell = new PdfPCell(new Phrase(ecartStr,
                new Font(Font.HELVETICA, 9, Font.BOLD, ecartColor)));
        ecartCell.setBorderWidth(0);
        ecartCell.setPadding(6);
        ecartCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        ecartCell.setBackgroundColor(bg);
        table.addCell(ecartCell);
    }

    private Color getClasseColor(ClasseCarbone classe) {
        return switch (classe) {
            case A -> VERT_FORET;
            case B -> VERT_ECLAT;
            case C -> new Color(120, 190, 50);
            case D -> ATTENTION;
            case E -> new Color(240, 130, 20);
            case F -> ERREUR;
            case G -> new Color(160, 20, 20);
        };
    }

    private String formatKg(Double value) {
        if (value == null) return "0";
        return String.format("%,.2f", value);
    }

    private String formatKpiValue(String formattedValue) {
        try {
            String cleaned = formattedValue.replace(",", "").replace(" ", "");
            double v = Double.parseDouble(cleaned);
            if (Math.abs(v) >= 1000) {
                return String.format("%,.0f", v);
            }
        } catch (NumberFormatException ignored) {
        }
        return formattedValue;
    }
}
