package com.github.liyibo1110.saleproduct.check.interfacecheck;

import com.github.liyibo1110.saleproduct.base.log.StructuredLog;
import com.github.liyibo1110.saleproduct.check.entity.InterfaceCheckReport;
import com.github.liyibo1110.saleproduct.check.mapper.InterfaceCheckReportMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author liyibo
 * @date 2026-07-17 11:12
 */
@Component
public class InterfaceCheckService {

    private static final Logger log = LoggerFactory.getLogger(InterfaceCheckService.class);

    private final JavaServiceCaller javaServiceCaller;
    private final ResponseComparator responseComparator;
    private final InterfaceCheckReportMapper reportMapper;
    private final InterfaceCheckConfig config;

    private final AtomicLong dailyCheckCount = new AtomicLong(0);
    private volatile long lastResetDay = 0;

    public InterfaceCheckService(JavaServiceCaller javaServiceCaller,
                                 ResponseComparator responseComparator,
                                 InterfaceCheckReportMapper reportMapper,
                                 InterfaceCheckConfig config) {
        this.javaServiceCaller = javaServiceCaller;
        this.responseComparator = responseComparator;
        this.reportMapper = reportMapper;
        this.config = config;
    }

    public void verify(TrafficRecord record) {
        // 采样率控制
        if (!config.shouldCheck())
            return;

        // 单日上限控制
        resetDailyCountIfNeeded();
        if (dailyCheckCount.get() >= config.getMaxDailyCheck())
            return;

        dailyCheckCount.incrementAndGet();

        String uri = record.getUri();
        // 调用Java读服务
        byte[] javaResponse = javaServiceCaller.call(uri, record.getJavaRequestBody());
        if (javaResponse == null)
            return;

        // 提取商品编码（用于报告关联）
        String merchandiseCode = extractMerchandiseCode(uri, record.getJavaRequestBody());

        // 开始比对，如果有差异，则会保存在FieldDiff里
        List<FieldDiff> diffs = responseComparator.compare(uri, record.getResponseBody(), javaResponse);
        if (diffs.isEmpty())
            return;

        // 将差异写入报告表
        for (FieldDiff diff : diffs) {
            InterfaceCheckReport report = new InterfaceCheckReport();
            report.setUri(uri);
            report.setMerchandiseCode(merchandiseCode);
            report.setPhpField(diff.getPhpField());
            report.setJavaField(diff.getJavaField());
            report.setPhpValue(diff.getPhpValue());
            report.setJavaValue(diff.getJavaValue());
            report.setCheckTime(LocalDateTime.now());
            report.setStatus("PENDING");
            reportMapper.insert(report);
        }

        StructuredLog.info(log)
                .message("接口校验发现差异")
                .put("uri", uri)
                .put("merchandiseCode", merchandiseCode)
                .put("diffCount", diffs.size())
                .log();
    }

    private String extractMerchandiseCode(String uri, byte[] requestBody) {
        if (requestBody == null)
            return null;
        String queryString = new String(requestBody, StandardCharsets.UTF_8);
        // 从query参数中提取code
        for (String param : queryString.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && "code".equals(kv[0]))
                return kv[1];
        }
        return null;
    }

    private void resetDailyCountIfNeeded() {
        // 当前时间戳对应的总天数
        long today = System.currentTimeMillis() / (24 * 60 * 60 * 1000);
        if (today != lastResetDay) {
            lastResetDay = today;
            dailyCheckCount.set(0);
        }
    }
}
