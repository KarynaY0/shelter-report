package shelter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class CsvImportService {

    private static final Logger log = LoggerFactory.getLogger(CsvImportService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final List<Animal> allAnimals = new ArrayList<>();
    private final Set<String> uniqueSpecies = new TreeSet<>();
    private final Map<String, List<Animal>> animalsBySpecies = new TreeMap<>();
    private final List<String> animalsNeedingVetInput = new ArrayList<>();
    private final List<Integer> invalidRowNumbers = new ArrayList<>();
    private int totalImported = 0;
    private int totalSkipped = 0;

    public void importFile(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            int rowNumber = i + 1;
            Animal animal = parseRow(line, rowNumber);
            if (animal == null) {
                totalSkipped++;
                invalidRowNumbers.add(rowNumber);
                continue;
            }
            totalImported++;
            allAnimals.add(animal);
            uniqueSpecies.add(animal.getSpecies());
            animalsBySpecies.computeIfAbsent(animal.getSpecies(), k -> new ArrayList<>()).add(animal);
            if (animal.getAge().isEmpty()) {
                animalsNeedingVetInput.add(animal.getName() + "(" + animal.getSpecies() + ")");
            }
        }
    }

    private Animal parseRow(String line, int rowNumber) {
        String[] parts = line.split(",", -1);
        if (parts.length != 5) {
            log.warn("Row {} skipped: expected 5 fields, found {}", rowNumber, parts.length);
            return null;
        }

        String name = parts[0].trim();
        String species = parts[1].trim();
        String ageRaw = parts[2].trim();
        String vaccinatedRaw = parts[3].trim();
        String dateRaw = parts[4].trim();

        if (name.isEmpty()) {
            log.warn("Row {} skipped: name missing", rowNumber);
            return null;
        }
        if (species.isEmpty()) {
            log.warn("Row {} skipped: species missing", rowNumber);
            return null;
        }
        if (!vaccinatedRaw.equalsIgnoreCase("true") && !vaccinatedRaw.equalsIgnoreCase("false")) {
            log.warn("Row {} skipped: invalid or missing vaccination status '{}'", rowNumber, vaccinatedRaw);
            return null;
        }

        Optional<Integer> age = Optional.empty();
        if (!ageRaw.isEmpty()) {
            try {
                int parsedAge = Integer.parseInt(ageRaw);
                if (parsedAge <= 0) {
                    log.warn("Row {} skipped: age must be positive, found '{}'", rowNumber, ageRaw);
                    return null;
                }
                age = Optional.of(parsedAge);
            } catch (NumberFormatException e) {
                log.warn("Row {} skipped: invalid age '{}'", rowNumber, ageRaw);
                return null;
            }
        }

        LocalDate intakeDate;
        try {
            intakeDate = LocalDate.parse(dateRaw, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            log.warn("Row {} skipped: invalid date '{}'", rowNumber, dateRaw);
            return null;
        }

        boolean vaccinated = Boolean.parseBoolean(vaccinatedRaw);

        return new Animal(name, species, age, vaccinated, intakeDate);
    }

    public List<Animal> getAllAnimals() {
        return allAnimals;
    }

    public Set<String> getUniqueSpecies() {
        return uniqueSpecies;
    }

    public Map<String, List<Animal>> getAnimalsBySpecies() {
        return animalsBySpecies;
    }

    public List<String> getAnimalsNeedingVetInput() {
        return animalsNeedingVetInput;
    }

    public List<Integer> getInvalidRowNumbers() {
        return invalidRowNumbers;
    }

    public int getTotalImported() {
        return totalImported;
    }

    public int getTotalSkipped() {
        return totalSkipped;
    }
}
