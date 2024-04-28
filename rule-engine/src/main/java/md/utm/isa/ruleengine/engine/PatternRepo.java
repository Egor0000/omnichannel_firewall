package md.utm.isa.ruleengine.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.ruleengine.config.RuleEngineProperties;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PatternRepo {
    private final Map<String, Double> patternRepo = new HashMap<>();
    private final ScheduledExecutorService patterFileRunner = Executors.newSingleThreadScheduledExecutor();
    private final RuleEngineProperties ruleEngineProperties;
    private final ObjectMapper objectMapper;

    private Trie trie = Trie.builder().ignoreCase().ignoreOverlaps().build();
    private long lastFileUpdate = 0l;

    @PostConstruct
    public void init() {
        patterFileRunner.scheduleAtFixedRate(this::updatePatternRepo, 0, 1, TimeUnit.MINUTES);
    }

    public Collection<Emit> matchText(String text) {
        return trie.parseText(text);
    }

    public Double getCoefficient(String pattern) {
        if (patternRepo.containsKey(pattern)) {
            return patternRepo.get(pattern);
        }
        return 0d;
    }

    private void updatePatternRepo() {
        File file = new File(ruleEngineProperties.getPatternFile());
        long lastModified = 0;
        if (file.exists()) {
            lastModified = file.lastModified();
        }

        if (lastModified != lastFileUpdate) {

            try (FileReader fr = new FileReader(file)) {
                Map<String, Double> patterns  = objectMapper.readValue(fr, new TypeReference<>() {});
                patternRepo.clear();
                patternRepo.putAll(patterns);
                trie = Trie.builder()
                        .addKeywords(patternRepo.keySet())
                        .ignoreCase()
                        .build();
                lastFileUpdate = lastModified;
            } catch (Exception e) {
                log.error("Failed to load pattern repo", e);
            }
        }
    }
}
