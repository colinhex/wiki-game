package neo4j.logging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Neo4jLogProvider implements LogProvider {
    static Neo4jLogProvider instance;
    public static Neo4jLogProvider getInstance() {
        if (instance == null) {
            instance = new Neo4jLogProvider();
        }
        return instance;
    }

    @Override
    public Log getLog( Class<?> loggingClass ) {
        return CustomLog.create( loggingClass );
    }

    @Override
    public Log getLog( String name ) {
        return CustomLog.create( name );
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class CustomLog implements Log {
        @Setter
        private Logger log;

        private static CustomLog create( Class<?> className ) {
            CustomLog customLog = new CustomLog();
            customLog.setLog( LoggerFactory.getLogger( className ) );
            return customLog;
        }

        private static CustomLog create( String name ) {
            CustomLog customLog = new CustomLog();
            customLog.setLog( LoggerFactory.getLogger( name ) );
            return customLog;
        }

        @Override
        public boolean isDebugEnabled() {
            return log.isDebugEnabled();
        }


        @Override
        public void debug( String message ) {
            log.debug( message );
        }


        @Override
        public void debug( String message, Throwable throwable ) {
            log.debug( message, throwable );
        }


        @Override
        public void debug( String format, Object... arguments ) {
            log.debug( format, arguments );
        }


        @Override
        public void info( String message ) {
            log.info( message );
        }


        @Override
        public void info( String message, Throwable throwable ) {
            log.info( message, throwable );
        }


        @Override
        public void info( String format, Object... arguments ) {
            log.info( format, arguments );
        }


        @Override
        public void warn( String message ) {
            log.warn( message );
        }


        @Override
        public void warn( String message, Throwable throwable ) {
            log.warn( message, throwable );
        }


        @Override
        public void warn( String format, Object... arguments ) {
            log.warn( format, arguments );
        }


        @Override
        public void error( String message ) {
            log.error( message );
        }


        @Override
        public void error( String message, Throwable throwable ) {
            log.error( message, throwable );
        }


        @Override
        public void error( String format, Object... arguments ) {
            log.error( format, arguments );
        }
    }

}
