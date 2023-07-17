package codefresco.logwatch.loginsight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import codefresco.logwatch.config.InfluxDBConfig;

@Service
public class InsightService {
  private final InfluxDBClient influxDBClient;
  private final InfluxDBConfig influxDbConfig;

  @Autowired
  public InsightService(InfluxDBClient influxDBClient, InfluxDBConfig influxDbConfig) {
    this.influxDBClient = influxDBClient;
    this.influxDbConfig = influxDbConfig;
  }

  public List<Map<String, Object>> queryLogs(String fieldName, int limit) {
    String query = String.format("""
        from(bucket: \"%s\")
        |> range(start: -1h)
        |> filter(fn: (r) => r["_field"] == \"%s\")
        |> group()
        |> tail(n: %d)
        """,
        influxDbConfig.influxDbBucket, fieldName.replace("\"", ""), limit);

    List<FluxTable> tables = influxDBClient.getQueryApi().query(query, influxDbConfig.influxDbOrg);

    List<Map<String, Object>> logs = new ArrayList<>();
    for (FluxTable table : tables) {
      for (FluxRecord record : table.getRecords()) {
        Map<String, Object> log = new HashMap<>();
        log.put("time", record.getTime());
        log.put("value", record.getValue());
        logs.add(log);
      }
    }
    return logs;
  }

}
