package keapoint.onlog.recommendation.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import keapoint.onlog.recommendation.base.BaseErrorCode;
import keapoint.onlog.recommendation.base.BaseException;
import keapoint.onlog.recommendation.dto.GetRecommendationReqDto;
import keapoint.onlog.recommendation.dto.GetRecommendationResDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class RecommendationService {

    @Value("${google_translate_key_path}")
    private String googleCredentialsPath;

    @Value("${karlo-api-key}")
    private String karloApiKey;

    @Value("${clova.api-key-id}")
    private String clovaApiKeyID;

    @Value("${clova.api-key}")
    private String clovaApiKey;

    public GetRecommendationResDto recommend(GetRecommendationReqDto data) throws BaseException {
        // karlo에 추천 이미지를 요청한다.
        CompletableFuture<List<String>> keywordImageFuture = requestImageToKarlo(data);

        // 비동기로 clova에 3줄 요약을 요청한다.
        CompletableFuture<String> summaryFuture = requestSummaryToClova(data.getContent());

        // 모든 비동기 작업이 완료될 때까지 기다린다.
        CompletableFuture.allOf(keywordImageFuture, summaryFuture).join();

        // 요약과 이미지를 합친 결과를 return한다.
        try {
            String summary = summaryFuture.get();
            List<String> imageList = keywordImageFuture.get();

            return new GetRecommendationResDto(summary, imageList);

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BaseException(BaseErrorCode.UNEXPECTED_ERROR);
        }
    }

    /**
     * 이미지 생성
     *
     * @param data 게시글 본문과 사용자가 지정한 해시태그가 들어있는 객체체     * @return 이미지 url
     */
    private CompletableFuture<List<String>> requestImageToKarlo(GetRecommendationReqDto data) throws BaseException {
        return CompletableFuture.supplyAsync(() -> {
            // TF-IDF 계산기를 통해 총 5개의 키워드를 생성한다.
            List<String> hashTagList = new ArrayList<>();
            hashTagList.addAll(data.getHashtag());
            hashTagList.addAll(TFIDFCalculator.getKeyWords(data.getContent(), 5 - data.getHashtag().size()));

            log.info("Keywords: " + String.join(", ", hashTagList));

            // Google translator에 요청해 해당 키워드를 영어로 번역한다
            List<String> translatedHashtagList = translateKeywords(hashTagList);
            log.info("Keywords: " + String.join(", ", translatedHashtagList));

            // 번역된 키워드를 karlo에 요청한다

            return translatedHashtagList;
        });
    }

    /**
     * 키워드들을 영어로 번역
     *
     * @param keywords 게시글 핵심 키워드 (5개)
     * @return 번역된 키워드 리스트
     */
    private List<String> translateKeywords(List<String> keywords) throws RuntimeException {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(googleCredentialsPath));
            Translate translateService = TranslateOptions.newBuilder().setCredentials(credentials).build().getService();

            List<String> translatedKeywords = new ArrayList<>();
            for (String keyword : keywords) {
                // Google translator에 요청해 해당 키워드를 영어로 번역한다.
                Translate.TranslateOption srcLang = Translate.TranslateOption.sourceLanguage("ko");
                Translate.TranslateOption tgtLang = Translate.TranslateOption.targetLanguage("en");

                String translatedText = translateService.translate(keyword, srcLang, tgtLang).getTranslatedText();
                log.info("Google translate: " + keyword + " -> " + translatedText);
                translatedKeywords.add(translatedText);
            }

            return translatedKeywords;

        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException("Unexpected error occurred");
        }
    }

    /**
     * 게시글 요약 메서드
     *
     * @param content 게시글 내용
     * @return 요약된 게시글
     * @throws BaseException 요청이 실패하거나 응답을 처리하는 도중 예외가 발생한 경우
     */
    private CompletableFuture<String> requestSummaryToClova(String content) {
        return CompletableFuture.supplyAsync(() -> { // 비동기적으로 실행되는 작업 생성
            HttpEntity<Map<String, Object>> entity = prepareRequestEntity(content);

            RestTemplate restTemplate = new RestTemplate();  // HTTP 요청을 보내기 위한 RestTemplate 객체 생성

            // Naver Clova 텍스트 요약 API에 POST 요청
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            "https://naveropenapi.apigw.ntruss.com/text-summary/v1/summarize",
                            HttpMethod.POST,
                            entity,
                            new ParameterizedTypeReference<>() {
                            }
                    );

            // 로깅
            log.info("Clova: " + response);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();

                if (body != null && body.containsKey("summary")) { // 응답 본문에서 "summary"라는 키의 값이 있는 경우
                    return (String) body.get("summary");

                } else { // 응답 본문에서 "summary"라는 키의 값이 없는 경우
                    throw new RuntimeException("Summary not found in response");
                }

            } else {  // 응답이 실패한 경우
                log.error(response.toString());
                throw new RuntimeException("Unexpected error occurred");
            }
        });
    }

    /**
     * HTTP 요청 객체를 준비하는 메서드
     *
     * @param content 게시글 내용
     * @return {@link HttpEntity} 객체. 이 객체는 Naver Clova 텍스트 요약 API에 보낼 HTTP POST 요청을 포함하고 있다.
     */
    private HttpEntity<Map<String, Object>> prepareRequestEntity(String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-NCP-APIGW-API-KEY-ID", clovaApiKeyID);
        headers.set("X-NCP-APIGW-API-KEY", clovaApiKey);

        Map<String, Object> documentMap = new HashMap<>();
        documentMap.put("title", "");
        documentMap.put("content", content);

        Map<String, String> optionMap = new HashMap<>();
        optionMap.put("language", "ko");
        optionMap.put("model", "general");
        optionMap.put("tone", "0");
        optionMap.put("summaryCount", "3");

        Map<String, Object> data = new HashMap<>();
        data.put("document", documentMap);
        data.put("option", optionMap);

        return new HttpEntity<>(data, headers);
    }

}
