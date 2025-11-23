package com.github.vielendanke;

import org.apache.spark.sql.*;

import static org.apache.spark.sql.functions.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        // 1. Инициализация Spark сессии
        // Настройки подключения к Cassandra берем из конфигурации или хардкодим для примера
        SparkSession spark = SparkSession.builder()
                .appName("JavaCassandraHourlyAggregation")
                .master("spark://spark-master:7077")
                .config("spark.cassandra.connection.host", "cassandra") // Имя сервиса в Docker
                .config("spark.cassandra.connection.port", "9042")
                .getOrCreate();

        // Убираем лишний шум в логах
        spark.sparkContext().setLogLevel("INFO");

        System.out.println("--- Запуск Java Spark Job: Агрегация данных ---");

        // 2. Чтение данных из таблицы trades
        Dataset<Row> tradesDf = spark.sqlContext()
                .read()
                .format("org.apache.spark.sql.cassandra")
                .option("keyspace", "moex")
                .option("table", "iss_data")
                .load();

        // Показываем схему и пример данных
        tradesDf.printSchema();
        tradesDf.show(5);

        // 3. Агрегация
        // Логика: withColumn("volume", price * quantity) -> groupBy(secid, window(ts, 1h)) -> sum(volume)
        Dataset<Row> resultDf = tradesDf
                .withColumn("volume", col("price").multiply(col("quantity")))
                .groupBy(
                        col("secid"),
                        functions.window(col("tradetime"), "1 hour")
                )
                .agg(sum("volume").alias("total_volume"))
                .select(
                        col("secid"),
                        col("window.start").alias("window_start"), // Извлекаем старт окна для Primary Key
                        col("total_volume")
                );

        System.out.println("--- Результат агрегации ---");
        resultDf.show(10);

        // 4. Запись результата обратно в Cassandra
        resultDf.write()
                .format("org.apache.spark.sql.cassandra")
                .option("table", "iss_data_hourly_volume")
                .option("keyspace", "moex")
                .mode(SaveMode.Append)
                .save();

        System.out.println("--- Данные успешно сохранены в таблицу hourly_volume ---");

        spark.stop();
    }
}
