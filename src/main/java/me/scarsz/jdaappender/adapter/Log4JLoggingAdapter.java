package me.scarsz.jdaappender.adapter;

import me.scarsz.jdaappender.ChannelLoggingHandler;
import me.scarsz.jdaappender.LogItem;
import me.scarsz.jdaappender.LogLevel;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

@Plugin(name = "DiscordSRV-ConsoleChannel", category = "Core", elementType = "appender", printObject = true)
public class Log4JLoggingAdapter extends AbstractAppender {

    private static final PatternLayout PATTERN_LAYOUT;
    static {
        Method createLayoutMethod = Arrays.stream(PatternLayout.class.getMethods())
                .filter(method -> method.getName().equals("createLayout"))
                .findFirst().orElseThrow(() -> new RuntimeException("Failed to reflectively find the Log4j PatternLayout#createLayout method"));

        if (createLayoutMethod == null) {
            PATTERN_LAYOUT = null;
        } else {
            Object[] args = new Object[createLayoutMethod.getParameterCount()];
            args[0] = "[%d{HH:mm:ss} %level]: %msg";
            if (args.length == 9) {
                // log4j 2.1
                args[5] = true;
                args[6] = true;
            }

            try {
                PATTERN_LAYOUT = (PatternLayout) createLayoutMethod.invoke(null, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to reflectively invoke the Log4j createLayout method");
            }
        }
    }

    private final ChannelLoggingHandler handler;

    public Log4JLoggingAdapter(ChannelLoggingHandler handler) {
        super("JDAAppender", null, PATTERN_LAYOUT, false);
        this.handler = handler;
    }

    @Override
    public void append(LogEvent event) {
        LogLevel level = event.getLevel() == Level.INFO
                ? LogLevel.INFO
                : event.getLevel() == Level.WARN
                        ? LogLevel.WARN
                        : event.getLevel() == Level.ERROR
                                ? LogLevel.ERROR
                                : null;

        if (level == null) return;

        handler.enqueue(new LogItem(
                event.getLoggerName(),
                event.getMillis(),
                level,
                LogItem.stripColors(event.getMessage().getFormattedMessage()),
                event.getThrown()
        ));
    }

    @Override
    public boolean isStarted() {
        return PATTERN_LAYOUT != null && handler.getChannelSupplier().get() != null;
    }

}
