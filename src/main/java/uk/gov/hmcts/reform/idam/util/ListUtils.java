package uk.gov.hmcts.reform.idam.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ListUtils {

    public List<List<String>> partition(List<String> listToPartition, int batchSize) {
        List<List<String>> partitions = new ArrayList<>();
        for (int i = 0; i < listToPartition.size(); i += batchSize) {
            partitions.add(listToPartition.subList(i, Math.min(i + batchSize, listToPartition.size())));
        }
        return partitions;
    }
}
