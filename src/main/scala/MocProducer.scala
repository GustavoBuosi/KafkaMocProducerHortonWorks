import java.io.{File, FileInputStream, FileNotFoundException, IOException}
import java.util
import java.util.concurrent.TimeUnit
import java.util.{Collections, Properties}
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, Producer, ProducerConfig, ProducerRecord, RecordMetadata}
import com.hortonworks.registries.schemaregistry.client.SchemaRegistryClient
import com.hortonworks.registries.schemaregistry.serdes.avro.kafka.KafkaAvroSerializer
import org.apache.avro.Schema
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.io.{DatumReader, DecoderFactory}
import org.apache.commons.io.FileUtils
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.{LoggerFactory}

object ProducerApplication {
  val LOG = LoggerFactory.getLogger("ProducerApplication")

  def main(args: Array[String]): Unit = {
    val config: Properties = new Properties
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, MetaInf.kafkaEnd)
    config.putAll(Collections.singletonMap(SchemaRegistryClient.Configuration.SCHEMA_REGISTRY_URL.name, MetaInf.schemaRegistryURL))
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[KafkaAvroSerializer].getName)
    sendMessage(config)
  }

  private class CustomizedProducerCallback extends Callback {
    override def onCompletion(recordMetadata: RecordMetadata, e: Exception): Unit = {
      if (e == null) {
        // Adjust this using the RecordMetadata variables available whenever needed.
        LOG.info(s"Received Metadata ${recordMetadata}")
      }
      else
        e.printStackTrace()
    }
  }

  def sendMessage(props: java.util.Properties): Unit = {
    try {
      val schema: Schema = new Schema.Parser().parse(new FileInputStream(MetaInf.avroMessagesSchemaFile))
      val jsonLines: util.List[String] = FileUtils.readLines(new File(MetaInf.messagesFilePath), "utf-8")
      LOG.info(s"Sending following JSON data parsed as HortonWorks' Avro: ${jsonLines}")
      var avroRecords: util.List[Any] = new util.ArrayList[Any]
      import scala.collection.JavaConversions._
      for (jsonLine <- jsonLines) {
        avroRecords.append(jsonToAvro(jsonLine,schema))
      }
      ProducerApplication.produceMessage(MetaInf.topic, avroRecords, props)
    } catch {
      case e: FileNotFoundException =>
        e.printStackTrace()
      case e: IOException =>
        e.printStackTrace()
    }
  }

  @throws[Exception]
  protected def jsonToAvro(jsonString: String, schema: Schema): Any = {
    val reader: DatumReader[AnyRef] = new GenericDatumReader[AnyRef](schema)
    var avroObject: Any = reader.read(null, DecoderFactory.get.jsonDecoder(schema, jsonString))
    if (schema.getType == Schema.Type.STRING) avroObject = avroObject.toString
    avroObject
  }

  def produceMessage(topicName: String, msgs: util.List[Any], config: java.util.Properties): Unit = {
    val producer: Producer[String, Any] = new KafkaProducer[String, Any](config)
    val callback: Callback = new ProducerApplication.CustomizedProducerCallback
    import scala.collection.JavaConversions._
    for (msg <- msgs) {
      LOG.info(s"Sending message: ${msg} to topic ${topicName}")
      val producerRecord: ProducerRecord[String, Any] = new ProducerRecord[String, Any](topicName, msg)
      producer.send(producerRecord, callback)
    }
    producer.flush()
    LOG.info("Complete! Producer is closing")
    producer.close(5, TimeUnit.SECONDS)
  }
}



