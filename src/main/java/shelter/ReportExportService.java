package shelter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ReportExportService {

    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public Path export(Path outputDir, ShelterReportData report) throws IOException {
        Files.createDirectories(outputDir);
        String fileName = "upload-report-" + LocalDateTime.now().format(FILE_TIMESTAMP) + ".txt";
        Path outputPath = outputDir.resolve(fileName);

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write("Shelter Upload Report");
            writer.newLine();
            writer.newLine();

            writer.write("Total imported: " + report.totalImported());
            writer.newLine();
            writer.write("Total skipped: " + report.totalSkipped());
            writer.newLine();
            writer.newLine();

            writer.write("Unique species:");
            writer.newLine();
            for (String species : report.uniqueSpecies()) {
                writer.write("- " + species);
                writer.newLine();
            }
            writer.newLine();

            writer.write("Per species report:");
            writer.newLine();
            for (String species : report.uniqueSpecies()) {
                writer.write(species + ":");
                writer.newLine();
                writer.write("  vaccinated: " + report.vaccinatedCounts().getOrDefault(species, 0L));
                writer.newLine();
                writer.write("  unvaccinated: " + report.unvaccinatedCounts().getOrDefault(species, 0L));
                writer.newLine();
                Optional<Animal> oldest = report.oldestPerSpecies().get(species);
                if (oldest != null && oldest.isPresent()) {
                    writer.write("  oldest: " + oldest.get().getName() + " (" + oldest.get().getAge().get() + ")");
                } else {
                    writer.write("  oldest: unknown");
                }
                writer.newLine();
            }
            writer.newLine();

            writer.write("Needs vet input: " + String.join(", ", report.animalsNeedingVetInput()));
            writer.newLine();
            writer.newLine();

            writer.write("Invalid rows: " + toCsv(report.invalidRowNumbers()));
            writer.newLine();
        }

        return outputPath;
    }

    private String toCsv(List<Integer> numbers) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numbers.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(numbers.get(i));
        }
        return sb.toString();
    }
}
