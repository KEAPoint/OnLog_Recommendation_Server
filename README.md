# OnLog_Recommendation_Server

## 🌐 프로젝트 개요

현재 저희는 람다를 활용한 [게시글 요약 서비스](https://github.com/KEAPoint/OnLog_Text_Summarization_Lambda)와 인스턴스에 컨테이너를
올린 [게시글 썸네일 추천 서비스](https://github.com/KEAPoint/OnLog_Image_Generation)를 운영 중입니다.

람다 서비스는 요청이 들어올 때마다 응답해주며, 그 처리 시간 동안의 비용을 부과합니다. 그러나 앞으로 사용자가 많아질 경우, 인스턴스 사용량보다 더 큰 비용이 발생할 가능성이 있습니다.

따라서, [게시글 요약 서비스](https://github.com/KEAPoint/OnLog_Text_Summarization_Lambda)
와 [썸네일 추천 서비스](https://github.com/KEAPoint/OnLog_Image_Generation)를 통합 운영함으로써 더욱 효율적으로 자원을 활용하고자 해당 프로젝트를 개발하게 되었습니다.

## 🛠️ 프로젝트 개발 환경

프로젝트는 아래 환경에서 개발되었습니다.

> OS: macOS Sonoma   
> IDE: Intellij IDEA  
> Java 17

## ✅ 프로젝트 실행

해당 프로젝트를 추가로 개발 혹은 실행시켜보고 싶으신 경우 아래의 절차에 따라 진행해주세요

#### 1. `secret.yml` 작성

```commandline
vi ./src/main/resources/secret.yml
```

```text
karlo-api-key: {KARLO_APL_KEY}

clova:
  api-key-id: {CLOVA_CLIIENT_ID}
  api-key: {CLOVA_CLIIENT_SECRET}
```

#### 2. google translate 사용을 위한 `google-translate-key.json` 추가

```commandline
vi ./src/main/resources/google-translate-key.json
```

```text
{
    "type": "{type}",
    "project_id": "{project_id}",
    "private_key_id": "{private_key_id}",
    "private_key": "{private_key}",
    "client_email": "{client_email}",
    "client_id": "{client_id}",
    "auth_uri": "{auth_uri}",
    "token_uri": "{token_uri}",
    "auth_provider_x509_cert_url": "{auth_provider_x509_cert_url}",
    "client_x509_cert_url": "{client_x509_cert_url}",
    "universe_domain": "{universe_domain}"
}
```

#### 3. 프로젝트 실행

```commandline
./gradlew bootrun
```

**참고) 프로젝트가 실행 중인 환경에서 아래 URL을 통해 API 명세서를 확인할 수 있습니다**

```commandline
http://localhost:8080/swagger-ui/index.html
```