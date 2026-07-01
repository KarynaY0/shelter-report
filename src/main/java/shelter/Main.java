package shelter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws IOException {
        Path dataDir = Path.of("src/main/resources/data");
        Path outputDir = Path.of("output");
        Path registryPath = outputDir.resolve("processed-files.txt");

        ProcessedFilesRegistry registry = new ProcessedFilesRegistry(registryPath);
        CsvImportService importService = new CsvImportService();

        List<Path> csvFiles;
        try (Stream<Path> walk = Files.walk(dataDir)) {
            csvFiles = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".csv"))
                    .collect(Collectors.toList());
        }

        for (Path file : csvFiles) {
            String fileName = file.getFileName().toString();
            if (registry.isProcessed(fileName)) {
                continue;
            }
            importService.importFile(file);
            registry.markProcessed(fileName);
        }

        ShelterAnalyticsService analytics = new ShelterAnalyticsService();
        ShelterReportData report = analytics.buildReport(importService);

        ReportExportService reportExportService = new ReportExportService();
        reportExportService.export(outputDir, report);

        JsonReportExportService jsonReportExportService = new JsonReportExportService();
        jsonReportExportService.export(outputDir, report);
    }
}
