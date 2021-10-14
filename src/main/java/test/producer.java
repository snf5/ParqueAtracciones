package test;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.awt.datatransfer.StringSelection;
import java.util.Properties;



public class producer {

        public static void main(String [] args){

            Properties proper = new Properties();

            proper.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            proper.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            proper.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());


            KafkaProducer<String, String> producer = new KafkaProducer<>(proper);


            producer.send(new ProducerRecord<String, String>("parque1", "keyA", "JavaApi"));

            producer.flush();
            producer.close();



        }

}
