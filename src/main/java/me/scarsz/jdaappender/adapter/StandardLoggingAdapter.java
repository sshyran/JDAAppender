package me.scarsz.jdaappender.adapter;

import lombok.Getter;
import me.scarsz.jdaappender.ChannelLoggingHandler;
import me.scarsz.jdaappender.LogItem;
import me.scarsz.jdaappender.LogLevel;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;

public class StandardLoggingAdapter {

    @Getter private final LogStream outStream;
    @Getter private final LogStream errStream;

    public StandardLoggingAdapter(ChannelLoggingHandler handler) {
        this.outStream = new LogStream(System.out, "SOUT", LogLevel.INFO, handler);
        this.errStream = new LogStream(System.err, "SERR", LogLevel.ERROR, handler);
    }

    static class LogStream extends PrintStream {

        private final String loggerName;
        private final LogLevel level;
        private final ChannelLoggingHandler handler;

        public LogStream(PrintStream standardStream, String loggerName, LogLevel level, ChannelLoggingHandler handler) {
            super(standardStream, true);
            this.loggerName = loggerName;
            this.level = level;
            this.handler = handler;
        }

        @Override
        public void println(Object o) {
            super.println(o);
            println(o.toString());
        }

        @Override
        public void println(String str) {
            super.println(str);
            handler.enqueue(new LogItem(loggerName, System.currentTimeMillis(), level, str, null));
        }

        @Override
        public PrintStream printf(@NotNull String format, Object... variables) {
            super.printf(format, variables);
            //noinspection RedundantStringFormatCall
            println(String.format(format, variables));
            return this;
        }

    }

}
