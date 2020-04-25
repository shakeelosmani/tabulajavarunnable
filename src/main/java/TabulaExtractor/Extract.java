package TabulaExtractor;

import java.io.*;
import java.util.Base64;
import java.util.List;
import java.util.ArrayList;

import technology.tabula.writers.JSONWriter;
import org.apache.commons.cli.ParseException;
import technology.tabula.Page;
import technology.tabula.Rectangle;
import technology.tabula.PageIterator;
import technology.tabula.Table;
import technology.tabula.detectors.DetectionAlgorithm;
import technology.tabula.detectors.NurminenDetectionAlgorithm;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import technology.tabula.ObjectExtractor;

import org.apache.pdfbox.pdmodel.PDDocument;



public class Extract {

    private String password;
    private TableExtractor tableExtractor;
    private List<Integer> pages;

    public void extractTable(String base64Data) {
        byte[] bytes = Base64.getDecoder().decode(base64Data);
        try (DataOutputStream os = new DataOutputStream(new FileOutputStream("output.pdf"))) {

            os.write(bytes);
            os.close();

        }catch (IOException e) {
            e.printStackTrace();
        }

        File pdfFile = new File("output.pdf");

        if(!pdfFile.exists()) {
            try {
                throw new ParseException("File does not exist");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            try {
                extractFileTables(pdfFile);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }

    public void extractFileTables(File pdfFile) throws ParseException {

        extractFile(pdfFile);
        return;
    }

    private void extractFile(File pdfFile) throws ParseException {
        PDDocument pdfDocument = null;
        try {
            pdfDocument = this.password == null ? PDDocument.load(pdfFile) : PDDocument.load(pdfFile, this.password);
            PageIterator pageIterator = getPageIterator(pdfDocument);
            List<Table> tables = new ArrayList<>();

            this.tableExtractor = createExtractor();

            while (pageIterator.hasNext()) {
                Page page = pageIterator.next();
                tables.addAll(tableExtractor.extractTables(page));
            }
            File outputFile = new File("output.json");
            BufferedWriter bufferedWriter = null;
            try {
                FileWriter fileWriter = new FileWriter(outputFile.getAbsoluteFile());
                bufferedWriter = new BufferedWriter(fileWriter);

                outputFile.createNewFile();
                JSONWriter writer = new JSONWriter();
                writer.write(bufferedWriter, tables);
            } catch (IOException e) {
                throw new ParseException("Cannot create file " + outputFile);
            } finally {
                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.close();
                    } catch (IOException e) {
                        System.out.println("Error in closing the BufferedWriter" + e);
                    }
                }
            }
        } catch (IOException e) {
            throw new ParseException(e.getMessage());
        } finally {
            try {
                if (pdfDocument != null) {
                    pdfDocument.close();
                }
            } catch (IOException e) {
                System.out.println("Error in closing pdf document" + e);
            }
        }
    }

    private PageIterator getPageIterator(PDDocument pdfDocument) throws IOException {
        ObjectExtractor extractor = new ObjectExtractor(pdfDocument);
        return (pages == null) ?
                extractor.extract() :
                extractor.extract(pages);
    }

    private static TableExtractor createExtractor() throws ParseException {
        TableExtractor extractor = new TableExtractor();
        extractor.setGuess(true);
        extractor.setMethod(ExtractionMethod.DECIDE);
        extractor.setUseLineReturns(false);
        return extractor;
    }


    private static class TableExtractor {
        private boolean guess = false;
        private boolean useLineReturns = false;
        private BasicExtractionAlgorithm basicExtractor = new BasicExtractionAlgorithm();
        private SpreadsheetExtractionAlgorithm spreadsheetExtractor = new SpreadsheetExtractionAlgorithm();
        private List<Float> verticalRulingPositions = null;
        private ExtractionMethod method = ExtractionMethod.BASIC;

        public TableExtractor() {
        }

        public void setVerticalRulingPositions(List<Float> positions) {
            this.verticalRulingPositions = positions;
        }

        public void setGuess(boolean guess) {
            this.guess = guess;
        }

        public void setUseLineReturns(boolean useLineReturns) {
            this.useLineReturns = useLineReturns;
        }

        public void setMethod(ExtractionMethod method) {
            this.method = method;
        }

        public List<Table> extractTables(Page page) {
            ExtractionMethod effectiveMethod = this.method;
            if (effectiveMethod == ExtractionMethod.DECIDE) {
                effectiveMethod = spreadsheetExtractor.isTabular(page) ?
                        ExtractionMethod.SPREADSHEET :
                        ExtractionMethod.BASIC;
            }
            switch (effectiveMethod) {
                case BASIC:
                    return extractTablesBasic(page);
                case SPREADSHEET:
                    return extractTablesSpreadsheet(page);
                default:
                    return new ArrayList<>();
            }
        }

        public List<Table> extractTablesBasic(Page page) {
            if (guess) {
                // guess the page areas to extract using a detection algorithm
                // currently we only have a detector that uses spreadsheets to find table areas
                DetectionAlgorithm detector = new NurminenDetectionAlgorithm();
                List<Rectangle> guesses = detector.detect(page);
                List<Table> tables = new ArrayList<>();

                for (Rectangle guessRect : guesses) {
                    Page guess = page.getArea(guessRect);
                    tables.addAll(basicExtractor.extract(guess));
                }
                return tables;
            }

            if (verticalRulingPositions != null) {
                return basicExtractor.extract(page, verticalRulingPositions);
            }
            return basicExtractor.extract(page);
        }

        public List<Table> extractTablesSpreadsheet(Page page) {
            // TODO add useLineReturns
            return spreadsheetExtractor.extract(page);
        }
    }

    private enum ExtractionMethod {
        BASIC,
        SPREADSHEET,
        DECIDE
    }





}
