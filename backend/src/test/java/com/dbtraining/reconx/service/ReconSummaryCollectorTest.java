package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ReconSummaryCollectorTest {

    @Test
    void serialAndParallelStreamsProduceSameSummary() {

        List<ReconResult> results = IntStream.range(0, 10_000)
                .mapToObj(i -> i % 2 == 0
                        ? ReconResult.matched("T" + i)
                        : ReconResult.breakResult("T" + i, "VALUE_MISMATCH", "Mismatch"))
                .toList();

        ReconSummary serial = results.stream()
                .collect(new ReconSummaryCollector());

        ReconSummary parallel = results.parallelStream()
                .collect(new ReconSummaryCollector());

        assertThat(serial).isEqualTo(parallel);
        assertThat(serial.total()).isEqualTo(10_000);
        assertThat(serial.matched()).isEqualTo(5_000);
        assertThat(serial.broken()).isEqualTo(5_000);
    }
}