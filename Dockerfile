FROM openjdk:17-jdk-alpine

# 인자 설정 - Jar_File
ARG JAR_FILE=build/libs/*.jar

# jar 파일 복제
COPY ${JAR_FILE} config.jar

# google translate key 파일 복사
COPY src/main/resources/google-translate-key.json /app/

# 실행 명령어
ENTRYPOINT ["java","-jar","config.jar"]