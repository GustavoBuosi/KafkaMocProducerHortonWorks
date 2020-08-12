import scala.util.Random

object MetaInf {

  val kafkaEnd = "localhost:9092"
  val schemaRegistryURL = "http://localhost:9090/api/v1" //deprecated. If you are using the Cloudera, port is
  // likely to be 7788.
  val topic = "testDStreamAvroWithIntWithDateNested"
  val messagesFilePath = "MensagensTestesWithIntWithDateNested.txt"
  // Generating random String for consumer group:
  val avroMessagesSchemaFile = "testeCronosWithIntWithDateNested.avsc"
  val maxNumPolls = 10
  val alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
  val consumerGroup = randStr(20)
  def randStr(n:Int): String = (1 to n).map(x => alpha(Random.nextInt(alpha.length - 1).abs)).mkString

}
