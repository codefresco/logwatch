package codefresco.logwatch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;

@Configuration
public class InfluxDBConfig {

  @Value("${INFLUXDB_URL}")
  private String influxDbUrl;

  @Value("${DOCKER_INFLUXDB_INIT_ADMIN_TOKEN}")
  private String influxDbToken;

  @Value("${DOCKER_INFLUXDB_INIT_ORG}")
  public String influxDbOrg;

  @Value("${DOCKER_INFLUXDB_INIT_BUCKET}")
  public String influxDbBucket;

  @Bean
  public InfluxDBClient influxDBClient() {
    return InfluxDBClientFactory.create(influxDbUrl, influxDbToken.toCharArray(), influxDbOrg, influxDbBucket);
  }
}