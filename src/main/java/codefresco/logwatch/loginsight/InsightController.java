package codefresco.logwatch.loginsight;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Validated
public class InsightController {

  private final InsightService insightService;

  public InsightController(InsightService insightService) {
    this.insightService = insightService;
  }

  @GetMapping("/recent")
  @ResponseBody
  public List<Map<String, Object>> getLogs(
      @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(100) int count) {

    List<Map<String, Object>> logs = insightService.queryLogs("short_message", count);

    return logs;
  }

  @GetMapping("/latency")
  @ResponseBody
  public List<Map<String, Object>> getLatencies(
      @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(100) int count) {

    List<Map<String, Object>> logs = insightService.queryLogs("short_message_latency", count);

    return logs;
  }
}