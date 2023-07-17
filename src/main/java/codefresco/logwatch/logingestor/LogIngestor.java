package codefresco.logwatch.logingestor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.zip.GZIPInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class LogIngestor implements Runnable {

  @Autowired
  @Qualifier("threadPoolTaskExecutor")
  private Executor executor;

  @Autowired
  private InfluxDBClient influxDBClient;

  private final DatagramSocket serverSocket;
  private final int bufferSize;
  private final int port;

  private static final ObjectMapper mapper = new ObjectMapper();

  LogIngestor() throws SocketException {
    this.port = Integer.parseInt(System.getenv("GELF_PORT"));
    this.bufferSize = Integer.parseInt(System.getenv("BUFFER_SIZE"));

    this.serverSocket = new DatagramSocket(port);
  }

  private String decompressGzip(byte[] compressed) throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
    GZIPInputStream gis = new GZIPInputStream(bis);
    BufferedReader br = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8));
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = br.readLine()) != null) {
      sb.append(line);
    }
    br.close();
    gis.close();
    bis.close();
    return sb.toString();
  }

  private Point parseGelfToPoint(String gelfMessage) {
    JsonNode jsonNode;
    Point point = Point.measurement("log")
        .addTag("source", "gelf")
        .time(Instant.now(), WritePrecision.NS);

    try {
      jsonNode = mapper.readTree(gelfMessage);
    } catch (IOException e) {
      // invalid json, write as text
      point.addField("message", gelfMessage);
      return point;
    }

    // see if this is a nested json message, parse and add
    jsonNode.fields().forEachRemaining(entry -> {
      String field = entry.getKey();
      JsonNode value = entry.getValue();
      try {
        JsonNode innerJsonNode = mapper.readTree(value.asText());
        point.addField(field, value.asText());
        innerJsonNode.fields().forEachRemaining(nestedField -> {
          point.addField(field + '_' + nestedField.getKey(), nestedField.getValue().asText());
        });

      } catch (Exception e) {
        point.addField(field, value.asText());
      }
    });

    return point;
  }

  private void receiveGelfLogs() {
    try (WriteApi writeApi = influxDBClient.makeWriteApi(WriteOptions.builder().flushInterval(1_00).build())) {

      byte[] receiveData = new byte[bufferSize];
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

      System.out.println("Listening on udp: " + serverSocket.getLocalPort());

      while (!Thread.currentThread().isInterrupted()) {
        serverSocket.receive(receivePacket);

        byte[] data = receivePacket.getData();
        String gelfMessage = decompressGzip(data);

        Point point = parseGelfToPoint(gelfMessage);
        writeApi.writePoint(point);

        System.out.println("Received and added to batch: " + point.toLineProtocol());
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      serverSocket.close();
    }
  }

  @PostConstruct
  public void init() {
    executor.execute(this);
  }

  @Override
  public void run() {
    receiveGelfLogs();
  }
}