package keapoint.onlog.recommendation.service;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TFIDFCalculator {
    /**
     * @param doc  list of strings
     * @param term String represents a term
     * @return term frequency of term in document
     */
    public double tf(List<String> doc, String term) {
        double result = 0;
        for (String word : doc) {
            if (term.equalsIgnoreCase(word))
                result++;
        }
        return result / doc.size();
    }

    /**
     * @param docs list of list of strings represents the dataset
     * @param term String represents a term
     * @return the inverse term frequency of term in documents
     */
    public double idf(List<List<String>> docs, String term) {
        double n = 0;
        for (List<String> doc : docs) {
            for (String word : doc) {
                if (term.equalsIgnoreCase(word)) {
                    n++;
                    break;
                }
            }
        }
        return Math.log(docs.size() / n);
    }

    /**
     * @param doc  a text document
     * @param docs all documents
     * @param term term
     * @return the TF-IDF of term
     */
    public double tfIdf(List<String> doc, List<List<String>> docs, String term) {
        return tf(doc, term) * idf(docs, term);
    }

    /**
     * 게시글 키워드 추출
     *
     * @param text        게시글
     * @param numKeywords 추출할 키워드 갯수
     * @return 추출된 키워드
     */
    public static List<String> getKeyWords(String text, int numKeywords) {
        List<List<String>> documents = Arrays.stream(text.split("[.\\n]"))
                .map(sentence -> Arrays.stream(sentence.trim().split("[ .,?!]")).toList())
                .toList();

        // 문서 내 중복되지 않는 단어들의 집합
        Set<String> words = documents.stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        TFIDFCalculator calculator = new TFIDFCalculator();

        // 각 단어에 대해 tf-idf를 계산하고 맵에 저장
        Map<String, Double> tfidfMap = new HashMap<>();
        for (String word : words) {
            double tfidfSum = 0;
            for (List<String> doc : documents) {
                tfidfSum += calculator.tfIdf(doc, documents, word);
            }
            tfidfMap.put(word, tfidfSum);
        }

        // 맵을 값에 따라 내림차순으로 정렬하고 결과를 numKeywords로 제한
        return tfidfMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(numKeywords)
                .map(Map.Entry::getKey)
                .toList();
    }

}
