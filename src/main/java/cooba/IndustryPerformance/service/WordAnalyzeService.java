package cooba.IndustryPerformance.service;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class WordAnalyzeService {
    private static final List<String> nutureStringList = Stream.of("a", "v", "n").collect(Collectors.toList());
    private static final int NWeightedPoint = 15;
    private static final int VerbWeightedPoint = 12;
    private static final int AdjWeightedPoint = 10;


    public void wordAnalysis(Map<String, Integer> keywordMap, String str) {
        if (keywordMap == null) {
            keywordMap = new ConcurrentHashMap<>();
        }

        for (Term term : ToAnalysis.parse(str).getTerms()) {
            if (nutureStringList.contains(term.getNatureStr())) {
                int point = Optional.ofNullable(keywordMap.get(term.getName() + " " + term.getNatureStr())).orElse(0);
                if (term.getNatureStr().equals("n")) {
                    point = point + NWeightedPoint;
                    if (term.next() != null && (term.next().getNatureStr().equals("v") || term.next().getNatureStr().equals("n"))) {
                        point += 5;
                    }
                    keywordMap.put(term.getName() + " " + term.getNatureStr(), point);
                } else if (term.getNatureStr().equals("v")) {
                    point = point + VerbWeightedPoint;
                    if (term.next() != null && (term.next().getNatureStr().equals("v") || term.next().getNatureStr().equals("n"))) {
                        point += 5;
                    }
                    keywordMap.put(term.getName() + " " + term.getNatureStr(), point);
                } else {
                    point = point + AdjWeightedPoint;
                    if (term.next() != null && (term.next().getNatureStr().equals("v") || term.next().getNatureStr().equals("n"))) {
                        point += 5;
                    }
                    keywordMap.put(term.getName() + " " + term.getNatureStr(), point);
                }
            }
        }
    }
}
