package root.tse.infrastructure;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import root.tse.domain.IdGenerator;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class IdGeneratorImpl implements IdGenerator {

    private static final int NUMBER_OF_IDS = 10;
    private static final int ID_LENGTH = 8;

    private final BlockingQueue<String> ids;
    private final ExecutorService executorService;

    public IdGeneratorImpl() {
        this.ids = generateIds();
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    @SneakyThrows
    public String generateId() {
        var id = ids.take();
        executorService.submit(() -> {
            try {
                ids.put(newId());
            } catch (Exception e) {
                log.error(">>> failed to add new id to queue", e);
            }
        });
        return id;
    }

    @SneakyThrows
    private BlockingQueue<String> generateIds() {
        var queueOfIds = new ArrayBlockingQueue<String>(NUMBER_OF_IDS);
        for (var i = 0; i < NUMBER_OF_IDS; i++) {
            queueOfIds.put(newId());
        }
        return queueOfIds;
    }

    private String newId() {
        return StringUtils.truncate(UUID.randomUUID().toString(), ID_LENGTH);
    }
}
