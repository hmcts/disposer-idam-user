package uk.gov.hmcts.reform.idam.util;


import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ListUtilsTest {

    @Test
    void partition() {
        ListUtils listUtils = new ListUtils();
        List<String> batchStaleUserIds = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
        int batchSize = 3;
        List<List<String>> partitions = listUtils.partition(batchStaleUserIds, batchSize);
        assertThat(partitions).hasSize(4);
        assertThat(partitions.get(0)).containsExactly("1", "2", "3");
        assertThat(partitions.get(1)).containsExactly("4", "5", "6");
        assertThat(partitions.get(2)).containsExactly("7", "8", "9");
        assertThat(partitions.get(3)).containsExactly("10");
    }

    @Test
    void singlePartition() {
        ListUtils listUtils = new ListUtils();
        List<String> batchStaleUserIds = List.of("1", "2", "3");
        int batchSize = 3;
        List<List<String>> partitions = listUtils.partition(batchStaleUserIds, batchSize);
        assertThat(partitions).hasSize(1);
        assertThat(partitions.getFirst()).containsExactly("1", "2", "3");
    }

    @Test
    void singleSmallerPartition() {
        ListUtils listUtils = new ListUtils();
        List<String> batchStaleUserIds = List.of("1", "2", "3");
        int batchSize = 5;
        List<List<String>> partitions = listUtils.partition(batchStaleUserIds, batchSize);
        assertThat(partitions).hasSize(1);
        assertThat(partitions.getFirst()).containsExactly("1", "2", "3");
    }


}
