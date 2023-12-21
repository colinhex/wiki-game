package utils;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StopWatch {
    long start;

    public static StopWatch create() {
        return new StopWatch( System.currentTimeMillis() );
    }

    public long measure() {
        return System.currentTimeMillis() - start;
    }

}
