package neo4j.algorithms.components;

import error.AlgorithmException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import utils.StopWatch;

@Slf4j
public abstract class MeasurableBlock implements Measurable {

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private StopWatch stopWatch;

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PRIVATE)
    private long measurement;

    public void markStart() {
        setStopWatch( StopWatch.create() );
    }

    public abstract void run() throws AlgorithmException;

    public void markEnd() {
        setMeasurement( getStopWatch().measure() );
        if (log.isTraceEnabled()) {
            log.trace( String.format( "### : %s : %s : (ms)", getClass().getName(), getMeasurement() ) );
        }
    }

    public MeasurableBlock execute() throws AlgorithmException {
        markStart();
        run();
        markEnd();
        return this;
    }

}
