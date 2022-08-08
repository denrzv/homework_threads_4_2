import java.util.Map;
import java.util.concurrent.Callable;

public class MapReaderWriter implements Callable<Long> {
    private final Map map;
    private final int[] numbers;
    private final int id;
    private final int startIndex;
    private final int endIndex;

    public MapReaderWriter(Map map, int[] numbers, int id, int startIndex, int endIndex) {
        this.map = map;
        this.numbers = numbers;
        this.id = id;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public Long call() {
        long totalWorkTime = 0;
        while (!Thread.interrupted()) {
            try {
                long workTime;
                long start = System.currentTimeMillis();
                for (int x = startIndex; x < endIndex; x++) {
                    map.put(numbers[x], numbers[x]);
                }
                workTime = System.currentTimeMillis() - start;
                System.out.println("Поток " + id + " запись данных в " + map.getClass().getName() + " --> " +
                        workTime + " мсек");
                totalWorkTime += workTime;

                Thread.sleep(1000);

                start = System.currentTimeMillis();
                for (int x = startIndex; x < endIndex; x++) {
                    map.get(numbers[x]);
                }
                workTime = System.currentTimeMillis() - start;
                System.out.println("Поток " + id + " чтение данных в " + map.getClass().getName() + " --> " +
                        workTime + " мсек");
                totalWorkTime += workTime;
                throw new InterruptedException();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return totalWorkTime;
    }
}
