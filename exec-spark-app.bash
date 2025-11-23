#!/bin/bash

mvn clean package -DskipTests

docker cp target/moex-spark-1.0.jar spark-master:/opt/spark/work-dir

docker exec -it spark-master \
  /opt/spark/bin/spark-submit \
  --class com.github.vielendanke.Main \
  --master spark://spark-master:7077 \
  moex-spark-1.0.jar