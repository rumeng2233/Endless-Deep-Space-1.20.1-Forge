package shirumengya.endless_deep_space.custom;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class IncompatibleModsException extends RuntimeException {
    public static final Logger LOGGER = LogUtils.getLogger();

    public IncompatibleModsException(String message) {
        super(message);
        LOGGER.error(LogUtils.FATAL_MARKER, message);
        Runtime.getRuntime().halt(-1);
    }
}
