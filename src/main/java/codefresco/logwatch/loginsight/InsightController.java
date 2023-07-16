package codefresco.logwatch.loginsight;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import codefresco.logwatch.config.InfluxDBConfig;

@Controller
public class InsightController {

  private final InfluxDBClient influxDBClient;
  private final InfluxDBConfig influxDbConfig;

  @Autowired
  public InsightController(InfluxDBClient influxDBClient, InfluxDBConfig influxDbConfig) {
    this.influxDBClient = influxDBClient;
    this.influxDbConfig = influxDbConfig;
  }

  @GetMapping("/recent")
  @ResponseBody
  public List<String> getLogs(@RequestParam(required = false, defaultValue = "10") int count) {
    String query = String.format("from(bucket: \"%s\") |> range(start: -15m) |> limit(n: %d)",
        influxDbConfig.influxDbBucket, count);

    List<FluxTable> tables = influxDBClient.getQueryApi().query(query, influxDbConfig.influxDbOrg);
    List<String> logs = new ArrayList<>();
    for (FluxTable table : tables) {
      for (FluxRecord record : table.getRecords()) {
        logs.add(record.getTime() + ": " + record.getValue());
      }
    }
    return logs;
  }
}