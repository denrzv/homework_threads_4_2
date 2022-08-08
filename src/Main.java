import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

public class Main {
    private static final int MAP_LIMIT = 1_000_000;
    private static final byte THREADS_COUNT = 10;
    private static final long TIMEOUT = 3000;

    public static void main(String[] args) throws InterruptedException {
        Map<Integer, Integer> synchronizedMap = Collections.synchronizedMap(new HashMap<>());
        ConcurrentHashMap<Integer, Integer> concurrentMap = new ConcurrentHashMap<>();
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        int[] numbers = getNumbers(MAP_LIMIT);

        List<MapReaderWriter> callables = getCallables(synchronizedMap, numbers, THREADS_COUNT);
        LongAdder workTime = new LongAdder();

        runCallables(pool, callables, workTime);
        callables.clear();

        Thread.sleep(TIMEOUT);
        System.out.println();
        workTime.reset();

        callables = getCallables(concurrentMap, numbers, THREADS_COUNT);
        runCallables(pool, callables, workTime);
        pool.shutdown();
    }

    private static void runCallables(ExecutorService pool, List<MapReaderWriter> callables, LongAdder workTime)
            throws InterruptedException {
        pool.invokeAll(callables)
                .parallelStream()
                .forEach(task -> {
                    try {
                        workTime.add(task.get());
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                });
        System.out.println("Общее время выполнения: " + workTime + " мсек");
    }

    public static int[] getNumbers(int limit) {
        return Arrays.stream(new int[limit])
                .parallel()
                .map(number -> number = new Random().nextInt())
                .toArray();
    }

public static List<MapReaderWriter> getCallables(Map map, int[] numbers, int threadsQty) {
        List<MapReaderWriter> callables = new ArrayList<>(threadsQty);
        int len = numbers.length;
        int endIndex;
        for (int i = 0; i < threadsQty; i++) {
            int startIndex = i;
            if (startIndex == 0) {
                endIndex = len / threadsQty;
            } else {
                startIndex = startIndex * len / threadsQty;
                endIndex = startIndex + len / threadsQty;
            }
            callables.add(new MapReaderWriter(map, numbers, i, startIndex, endIndex));
        }
        return callables;
    }
}