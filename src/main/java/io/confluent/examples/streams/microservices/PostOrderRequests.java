package io.confluent.examples.streams.microservices;

import io.confluent.examples.streams.avro.microservices.OrderState;
import io.confluent.examples.streams.avro.microservices.Product;
import io.confluent.examples.streams.microservices.domain.Schemas;
import io.confluent.examples.streams.microservices.domain.Schemas.Topics;
import io.confluent.examples.streams.microservices.domain.beans.OrderBean;
import io.confluent.examples.streams.microservices.util.Paths;
import org.apache.kafka.streams.KeyValue;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.List;

import static io.confluent.examples.streams.avro.microservices.Product.JUMPERS;
import static io.confluent.examples.streams.avro.microservices.Product.UNDERPANTS;
import static io.confluent.examples.streams.microservices.domain.beans.OrderId.id;
import static io.confluent.examples.streams.microservices.util.MicroserviceUtils.MIN;
import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/*
 * confluent start
 * mvn compile
 * mvn exec:java -Dexec.mainClass=io.confluent.examples.streams.microservices.OrdersService -Dexec.args="localhost:9092 http://localhost:8081 localhost 5432"
 * mvn exec:java -Dexec.mainClass=io.confluent.examples.streams.microservices.PostOrderRequests -Dexec.args="5432"
 * confluent consume orders --value-format avro
 */

public class PostOrderRequests {

  private static GenericType<OrderBean> newBean() {
    return new GenericType<OrderBean>() {
    };
  }

  public static void main(String [] args) throws Exception {

    final int restPort = args.length > 0 ? Integer.valueOf(args[0]) : 5432;

    final String HOST = "localhost";
    List<Service> services = new ArrayList<>();
    OrderBean returnedBean;
    Paths path = new Paths("localhost", restPort == 0 ? 5432 : restPort);

    final ClientConfig clientConfig = new ClientConfig();
    clientConfig.property(ClientProperties.CONNECT_TIMEOUT, 60000)
        .property(ClientProperties.READ_TIMEOUT, 60000);
    Client client = ClientBuilder.newClient(clientConfig);

    // send 2000 orders, one every 1000 milliseconds
    for (int i = 0; i < 2000; i++) {
      OrderBean inputOrder = new OrderBean(id(i), 2L, OrderState.CREATED, Product.JUMPERS, 1, 1d);

      // POST order
      client.target(path.urlPost()).request(APPLICATION_JSON_TYPE).post(Entity.json(inputOrder));
      System.out.printf("Posted order request: %d\n", i);

      // GET order, assert that it is Validated
      //returnedBean = client.target(path.urlGetValidated(i)).queryParam("timeout", MIN)
      //  .request(APPLICATION_JSON_TYPE).get(newBean());
      //assertThat(returnedBean).isEqualTo(new OrderBean(
      //    inputOrder.getId(),
      //    inputOrder.getCustomerId(),
      //    OrderState.VALIDATED,
      //    inputOrder.getProduct(),
      //    inputOrder.getQuantity(),
      //    inputOrder.getPrice()
      //));

      Thread.sleep(1000L);
    }
  }

}
