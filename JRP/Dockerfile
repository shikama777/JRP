# JDKランタイム（軽量版）
FROM eclipse-temurin:21-jdk

# 作業ディレクトリを作成
WORKDIR /app

# Jar をコピー（ファイル名は自分のJarに置き換え）
COPY target/JRP-0.0.1-SNAPSHOT.jar jrp.jar

ENV SPRING_PROFILES_ACTIVE=production

# アプリを起動
ENTRYPOINT ["java", "-jar", "/app/jrp.jar"]

ENV GOOGLE_APPLICATION_CREDENTIALS=/app/firebase-key.json
COPY /keito-life-project-877bce615cf2.json /app/firebase-key.json
