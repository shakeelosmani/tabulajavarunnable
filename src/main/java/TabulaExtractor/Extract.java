package TabulaExtractor;

import java.io.*;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import technology.tabula.*;
import technology.tabula.json.RectangularTextContainerSerializer;
import technology.tabula.json.TableSerializer;
import technology.tabula.writers.JSONWriter;
import org.apache.commons.cli.ParseException;
import technology.tabula.detectors.DetectionAlgorithm;
import technology.tabula.detectors.NurminenDetectionAlgorithm;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import org.apache.pdfbox.pdmodel.PDDocument;


public class Extract {

    private String password;
    private TableExtractor tableExtractor;
    private List<Integer> pages;

    public String extractTable(String base64Data) {
        String result = "";
        byte[] bytes = Base64.getDecoder().decode(base64Data);
        try (DataOutputStream os = new DataOutputStream(new FileOutputStream("output.pdf"))) {

            os.write(bytes);
            os.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        File pdfFile = new File("output.pdf");

        if (!pdfFile.exists()) {
            try {
                throw new ParseException("File does not exist");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            try {
               result = extractFileTables(pdfFile);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public String extractFileTables(File pdfFile) throws ParseException {

        String result = extractFile(pdfFile);
        return result;
    }

    private String extractFile(File pdfFile) throws ParseException {
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

            Gson gson = gson();
            JsonArray array = new JsonArray();
            Iterator var5 = tables.iterator();

            while (var5.hasNext()) {
                Table table = (Table) var5.next();
                array.add(gson.toJsonTree(table, Table.class));
            }

            return gson.toJson(array);


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

    private static Gson gson() {
        return (new GsonBuilder()).registerTypeAdapter(Table.class, TableSerializer.INSTANCE).registerTypeAdapter(RectangularTextContainer.class, RectangularTextContainerSerializer.INSTANCE).registerTypeAdapter(Cell.class, RectangularTextContainerSerializer.INSTANCE).registerTypeAdapter(TextChunk.class, RectangularTextContainerSerializer.INSTANCE).create();
    }

    private enum ExtractionMethod {
        BASIC,
        SPREADSHEET,
        DECIDE
    }


}
