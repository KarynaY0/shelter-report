package shelter;

import java.util.*;
import java.util.stream.Collectors;

public class ShelterAnalyticsService {

    public ShelterReportData buildReport(CsvImportService importService) {
        Map<String, List<Animal>> bySpecies = importService.getAnimalsBySpecies();

        Map<String, Long> vaccinatedCounts = bySpecies.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().stream().filter(Animal::isVaccinated).count(),
                        (a, b) -> a, TreeMap::new));

        Map<String, Long> unvaccinatedCounts = bySpecies.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().stream().filter(a -> !a.isVaccinated()).count(),
                        (a, b) -> a, TreeMap::new));

        Map<String, Optional<Animal>> oldestPerSpecies = new TreeMap<>();
        for (Map.Entry<String, List<Animal>> entry : bySpecies.entrySet()) {
            Optional<Animal> oldest = entry.getValue().stream()
                    .filter(a -> a.getAge().isPresent())
                    .max(Comparator.comparingInt(a -> a.getAge().get()));
            oldestPerSpecies.put(entry.getKey(), oldest);
        }

        return new ShelterReportData(
                importService.getTotalImported(),
                importService.getTotalSkipped(),
                importService.getUniqueSpecies(),
                vaccinatedCounts,
                unvaccinatedCounts,
                oldestPerSpecies,
                importService.getAnimalsNeedingVetInput(),
                importService.getInvalidRowNumbers()
        );
    }
}
