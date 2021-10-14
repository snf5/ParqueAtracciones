package test;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

public class consumer {

    public static void main(String [] args){

        Properties proper = new Properties();

        proper.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        proper.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        proper.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        proper.put(ConsumerConfig.GROUP_ID_CONFIG, "testGroup");
        proper.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(proper);
        consumer.subscribe(Collections.singleton("parque1"));

        long count = 0L;
        while(count < 100){
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));

            for(ConsumerRecord<String, String> record : records){
                System.out.println("MSG: " + record.toString());
                count++;
            }
        }


    }
}
